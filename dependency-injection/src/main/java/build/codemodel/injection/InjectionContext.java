package build.codemodel.injection;

/*-
 * #%L
 * Dependency Injection
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

import build.base.foundation.Lazy;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.jdk.TypeUsages;
import build.codemodel.jdk.descriptor.MethodType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An internal {@link Context} implementation.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
class InjectionContext
    implements Context {

    /**
     * The {@link InjectionFramework} that created the {@link Context}.
     */
    private final InjectionFramework injectionFramework;

    /**
     * The {@link Binding}s in the {@link Context} by {@link Dependency}.
     */
    private final ConcurrentHashMap<Dependency, Binding<?>> bindingsByDependency;

    /**
     * The multibinding entries keyed by element type.
     */
    private final ConcurrentHashMap<Class<?>, MultiBindingEntry<?>> multiBindings;

    /**
     * The {@link Resolver} defined by the {@link Binding}s in this {@link Context}, so this {@link Context}
     * may be used to resolve {@link Binding}s of other {@link Context}s.
     */
    private final Resolver<Object> resolver;

    /**
     * The {@link ChainedResolver} of {@link Resolver}s added to this {@link InjectionContext}.
     */
    private final ChainedResolver chainedResolver;

    /**
     * Constructs an {@link InjectionContext}.
     *
     * @param injectionFramework the {@link InjectionFramework}
     */
    @SuppressWarnings("unchecked")
    InjectionContext(final InjectionFramework injectionFramework) {
        this.injectionFramework = Objects
            .requireNonNull(injectionFramework, "The Injection framework must not be null");
        this.bindingsByDependency = new ConcurrentHashMap<>();
        this.multiBindings = new ConcurrentHashMap<>();
        this.chainedResolver = ChainedResolver.create();
        this.resolver = ChainedResolver.create(
            dependency -> Optional.ofNullable((Binding<Object>) this.bindingsByDependency.get(dependency)),
            dependency -> (Optional<Binding<Object>>) (Optional<?>) resolveMultiBinding(dependency),
            chainedResolver);
    }

    @Override
    public Context addResolver(final Resolver<?> resolver) {
        if (resolver != null) {
            this.chainedResolver.addResolver(resolver);
        }
        return this;
    }

    @Override
    public Context addResolver(final BiFunction<? super InjectionFramework, ? super Context, Resolver<?>> supplier) {

        return supplier == null
            ? this
            : addResolver(supplier.apply(this.injectionFramework, this));
    }

    @Override
    public <T> BindingBuilder<T> bind(final Class<T> bindingClass) {
        Objects.requireNonNull(bindingClass, "The Binding Class must not be null");

        final var codeModel = this.injectionFramework.codeModel();
        final var typeUsage = codeModel.getTypeUsage(bindingClass);

        return new AbstractBindingBuilder<>(this.injectionFramework, typeUsage) {
            @Override
            public Binding<T> to(final T value) {
                final var dependency = IndependentDependency.of(
                    typeUsage,
                    this.injectionFramework::getQualifierAnnotationTypes);

                return addBinding(dependency, SingletonValueBinding.of(dependency, value));
            }

            @Override
            public Binding<T> to(final Class<? extends T> concreteClass) {
                Objects.requireNonNull(concreteClass, "The Binding Value Class must not be null");

                final var dependency = IndependentDependency.of(
                    typeUsage,
                    this.injectionFramework::getQualifierAnnotationTypes);

                final var typeDescriptor = codeModel.getJDKTypeDescriptor(concreteClass)
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Could not resolve a TypeDescriptor for " + concreteClass));

                return addBinding(dependency, this.injectionFramework.isSingleton(typeDescriptor)
                    ? new LazySingletonClassBinding<>(dependency, concreteClass)
                    : new NonSingletonClassBinding<T>(dependency, concreteClass));
            }

            @Override
            public Binding<T> to(final Supplier<T> supplier) {
                final var dependency = IndependentDependency.of(
                    typeUsage,
                    this.injectionFramework::getQualifierAnnotationTypes);

                return addBinding(dependency, new SupplierBinding<>(dependency, supplier));
            }
        };
    }

    /**
     * Attempts to add the specified {@link Dependency} {@link Binding}.
     *
     * @param dependency the {@link Dependency}
     * @param binding    the {@link Binding}
     * @return the {@link Binding} that was added
     * @throws BindingAlreadyExistsException if a {@link Binding} for the {@link Dependency} already exists
     */
    private <T> Binding<T> addBinding(final Dependency dependency, final Binding<T> binding) {
        this.bindingsByDependency.compute(dependency, (_, existing) -> {
            if (existing != null) {
                throw new BindingAlreadyExistsException("Binding for [" + dependency + "] already exists!");
            }
            return binding;
        });
        return binding;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MultiBinder<T> bindSet(final Class<T> type) {
        Objects.requireNonNull(type, "The element type must not be null");
        final var entry = (MultiBindingEntry<T>) this.multiBindings
            .computeIfAbsent(type, _ -> new MultiBindingEntry<T>());
        return new MultiBinder<T>() {
            @Override
            public MultiBinder<T> add(final T value) {
                entry.addValue(value);
                return this;
            }

            @Override
            public MultiBinder<T> add(final Class<? extends T> implementationClass) {
                entry.addSupplier(() -> InjectionContext.this.create(implementationClass));
                return this;
            }

            @Override
            public MultiBinder<T> add(final Supplier<? extends T> supplier) {
                entry.addSupplier(() -> supplier.get());
                return this;
            }
        };
    }

    /**
     * Attempts to resolve a multibinding for collection types ({@link Set}, {@link Collection},
     * {@link Iterable}, {@link java.util.stream.Stream}, {@link List}).
     *
     * @param dependency the {@link Dependency} to resolve
     * @return the resolved {@link Binding}, or empty if not a supported collection multibinding
     */
    @SuppressWarnings("unchecked")
    private Optional<? extends Binding<?>> resolveMultiBinding(final Dependency dependency) {
        if (!(dependency.typeUsage() instanceof GenericTypeUsage generic)) {
            return Optional.empty();
        }

        final String rawName = generic.typeName().canonicalName();
        if (!Set.class.getCanonicalName().equals(rawName)
            && !Collection.class.getCanonicalName().equals(rawName)
            && !Iterable.class.getCanonicalName().equals(rawName)
            && !Stream.class.getCanonicalName().equals(rawName)
            && !List.class.getCanonicalName().equals(rawName)) {
            return Optional.empty();
        }

        return TypeUsages.getFirstTypeParameterClass(generic)
            .map(elementClass -> (MultiBindingEntry<Object>) this.multiBindings.get(elementClass))
            .map(entry -> switch (rawName) {
                case "java.util.Set" -> ValueBinding.of(dependency, (Object) entry.buildSet());
                case "java.util.Collection", "java.lang.Iterable" ->
                    ValueBinding.of(dependency, (Object) (Collection<?>) entry.buildSet());
                case "java.util.List" -> ValueBinding.of(dependency, (Object) List.copyOf(entry.buildSet()));
                case "java.util.stream.Stream" ->
                    new SupplierBinding<>(dependency, (Supplier<Object>) () -> entry.buildSet().stream());
                default -> null;
            });
    }

    @Override
    public Resolver<Object> resolver() {
        return this.resolver;
    }

    @Override
    public <T> T inject(final T injectable)
        throws InjectionException {

        // we can't inject into arrays, enums, interfaces or primitives
        if (injectable == null
            || injectable.getClass().isArray()
            || injectable.getClass().isEnum()
            || injectable.getClass().isPrimitive()) {
            return injectable;
        }

        final var codeModel = this.injectionFramework.codeModel();
        final var typeUsage = codeModel.getTypeUsage(injectable.getClass());
        final var dependency = IndependentDependency.of(
            typeUsage,
            this.injectionFramework::getQualifierAnnotationTypes);

        return new ResolvableObject<>(dependency, injectable)
            .resolve();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> requiredClass)
        throws InjectionException {

        Objects.requireNonNull(requiredClass, "The required class must not be null");

        final var codeModel = this.injectionFramework.codeModel();
        final var typeUsage = codeModel.getTypeUsage(requiredClass);
        final var dependency = IndependentDependency.of(
            typeUsage,
            this.injectionFramework::getQualifierAnnotationTypes);

        return (T) getValue(Optional.empty(), dependency)
            .orElseGet(() -> new ResolvableClass<>(Optional.empty(), dependency, requiredClass)
                .resolve());
    }

    @Override
    public <T> T create(final TypeUsage typeUsage)
        throws InjectionException {

        return create(IndependentDependency.of(typeUsage, this.injectionFramework::getQualifierAnnotationTypes));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(final Dependency dependency)
        throws InjectionException {

        Objects.requireNonNull(dependency, "The Dependency must not be null");

        return (T) getValue(Optional.empty(), dependency)
            .orElseThrow(() -> new UnsatisfiedDependencyException(dependency));
    }

    @Override
    public Context newContext() {
        return this.injectionFramework.newContext(this.resolver());
    }

    /**
     * Attempts to resolve an existing value for the specified {@link Dependency}.
     *
     * @param requiredBy the {@link Resolvable} this {@link Dependency} value is required by
     * @param dependency the {@link Dependency}
     * @param <T>        the type of value
     * @return the {@link Optional} resolved value
     */
    @SuppressWarnings("unchecked")
    private <T> Optional<T> getValue(final Optional<Resolvable<?>> requiredBy,
                                     final Dependency dependency) {

        final var codeModel = this.injectionFramework.codeModel();

        final var binding = resolver()
            .resolve(dependency)
            .orElse(null);

        if (binding instanceof ValueBinding<Object> valueBinding) {
            return Optional.of((T) valueBinding.value());
        } else if (binding instanceof LazySingletonClassBinding<Object> lazySingletonClassBinding) {
            final var concreteClass = lazySingletonClassBinding.concreteClass();
            final var singletonTypeUsage = codeModel.getTypeUsage(concreteClass);
            final var singletonDependency = IndependentDependency.of(
                singletonTypeUsage,
                _ -> injectionFramework.getQualifierAnnotationTypes(binding.dependency().typeUsage()));

            return lazySingletonClassBinding.value()
                .computeIfAbsent(() -> new ResolvableClass<>(requiredBy, singletonDependency, concreteClass)
                    .resolve())
                .map(object -> (T) object)
                .optional();
        } else if (binding instanceof NonSingletonClassBinding<Object> nonSingletonClassBinding) {
            return Optional.of(new ResolvableClass<T>(
                requiredBy,
                dependency,
                (Class<? extends T>) nonSingletonClassBinding.concreteClass())
                .resolve());
        }

        if (binding == null) {
            // obtain the concrete Class for the Dependency
            final var concreteClass = resolveClassFrom(dependency);

            // obtain the TypeDescriptor for the Dependency
            final var typeDescriptor = codeModel.getJDKTypeDescriptor(concreteClass)
                .orElseThrow(() -> new UnsatisfiedDependencyException(dependency));

            if (this.injectionFramework.isSingleton(typeDescriptor)) {
                // establish a binding for the singleton
                bind(concreteClass).to(concreteClass);

                // now attempt to get the value again
                return getValue(requiredBy, dependency);
            } else if (requiredBy.isEmpty()) {
                // when we don't have a requiredBy, that means we're trying to instantiate the class directly
                // (which is ok)
                return Optional.of(new ResolvableClass<T>(
                    requiredBy,
                    dependency,
                    (Class<? extends T>) concreteClass)
                    .resolve());
            }
        }

        return Optional.empty();
    }

    /**
     * Attempts to obtain the {@link Class} represented by the {@link Dependency}.
     *
     * @param dependency the {@link Dependency}
     * @param <T>        the type of {@link Class}
     * @return the {@link Optional} {@link Class}
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> resolveClassFrom(final Dependency dependency) {
        if (dependency.typeUsage() instanceof NamedTypeUsage namedTypeUsage) {
            try {
                return (Class<T>) Class.forName(namedTypeUsage.typeName().canonicalName());
            } catch (final ClassNotFoundException e) {
                throw new UnsatisfiedDependencyException(dependency, e);
            }
        } else {
            throw new UnsatisfiedDependencyException(dependency, "Failed to determine Class");
        }
    }

    /**
     * Represents a {@link Dependency} that is to be resolved through <i>Dependency Injection</i> using
     * the {@link Binding}s provided by the {@link Context}.
     *
     * @param <T> the type to be resolved
     */
    private interface Resolvable<T> {

        /**
         * The {@link Resolvable} that established this {@link Resolvable} as it is required by the former to be
         * resolved.
         *
         * @return the {@link Optional} {@link Resolvable} that requires this {@link Resolvable}
         */
        Optional<Resolvable<?>> requiredBy();

        /**
         * The {@link Dependency} to be resolved.
         *
         * @return the {@link Dependency} to be resolved
         */
        Dependency dependency();

        /**
         * The {@link InjectableDescriptor} defined for the {@link Dependency}.
         *
         * @return the {@link InjectableDescriptor}
         */
        InjectableDescriptor injectableDescriptor();

        /**
         * Attempts to resolve the {@link Resolvable}.
         *
         * @return the resolved {@link Object}
         * @throws InjectionException should injection fail
         */
        T resolve()
            throws InjectionException;
    }

    /**
     * An {@code abstract} {@link Resolvable}.
     *
     * @param <T> the type to be resolved
     */
    private abstract class AbstractResolvable<T>
        implements Resolvable<T> {

        /**
         * The {@link Resolvable} that requires this {@link Resolvable} to be resolved.
         */
        private final Optional<Resolvable<?>> requiredBy;

        /**
         * The {@link Dependency} to be resolved.
         */
        private final Dependency dependency;

        /**
         * The {@link InjectableDescriptor} defined for the {@link Dependency}.
         */
        private final InjectableDescriptor injectableDescriptor;

        /**
         * Constructs an {@link AbstractResolvable}.
         *
         * @param requiredBy           the {@link Optional} {@link Resolvable} that requires this {@link Resolvable}
         * @param dependency           the {@link Dependency}
         * @param injectableDescriptor the {@link InjectableDescriptor} for the {@link Dependency}
         */
        protected AbstractResolvable(final Optional<Resolvable<?>> requiredBy,
                                     final Dependency dependency,
                                     final InjectableDescriptor injectableDescriptor) {

            this.requiredBy = requiredBy == null ? Optional.empty() : requiredBy;
            this.dependency = Objects.requireNonNull(dependency, "The Dependency must not be null");
            this.injectableDescriptor = Objects
                .requireNonNull(injectableDescriptor, "The InjectableDescriptor must not be null");
        }

        @Override
        public Optional<Resolvable<?>> requiredBy() {
            return this.requiredBy;
        }

        @Override
        public Dependency dependency() {
            return this.dependency;
        }

        @Override
        public InjectableDescriptor injectableDescriptor() {
            return this.injectableDescriptor;
        }

        /**
         * Resolves the {@link Dependency}s using the {@link Context}.
         *
         * @param dependencies the {@link Dependency}
         * @return an {@link IdentityHashMap} containing the resolved {@link Dependency}s
         */
        protected IdentityHashMap<Dependency, Object> resolveDependencies(final Stream<Dependency> dependencies) {

            // Dependencies are requiredBy this Resolvable
            final var requiredBy = Optional.<Resolvable<?>>of(this);

            // resolve the dependencies
            final var resolvedDependencies = new IdentityHashMap<Dependency, Object>();

            dependencies.forEach(dependency -> resolvedDependencies
                .put(dependency,
                    getValue(requiredBy, dependency)
                        .orElseThrow(() -> new UnsatisfiedDependencyException(dependency, buildRequiredByChain()))));

            return resolvedDependencies;
        }

        /**
         * Builds a human-readable "required by" chain from this {@link Resolvable} up to the root, suitable for
         * inclusion in an {@link UnsatisfiedDependencyException} message.
         *
         * @return a multi-line string such as {@code "\n  required by Foo\n  required by Bar"}, or an empty string if
         * there are no requesters
         */
        private String buildRequiredByChain() {
            final var sb = new StringBuilder();
            var current = Optional.<Resolvable<?>>of(this);
            while (current.isPresent()) {
                sb.append("\n  required by ").append(current.get().dependency());
                current = current.get().requiredBy();
            }
            return sb.toString();
        }

        /**
         * Perform {@link FieldInjectionPoint} and {@link MethodInjectionPoint} injection into the specified
         * injectable {@link Object}.
         *
         * @param object               the {@link Object}
         * @param resolvedDependencies the resolved {@link Dependency}s
         */
        protected T inject(final T object,
                           final IdentityHashMap<Dependency, Object> resolvedDependencies) {

            this.injectableDescriptor.injectionPoints()
                .filter(injectionPoint -> !(injectionPoint instanceof ConstructorInjectionPoint))
                .forEach(injectionPoint -> {
                    // resolve the values for injection
                    final Object[] values = injectionPoint
                        .dependencies()
                        .map(resolvedDependencies::get)
                        .toArray();

                    injectionPoint.inject(object, values);
                });

            // invoke the @PostInject methods
            this.injectableDescriptor.postInjectionMethods()
                .map(methodDescriptor -> methodDescriptor.getTrait(MethodType.class).orElse(null))
                .filter(Objects::nonNull)
                .map(MethodType::method)
                .forEach(method -> {
                    try {
                        method.setAccessible(true);
                        method.invoke(object);
                    } catch (final IllegalAccessException | InvocationTargetException e) {
                        throw new InjectionException(
                            "Invoking @PostInject method " + method + " on " + object.getClass(), e);
                    }
                });

            return object;
        }
    }

    /**
     * Represents an {@link Object} upon which {@link Dependency}s need to be resolved through resolving
     * {@link FieldInjectionPoint}s and {@link MethodInjectionPoint}s.   As the {@link Object} already exists,
     * there's no need for resolving a {@link ConstructorInjectionPoint}.
     *
     * @param <T> the type of the {@link Object}
     */
    private class ResolvableObject<T>
        extends AbstractResolvable<T> {

        /**
         * The {@link Object} into which injection is to occur.
         */
        private final T object;

        /**
         * The {@link Dependency}s to be resolved for the {@link Object}
         */
        private final ArrayList<Dependency> dependenciesToBeResolved;

        /**
         * Constructs a {@link ResolvableObject}.
         *
         * @param dependency the {@link Dependency} for which the {@link Object} is being resolved
         * @param object     the {@link Object} to resolve
         */
        ResolvableObject(final Dependency dependency,
                         final T object) {

            super(
                Optional.empty(),
                dependency,
                InjectionContext.this.injectionFramework
                    .getInjectableDescriptor(
                        Objects.requireNonNull(object, "The Object must not be null")
                            .getClass()));

            this.object = object;
            this.dependenciesToBeResolved = new ArrayList<>();

            injectableDescriptor()
                .injectionPoints()
                .filter(injectionPoint -> !(injectionPoint instanceof ConstructorInjectionPoint))
                .flatMap(InjectionPoint::dependencies)
                .forEach(this.dependenciesToBeResolved::add);
        }

        @Override
        public T resolve()
            throws InjectionException {

            // return immediately when the object is not injectable
            if (!injectableDescriptor().isInjectable()) {
                return this.object;
            }

            // resolve the dependencies
            final var resolvedDependencies = resolveDependencies(this.dependenciesToBeResolved.stream());

            // perform injection using the resolved dependencies
            return inject(this.object, resolvedDependencies);
        }
    }

    /**
     * Represents a concrete {@link Class} to be instantiated either using the available
     * {@link ConstructorInjectionPoint} (thus constructor injection) or the {@code default} constructor, after which
     * remaining {@link Dependency}s will be injected through resolving {@link FieldInjectionPoint}s and
     * {@link MethodInjectionPoint}s.
     *
     * @param <T> the type of the {@link Object}
     */
    private class ResolvableClass<T>
        extends AbstractResolvable<T> {

        /**
         * The concrete {@link Class} to be instantiated and injected.
         */
        private final Class<? extends T> concreteClass;

        /**
         * The {@link Dependency}s to be resolved for the {@link Object}
         */
        private final ArrayList<Dependency> dependenciesToBeResolved;

        /**
         * The {@link Optional} {@link ConstructorInjectionPoint} to use for constructing the concrete {@link Class}.
         */
        private final Optional<ConstructorInjectionPoint> constructorInjectionPoint;

        /**
         * Constructs a {@link ResolvableClass}.
         *
         * @param dependency    the {@link Dependency} for which the {@link Object} is being resolved
         * @param requiredBy    the {@link Optional} {@link Resolvable} that requires this {@link Resolvable}
         * @param concreteClass the concrete {@link Class} to instantiate
         */
        ResolvableClass(final Optional<Resolvable<?>> requiredBy,
                        final Dependency dependency,
                        final Class<? extends T> concreteClass) {

            super(requiredBy,
                dependency,
                InjectionContext.this.injectionFramework
                    .getInjectableDescriptor(
                        Objects.requireNonNull(concreteClass, "The concrete Class must not be null")));

            // confirm none of the requireBy Resolvables are for the same Dependency (if we do, we have a cycle)
            requiredBy.ifPresent(resolvable -> {
                var current = resolvable;
                while (current != null) {
                    if (current.dependency().equals(dependency)) {
                        throw new CyclicDependencyException(
                            current.dependency(),
                            resolvable.dependency());
                    }
                    current = current.requiredBy().orElse(null);
                }
            });

            this.concreteClass = Objects.requireNonNull(concreteClass, "The concrete Class must not be null");
            this.dependenciesToBeResolved = new ArrayList<>();

            // determine the InjectionPoints that need resolving and their associated Dependencies
            final var constructorInjectionPoint = Lazy.<ConstructorInjectionPoint>empty();

            injectableDescriptor()
                .injectionPoints()
                .peek(injectionPoint -> injectionPoint
                    .dependencies()
                    .forEach(this.dependenciesToBeResolved::add))
                .forEach(injectionPoint -> {
                    // capture the ConstructorInjectionPoint to later use for construction
                    if (injectionPoint instanceof ConstructorInjectionPoint constructor) {
                        constructorInjectionPoint.set(constructor);
                    }
                });

            // retain the captured ConstructorInjectionPoint
            this.constructorInjectionPoint = constructorInjectionPoint.optional();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T resolve()
            throws InjectionException {

            // resolve the dependencies
            final var resolvedDependencies = resolveDependencies(this.dependenciesToBeResolved.stream());

            // construct the object
            final var object = this.constructorInjectionPoint
                .map(constructorInjectionPoint -> {
                    // attempt to instantiate the object using the resolved dependencies

                    // resolve values for injection
                    final Object[] values = constructorInjectionPoint.dependencies()
                        .map(resolvedDependencies::get)
                        .toArray();

                    // create the instance
                    return (T) constructorInjectionPoint.inject(null, values);
                })
                .orElseGet(() -> {
                    // locate and use the default constructor
                    for (final Constructor<?> constructor : this.concreteClass.getDeclaredConstructors()) {
                        if (constructor.getParameterCount() == 0) {
                            try {
                                // ensure the constructor is accessible
                                // (it may not be if it's an internal / private class we're creating)
                                constructor.setAccessible(true);

                                // create the instance
                                return (T) constructor.newInstance();
                            } catch (final InvocationTargetException
                                           | InstantiationException
                                           | IllegalAccessException e) {
                                throw new UnsatisfiedDependencyException(
                                    dependency(),
                                    "Failed to instantiate with default no-args constructor", e);
                            }
                        }
                    }

                    throw new UnsatisfiedDependencyException(dependency(), "Failed to locate no-args constructor");
                });

            // perform injection using the resolved dependencies
            return inject(object, resolvedDependencies);
        }
    }

    /**
     * Accumulates suppliers for a multibinding of element type {@code T}.
     *
     * @param <T> the element type
     */
    private static class MultiBindingEntry<T> {

        private final CopyOnWriteArrayList<Supplier<T>> suppliers = new CopyOnWriteArrayList<>();

        @SuppressWarnings("unchecked")
        void addValue(final T value) {
            this.suppliers.add(() -> value);
        }

        void addSupplier(final Supplier<T> supplier) {
            this.suppliers.add(supplier);
        }

        Set<T> buildSet() {
            return this.suppliers.stream()
                .map(Supplier::get)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        }
    }
}
