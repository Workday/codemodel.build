package build.codemodel.annotation.processing;

/*-
 * #%L
 * JDK Annotation Processor
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

import build.base.foundation.Capture;
import build.base.foundation.Introspection;
import build.base.foundation.Lazy;
import build.base.telemetry.Error;
import build.base.telemetry.TelemetryRecorder;
import build.base.telemetry.foundation.MessagerBasedTelemetryRecorder;
import build.base.telemetry.foundation.ObservableTelemetryRecorder;
import build.codemodel.annotation.discovery.AnnotationDiscovery;
import build.codemodel.annotation.discovery.Discoverable;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.ThrowableDescriptor;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.CachingNameProvider;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.IntersectionTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnionTypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.VoidTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.framework.Framework;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.builder.FrameworkBuilder;
import build.codemodel.framework.compiler.Compilation;
import build.codemodel.framework.compiler.Compiler;
import build.codemodel.framework.compiler.TypeChecker;
import build.codemodel.framework.completer.Completer;
import build.codemodel.framework.initialization.Enricher;
import build.codemodel.framework.initialization.Initializer;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.descriptor.JDKClassTypeDescriptor;
import build.codemodel.jdk.descriptor.JDKInterfaceTypeDescriptor;
import build.codemodel.jdk.descriptor.MethodImplementationDescriptor;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.ImplementsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import build.codemodel.objectoriented.naming.MethodName;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

/**
 * A {@link Processor} to discover, reverse-engineer and process discoverable annotations from source and byte code.
 *
 * @author brian.oliver
 * @see AnnotationDiscovery
 * @see Discoverable
 * @since Jan-2024
 */
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class AnnotationProcessor
    extends AbstractProcessor {

    /**
     * The {@link Processor} option defining a regular expression to exclude types.
     */
    private final String EXCLUDED_TYPES_PATTERN = "codemodel.excluded.types.pattern";

    /**
     * The {@link AnnotationDiscovery} implementations to use for discovering types to reverse-engineer.
     */
    private final ArrayList<AnnotationDiscovery> annotationDiscoveries;

    /**
     * The {@link TypeName}s resolved for {@link TypeElement}s.
     */
    private final HashMap<TypeElement, TypeName> typeNames;

    /**
     * The {@link Lazy} initialized {@link TelemetryRecorder} for building {@link CodeModel}s.
     */
    private final Lazy<ObservableTelemetryRecorder> lazyTelemetryRecorder;

    /**
     * The {@link Lazy} initialized {@link Framework} for building {@link CodeModel}s.
     */
    private final Lazy<Framework> lazyFramework;

    /**
     * The {@link Lazy} initialized {@link CodeModel} into which to reverse-engineer the compiler code model.
     */
    private final Lazy<CodeModel> lazyCodeModel;

    /**
     * The {@link Capture}d {@link Compilation}, available when a {@link CodeModel} has been successfully compiled.
     */
    private final Capture<Compilation> capturedCompilation;

    /**
     * The {@link Lazy}ily initialized {@link Predicate} for excluding type to discover/reverse-engineer.
     */
    private final Lazy<Predicate<String>> lazyExcludedTypesPredicate;

    /**
     * Constructs a {@link AnnotationProcessor}.
     */
    public AnnotationProcessor() {
        super();

        this.annotationDiscoveries = new ArrayList<>();
        this.typeNames = new HashMap<>();

        this.lazyTelemetryRecorder = Lazy.empty();
        this.lazyFramework = Lazy.empty();
        this.lazyCodeModel = Lazy.empty();
        this.capturedCompilation = Capture.empty();
        this.lazyExcludedTypesPredicate = Lazy.empty();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return this.annotationDiscoveries.stream()
            .flatMap(AnnotationDiscovery::getDiscoverableAnnotationTypes)
            .map(Class::getCanonicalName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(EXCLUDED_TYPES_PATTERN);
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processing) {
        super.init(processing);

        final var messager = processing.getMessager();
        messager.printNote("Initializing " + this);

        // --------
        // establish a FrameworkBuilder to build a Framework suitable for Annotation Processing
        final var builder = new FrameworkBuilder();

        builder.withFileSystem(FileSystems::getDefault);
        builder.withNameProvider(() -> new CachingNameProvider(new NonCachingNameProvider()));

        builder.bind(Messager.class).to(messager);
        builder.bind(Filer.class).to(processing.getFiler());

        // use the ClassLoader of this AnnotationProcessor for loading Services from the Compiler-tooling Module/ClassPath!
        // (using the default ClassLoader will not find the Annotation-based Services!)
        final var serviceLoaderClassLoader = AnnotationProcessor.class.getClassLoader();
        builder.withPlugins(ServiceLoader.load(Plugin.class, serviceLoaderClassLoader));

        // --------
        // establish the TelemetryRecorder for the Framework
        final var telemetryRecorder = ObservableTelemetryRecorder.of(MessagerBasedTelemetryRecorder.of(messager));
        this.lazyTelemetryRecorder.set(telemetryRecorder);

        // --------
        // determine the excluded type filer
        try {
            Optional.ofNullable(processing.getOptions().get(EXCLUDED_TYPES_PATTERN))
                .map(Pattern::compile)
                .map(Pattern::asPredicate)
                .map(this.lazyExcludedTypesPredicate::set);
        }
        catch (final PatternSyntaxException e) {
            telemetryRecorder.warn(
                "The Code Model AnnotationProcessor option %s cannot be parsed as a regular expression", e);
        }

        // --------
        // establish the Framework to use for creating and compiling CodeModels
        final var framework = builder.build();
        this.lazyFramework.set(framework);

        messager.printNote("Loaded " + framework.plugins().count() + " Framework Plugin(s)");
        messager.printNote("Loaded " + framework
            .plugins(Initializer.class)
            .count() + " Initializer(s)");
        messager.printNote("Loaded " + framework
            .plugins(Enricher.class)
            .count() + " Enrichers(s)");
        messager.printNote("Loaded " + framework.plugins(TypeChecker.class).count() + " TypeChecker(s)");
        messager.printNote("Loaded " + framework.plugins(Compiler.class).count() + " Compiler(s)");
        messager.printNote("Loaded " + framework.plugins(Completer.class).count() + " Completer(s)");

        // --------
        // bootstrap the AnnotationDiscovery (using a ServiceLoader)
        final var bootstrapNameProvider = new NonCachingNameProvider();
        final var bootstrapCodeModel = new JDKCodeModel(bootstrapNameProvider);

        final var bootstrapInjectionFramework = new InjectionFramework(bootstrapCodeModel);
        final var bootstrapContext = bootstrapInjectionFramework.newContext();

        bootstrapContext.bind(FileSystem.class).to(framework.fileSystem());
        bootstrapContext.bind(NameProvider.class).to(framework.nameProvider());

        ServiceLoader.load(AnnotationDiscovery.class, serviceLoaderClassLoader).stream()
            .map(ServiceLoader.Provider::get)
            .peek(annotationDiscovery -> messager
                .printNote("Loaded Annotation Discovery: " + annotationDiscovery.getClass().getSimpleName()))
            .peek(bootstrapContext::inject)
            .forEach(this.annotationDiscoveries::add);

        messager.printNote("Loaded " + this.annotationDiscoveries.size() + " AnnotationDiscovery(s)");
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment round) {

        final var processing = this.processingEnv;
        final var messager = processing.getMessager();
        final var framework = this.lazyFramework.get();
        final var telemetryRecorder = this.lazyTelemetryRecorder.get();

        // obtain the CodeModel, or compute a new one
        final var codeModel = this.lazyCodeModel.computeIfAbsent(() -> {
            final var newCodeModel = framework.newCodeModel(JDKCodeModel.class);
            messager.printNote(
                "Created CodeModel with " + newCodeModel.typeDescriptors().count() + " TypeDescriptor(s)");
            return newCodeModel;
        }).orElseThrow();

        if (!annotations.isEmpty()) {
            // perform annotation processing

            // --------
            // STAGE 1: Discover TypeDescriptors for the discoverable models
            messager.printNote("Stage 1: Discovering and Building Code Model");

            // establish a LinkedHashMap-based queue of pending TypeElements to be processed
            // (these may or may not be annotated, but we start with @Discoverable annotated ones!)
            final var pending = new LinkedHashMap<TypeName, TypeElement>();
            annotations.stream()
                .flatMap(annotation -> round.getElementsAnnotatedWith(annotation).stream())
                .filter(element -> element.getKind().isDeclaredType())
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .forEach(typeElement -> {
                    final var typeName = getTypeName(typeElement);
                    include(codeModel, typeName, typeElement, pending);
                });

            while (!pending.isEmpty()) {
                // obtain the first entry for processing (but don't remove it)
                // ( we want to prevent recursive processing for recursively defined types)
                final var entry = pending.firstEntry();
                final var typeName = entry.getKey();
                final var typeElement = entry.getValue();

                createTypeDescriptor(codeModel, typeName, typeElement, pending);

                // now remove it!
                pending.remove(typeName);
            }

            // enrich the CodeModel
            framework.enrich(codeModel, telemetryRecorder);

            // attempt to type-check the CodeModel
            framework.typeCheck(codeModel, telemetryRecorder)
                .ifPresent(__ -> {
                    // dump out the TypeDescriptors
                    codeModel.typeDescriptors()
                        .forEach(typeDescriptor -> messager.printNote("TypeDescriptor: "
                            + typeDescriptor.typeName()
                            + (typeDescriptor.traits(ExtendsTypeDescriptor.class).findFirst().isPresent()
                            ? " extends " + typeDescriptor.traits(ExtendsTypeDescriptor.class)
                            .map(ExtendsTypeDescriptor::parentTypeUsage)
                            .map(Objects::toString)
                            .collect(Collectors.joining(", "))
                            : "")
                            + (typeDescriptor.traits(ImplementsTypeDescriptor.class).findFirst().isPresent()
                            ? " implements " + typeDescriptor.traits(ImplementsTypeDescriptor.class)
                            .map(ImplementsTypeDescriptor::parentTypeUsage)
                            .map(Objects::toString)
                            .collect(Collectors.joining(", "))
                            : "")
                            + (typeDescriptor.traits().findFirst().isPresent()
                            ? " Traits["
                            + typeDescriptor.traits().map(Object::getClass).map(Class::getSimpleName)
                            .collect(Collectors.joining(", ")) + "]"
                            : "")));

                    // --------
                    // STAGE 3: Compile the Code Model
                    messager.printNote("Stage 2: Compiling Code Model");
                    framework.compile(codeModel, telemetryRecorder)
                        .ifPresentOrElse(this.capturedCompilation::set, this.capturedCompilation::clear);
                });
        }
        else {
            // --------
            // STAGE 4: Complete Processing (the last round without any Annotations to Process)

            // perform finishing if there were no errors reported
            if (!telemetryRecorder.hasObserved(Error.class) && this.capturedCompilation.isPresent()) {

                this.capturedCompilation.ifPresent(compilation -> {
                    messager.printNote("Stage 3: Completing Compilation");
                    framework.complete(compilation, telemetryRecorder);
                });
            }
        }

        return false;
    }

    /**
     * Includes the specified {@link TypeUsage} for discovery should it not already discovered or pending discovery.
     *
     * @param codeModel       the {@link CodeModel}
     * @param typeUsage        the {@link TypeUsage}
     * @param enclosingElement the {@link Element} in which the {@link TypeUsage} is defined
     * @param pending          the pending {@link TypeElement}s to process
     */
    private void include(final CodeModel codeModel,
                         final TypeUsage typeUsage,
                         final Element enclosingElement,
                         final LinkedHashMap<TypeName, TypeElement> pending) {

        // only SpecificTypeUsages are discoverable
        if (typeUsage instanceof SpecificTypeUsage specificTypeUsage) {
            include(codeModel, specificTypeUsage.typeName(), enclosingElement, pending);
        }
    }

    /**
     * Includes the specified {@link TypeName} for discovery should it not already discovered or pending discovery.
     *
     * @param codeModel       the {@link CodeModel}
     * @param typeName         the {@link TypeName}
     * @param enclosingElement the {@link Element} in which the {@link TypeUsage} is defined
     * @param pending          the pending {@link TypeElement}s to process
     */
    private void include(final CodeModel codeModel,
                         final TypeName typeName,
                         final Element enclosingElement,
                         final LinkedHashMap<TypeName, TypeElement> pending) {

        // only include the TypeName when it's not excluded
        if (this.lazyExcludedTypesPredicate
            .map(predicate -> !predicate.test(typeName.canonicalName()))
            .orElse(true)) {

            // we only need to discover the Type when we don't already have a TypeDescriptor
            // and the type name is not already pending processing
            {
                if (codeModel.getTypeDescriptor(typeName).isEmpty() &&
                    !pending.containsKey(typeName)) {

                    final var processing = this.processingEnv;
                    final var messager = processing.getMessager();

                    // attempt to determine the TypeElement for the required type
                    final var typeElement = processing.getElementUtils().getTypeElement(typeName.canonicalName());

                    if (typeElement == null) {
                        // warn when the type is not primitive
                        if (!isPrimitive(typeName)) {
                            messager.printWarning("Failed to determine TypeElement for " + typeName.canonicalName(),
                                enclosingElement);
                        }
                    }
                    else {
                        pending.putLast(typeName, typeElement);
                    }
                }
            }
        }
    }

    /**
     * Determines if the specified {@link TypeName} represents a primitive type.
     *
     * @param typeName the {@link TypeName}
     * @return {@code true} if the {@link TypeName} represents a primitive type, {@code false} otherwise
     */
    private boolean isPrimitive(final TypeName typeName) {
        return typeName != null
            && typeName.namespace()
            .filter(namespace -> namespace.toString().equals("java.lang"))
            .isPresent()
            && Introspection.primitives().anyMatch(primitive -> typeName.name().toString().equals(primitive.getName()));
    }

    /**
     * Obtains a {@link TypeName} for the specified {@link TypeElement}.
     *
     * @param element the {@link TypeElement}
     * @return the {@link TypeName}
     */
    private TypeName getTypeName(final TypeElement element) {

        final var existing = this.typeNames.get(element);
        if (existing != null) {
            return existing;
        }

        // attempt to determine the TypeName based on the Element
        final var typeName = getTypeName((Element) element);

        // notify the bridge of the TypeName for the TypeElement
        this.typeNames.putIfAbsent(element, typeName);

        return typeName;
    }

    /**
     * Obtains a {@link TypeName} for the specified {@link Element}.
     *
     * @param element the {@link Element}
     * @return the {@link TypeName}
     */
    private TypeName getTypeName(final Element element) {

        final var processing = this.processingEnv;
        final var elements = processing.getElementUtils();
        final var codeModel = this.lazyCodeModel.get();
        final var naming = codeModel.getNameProvider();

        // determine the Module
        final var moduleName = Optional.ofNullable(elements.getModuleOf(element))
            .flatMap(moduleElement -> moduleElement.getQualifiedName().isEmpty()
                ? Optional.empty()
                : Optional.of(moduleElement))
            .flatMap(moduleElement -> naming.getModuleName(moduleElement.getQualifiedName()));

        // determine the Namespace
        final var namespace = Optional.ofNullable(elements.getPackageOf(element))
            .flatMap(packageElement -> packageElement.getQualifiedName().isEmpty()
                ? Optional.empty()
                : Optional.of(packageElement))
            .flatMap(packageElement -> naming.getNamespace(packageElement.getQualifiedName()));

        // determine the enclosing TypeName
        // (do this by determining the TypeName of the enclosing element - if it's a TypeElement!)
        final var enclosingElement = element.getEnclosingElement();
        final var enclosingTypeName = enclosingElement instanceof TypeElement enclosingTypeElement
            ? Optional.of(getTypeName(enclosingTypeElement))
            : Optional.<TypeName>empty();

        // determine the name of the Type
        final var name = naming.getIrreducibleName(element.getSimpleName());

        // create a TypeName
        return naming.getTypeName(moduleName, namespace, enclosingTypeName, name);
    }

    /**
     * Obtain the {@link TypeUsage} specified by the provided {@link TypeMirror} within the provided
     * enclosing {@link Element}.
     *
     * @param typeMirror       the {@link TypeMirror}
     * @param enclosingElement the enclosing {@link Element}
     * @return the {@link TypeUsage}
     */
    private TypeUsage resolveTypeUsage(final TypeMirror typeMirror,
                                       final Element enclosingElement) {

        final var types = this.processingEnv.getTypeUtils();
        final var elements = this.processingEnv.getElementUtils();
        final var messager = this.processingEnv.getMessager();
        final var codeModel = this.lazyCodeModel.get();
        final var nameProvider = codeModel.getNameProvider();
        final var recorder = this.lazyTelemetryRecorder.get();

        final var pending = new LinkedHashMap<TypeMirror, Lazy<TypeUsage>>();
        final var enclosing = new HashMap<TypeMirror, Element>();
        final var resolved = new LinkedHashMap<TypeMirror, TypeUsage>();
        final var annotations = new HashMap<TypeMirror, ArrayList<AnnotationTypeUsage>>();

        pending.put(typeMirror, Lazy.empty());
        enclosing.put(typeMirror, enclosingElement);

        while (!pending.isEmpty()) {

            final var entry = pending.lastEntry();
            final var pendingTypeMirror = entry.getKey();
            final var pendingLazyTypeUsage = entry.getValue();
            final var pendingElement = enclosing.get(pendingTypeMirror);

            if (pendingLazyTypeUsage.isPresent()) {
                resolved.put(pendingTypeMirror, pendingLazyTypeUsage.get());
                pending.remove(pendingTypeMirror);
            }
            else if (resolved.containsKey(pendingTypeMirror)) {
                pending.remove(pendingTypeMirror);
            }
            else {
                // resolve the AnnotationTypeUsages for the TypeMirror (when not already done so)
                annotations.computeIfAbsent(pendingTypeMirror, __ ->
                    pendingTypeMirror.getAnnotationMirrors().stream()
                        .map(mirror -> createAnnotationTypeUsage(pendingElement, mirror))
                        .collect(Collectors.toCollection(ArrayList::new)));

                // resolve the TypeUsage from the TypeMirror (by visiting it)
                pendingTypeMirror.accept(new TypeVisitor<Lazy<TypeUsage>, Lazy<TypeUsage>>() {
                    @Override
                    public Lazy<TypeUsage> visit(final TypeMirror t,
                                                 final Lazy<TypeUsage> lazyTypeUsage) {

                        // NOTE: Not sure what to do with this? When is it actually called?
                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitPrimitive(final PrimitiveType primitiveType,
                                                          final Lazy<TypeUsage> lazyTypeUsage) {

                        final var moduleName = nameProvider.getModuleName("java.base");
                        final var namespace = nameProvider.getNamespace("java.lang");

                        final var typeName = nameProvider.getTypeName(
                            moduleName,
                            namespace,
                            Optional.empty(),
                            nameProvider.getIrreducibleName(primitiveType.toString()));

                        final var typeUsage = SpecificTypeUsage.of(codeModel, typeName);

                        // include the annotation defined traits
                        annotations.get(pendingTypeMirror).stream()
                            .forEach(typeUsage::addTrait);

                        lazyTypeUsage.set(typeUsage);

                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitNull(final NullType nullType,
                                                     final Lazy<TypeUsage> lazyTypeUsage) {

                        final var typeUsage = UnknownTypeUsage.create(codeModel);
                        lazyTypeUsage.set(typeUsage);
                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitArray(final ArrayType arrayType,
                                                      final Lazy<TypeUsage> lazyTypeUsage) {

                        // resolve the ComponentType, otherwise queue it for resolution
                        final var componentType = arrayType.getComponentType();
                        final var lazyComponentTypeUsage = Lazy.ofNullable(resolved.get(componentType))
                            .or(() -> pending.get(componentType));

                        if (lazyComponentTypeUsage.isEmpty()) {
                            // queue the ComponentType to be resolved
                            pending.putIfAbsent(componentType, lazyComponentTypeUsage);
                            enclosing.putIfAbsent(componentType, pendingElement);
                        }

                        final var typeUsage = ArrayTypeUsage.of(codeModel, lazyComponentTypeUsage);

                        annotations.get(pendingTypeMirror).stream()
                            .forEach(typeUsage::addTrait);

                        lazyTypeUsage.set(typeUsage);

                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitDeclared(final DeclaredType declaredType,
                                                         final Lazy<TypeUsage> lazyTypeUsage) {

                        final var typeElement = (TypeElement) types.asElement(declaredType);

                        final var moduleName = Optional.ofNullable(elements.getModuleOf(typeElement))
                            .flatMap(moduleElement -> nameProvider.getModuleName(moduleElement.getQualifiedName()));

                        final var fullyQualifiedTypeName = typeElement.getQualifiedName().toString();
                        final var typeName = nameProvider.getTypeName(moduleName, fullyQualifiedTypeName);

                        if (typeElement.getTypeParameters().isEmpty()) {
                            final var typeUsage = SpecificTypeUsage.of(codeModel, typeName);

                            annotations.get(pendingTypeMirror).stream()
                                .forEach(typeUsage::addTrait);

                            lazyTypeUsage.set(typeUsage);
                        }
                        else {
                            final var genericTypeUsage = GenericTypeUsage.of(
                                codeModel,
                                typeName,
                                declaredType
                                    .getTypeArguments().stream()
                                    .map(argumentMirror -> {
                                        final var lazyArgumentMirror = Lazy.ofNullable(resolved.get(argumentMirror))
                                            .or(() -> pending.get(argumentMirror));

                                        if (lazyArgumentMirror.isEmpty()) {
                                            pending.putIfAbsent(argumentMirror, lazyArgumentMirror);
                                            enclosing.putIfAbsent(argumentMirror, pendingElement);
                                        }
                                        return lazyArgumentMirror;
                                    }));

                            // TODO: include annotations defined on the types usage

                            lazyTypeUsage.set(genericTypeUsage);
                        }

                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitError(final ErrorType errorType,
                                                      final Lazy<TypeUsage> lazyTypeUsage) {

                        final var element = errorType.asElement();

                        // Record the ErrorType as an error/unknown type that we can't process
                        recorder.error(ElementLocation.of(element),
                            "Missing or undefined type " + element.getSimpleName());

                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitTypeVariable(final TypeVariable typeVariable,
                                                             final Lazy<TypeUsage> lazyTypeUsage) {

                        final var typeName = nameProvider.getTypeName(
                            Optional.empty(),
                            typeVariable.asElement().getSimpleName().toString());

                        // resolve the LowerBound, otherwise queue it for resolution
                        final var lowerBound = typeVariable.getLowerBound();

                        // only resolve non-NULL lower bounds
                        // (in NULL lower bounds mean undefined, so we can map them to empty)
                        final var optionalLazyLowerBoundTypeUsage = lowerBound.getKind() == TypeKind.NULL
                            ? Optional.<Lazy<TypeUsage>>empty()
                            : Optional.of(
                                Lazy.ofNullable(resolved.get(lowerBound))
                                    .or(() -> pending.get(lowerBound)));

                        optionalLazyLowerBoundTypeUsage.ifPresent(lazyLowerBoundTypeUsage -> {
                            if (lazyLowerBoundTypeUsage.isEmpty()) {
                                // queue the LowerBound to be resolved
                                pending.putIfAbsent(lowerBound, lazyLowerBoundTypeUsage);
                                enclosing.putIfAbsent(lowerBound, typeVariable.asElement());
                            }
                        });

                        // resolve the UpperBound, otherwise queue it for resolution
                        // (in java.lang.Object upper bounds mean undefined, so we can map them to empty)
                        final var upperBound = typeVariable.getUpperBound();
                        final var optionalLazyUpperBoundTypeUsage = upperBound.getKind() == TypeKind.DECLARED
                            && ((DeclaredType) upperBound).asElement().getSimpleName()
                            .toString()
                            .equals(Object.class.getSimpleName())
                            ? Optional.<Lazy<TypeUsage>>empty()
                            : Optional.of(Lazy.ofNullable(resolved.get(upperBound))
                                .or(() -> pending.get(upperBound)));

                        optionalLazyUpperBoundTypeUsage.ifPresent(lazyUpperBoundTypeUsage -> {
                            if (lazyUpperBoundTypeUsage.isEmpty()) {
                                // queue the UpperBound to be resolved
                                pending.putIfAbsent(upperBound, lazyUpperBoundTypeUsage);
                                enclosing.putIfAbsent(upperBound, typeVariable.asElement());
                            }
                        });

                        final var typeUsage = TypeVariableUsage
                            .of(codeModel, typeName, optionalLazyLowerBoundTypeUsage, optionalLazyUpperBoundTypeUsage);

                        // TODO: include annotations defined on the types usage

                        lazyTypeUsage.set(typeUsage);

                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitWildcard(final WildcardType wildcardType,
                                                         final Lazy<TypeUsage> lazyTypeUsage) {

                        // TODO: include annotations defined on the types usage

                        lazyTypeUsage.set(WildcardTypeUsage.create(codeModel));
                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitExecutable(final ExecutableType executableType,
                                                           final Lazy<TypeUsage> lazyTypeUsage) {

                        // SKIP: We aren't yet required to determine the TypeUsage of an ExecutableType
                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitNoType(final NoType noType,
                                                       final Lazy<TypeUsage> lazyTypeUsage) {

                        // TODO: include annotations defined on the types usage

                        lazyTypeUsage.set(VoidTypeUsage.create(codeModel));
                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitUnknown(final TypeMirror unknownType,
                                                        final Lazy<TypeUsage> lazyTypeUsage) {

                        // TODO: include annotations defined on the types usage

                        lazyTypeUsage.set(UnknownTypeUsage.create(codeModel));
                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitUnion(final UnionType unionType,
                                                      final Lazy<TypeUsage> lazyTypeUsage) {

                        // queue the alternative for resolution (if not already resolved)
                        lazyTypeUsage.set(UnionTypeUsage.of(codeModel, unionType.getAlternatives().stream()
                            .map(alternativeMirror -> {
                                final var lazyAlternativeMirror = Lazy.ofNullable(resolved.get(alternativeMirror))
                                    .or(() -> pending.get(alternativeMirror));

                                if (lazyAlternativeMirror.isEmpty()) {
                                    pending.putIfAbsent(alternativeMirror, lazyAlternativeMirror);
                                    enclosing.putIfAbsent(alternativeMirror, pendingElement);
                                }

                                return lazyAlternativeMirror;
                            })));

                        // TODO: include annotations defined on the types usage

                        return lazyTypeUsage;
                    }

                    @Override
                    public Lazy<TypeUsage> visitIntersection(final IntersectionType intersectionType,
                                                             final Lazy<TypeUsage> lazyTypeUsage) {

                        // queue the alternative for resolution (if not already resolved)
                        lazyTypeUsage.set(IntersectionTypeUsage.of(codeModel, intersectionType.getBounds().stream()
                            .map(boundMirror -> {
                                final var lazyBoundMirror = Lazy.ofNullable(resolved.get(boundMirror))
                                    .or(() -> pending.get(boundMirror));

                                if (lazyBoundMirror.isEmpty()) {
                                    pending.putIfAbsent(boundMirror, lazyBoundMirror);
                                    enclosing.putIfAbsent(boundMirror, pendingElement);
                                }

                                return lazyBoundMirror;
                            })));

                        // TODO: include annotations defined on the types usage

                        return lazyTypeUsage;
                    }
                }, pendingLazyTypeUsage);
            }
        }

        // return the resolved TypeMirror
        final var typeUsage = resolved.get(typeMirror);

        if (typeUsage == null) {
            // defensive
            messager.printError("Unsupported type " + typeMirror, enclosingElement);
            throw new IllegalStateException("Failed to resolve the TypeUsage for:" + typeMirror);
        }
        return typeUsage;
    }

    /**
     * Creates a {@link FieldDescriptor} for the {@link TypeDescriptor} based on an {@link ExecutableElement}.
     *
     * @param codeModel     the {@link CodeModel}
     * @param typeDescriptor the {@link TypeDescriptor} in which to define the {@link FieldDescriptor}
     * @param fieldElement   the {@link TypeElement} for the <i>field</i>
     * @param pending        the pending {@link TypeElement}s to be processed
     * @return a {@link FieldDescriptor}
     */
    private FieldDescriptor createFieldDescriptor(final CodeModel codeModel,
                                                  final TypeDescriptor typeDescriptor,
                                                  final VariableElement fieldElement,
                                                  final LinkedHashMap<TypeName, TypeElement> pending) {

        final var nameProvider = codeModel.getNameProvider();

        final var fieldName = nameProvider.getIrreducibleName(fieldElement.getSimpleName());
        final var type = resolveTypeUsage(fieldElement.asType(), fieldElement);

        final var fieldDescriptor = FieldDescriptor.of(codeModel, fieldName, type);
        fieldDescriptor.addTrait(ElementLocation.of(fieldElement));

        // include the AnnotationUsages (as traits)
        fieldElement.getAnnotationMirrors().stream()
            .map(mirror -> createAnnotationTypeUsage(fieldElement, mirror))
            .forEach(fieldDescriptor::addTrait);

        typeDescriptor.addTrait(fieldDescriptor);

        // include the Static trait (if necessary)
        if (fieldElement.getModifiers().contains(Modifier.STATIC)) {
            fieldDescriptor.addTrait(Static.STATIC);
        }

        // include the AccessModifier for the Field
        getAccessModifier(fieldElement.getModifiers())
            .ifPresent(fieldDescriptor::addTrait);

        // ensure the discovery of types used by the FieldDescriptor
        fieldDescriptor.dependencies()
            .forEach(typeUsage -> include(codeModel, typeUsage, fieldElement, pending));

        return fieldDescriptor;
    }

    /**
     * Creates a {@link MethodDescriptor} for the {@link TypeDescriptor} based on an {@link ExecutableElement}.
     *
     * @param codeModel     the {@link CodeModel}
     * @param typeDescriptor the {@link TypeDescriptor} in which the {@link MethodDescriptor} is being defined
     * @param methodElement  the {@link ExecutableElement} for the <i>constructor</i>
     * @param pending        the pending {@link TypeElement}s to be processed
     * @return a {@link MethodDescriptor}
     */
    private MethodDescriptor createMethodDescriptor(final CodeModel codeModel,
                                                    final TypeDescriptor typeDescriptor,
                                                    final ExecutableElement methodElement,
                                                    final LinkedHashMap<TypeName, TypeElement> pending) {

        final var nameProvider = codeModel.getNameProvider();
        final var name = nameProvider.getIrreducibleName(methodElement.getSimpleName());
        final var returnType = resolveTypeUsage(methodElement.getReturnType(), methodElement);

        final var methodName = MethodName.of(
            typeDescriptor.typeName().moduleName(),
            typeDescriptor.typeName().namespace(),
            Optional.of(typeDescriptor.typeName()),
            name);

        final var formalParameters = methodElement.getParameters().stream()
            .map(variable -> {
                final var formalParameterName = variable.getSimpleName().isEmpty()
                    ? Optional.<IrreducibleName>empty()
                    : Optional.of(nameProvider.getIrreducibleName(variable.getSimpleName()));

                final var formalParameterType = resolveTypeUsage(variable.asType(), variable);

                final var formalParameterDescriptor = FormalParameterDescriptor
                    .of(codeModel, formalParameterName, formalParameterType);

                formalParameterDescriptor.addTrait(ElementLocation.of(variable));

                return formalParameterDescriptor;
            });

        final var methodDescriptor = MethodDescriptor
            .of(typeDescriptor, methodName, returnType, formalParameters);

        methodElement.getThrownTypes().stream()
            .map(typeMirror -> resolveTypeUsage(typeMirror, methodElement))
            .map(ThrowableDescriptor::of)
            .forEach(methodDescriptor::addTrait);

        methodDescriptor.addTrait(ElementLocation.of(methodElement));

        // include the AnnotationUsages (as Traits)
        methodElement.getAnnotationMirrors().stream()
            .map(mirror -> createAnnotationTypeUsage(methodElement, mirror))
            .forEach(methodDescriptor::addTrait);

        // include the MethodImplementationDescriptor (when the constructor has an implementation)
        if (methodElement.isDefault()) {
            methodDescriptor.addTrait(new MethodImplementationDescriptor(methodDescriptor));
        }

        // include the Static trait (if necessary)
        if (methodElement.getModifiers().contains(Modifier.STATIC)) {
            methodDescriptor.addTrait(Static.STATIC);
        }

        // include the AccessModifier for the Method
        getAccessModifier(methodElement.getModifiers())
            .ifPresent(methodDescriptor::addTrait);

        // include the Classification for the Method
        methodDescriptor.addTrait(getClassification(methodElement.getModifiers()));

        // include the MethodDescriptor in the TypeDescriptor
        typeDescriptor.addTrait(methodDescriptor);

        // ensure the discovery of types used by the MethodPrototype
        methodDescriptor.dependencies()
            .forEach(typeUsage -> include(codeModel, typeUsage, methodElement, pending));

        return methodDescriptor;
    }

    /**
     * Creates a {@link TypeDescriptor} using the {@link CodeModel} based on a {@link TypeElement}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param typeName    the {@link TypeName}
     * @param typeElement the {@link TypeElement}
     * @param pending     the pending {@link TypeElement}s by {@link TypeName}s to process
     * @return a {@link TypeDescriptor}
     */
    private TypeDescriptor createTypeDescriptor(final CodeModel codeModel,
                                                final TypeName typeName,
                                                final TypeElement typeElement,
                                                final LinkedHashMap<TypeName, TypeElement> pending) {

        try {
            final var processing = this.processingEnv;
            final var messager = processing.getMessager();

            // attempt to find the existing TypePrototype for the TypeName
            final var existing = codeModel.getTypeDescriptor(typeName).orElse(null);
            if (existing != null) {
                return existing;
            }

            // establish the TypeDescriptor for the TypeElement
            final var typeDescriptor = codeModel.createTypeDescriptor(typeName, typeElement.getKind().isInterface()
                ? JDKInterfaceTypeDescriptor::of
                : JDKClassTypeDescriptor::of);

            typeDescriptor.addTrait(ElementLocation.of(typeElement));

            messager.printNote("Discovered TypeName [" + typeName + "]", typeElement);

            // discover the (generic) type parameters
            if (!typeElement.getTypeParameters().isEmpty()) {

                final var typeVariableUsages = typeElement.getTypeParameters().stream()
                    .map(typeParameterElement -> resolveTypeUsage(typeParameterElement.asType(), typeParameterElement))
                    .peek(typeUsage -> {
                        if (!(typeUsage instanceof TypeVariableUsage)) {
                            messager.printError(
                                "The type [" + typeName + "] defines a non-TypeVariable type with [" + typeUsage + "]",
                                typeElement);
                        }
                    })
                    .filter(TypeVariableUsage.class::isInstance)
                    .map(TypeVariableUsage.class::cast);

                typeDescriptor.addTrait(ParameterizedTypeDescriptor.of(codeModel, typeVariableUsages));
            }

            // include the Static trait (if necessary)
            if (typeElement.getModifiers().contains(Modifier.STATIC)) {
                typeDescriptor.addTrait(Static.STATIC);
            }

            // discover the AccessModifier
            getAccessModifier(typeElement.getModifiers())
                .ifPresent(typeDescriptor::addTrait);

            // discover the Classification
            typeDescriptor.addTrait(getClassification(typeElement.getModifiers()));

            // discover the ExtendsTypeDescriptors
            //if (typeElement.getSuperclass().getKind() != TypeKind.NONE) {
            Stream.of(typeElement.getSuperclass())
                .map(superMirror -> resolveTypeUsage(superMirror, typeElement))
                .filter(NamedTypeUsage.class::isInstance)
                .map(NamedTypeUsage.class::cast)
                .filter(namedTypeUsage -> !(namedTypeUsage instanceof VoidTypeUsage))
                .forEach(superType -> typeDescriptor.addTrait(ExtendsTypeDescriptor.of(superType)));
            //}

            // discover the ImplementsTypeDescriptors
            typeElement.getInterfaces().stream()
                .map(implementsMirror -> resolveTypeUsage(implementsMirror, typeElement))
                .filter(NamedTypeUsage.class::isInstance)
                .map(NamedTypeUsage.class::cast)
                .forEach(interfaceType -> typeDescriptor.addTrait(ImplementsTypeDescriptor.of(interfaceType)));

            // discover the FieldDescriptors
            typeElement.getEnclosedElements().stream()
                .filter(enclosing -> enclosing.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .forEach(enclosing -> createFieldDescriptor(codeModel, typeDescriptor, enclosing, pending));

            // discover the MethodDescriptors
            typeElement.getEnclosedElements().stream()
                .filter(enclosing -> enclosing.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .forEach(enclosing -> createMethodDescriptor(codeModel, typeDescriptor, enclosing, pending));

            // include the AnnotationUsages (as Traits)
            typeElement.getAnnotationMirrors().stream()
                .map(mirror -> createAnnotationTypeUsage(typeElement, mirror))
                .forEach(typeDescriptor::addTrait);

            // ensure the discovery of types used by the TypePrototype
            typeDescriptor.dependencies()
                .forEach(typeUsage -> include(codeModel, typeUsage, typeElement, pending));

            return typeDescriptor;
        }
        catch (final StackOverflowError e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Determine the {@link AnnotationTypeUsage} based on the {@link AnnotationMirror}.
     *
     * @param enclosingElement the {@link Element} in which the {@link AnnotationTypeUsage} occurs
     * @param annotationMirror the {@link AnnotationMirror} for the {@link AnnotationTypeUsage}
     * @return the {@link AnnotationTypeUsage}
     */
    private AnnotationTypeUsage createAnnotationTypeUsage(final Element enclosingElement,
                                                          final AnnotationMirror annotationMirror) {

        final var codeModel = this.lazyCodeModel.get();
        final var nameProvider = codeModel.getNameProvider();

        // establish a Location for the AnnotationTypeUsage
        final var annotationLocation = ElementLocation.ofNullable(enclosingElement)
            .map(elementLocation -> elementLocation.createAnnotationMirrorLocation(annotationMirror));

        // determine the AnnotationUsage TypeName from the AnnotationMirror AnnotationType Element
        final var annotationTypeName = getTypeName(annotationMirror.getAnnotationType().asElement());

        // resolve the annotation values from the AnnotationMirror
        final var values = new ArrayList<AnnotationValue>();

        annotationMirror.getElementValues()
            .forEach((executableElement, annotatedElement) -> {
                final var name = nameProvider.getIrreducibleName(executableElement.getSimpleName());
                final var value = annotatedElement.getValue();
                final var valueLocation = annotationLocation
                    .map(location -> location.createAnnotationValueLocation(annotatedElement));

                final var annotationValue = AnnotationValue.of(codeModel, name, value);

                valueLocation.ifPresent(annotationValue::addTrait);

                values.add(annotationValue);
            });

        final var annotationTypeUsage = AnnotationTypeUsage.of(codeModel, annotationTypeName, values.stream());

        annotationLocation.ifPresent(annotationTypeUsage::addTrait);

        return annotationTypeUsage;
    }

    /**
     * Attempts to determine the {@link AccessModifier} given {@link Modifier}s.
     *
     * @param modifiers the {@link Modifier}s
     * @return the {@link Optional} {@link AccessModifier}
     */
    private Optional<AccessModifier> getAccessModifier(final Set<Modifier> modifiers) {

        if (modifiers == null || modifiers.isEmpty()) {
            return Optional.empty();
        }

        if (modifiers.contains(Modifier.PUBLIC)) {
            return Optional.of(AccessModifier.PUBLIC);
        }
        else if (modifiers.contains(Modifier.PROTECTED)) {
            return Optional.of(AccessModifier.PROTECTED);
        }
        else if (modifiers.contains(Modifier.PRIVATE)) {
            return Optional.of(AccessModifier.PRIVATE);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Attempts to determine the {@link Classification} given {@link Modifier}s.
     *
     * @param modifiers the {@link Modifier}s
     * @return the {@link Classification}
     */
    private Classification getClassification(final Set<Modifier> modifiers) {

        if (modifiers == null || modifiers.isEmpty()) {
            return Classification.CONCRETE;
        }

        if (modifiers.contains(Modifier.ABSTRACT)) {
            return Classification.ABSTRACT;
        }
        else {
            return Classification.CONCRETE;
        }
    }

    /**
     * Obtains the {@link Optional}ly initialized {@link CodeModel}.
     *
     * @return the {@link Optional} {@link CodeModel}
     */
    public Optional<CodeModel> getCodeModel() {
        return this.lazyCodeModel.optional();
    }

    /**
     * Obtains the {@link Lazy}ily initialized {@link Framework}.
     *
     * @return the {@link Lazy} {@link Framework}
     */
    public Lazy<Framework> getFramework() {
        return this.lazyFramework;
    }
}
