package build.codemodel.jdk;

/*-
 * #%L
 * JDK Code Model
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
import build.base.foundation.stream.Streams;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.ModuleDescriptor;
import build.codemodel.foundation.descriptor.NamespaceDescriptor;
import build.codemodel.foundation.descriptor.ThrowableDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnionTypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.jdk.descriptor.ConstructorType;
import build.codemodel.jdk.descriptor.FieldType;
import build.codemodel.jdk.descriptor.JDKType;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.jdk.descriptor.MethodType;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.objectoriented.ObjectOrientedCodeModel;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.ImplementsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.naming.MethodName;
import jakarta.inject.Inject;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A <i>JDK-based</i> {@link ObjectOrientedCodeModel} allowing the runtime discovery, access and representation of
 * {@link TypeDescriptor}s based using <a href="https://en.wikipedia.org/wiki/Reflective_programming">Reflection</a>.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public class JDKCodeModel
    extends ObjectOrientedCodeModel {

    /**
     * Constructs an empty {@link JDKCodeModel}.
     *
     * @param nameProvider the {@link NameProvider}
     */
    @Inject
    public JDKCodeModel(final NameProvider nameProvider) {
        super(nameProvider);

        // establish the foundation types for the CodeModel
        initialize();
    }

    /**
     * Constructs an {@link JDKCodeModel} using a {@link Marshaller}.
     *
     * @param nameProvider         the {@link NameProvider}
     * @param marshaller           the {@link Marshaller}
     * @param traits               the {@link Traitable}
     * @param typeDescriptors      the {@link Stream} of {@link Marshalled} {@link TypeDescriptor}s
     * @param moduleDescriptors    the {@link Stream} of {@link Marshalled} {@link ModuleDescriptor}s
     * @param namespaceDescriptors the {@link Stream} of {@link Marshalled} {@link NamespaceDescriptor}s
     */
    @Unmarshal
    public JDKCodeModel(@Bound final NameProvider nameProvider,
                         @Bound final Marshaller marshaller,
                         final Stream<Marshalled<Trait>> traits,
                         final Stream<Marshalled<TypeDescriptor>> typeDescriptors,
                         final Stream<Marshalled<ModuleDescriptor>> moduleDescriptors,
                         final Stream<Marshalled<NamespaceDescriptor>> namespaceDescriptors) {

        super(nameProvider, marshaller, traits, typeDescriptors, moduleDescriptors, namespaceDescriptors);
    }

    /**
     * Destructs an {@link ObjectOrientedCodeModel} so it can be {@link Marshal}led.
     *
     * @param marshaller           the {@link Marshaller} to use for marshalling
     * @param traits               the {@link Marshalled} {@link Trait}s of the {@link CodeModel} itself
     * @param typeDescriptors      the {@link Stream} of marshallable {@link TypeDescriptor}s
     * @param moduleDescriptors    the {@link Stream} of marshallable {@link ModuleDescriptor}s
     * @param namespaceDescriptors the {@link Stream} of marshallable {@link NamespaceDescriptor}s
     */
    @Marshal
    public void destructor(@Bound final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<TypeDescriptor>>> typeDescriptors,
                           final Out<Stream<Marshalled<ModuleDescriptor>>> moduleDescriptors,
                           final Out<Stream<Marshalled<NamespaceDescriptor>>> namespaceDescriptors) {

        super.destructor(marshaller, traits, typeDescriptors, moduleDescriptors, namespaceDescriptors);
    }

    /**
     * Obtains the {@link TypeUsage} for the specified {@link AnnotatedType} using reflection.
     *
     * @param annotatedType the {@link AnnotatedType}
     * @return the {@link TypeUsage}
     */
    public TypeUsage getTypeUsage(final AnnotatedType annotatedType) {
        Objects.requireNonNull(annotatedType, "The AnnotatedType must not be null");

        final var typeUsage = getTypeUsage(annotatedType.getType());

        // include the annotations on the TypeUsage
        getAnnotations(annotatedType)
            .forEach(typeUsage::addTrait);

        return typeUsage;
    }

    /**
     * Obtains the {@link TypeUsage} for the specified {@link Parameter} using reflection.
     *
     * @param parameter the {@link Parameter}
     * @return the {@link TypeUsage}
     */
    public TypeUsage getTypeUsage(final Parameter parameter) {
        Objects.requireNonNull(parameter, "The Parameter must not be null");

        final var typeUsage = getTypeUsage(parameter.getParameterizedType());

        // include the annotations on the TypeUsage
        getAnnotations(parameter)
            .forEach(typeUsage::addTrait);

        return typeUsage;
    }

    /**
     * Obtains the {@link TypeUsage} for the specified {@link Type} with the
     * provided {@link Annotation}s using reflection.
     *
     * @param type        the {@link Type}
     * @param annotations the {@link Annotation}s
     * @return the {@link TypeUsage}
     */
    public TypeUsage getTypeUsage(final Type type,
                                  final Annotation... annotations) {

        final TypeUsage typeUsage;

        final var nameProvider = getNameProvider();

        if (type instanceof Class<?> classType) {
            final var typeName = nameProvider.getTypeName(classType);
            typeUsage = SpecificTypeUsage.of(this, typeName);
        }
        else if (type instanceof ParameterizedType parameterizedType) {
            final var rawTypeUsage = getNamedTypeUsage(parameterizedType.getRawType())
                .orElseThrow(() -> new IllegalStateException("RawType of ParameterizedType is not named:" + type));
            final var parameters = Arrays.stream(parameterizedType.getActualTypeArguments())
                .map(this::getTypeUsage)
                .toArray(TypeUsage[]::new);

            typeUsage = GenericTypeUsage.of(this, rawTypeUsage.typeName(), parameters);
        }
        else if (type instanceof TypeVariable<?> typeVariable) {
            final var variableName = nameProvider.getIrreducibleName(typeVariable.getName());
            final var typeName = nameProvider.getTypeName(Optional.empty(), variableName);

            // guard against self-referential bounds like T extends Comparable<T> or E extends Enum<E>
            final var inProgress = IN_PROGRESS_TYPE_VARIABLES.get();
            Optional<Lazy<TypeUsage>> upperBound = Optional.empty();

            if (inProgress.add(typeVariable)) {
                try {
                    // determine upper bounds from TypeVariable
                    final List<Lazy<TypeUsage>> upperBoundTypeUsages = Streams.of(typeVariable.getAnnotatedBounds())
                        .map(annotatedType -> Lazy.of(getTypeUsage(annotatedType)))
                        .toList();

                    upperBound = upperBoundTypeUsages.isEmpty()
                        ? Optional.empty()
                        : (upperBoundTypeUsages.size() == 1)
                            ? Optional.of(upperBoundTypeUsages.getFirst())
                            : Optional.of(Lazy.of(UnionTypeUsage.of(this, upperBoundTypeUsages.stream())));
                }
                finally {
                    inProgress.remove(typeVariable);
                }
            }

            typeUsage = TypeVariableUsage.of(this, typeName, Optional.empty(), upperBound);
        }
        else if (type instanceof WildcardType wildcardType) {
            final java.lang.reflect.Type[] lowers = wildcardType.getLowerBounds();
            final java.lang.reflect.Type[] uppers = wildcardType.getUpperBounds();

            final Optional<Lazy<TypeUsage>> optLower = lowers.length > 0
                ? Optional.of(Lazy.of(getTypeUsage(lowers[0])))
                : Optional.empty();

            // getUpperBounds() returns [Object] for unbounded `?` — treat that as no explicit upper bound
            final Optional<Lazy<TypeUsage>> optUpper = uppers.length > 0 && !uppers[0].equals(Object.class)
                ? Optional.of(Lazy.of(getTypeUsage(uppers[0])))
                : Optional.empty();

            typeUsage = WildcardTypeUsage.of(this, optLower, optUpper);
        }
        else if (type instanceof GenericArrayType genericArrayType) {
            typeUsage = ArrayTypeUsage.of(
                this,
                Lazy.of(getTypeUsage(genericArrayType.getGenericComponentType())));
        }
        else {
            typeUsage = UnknownTypeUsage.create(this);
        }

        // include the provided Annotations
        if (annotations != null && annotations.length > 0) {
            Arrays.stream(annotations)
                .map(this::getAnnotation)
                .forEach(typeUsage::addTrait);
        }

        return typeUsage;
    }

    /**
     * Attempts to obtain the {@link NamedTypeUsage} for the specified {@link AnnotatedType} using reflection.
     *
     * @param type the {@link AnnotatedType}
     * @return the {@link Optional} {@link NamedTypeUsage} or {@link Optional#empty()} if the {@link AnnotatedType} is not named
     */
    public Optional<NamedTypeUsage> getNamedTypeUsage(final AnnotatedType type) {

        return getTypeUsage(type) instanceof NamedTypeUsage namedTypeUsage
            ? Optional.of(namedTypeUsage)
            : Optional.empty();
    }

    /**
     * Attempts to obtain the {@link NamedTypeUsage} for the specified {@link build.base.marshalling.Parameter} using reflection.
     *
     * @param parameter the {@link Parameter}
     * @return the {@link Optional} {@link NamedTypeUsage} or {@link Optional#empty()} if the {@link Parameter} is not named
     */
    public Optional<NamedTypeUsage> getNamedTypeUsage(final Parameter parameter) {

        return getTypeUsage(parameter) instanceof NamedTypeUsage namedTypeUsage
            ? Optional.of(namedTypeUsage)
            : Optional.empty();
    }

    /**
     * Attempts to obtain the {@link NamedTypeUsage} for the specified {@link Type} using reflection.
     *
     * @param type the {@link Type}
     * @return the {@link Optional} {@link NamedTypeUsage} or {@link Optional#empty()} if the {@link Type} is not named
     */
    public Optional<NamedTypeUsage> getNamedTypeUsage(final Type type) {

        return getTypeUsage(type) instanceof NamedTypeUsage namedTypeUsage
            ? Optional.of(namedTypeUsage)
            : Optional.empty();
    }

    /**
     * Attempts to obtain the {@link JDKTypeDescriptor} for the specified {@link Type} using reflection.
     * <p>
     * Some {@link Type}s can't, don't or won't produce {@link TypeDescriptor}s. For example; arrays, intersections,
     * wildcards, type variables.  These are examples of uses or declarations of types, and thus don't have
     * {@link TypeDescriptor}s.  The type information for these {@link Type}s are adequately described by
     * {@link TypeUsage}s.
     *
     * @param type the {@link Type}
     * @return the {@link Optional} {@link TypeDescriptor}
     */
    public Optional<JDKTypeDescriptor> getJDKTypeDescriptor(final Type type) {

        if (type instanceof Class<?> classType) {
            // resolve the Class, including any related (super and interface) classes
            final var queue = new ArrayList<Class<?>>(10);
            queue.add(classType);

            final var resolved = new LinkedHashMap<Class<?>, JDKTypeDescriptor>();

            while (!queue.isEmpty()) {
                final Class<?> current = queue.removeFirst();

                getJDKTypeDescriptor(current, clazz -> {
                    if (!resolved.containsKey(clazz)) {
                        queue.add(clazz);
                    }
                }).ifPresent(typeDescriptor -> resolved.put(current, typeDescriptor));
            }

            return Optional.ofNullable(resolved.get(classType));
        }
        else if (type instanceof ParameterizedType parameterizedType) {
            // for ParameterizedTypes we return the TypeDescriptor of the Raw Type
            return getJDKTypeDescriptor(parameterizedType.getRawType());
        }

        // unsupported type of types (like Arrays etc) don't get TypeDescriptors
        return Optional.empty();
    }

    /**
     * Attempts to resolve a {@link TypeDescriptor} for the specified {@link Class} and that {@link Class} only.  Any
     * super or interface {@link Class}es will be provided to the {@link Consumer} to track potential other resolutions,
     * but they won't occur here.
     *
     * @param classType the {@link Class} to resolve
     * @param consumer  the {@link Consumer} of other potential {@link Class}es to resolve
     * @return the {@link Optional}ly resolved {@link TypeDescriptor}, or {@link Optional#empty()} if it could not be
     * resolved
     */
    private Optional<JDKTypeDescriptor> getJDKTypeDescriptor(final Class<?> classType,
                                                             final Consumer<? super Class<?>> consumer) {

        final var nameProvider = getNameProvider();

        final var typeName = nameProvider.getTypeName(classType);
        final var existingTypeDescriptor = getTypeDescriptor(typeName);

        if (existingTypeDescriptor.isPresent()) {
            return existingTypeDescriptor
                .filter(JDKTypeDescriptor.class::isInstance)
                .map(JDKTypeDescriptor.class::cast);
        }

        // create the TypeDescriptor for the Class
        final var typeDescriptor = createTypeDescriptor(typeName, JDKTypeDescriptor.supplier(classType));

        // include the JDKType in the TypeDescriptor
        typeDescriptor.addTrait(new JDKType(classType));

        final var classModifier = classType.getModifiers();

        // include the Static trait (if necessary)
        if (Modifier.isStatic(classModifier)) {
            typeDescriptor.addTrait(Static.STATIC);
        }

        // include the AccessModifier
        getAccessModifier(classModifier)
            .ifPresent(typeDescriptor::addTrait);

        // include the Classification
        typeDescriptor.addTrait(getClassification(classModifier));

        // TODO: include the generic parameter declarations on the type itself!

        // include the ExtendsTypeDescriptor (should a super-class be defined)
        final var superType = classType.getAnnotatedSuperclass();
        if (superType != null) {
            final var extendsTypeDescriptor = ExtendsTypeDescriptor.of(getNamedTypeUsage(superType)
                .orElseThrow(() -> new IllegalStateException(
                    "Super class " + superType + " of " + typeName + " is not named!")));

            typeDescriptor.addTrait(extendsTypeDescriptor);

            if (superType.getType() instanceof Class<?> superClass) {
                consumer.accept(superClass);
            }
        }

        // include the ImplementsTypeDescriptors for implemented Interfaces
        Streams.of(classType.getAnnotatedInterfaces())
            .forEach(interfaceType -> {
                final var interfaceTypeUsage = getNamedTypeUsage(interfaceType)
                    .orElseThrow(() -> new IllegalStateException(
                        "The interface type " + interfaceType + " is not named!"));

                final var implementsTypeDescriptor = ImplementsTypeDescriptor.of(interfaceTypeUsage);
                typeDescriptor.addTrait(implementsTypeDescriptor);

                if (interfaceType.getType() instanceof Class<?> interfaceClass) {
                    consumer.accept(interfaceClass);
                }
            });

        // include ConstructorDescriptor for the declared Constructors
        Streams.of(classType.getDeclaredConstructors())
            .forEach(constructor -> {

                final var formalParameters = getFormalParameters(constructor.getParameters());
                final var constructorDescriptor = ConstructorDescriptor.of(typeDescriptor, formalParameters);

                // include the annotations on the ConstructorDescriptor
                getAnnotations(constructor)
                    .forEach(constructorDescriptor::addTrait);

                // include the AccessModifier
                getAccessModifier(constructor.getModifiers())
                    .ifPresent(constructorDescriptor::addTrait);

                // include the ThrowableDescriptors
                Streams.of(constructor.getAnnotatedExceptionTypes())
                    .forEach(exceptionType -> {
                        final var exceptionTypeUsage = getNamedTypeUsage(exceptionType)
                            .orElseThrow(() -> new IllegalStateException(
                                "The exception type " + exceptionType + " is not named!"));
                        final var throwableDescriptor = ThrowableDescriptor.of(exceptionTypeUsage);
                        typeDescriptor.addTrait(throwableDescriptor);
                    });

                // include the ConstructorType
                constructorDescriptor.addTrait(new ConstructorType(constructor));

                typeDescriptor.addTrait(constructorDescriptor);
            });

        // include MethodDescriptors for the declared Methods
        Streams.of(classType.getDeclaredMethods())
            .forEach(method -> {
                final var methodName = MethodName.of(
                    typeName.moduleName(),
                    typeName.namespace(),
                    Optional.of(typeName),
                    nameProvider.getIrreducibleName(method.getName()));

                final var returnType = getTypeUsage(method.getAnnotatedReturnType());
                final var formalParameters = getFormalParameters(method.getParameters());

                final var methodDescriptor = MethodDescriptor
                    .of(typeDescriptor, methodName, returnType, formalParameters);

                final var methodModifiers = method.getModifiers();

                // include the Static trait (if necessary)
                if (Modifier.isStatic(methodModifiers)) {
                    methodDescriptor.addTrait(Static.STATIC);
                }

                // include the AccessModifier
                getAccessModifier(methodModifiers)
                    .ifPresent(methodDescriptor::addTrait);

                // include the Classification
                methodDescriptor.addTrait(getClassification(methodModifiers));

                // include the annotations on the MethodDescriptor
                getAnnotations(method)
                    .forEach(methodDescriptor::addTrait);

                // include the ThrowableDescriptors
                Streams.of(method.getAnnotatedExceptionTypes())
                    .forEach(exceptionType -> {
                        final var exceptionTypeUsage = getNamedTypeUsage(exceptionType)
                            .orElseThrow(() -> new IllegalStateException(
                                "The exception type " + exceptionType + " is not named!"));
                        final var throwableDescriptor = ThrowableDescriptor.of(exceptionTypeUsage);
                        typeDescriptor.addTrait(throwableDescriptor);
                    });

                // include the MethodType
                methodDescriptor.addTrait(new MethodType(method));

                typeDescriptor.addTrait(methodDescriptor);
            });

        // include FieldDescriptors for the declared Fields
        Streams.of(classType.getDeclaredFields())
            .forEach(field -> {
                final var fieldName = nameProvider.getIrreducibleName(field.getName());

                // NOTE: Field.getAnnotatedType() does not define the field annotations, thus
                // we must use Field.getGenericType() to obtain the TypeUsage for the Field and
                // not Field.getAnnotatedType()
                final var fieldType = getTypeUsage(field.getGenericType());

                // include the annotations on the Field TypeUsage
                getAnnotations(field)
                    .forEach(fieldType::addTrait);

                final var fieldDescriptor = FieldDescriptor.of(this, fieldName, fieldType);
                final var fieldModifiers = field.getModifiers();

                // include the Static trait (if necessary)
                if (Modifier.isStatic(fieldModifiers)) {
                    fieldDescriptor.addTrait(Static.STATIC);
                }

                // include the AccessModifier
                getAccessModifier(fieldModifiers)
                    .ifPresent(fieldDescriptor::addTrait);

                // include the Classification
                fieldDescriptor.addTrait(getClassification(fieldModifiers));

                // include the FieldType
                fieldDescriptor.addTrait(new FieldType(field));

                typeDescriptor.addTrait(fieldDescriptor);
            });

        // include the annotations on the TypeDescriptor (from the Class Type)
        getAnnotations(classType)
            .forEach(typeDescriptor::addTrait);

        return Optional.of(typeDescriptor);

    }

    /**
     * Obtains a {@link Stream} of {@link Trait}s of the specified {@link Class} from the provided
     * {@link JDKTypeDescriptor}, including those that are transitively defined in the
     * <a href="https://en.wikipedia.org/wiki/Class_hierarchy">Class Hierarchy</a> through the use of
     * {@link ExtendsTypeDescriptor}s  {@link ImplementsTypeDescriptor}s, returned in-order of
     * discovery from the {@link JDKTypeDescriptor} and <i>up</i>.
     *
     * @param <T>                the type of {@link Trait}
     * @param javaTypeDescriptor the {@link JDKTypeDescriptor}
     * @param traitClass         the {@link Class} of {@link Trait}
     * @return a {@link Stream} of {@link Trait}s
     */
    public <T extends Trait> Stream<T> getTraitsInHierarchy(final JDKTypeDescriptor javaTypeDescriptor,
                                                            final Class<T> traitClass) {

        if (javaTypeDescriptor == null || traitClass == null) {
            return Stream.empty();
        }

        // assume no results (but we want to keep them ordered)
        final LinkedHashSet<T> traits = new LinkedHashSet<>();

        // we use a queue to avoid recursion
        final Queue<JDKTypeDescriptor> queue = new LinkedList<>();
        queue.offer(javaTypeDescriptor);

        // we don't want to re-process previously seen TypeDescriptors
        final HashSet<JDKTypeDescriptor> processed = new HashSet<>();

        while (!queue.isEmpty()) {

            final var current = queue.poll();

            // include the current TypeDescriptor as being processed
            processed.add(current);

            // include the Traits for this Traitable
            current.traits(traitClass)
                .forEach(traits::add);

            // include processing of the interfaces to discover Traits (if not already processed)
            current.interfaceTypeUsages()
                .map(typeUsage -> getJDKTypeDescriptor(typeUsage).orElse(null))
                .filter(Objects::nonNull)
                .filter(descriptor -> !processed.contains(descriptor))
                .forEach(queue::offer);

            // include processing of the super class to discover Traits (if not already processed)
            current.parentTypeUsage()
                .flatMap(this::getJDKTypeDescriptor)
                .filter(descriptor -> !processed.contains(descriptor))
                .ifPresent(queue::offer);
        }

        return traits.stream();
    }

    /**
     * Attempts to obtain the {@link JDKTypeDescriptor} for the specified {@link TypeName}.
     *
     * @param typeName the {@link TypeName}
     * @return the {@link Optional} {@link JDKTypeDescriptor} or {@link Optional#empty()} if unavailable
     */
    public Optional<JDKTypeDescriptor> getJDKTypeDescriptor(final TypeName typeName) {

        if (typeName == null) {
            return Optional.empty();
        }

        try {
            final var typeUsageClass = Thread.currentThread().getContextClassLoader()
                .loadClass(typeName.canonicalName());

            return getJDKTypeDescriptor(typeUsageClass);
        }
        catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to obtain the {@link JDKTypeDescriptor} for the specified {@link TypeUsage}.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link Optional} {@link JDKTypeDescriptor} or {@link Optional#empty()} if unavailable
     */
    public Optional<JDKTypeDescriptor> getJDKTypeDescriptor(final TypeUsage typeUsage) {

        return typeUsage instanceof NamedTypeUsage namedTypeUsage
            ? getJDKTypeDescriptor(namedTypeUsage.typeName())
            : Optional.empty();
    }

    /**
     * Obtains the {@link AnnotationTypeUsage}s defined by the specified {@link AnnotatedElement}.
     *
     * @param element the {@link AnnotatedElement}
     * @return a {@link Stream} of {@link AnnotationTypeUsage}s
     */
    public Stream<AnnotationTypeUsage> getAnnotations(final AnnotatedElement element) {

        return element == null
            ? Stream.empty()
            : Streams.of(element.getDeclaredAnnotations())
                .map(this::getAnnotation);
    }

    /**
     * Obtains the {@link AnnotationTypeUsage} represented by the specified {@link Annotation}.
     *
     * @param annotation the {@link Annotation}
     * @return the corresponding {@link AnnotationTypeUsage}
     */
    public AnnotationTypeUsage getAnnotation(final Annotation annotation) {
        Objects.requireNonNull(annotation, "The Annotation must not be null");

        final var nameProvider = getNameProvider();

        final var annotationType = annotation.annotationType();
        final var typeName = nameProvider.getTypeName(annotationType);

        final var annotationValues = Streams.of(annotationType.getDeclaredMethods())
            .filter(method -> !method.isDefault() && method.getParameters().length == 0)
            .map(method -> {
                try {
                    final var name = nameProvider.getIrreducibleName(method.getName());
                    final var value = method.invoke(annotation);
                    return AnnotationValue.of(this, name, value);
                }
                catch (final IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            });

        return AnnotationTypeUsage.of(this, typeName, annotationValues);
    }

    /**
     * Determines the {@link AccessModifier} based on the specified modifiers.
     *
     * @param modifiers the modifiers
     * @return the {@link Optional} {@link AccessModifier}, {@link Optional#empty()} if none specified
     */
    private Optional<AccessModifier> getAccessModifier(final int modifiers) {
        return Modifier.isPublic(modifiers)
            ? Optional.of(AccessModifier.PUBLIC)
            : (Modifier.isProtected(modifiers)
                ? Optional.of(AccessModifier.PROTECTED)
                : (Modifier.isPrivate(modifiers) ? Optional.of(AccessModifier.PRIVATE) : Optional.empty()));
    }

    /**
     * Determines the {@link Classification} based on the specified modifiers.
     *
     * @param modifiers the modifiers
     * @return the {@link Classification}
     */
    private Classification getClassification(final int modifiers) {
        // include the Classification
        if (Modifier.isAbstract(modifiers)
            || Modifier.isInterface(modifiers)) {

            return Classification.ABSTRACT;
        }
        else if (Modifier.isFinal(modifiers)) {
            return Classification.FINAL;
        }
        else {
            return Classification.CONCRETE;
        }
    }

    /**
     * Determines the {@link FormalParameterDescriptor}s based on the specified {@link Parameter}s.
     *
     * @param parameters the {@link Parameter}s
     * @return a {@link Stream} of {@link FormalParameterDescriptor}s
     */
    private Stream<FormalParameterDescriptor> getFormalParameters(final Parameter... parameters) {

        final var nameProvider = getNameProvider();

        return Streams.of(parameters)
            .map(parameter -> {
                final var parameterName = parameter.isNamePresent()
                    ? Optional.of(nameProvider.getIrreducibleName(parameter.getName()))
                    : Optional.<IrreducibleName>empty();

                final var parameterType = getTypeUsage(parameter);

                return FormalParameterDescriptor.of(this, parameterName, parameterType);
            });
    }

    /**
     * Initializes the {@link JDKCodeModel} with the foundation types through reflection.
     */
    private void initialize() {

        Stream.of(
                // primitive types
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,

                // wrapper types
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                Boolean.class,
                Character.class,

                // other classes
                String.class,

                // classes for which we don't require discovery
                Class.class,
                ClassLoader.class,
                Object.class,
                Optional.class,
                Stream.class,
                Throwable.class,
                Exception.class)
            .forEach(javaClass -> {

                final var nameProvider = getNameProvider();

                final var typeName = nameProvider.getTypeName(javaClass);

                final var javaTypeDescriptor = createTypeDescriptor(typeName,
                    JDKTypeDescriptor.supplier(javaClass));

                // include the corresponding Type from which the TypeDescriptor was established
                javaTypeDescriptor.addTrait(new JDKType(javaClass));

                // include the ExtendsTypeDescriptor trait for the super class
                final var superClass = javaClass.getSuperclass();
                if (superClass != null) {
                    javaTypeDescriptor.addTrait(
                        ExtendsTypeDescriptor.of(
                            SpecificTypeUsage.of(this, nameProvider.getTypeName(superClass))));
                }

                // include the ImplementsTypeDescriptor traits for the interfaces
                Streams.of(javaClass.getInterfaces())
                    .forEach(javaInterface -> {
                        javaTypeDescriptor.addTrait(
                            ImplementsTypeDescriptor.of(
                                SpecificTypeUsage.of(this, nameProvider.getTypeName(javaInterface))));
                    });
            });
    }

    private static final ThreadLocal<Set<TypeVariable<?>>> IN_PROGRESS_TYPE_VARIABLES =
        ThreadLocal.withInitial(HashSet::new);

    static {
        // register this type to be usable for marshalling
        Marshalling.register(JDKCodeModel.class, MethodHandles.lookup());
    }
}
