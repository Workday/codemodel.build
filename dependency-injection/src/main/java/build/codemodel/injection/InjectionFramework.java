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

import build.base.foundation.stream.Streams;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.descriptor.FieldType;
import build.codemodel.jdk.descriptor.JDKType;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link JDKCodeModel}-based <a href="https://en.wikipedia.org/wiki/Adapter_pattern">Adapter</a> allowing the
 * runtime discovery of {@link InjectionPoint}s and <a href="https://jcp.org/en/jsr/detail?id=330">JSR-330</a>-compliant
 * <a href="https://en.wikipedia.org/wiki/Dependency_injection">Dependency Injection</a> of {@link Class}es
 * and {@link Object}s
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public class InjectionFramework {

    /**
     * The {@link JDKCodeModel} to use to discover {@link InjectionPoint}s.
     */
    private final JDKCodeModel codeModel;

    /**
     * Constructs an {@link InjectionFramework}.
     *
     * @param codeModel the {@link JDKCodeModel}
     */
    public InjectionFramework(final JDKCodeModel codeModel) {
        this.codeModel = Objects
            .requireNonNull(codeModel, "The CodeModel must not be null");
    }

    /**
     * Creates an {@link InjectionFramework} with a default non-caching {@link JDKCodeModel}.
     *
     * @return a new {@link InjectionFramework}
     */
    public static InjectionFramework create() {
        return new InjectionFramework(new JDKCodeModel(new NonCachingNameProvider()));
    }

    /**
     * Obtains the {@link JDKCodeModel} used by {@link InjectionFramework}.
     *
     * @return the {@link JDKCodeModel}
     */
    public JDKCodeModel codeModel() {
        return this.codeModel;
    }

    /**
     * Determines if the specified {@link JDKTypeDescriptor}, usually for an {@link Annotation}, is
     * annotated with {@link Qualifier}.
     *
     * @param annotationTypeDescriptor the {@link JDKTypeDescriptor}
     * @return {@code true} if the {@link JDKTypeDescriptor} has a {@link Qualifier} annotation, {@code false} otherwise
     */
    public boolean hasQualifierAnnotation(final JDKTypeDescriptor annotationTypeDescriptor) {
        return annotationTypeDescriptor != null
            && annotationTypeDescriptor.traits(AnnotationTypeUsage.class)
            .anyMatch(metaAnnotationTypeUsage -> metaAnnotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(Qualifier.class.getCanonicalName()));
    }

    /**
     * Determines the {@link AnnotationTypeUsage}s of the specified {@link Traitable} that have the
     * {@link Qualifier} meta-annotation.
     *
     * @param traitable {@link Traitable}
     * @return the {@link Stream} of {@link Qualifier} {@link AnnotationTypeUsage}s
     */
    public Stream<AnnotationTypeUsage> getQualifierAnnotationTypes(final Traitable traitable) {
        if (traitable == null) {
            return Stream.empty();
        }

        return traitable.traits(AnnotationTypeUsage.class)
            .map(annotationTypeUsage -> this.codeModel.getJDKTypeDescriptor(annotationTypeUsage.typeName())
                .filter(this::hasQualifierAnnotation)
                .map(_ -> annotationTypeUsage)
                .orElse(null))
            .filter(Objects::nonNull);
    }

    /**
     * Creates an {@link InjectableDescriptor} for the specified injectable {@link Class} and
     * {@link JDKTypeDescriptor}, without modifying the {@link JDKTypeDescriptor} or adding the
     * {@link InjectableDescriptor} to it.
     *
     * @param injectableClass the injectable {@link Class}
     * @param typeDescriptor  the {@link JDKTypeDescriptor}
     * @return the {@link InjectableDescriptor}
     */
    private InjectableDescriptor createInjectableDescriptor(final Class<?> injectableClass,
                                                            final JDKTypeDescriptor typeDescriptor) {

        // the InjectionPoints in order of their discovery
        final var injectionPoints = new LinkedHashMap<String, InjectionPoint>();

        // the @PostInject MethodDescriptors in order of their discovery
        final var postInjectMethodDescriptors = new LinkedHashSet<MethodDescriptor>();

        // create a deque of the hierarchy of Classes to process
        // (with the current class at the front of the deque)
        final var deque = new ArrayDeque<Class<?>>();

        var nextInQueue = injectableClass;
        while (nextInQueue != null && !nextInQueue.equals(Object.class)) {
            deque.push(nextInQueue);
            nextInQueue = nextInQueue.getSuperclass();
        }

        // detect the InjectionPoints in the Classes (in reverse order ie: "super classes down")
        // so that super-class Injection Points are discovered and ordered first
        while (!deque.isEmpty()) {
            // obtain the TypeDescriptor for the injectable class from the ReflectionFramework
            // (we don't use reflection)
            final var currentClass = deque.pop();

            final var currentTypeDescriptor = this.codeModel
                .getJDKTypeDescriptor(currentClass)
                .orElseThrow(
                    () -> new InjectionException("Failed to discover the InjectionPoints for " + injectableClass));

            // include the declared non-static, non-final fields annotated with @Inject
            // (that's not already a FieldInjectionPoint)
            currentTypeDescriptor.declaredFields()
                .filter(fieldDescriptor ->
                    isInjectionPoint(fieldDescriptor.type())
                        && fieldDescriptor.getTrait(Classification.class)
                        .map(classification -> classification == Classification.CONCRETE)
                        .orElse(false))
                .forEach(fieldDescriptor -> {
                    final var fieldType = fieldDescriptor.trait(FieldType.class);
                    final var name = fieldDescriptor.fieldName() + " " + fieldType.field().toGenericString();
                    final var fieldInjectionPoint = FieldInjectionPoint.of(
                        currentTypeDescriptor,
                        fieldDescriptor,
                        this::getQualifierAnnotationTypes);

                    injectionPoints.put(name, fieldInjectionPoint);
                });

            // include the declared non-abstract non-static Methods annotated with @Inject
            // (that's not already a MethodInjectionPoint)
            // (removing those that are overridden)
            currentTypeDescriptor.declaredMethods()
                .forEach(methodDescriptor -> {
                    final var signature = methodDescriptor.signature();

                    // remove any previous MethodInjectionPoint with the same signature
                    // (as the current class is overriding it)
                    injectionPoints.remove(signature);

                    if (isInjectionPoint(methodDescriptor)) {
                        final var methodInjectionPoint = MethodInjectionPoint
                            .of(currentTypeDescriptor, methodDescriptor, this::getQualifierAnnotationTypes);

                        injectionPoints.put(signature, methodInjectionPoint);
                    }

                    // include the MethodDescriptor as a @PostInject
                    if (isPostInject(methodDescriptor)) {
                        postInjectMethodDescriptors.add(methodDescriptor);
                    }
                });
        }

        // include the ConstructorInjectionPoint (there should be only one)
        // determine the constructor annotated with @Inject
        // (that's not already a ConstructorInjectionPoint)
        typeDescriptor.declaredConstructors()
            .filter(this::isInjectionPoint)
            .findFirst()
            .ifPresent(constructorDescriptor -> {
                final var constructorInjectionPoint = ConstructorInjectionPoint
                    .of(typeDescriptor, constructorDescriptor, this::getQualifierAnnotationTypes);

                injectionPoints.put("constructor", constructorInjectionPoint);
            });

        return InjectableDescriptor.of(
            typeDescriptor,
            injectionPoints.values().stream(),
            postInjectMethodDescriptors.stream());
    }

    /**
     * Obtains the {@link InjectableDescriptor} for the specified {@link Class}, using the {@link JDKCodeModel} if
     * necessary to establish one.
     *
     * @param injectableClass the {@link Class}
     * @return the {@link InjectableDescriptor} for the {@link Class}
     */
    public InjectableDescriptor getInjectableDescriptor(final Class<?> injectableClass) {

        // determine the TypeDescriptor for the potentially injectable class
        final JDKTypeDescriptor typeDescriptor = this.codeModel
            .getJDKTypeDescriptor(injectableClass)
            .orElseThrow(
                () -> new InjectionException("Failed to discover the InjectionPoints for " + injectableClass));

        return typeDescriptor.computeIfAbsent(InjectableDescriptor.class, _ ->
                createInjectableDescriptor(injectableClass, typeDescriptor))
            .orElseThrow();
    }

    /**
     * Obtains the {@link InjectableDescriptor} for the specified {@link JDKTypeDescriptor}, using the
     * {@link JDKTypeDescriptor} if necessary to establish one.
     *
     * @param typeDescriptor the {@link JDKTypeDescriptor}
     * @return the {@link InjectableDescriptor} for the {@link JDKTypeDescriptor}
     */
    public InjectableDescriptor getInjectableDescriptor(final JDKTypeDescriptor typeDescriptor) {

        Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null");

        final var injectableClass = typeDescriptor.getTrait(JDKType.class)
            .map(JDKType::type)
            .filter(Class.class::isInstance)
            .map(Class.class::cast)
            .orElseThrow(() -> new IllegalArgumentException("Can't determine Class for the TypeDescriptor"));

        return typeDescriptor.computeIfAbsent(InjectableDescriptor.class, _ ->
                createInjectableDescriptor(injectableClass, typeDescriptor))
            .orElseThrow();
    }

    /**
     * Determines if a {@link Traitable} represents an {@link InjectionPoint}, which means it is
     * non-{@code static}, non-{@code abstract} with an {@link AnnotationTypeUsage} for {@link Inject}.
     *
     * @param traitable the {@link Traitable}
     * @return {@code true} if the {@link Traitable} contains an {@link Inject} use, {@code false} otherwise
     */
    public boolean isInjectionPoint(final Traitable traitable) {
        return traitable.getTrait(Static.class).isEmpty()
            && traitable.getTrait(Classification.class)
            .map(classification -> classification != Classification.ABSTRACT)
            .orElse(true)
            && traitable.traits(AnnotationTypeUsage.class)
            .anyMatch(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(Inject.class.getCanonicalName()));
    }

    /**
     * Determines if the specified {@link JDKTypeDescriptor} is annotated as a {@link Singleton}.
     *
     * @param typeDescriptor the {@link JDKTypeDescriptor}
     * @return {@code true} if annotated as a {@link Singleton}, otherwise {@code false}
     */
    public boolean isSingleton(final JDKTypeDescriptor typeDescriptor) {
        return typeDescriptor != null
            && typeDescriptor.traits(AnnotationTypeUsage.class)
            .anyMatch(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(Singleton.class.getCanonicalName()));
    }

    /**
     * Determines if a {@link MethodDescriptor} represents an {@link PostInject} method, which means it is
     * non-{@code static}, non-{@code abstract} with an {@link AnnotationTypeUsage} for {@link PostInject}.
     *
     * @param descriptor the {@link MethodDescriptor}
     * @return {@code true} if the {@link Traitable} contains an {@link Inject} use, {@code false} otherwise
     */
    public boolean isPostInject(final MethodDescriptor descriptor) {
        return descriptor.getTrait(Static.class).isEmpty()
            && descriptor.getTrait(Classification.class)
            .map(classification -> classification != Classification.ABSTRACT)
            .orElse(true)
            && descriptor.traits(AnnotationTypeUsage.class)
            .anyMatch(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(PostInject.class.getCanonicalName()));
    }

    /**
     * Determines if a {@link MethodDescriptor} represents a {@link Provides} method, which means it is
     * non-{@code static}, non-{@code abstract}, non-{@code void} with an {@link AnnotationTypeUsage} for
     * {@link Provides}.
     *
     * <p>This is a descriptor-level predicate intended for use when scanning a {@link JDKTypeDescriptor}
     * to identify provider methods before constructing a {@link ProvidesResolver}.
     *
     * @param descriptor the {@link MethodDescriptor}
     * @return {@code true} if the {@link MethodDescriptor} has a {@link Provides} annotation, {@code false} otherwise
     */
    public boolean isProvides(final MethodDescriptor descriptor) {
        return descriptor.getTrait(Static.class).isEmpty()
            && descriptor.getTrait(Classification.class)
            .map(classification -> classification != Classification.ABSTRACT)
            .orElse(true)
            && descriptor.traits(AnnotationTypeUsage.class)
            .anyMatch(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(Provides.class.getCanonicalName()));
    }

    /**
     * Creates a new empty {@link Context}.
     *
     * @return a new {@link Context}
     */
    public Context newContext() {
        return new InjectionContext(this);
    }

    /**
     * Creates a new {@link Context} initialized with the specified {@link Resolver}s.
     *
     * @param resolvers the {@link Resolver}s
     * @return a new {@link Context}
     */
    public Context newContext(final Resolver<?>... resolvers) {
        final var context = new InjectionContext(this);

        Streams.of(resolvers)
            .filter(Objects::nonNull)
            .forEach(context::addResolver);

        return context;
    }

    /**
     * Creates a new {@link Context} with the specified {@link Module}s pre-installed.
     *
     * @param modules the {@link Module}s to install
     * @return a new {@link Context}
     */
    public Context newContext(final Module... modules) {
        final var context = new InjectionContext(this);

        Streams.of(modules)
            .filter(Objects::nonNull)
            .forEach(module -> module.configure(context));

        return context;
    }
}
