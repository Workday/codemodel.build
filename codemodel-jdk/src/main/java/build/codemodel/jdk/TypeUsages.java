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

import build.base.foundation.Introspection;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper methods for working with JDK-based {@link TypeUsage}s.
 *
 * @author brian.oliver
 * @since Jun-2024
 */
public final class TypeUsages {

    /**
     * The JDK primitive {@link Class}es, keyed by the bare name (e.g. {@code "int"}) that a primitive
     * {@link TypeName#name()} carries. Derived from {@link Introspection#primitives()} rather than hardcoded, so
     * it can't drift from the JDK's actual set of primitive types.
     */
    private static final Map<String, Class<?>> PRIMITIVE_CLASSES_BY_NAME = Introspection.primitives()
        .collect(Collectors.toUnmodifiableMap(Class::getName, Function.identity()));

    /**
     * Prevent instantiation
     */
    private TypeUsages() {
        // prevent instantiation
    }

    /**
     * Determines whether the specified {@link TypeName} represents one of the JDK primitive types, which are
     * modeled with a synthetic {@code java.lang} namespace despite not actually residing there.
     *
     * @param typeName the {@link TypeName}
     * @return {@code true} if the {@link TypeName} represents a primitive type, {@code false} otherwise
     */
    public static boolean isPrimitive(final TypeName typeName) {
        return typeName.namespace()
            .map(namespace -> "java.lang".equals(namespace.toString()))
            .orElse(false)
            && PRIMITIVE_CLASSES_BY_NAME.containsKey(typeName.name().toString());
    }

    /**
     * Determines if the specified {@link TypeUsage} is for the {@code boolean} or {@code Boolean} type.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return {@code true} if a boolean {@link TypeUsage}, {@code false} otherwise
     */
    public static boolean isBoolean(final TypeUsage typeUsage) {
        if (!(typeUsage instanceof SpecificTypeUsage specificTypeUsage)) {
            return false;
        }

        final var typeName = specificTypeUsage.typeName();
        return (isPrimitive(typeName) && typeName.name().toString().equals("boolean"))
            || typeName.canonicalName().equals("java.lang.Boolean");
    }

    /**
     * Determines the {@link Type} name for the given {@link TypeName} when used in the {@link Optional}ly specified
     * package {@link Namespace}.
     *
     * @param typeName          the {@link TypeName}
     * @param namespace         the {@link Optional} {@link Namespace}
     * @param importedTypeNames the {@link ImportedTypeNames} representing the currently imported {@link TypeName}s
     * @return the {@link Type} name
     */
    public static String getJDKTypeName(final TypeName typeName,
                                        final Optional<Namespace> namespace,
                                        final ImportedTypeNames importedTypeNames) {

        Objects.requireNonNull(typeName, "The TypeName must not be null");
        Objects.requireNonNull(namespace, "The Namespace must not be null");

        // java.lang types don't need importing or require fully-qualified-names
        if (typeName.namespace()
            .map(packageName -> packageName.toString().startsWith("java.lang"))
            .orElse(false)) {

            // TODO: include the enclosing typename if one is defined?
            return typeName.name().toString();
        }

        // TypeNames in the same Namespace don't need importing or require fully-qualified-names
        if (typeName.namespace().equals(namespace) && typeName.enclosingTypeName().isEmpty()) {

            // TODO: include the enclosing typename if one is defined?
            return typeName.name().toString();
        }

        // attempt to import the type name
        if (importedTypeNames.include(typeName)) {
            return typeName.name().toString();
        } else {
            return typeName.canonicalName();
        }
    }

    /**
     * Attempts to determine the {@link Type} declaration for the given {@link TypeUsage} as a variable when used
     * in the {@link Optional}ly specified package {@link Namespace}.
     *
     * @param typeUsage         the {@link TypeUsage}
     * @param namespace         the {@link Optional} {@link Namespace}
     * @param importedTypeNames the {@link ImportedTypeNames} representing the currently imported {@link TypeName}s
     * @return the {@link Optional} {@link Type} name, otherwise {@link Optional#empty()} if one can't be determined
     */
    public static Optional<String> getVariableTypeDeclaration(final TypeUsage typeUsage,
                                                              final Optional<Namespace> namespace,
                                                              final ImportedTypeNames importedTypeNames) {

        // handle Generic Type Usage
        if (typeUsage instanceof GenericTypeUsage genericTypeUsage) {
            return Optional.of(getJDKTypeName(genericTypeUsage.typeName(), namespace, importedTypeNames)
                + genericTypeUsage.parameters()
                .map(parameter -> getVariableTypeDeclaration(parameter, namespace, importedTypeNames).orElse(
                    "Object"))
                .collect(Collectors.joining(", ", "<", ">")));
        }

        // handle Array Type Usage
        if (typeUsage instanceof ArrayTypeUsage arrayTypeUsage) {
            return Optional.of(
                getVariableTypeDeclaration(arrayTypeUsage.type(), namespace, importedTypeNames) + "[]");
        }

        return typeUsage instanceof NamedTypeUsage namedTypeUsage
            ? Optional.of(getJDKTypeName(namedTypeUsage.typeName(), namespace, importedTypeNames))
            : Optional.empty();
    }

    /**
     * Attempts to obtain the {@link Class} given a {@link TypeUsage} using the specified {@link ClassLoader}.
     *
     * @param typeUsage   the {@link TypeUsage}
     * @param classLoader the {@link ClassLoader}
     * @return the {@link Optional} {@link Class} or {@link Optional#empty()} if there's no such {@link Class} available
     */
    public static Optional<Class<?>> getClass(final TypeUsage typeUsage,
                                              final ClassLoader classLoader) {

        if (classLoader == null) {
            return Optional.empty();
        }

        if (typeUsage instanceof ArrayTypeUsage arrayTypeUsage) {
            return getClass(arrayTypeUsage.type(), classLoader);
        }

        if (!(typeUsage instanceof NamedTypeUsage namedTypeUsage)) {
            return Optional.empty();
        }

        final var typeName = namedTypeUsage.typeName();

        if (isPrimitive(typeName)) {
            return Optional.ofNullable(PRIMITIVE_CLASSES_BY_NAME.get(typeName.name().toString()));
        }

        try {
            return Optional.ofNullable(classLoader.loadClass(typeName.binaryName()));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to obtain the {@link Class} given a {@link TypeUsage} using the {@link Thread} {@link ClassLoader}.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link Optional} {@link Class} or {@link Optional#empty()} if there's no such {@link Class} available
     * @see #getClass(TypeUsage, ClassLoader)
     */
    public static Optional<Class<?>> getThreadContextClass(final TypeUsage typeUsage) {
        return getClass(typeUsage, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Attempts to obtain the {@link Class} given a {@link TypeUsage} using the System {@link ClassLoader}.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link Optional} {@link Class} or {@link Optional#empty()} if there's no such {@link Class} available
     * @see #getClass(TypeUsage, ClassLoader)
     */
    public static Optional<Class<?>> getSystemClass(final TypeUsage typeUsage) {
        return getClass(typeUsage, ClassLoader.getSystemClassLoader());
    }

    /**
     * Attempts to obtain the {@link Class} given a {@link TypeUsage} using the Platform {@link ClassLoader}.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link Optional} {@link Class} or {@link Optional#empty()} if there's no such {@link Class} available
     * @see #getClass(TypeUsage, ClassLoader)
     */
    public static Optional<Class<?>> getPlatformClass(final TypeUsage typeUsage) {
        return getClass(typeUsage, ClassLoader.getPlatformClassLoader());
    }

    /**
     * Attempts to obtain the {@link Class} of the first type parameter of a {@link GenericTypeUsage} using the
     * {@link Thread} {@link ClassLoader}.
     * <p>
     * For example, given {@code Optional<String>}, this returns {@code Optional.of(String.class)}.
     * Returns {@link Optional#empty()} if the {@link TypeUsage} is not a {@link GenericTypeUsage} or has no
     * type parameters.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link Optional} {@link Class} of the first type parameter, or {@link Optional#empty()}
     */
    public static Optional<Class<?>> getFirstTypeParameterClass(final TypeUsage typeUsage) {
        if (typeUsage instanceof GenericTypeUsage gtu) {
            return gtu.parameters().findFirst()
                .flatMap(TypeUsages::getThreadContextClass);
        }
        return Optional.empty();
    }
}
