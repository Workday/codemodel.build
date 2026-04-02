package build.codemodel.framework.builder;

/*-
 * #%L
 * Code Model Framework Builder
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.foundation.Introspection;
import build.base.foundation.Requires;
import build.base.foundation.stream.Streamable;
import build.base.foundation.stream.Streams;
import build.base.mereology.Strategy;
import build.base.telemetry.Error;
import build.base.telemetry.TelemetryRecorder;
import build.base.telemetry.foundation.ObservableTelemetryRecorder;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.framework.Framework;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.Targetable;
import build.codemodel.framework.compiler.Compilation;
import build.codemodel.framework.compiler.Compiler;
import build.codemodel.framework.compiler.TypeChecker;
import build.codemodel.framework.completer.Completer;
import build.codemodel.framework.completer.Completion;
import build.codemodel.framework.initialization.Enricher;
import build.codemodel.framework.initialization.Initializer;
import build.codemodel.injection.Context;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.jdk.JDKCodeModel;

import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An internal implementation of a {@link Framework} that will be constructed using the {@link FrameworkBuilder}.
 *
 * @author brian.oliver
 * @since Apr-2024
 */
class InternalFramework
    implements Framework {

    /**
     * The {@link FileSystem} for interacting with files.
     */
    private final FileSystem fileSystem;

    /**
     * The {@link Plugin}s ordered by {@link Requires}.
     */
    private final ArrayList<Plugin> plugins;

    /**
     * The {@link Plugin}s arranged by {@link Class}, allowing for fast lookup.
     */
    private final LinkedHashMap<Class<?>, LinkedHashSet<Plugin>> pluginsByClass;

    /**
     * The {@link NameProvider}.
     */
    private final NameProvider nameProvider;

    /**
     * The {@link InjectionFramework} with which to perform Dependency Injection
     */
    private final InjectionFramework injectionFramework;

    /**
     * A {@link Context} with which to perform Dependency Injection.
     */
    private final Context context;

    /**
     * Constructs an {@link InternalFramework}.
     *
     * @param fileSystem   the {@link FileSystem}
     * @param nameProvider the {@link NameProvider}
     * @param plugins      the {@link Plugin}s
     */
    InternalFramework(final FileSystem fileSystem,
                      final NameProvider nameProvider,
                      final Stream<? extends Plugin> plugins) {

        this.fileSystem = Objects.requireNonNull(fileSystem, "The FileSystem must not be null");
        this.nameProvider = Objects.requireNonNull(nameProvider, "The NameProvider must not be null");

        // order the Plugins by @Requires
        this.plugins = plugins == null
            ? new ArrayList<>()
            : Streams.sortByRequires(plugins.filter(Objects::nonNull), Streams.SortOrder.FIRST)
                .collect(Collectors.toCollection(ArrayList::new));

        // arrange plugins according to their type and interface types
        this.pluginsByClass = new LinkedHashMap<>();

        this.plugins.forEach(plugin -> Streams.concat(
                Stream.of(plugin.getClass()),
                Introspection.getAll(plugin.getClass(), Class::getInterfaces))
            .forEach(c -> {
                this.pluginsByClass.compute(c, (__, existing) -> {
                    final LinkedHashSet<Plugin> set = existing == null
                        ? new LinkedHashSet<>()
                        : existing;

                    set.add(plugin);
                    return set;
                });
            }));

        // establish the InjectionFramework and Context to be used to perform Dependency Injection by the Code Model
        this.injectionFramework = new InjectionFramework(new JDKCodeModel(this.nameProvider));

        this.context = this.injectionFramework.newContext();

        this.context.bind(Framework.class).to(this);
        this.context.bind(FileSystem.class).to(this.fileSystem);
        this.context.bind(NameProvider.class).to(this.nameProvider);
    }

    @Override
    public FileSystem fileSystem() {
        return this.fileSystem;
    }

    @Override
    public Stream<Plugin> plugins() {
        return this.plugins.stream();
    }

    @Override
    public <T> Stream<T> plugins(final Class<T> requiredClass) {
        final var set = this.pluginsByClass.get(requiredClass);

        return set == null
            ? Stream.empty()
            : set.stream().map(requiredClass::cast);
    }

    @Override
    public NameProvider nameProvider() {
        return this.nameProvider;
    }

    /**
     * Initializes a newly created {@link CodeModel} using {@link Initializer}s.
     *
     * @param codeModel the {@link CodeModel}
     * @return the {@link CodeModel} to permit fluent-style method invocation
     */
    private CodeModel initialize(final CodeModel codeModel) {
        plugins(Initializer.class)
            .forEach(initializer -> initializer.initialize(codeModel));

        return codeModel;
    }

    @Override
    public CodeModel newCodeModel() {
        return initialize(new ConceptualCodeModel(nameProvider));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CodeModel> T newCodeModel(final Class<T> codeModelClass) {

        final var codeModel = this.context.create(codeModelClass);
        return (T) initialize(codeModel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CodeModel> T enrich(final T codeModel, final TelemetryRecorder telemetryRecorder) {

        // enrich the CodeModel using the Enricher(s) until no further enrichment occurs

        // --------
        // determine the Enrichers (from the Plugins)
        final var enrichers = plugins(Enricher.class)
            .map(enricher -> (Enricher<?, ?>) enricher)
            .toList();

        // only perform enrichment when there's enricher(s)
        if (!enrichers.isEmpty()) {
            // --------
            // collect the Traitables from the CodeModel and arrange by type so that may be enriched (by type)
            final var traitables = new HashSet<Traitable>();
            final var traitablesByClass = new HashMap<Class<?>, HashSet<Traitable>>();
            final var interfacesByClass = new HashMap<Class<?>, HashSet<Class<?>>>();

            // start with the CodeModel to find the Traitables
            final var queue = new LinkedHashSet<Traitable>();
            queue.add(codeModel);

            // include the TypeDescriptors
            codeModel.typeDescriptors()
                .forEach(queue::add);

            // --------
            // perform enriching of those types that are assignable to the Enrichers
            //  (continue until no new traits are created)

            final var newTraitCreated = new AtomicBoolean();
            do {
                // index the queued Traitables by type so they may be enriched (by type)
                while (!queue.isEmpty()) {
                    final var traitable = queue.removeFirst();

                    // include the Traitable as being processed
                    traitables.add(traitable);

                    // include the Traitable interfaces
                    final var traitableClass = traitable.getClass();
                    interfacesByClass.compute(traitableClass, (__, existing) -> existing == null
                            ? Introspection.getAllInterfaces(traitableClass)
                            .collect(Collectors.toCollection(LinkedHashSet::new))
                            : existing)
                        .forEach(interfaceClass -> traitablesByClass.compute(interfaceClass, (__, existing) -> {
                            final var set = existing == null
                                ? new LinkedHashSet<Traitable>()
                                : existing;

                            set.add(traitable);
                            return set;
                        }));

                    // queue the Traitables of the Traitable
                    traitable.traits(Traitable.class)
                        .filter(t -> !traitables.contains(t) && !queue.contains(t))
                        .forEach(queue::add);
                }

                // assume no new traits will be created
                newTraitCreated.set(false);

                // attempt to enrich the traitables by type for each Enricher
                enrichers.forEach(enricher -> traitablesByClass.forEach((traitableClass, set) -> {
                    enricher.getTargetClass()
                        .filter(targetClass -> targetClass.isAssignableFrom(traitableClass))
                        .ifPresent(__ -> set.forEach(traitable -> {
                            final var factory = (Enricher<Traitable, Trait>) enricher;

                            if (factory.isTraitPermitted(traitable)) {
                                factory.create(traitable)
                                    .forEach(trait -> {
                                        // include the Trait in the Traitable (if not already added!)
                                        traitable.addTrait(trait);

                                        newTraitCreated.set(true);

                                        // queue the new traitable Trait to process (in the next round)
                                        if (trait instanceof Traitable traitableTrait) {
                                            queue.add(traitableTrait);
                                        }
                                    });
                            }
                        }));
                }));
            } while (newTraitCreated.get());
        }

        return codeModel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CodeModel> Optional<T> typeCheck(final T codeModel,
                                                        final TelemetryRecorder telemetryRecorder) {

        final var observableRecorder = ObservableTelemetryRecorder.of(telemetryRecorder);

        // a TraitVisitor to perform type checking on a Trait
        final Consumer<Trait> typeCheckingConsumer = trait -> {
            final var traitClass = (Class<Trait>) trait.getClass();
            typeCheckersFor(traitClass)
                .forEach(typeChecker -> typeChecker.check(trait, codeModel, observableRecorder));
        };

        // perform type checking on TypeDescriptors
        processTypeDescriptors(
            codeModel,
            TypeChecker.class,
            (typeDescriptor, typeChecker) -> typeChecker.check(typeDescriptor, codeModel, observableRecorder),
            typeCheckingConsumer);

        // perform type checking on the CodeModel
        typeCheckersFor(CodeModel.class)
            .forEach(typeChecker -> typeChecker.check(codeModel, codeModel, observableRecorder));

        // perform type checking on the CodeModel Traits
        codeModel.traits()
            .forEach(trait -> {
                typeCheckingConsumer.accept(trait);

                if (trait instanceof Traitable traitable) {
                    traitable.traverse(Trait.class)
                        .strategy(Strategy.DepthFirst)
                        .forEach(typeCheckingConsumer);
                }
            });

        return observableRecorder.hasObserved(Error.class)
            ? Optional.empty()
            : Optional.of(codeModel);
    }

    /**
     * Obtains the {@link TypeChecker}s applicable for the specified {@link Class}.
     *
     * @param c   the {@link Class}
     * @param <T> the type of {@link Class}
     * @return the {@link Streamable} {@link TypeChecker}s for the specified {@link Class}
     */
    @SuppressWarnings("unchecked")
    private <T> Stream<TypeChecker<T>> typeCheckersFor(final Class<T> c) {
        return targetablesFor(TypeChecker.class, c)
            .map(typeChecker -> (TypeChecker<T>) typeChecker);
    }

    /**
     * Obtains the {@link Compiler}s applicable for the specified {@link Class}.
     *
     * @param c   the {@link Class}
     * @param <T> the type of {@link Class}
     * @return the {@link Streamable} {@link Compiler}s for the specified {@link Class}
     */
    @SuppressWarnings("unchecked")
    private <T> Stream<Compiler<T>> compilersFor(final Class<T> c) {
        return targetablesFor(Compiler.class, c)
            .map(compiler -> (Compiler<T>) compiler);
    }

    /**
     * Obtains the {@link Completer}s applicable for the specified {@link Class}.
     *
     * @param c   the {@link Class}
     * @param <T> the type of {@link Class}
     * @return the {@link Streamable} {@link Completer}s for the specified {@link Class}
     */
    @SuppressWarnings("unchecked")
    private <T> Stream<Completer<T>> completersFor(final Class<T> c) {
        return targetablesFor(Completer.class, c)
            .map(compiler -> (Completer<T>) compiler);
    }

    private <T, U extends Targetable<? super T>> Stream<U> targetablesFor(final Class<U> targetableClass,
                                                                          final Class<T> c) {
        return plugins(targetableClass)
            .map(targetable -> (Targetable<?>) targetable)
            .filter(targetable -> targetable.getTargetClass()
                .map(targetClass -> targetClass.isAssignableFrom(c))
                .orElse(false))
            .map(targetableClass::cast);
    }

    @Override
    public Optional<Compilation> compile(final CodeModel codeModel, final TelemetryRecorder telemetryRecorder) {

        // we need to observe and query the Telemetry that is produced (to determine errors)
        final var observableRecorder = ObservableTelemetryRecorder.of(telemetryRecorder);

        // a TraitVisitor to perform compiling of a Trait
        final Consumer<Trait> traitCompiler = trait -> {
            final var traitClass = (Class<Trait>) trait.getClass();
            compilersFor(traitClass)
                .forEach(compiler -> compiler.compile(trait, codeModel, observableRecorder));
        };

        // perform compilation of the TypeDescriptors
        processTypeDescriptors(
            codeModel,
            Compiler.class,
            (typeDescriptor, compiler) -> compiler.compile(typeDescriptor, codeModel, observableRecorder),
            traitCompiler);

        // perform compilation of the CodeModel
        compilersFor(CodeModel.class)
            .forEach(compiler -> compiler.compile(codeModel, codeModel, observableRecorder));

        // perform compilation on the CodeModel Traits
        codeModel.traits()
            .forEach(trait -> {
                traitCompiler.accept(trait);

                if (trait instanceof Traitable traitable) {
                    traitable.traverse(Trait.class)
                        .strategy(Strategy.DepthFirst)
                        .forEach(traitCompiler);
                }
            });

        return observableRecorder.hasObserved(Error.class)
            ? Optional.empty()
            : Optional.of((Compilation) () -> codeModel);
    }

    @Override
    public Optional<Completion> complete(final Compilation compilation, final TelemetryRecorder telemetryRecorder) {

        if (compilation == null) {
            return Optional.empty();
        }

        final var codeModel = compilation.codeModel();

        // we need to observe and query the Telemetry that is produced (to determine errors)
        final var observableRecorder = ObservableTelemetryRecorder.of(telemetryRecorder);

        // a Consumer to complete a Trait
        final Consumer<Trait> traitCompleter = trait -> {
            final var traitClass = (Class<? extends Trait>) trait.getClass();
            completersFor(traitClass)
                .map(completer -> (Completer<Trait>) completer)
                .forEach(completer -> completer.complete(trait, compilation, observableRecorder));
        };

        // perform completion of the TypeDescriptors
        processTypeDescriptors(
            codeModel,
            Completer.class,
            (typeDescriptor, completer) -> completer.complete(typeDescriptor, compilation, observableRecorder),
            traitCompleter);

        // perform completion of the CodeModel-specific Traits
        completersFor(CodeModel.class)
            .forEach(completer -> completer.complete(codeModel, compilation, observableRecorder));

        // perform compilation on the CodeModel-specific Traits
        codeModel.traits()
            .forEach(trait -> {
                traitCompleter.accept(trait);

                if (trait instanceof Traitable traitable) {
                    traitable.traverse(Trait.class)
                        .strategy(Strategy.DepthFirst)
                        .forEach(traitCompleter);
                }
            });

        return observableRecorder.hasObserved(Error.class)
            ? Optional.empty()
            : Optional.of(compilation::codeModel);
    }

    private <T extends Targetable<TypeDescriptor>> void processTypeDescriptors(final CodeModel codeModel,
                                                                               final Class<T> pluginClass,
                                                                               final BiConsumer<TypeDescriptor, T> processor,
                                                                               final Consumer<Trait> consumer) {

        final var hasPluginsForTypeDescriptors = hasPluginsForTypeDescriptors(pluginClass);
        final var hasPluginsForTraitOrSubclasses = hasPluginsForTraitOrSubclasses(pluginClass);

        if (!hasPluginsForTypeDescriptors && !hasPluginsForTraitOrSubclasses) {
            return;
        }

        codeModel.typeDescriptors()
            .forEach(typeDescriptor -> {
                // process the TypeDescriptor
                targetablesFor(pluginClass, TypeDescriptor.class)
                    .forEach(plugin -> processor.accept(typeDescriptor, plugin));

                // process the Traits (and their Traits etc.) of the TypeDescriptor
                if (hasPluginsForTraitOrSubclasses) {
                    typeDescriptor.traverse(Trait.class)
                        .strategy(Strategy.DepthFirst)
                        .stream()
                        .forEach(consumer);
                }
            });
    }

    private <T extends Targetable<?>> boolean hasPluginsForTypeDescriptors(final Class<T> pluginClass) {
        return plugins(pluginClass)
            .map(Targetable::getTargetClass)
            .flatMap(Optional::stream)
            .anyMatch(TypeDescriptor.class::isAssignableFrom);
    }

    private <T extends Targetable<?>> boolean hasPluginsForTraitOrSubclasses(final Class<T> pluginClass) {
        final Set<Class<?>> targetClasses = plugins(pluginClass)
            .map(Targetable::getTargetClass)
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());

        return getAllClasses(targetClasses).stream().anyMatch(Trait.class::isAssignableFrom);
    }

    private Set<Class<?>> getAllClasses(final Set<Class<?>> targetClasses) {
        final Set<Class<?>> allClasses = new HashSet<>();
        for (Class<?> targetClass : targetClasses) {
            Class<?> currentClass = targetClass;
            while (currentClass != null) {
                allClasses.add(currentClass);
                allClasses.addAll(Arrays.asList(currentClass.getInterfaces()));
                currentClass = currentClass.getSuperclass();
            }
        }
        return allClasses;
    }
}
