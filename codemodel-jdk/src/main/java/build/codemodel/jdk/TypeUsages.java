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
import build.codemodel.foundation.usage.WildcardTypeUsage;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    /**
     * Determines if {@code candidate} is compatible with the (possibly wildcard-bearing) {@code requested}
     * {@link TypeUsage} - i.e. whether {@code candidate} could stand in wherever {@code requested} is
     * expected, per ordinary JLS assignability (covariant: a subtype is always compatible with its
     * supertype).
     *
     * <p>This is distinct from the <i>invariant</i> rules governing {@code requested}'s own generic type
     * arguments, if it has any - per <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.5.1">JLS 4.5.1</a>,
     * a concrete type argument (no wildcard) must match exactly, since {@code List<Impl>} is never
     * compatible with a requested {@code List<Base>} even though {@code Impl} is compatible with
     * {@code Base} at the top level. Each type argument is compared via
     * {@link #isArgumentCompatible(TypeUsage, TypeUsage, JDKCodeModel)}, which enforces that invariance
     * (relaxed only where {@code requested}'s argument is itself a wildcard, or {@code candidate} supplies
     * no type arguments at all - a raw usage, compatible with any parameterization of the same raw type).
     *
     * @param requested the (possibly wildcard-bearing) requested {@link TypeUsage}
     * @param candidate the candidate {@link TypeUsage}
     * @param codeModel the {@link JDKCodeModel} used to check bound assignability, scanning either side of the
     *                  comparison on demand if not already present
     * @return {@code true} if {@code candidate} is compatible with {@code requested}, {@code false} otherwise
     * @see #isAssignable(TypeUsage, TypeUsage, JDKCodeModel)
     */
    public static boolean isCompatible(final TypeUsage requested,
                                       final TypeUsage candidate,
                                       final JDKCodeModel codeModel) {

        if (requested instanceof WildcardTypeUsage wildcard) {
            return wildcardCompatible(wildcard, candidate, codeModel);
        }

        if (candidate instanceof WildcardTypeUsage) {
            // a wildcard can only appear on the candidate side when requested is itself a wildcard
            // (handled above) - a wildcard can never satisfy a requested concrete or generic type
            return false;
        }

        if (!(requested instanceof GenericTypeUsage requestedGeneric)) {
            // requested carries no type argument for invariance to apply to - ordinary (covariant)
            // JLS assignability governs the whole type
            return isAssignable(candidate, requested, codeModel);
        }

        if (!(candidate instanceof NamedTypeUsage candidateNamed)) {
            return false;
        }

        if (!requestedGeneric.typeName().equals(candidateNamed.typeName())) {
            // candidate isn't a usage of the same generic declaration as requested, so there's no
            // positional type argument list to compare invariantly against requested's. If candidate
            // itself carries no reified type argument, it's a raw usage compatible with any
            // parameterization, so ordinary raw (erasure) assignability governs; otherwise candidate is
            // concretely parameterized down a different branch of the hierarchy (e.g. ArrayList<Impl>
            // against a requested List<Base>) and verifying invariance would require substituting type
            // arguments through the intervening supertypes, which isn't modeled here - conservatively
            // treat it as incompatible rather than risk a false positive by ignoring the argument
            // mismatch entirely
            return (!(candidate instanceof GenericTypeUsage candidateGenericRawCheck)
                    || candidateGenericRawCheck.parameters().findAny().isEmpty())
                && isAssignable(candidate, requested, codeModel);
        }

        if (!(candidate instanceof GenericTypeUsage candidateGeneric)
            || candidateGeneric.parameters().findAny().isEmpty()) {
            // the candidate is a raw usage of the same raw type - compatible with any parameterization,
            // since there is no reified type argument to conflict with the requested wildcard
            return true;
        }

        return parametersCompatible(requestedGeneric, candidateGeneric, codeModel);
    }

    /**
     * Determines if {@code requestedGeneric} and {@code candidateGeneric} - already established to share a
     * common raw type - have the same number of type arguments, each pairwise compatible per
     * {@link #isArgumentCompatible(TypeUsage, TypeUsage, JDKCodeModel)}.
     */
    private static boolean parametersCompatible(final GenericTypeUsage requestedGeneric,
                                                final GenericTypeUsage candidateGeneric,
                                                final JDKCodeModel codeModel) {

        final var requestedParameters = requestedGeneric.parameters().toList();
        final var candidateParameters = candidateGeneric.parameters().toList();

        return requestedParameters.size() == candidateParameters.size()
            && IntStream.range(0, requestedParameters.size())
            .allMatch(i ->
                isArgumentCompatible(requestedParameters.get(i), candidateParameters.get(i), codeModel));
    }

    /**
     * Determines if {@code candidate} is compatible with {@code requested} in a generic type <i>argument</i>
     * position, per the invariant containment rules of
     * <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.5.1">JLS 4.5.1</a> - unlike
     * {@link #isCompatible(TypeUsage, TypeUsage, JDKCodeModel)}, a concrete (non-wildcard) {@code requested}
     * argument requires an exact match rather than mere assignability, since generic type arguments are
     * invariant without a wildcard.
     *
     * @param requested the requested type argument
     * @param candidate the candidate type argument
     * @param codeModel the {@link JDKCodeModel} used to check wildcard bound assignability
     * @return {@code true} if {@code candidate} is compatible with {@code requested} in argument position
     * @see #isCompatible(TypeUsage, TypeUsage, JDKCodeModel)
     */
    private static boolean isArgumentCompatible(final TypeUsage requested,
                                                final TypeUsage candidate,
                                                final JDKCodeModel codeModel) {

        if (requested instanceof WildcardTypeUsage wildcard) {
            return wildcardCompatible(wildcard, candidate, codeModel);
        }

        if (candidate instanceof WildcardTypeUsage) {
            return false;
        }

        if (!(requested instanceof GenericTypeUsage requestedGeneric)) {
            // a concrete type argument is invariant per JLS 4.5.1 - matching List<Person> against
            // List<Car> must never collide, even where Person and Car are otherwise assignable
            return requested.canonicalName().equals(candidate.canonicalName());
        }

        if (!(candidate instanceof NamedTypeUsage candidateNamed)
            || !requestedGeneric.typeName().equals(candidateNamed.typeName())) {
            return false;
        }

        if (!(candidate instanceof GenericTypeUsage candidateGeneric)
            || candidateGeneric.parameters().findAny().isEmpty()) {
            return true;
        }

        return parametersCompatible(requestedGeneric, candidateGeneric, codeModel);
    }

    /**
     * Determines if {@code candidate} is compatible with a {@code requested} that is itself a
     * {@link WildcardTypeUsage}, shared between {@link #isCompatible(TypeUsage, TypeUsage, JDKCodeModel)} and
     * {@link #isArgumentCompatible(TypeUsage, TypeUsage, JDKCodeModel)} since a wildcard {@code requested} is
     * handled identically at the top level and in argument position - only a non-wildcard {@code requested}
     * distinguishes covariance from invariance.
     */
    private static boolean wildcardCompatible(final WildcardTypeUsage wildcard,
                                              final TypeUsage candidate,
                                              final JDKCodeModel codeModel) {

        if (candidate instanceof WildcardTypeUsage candidateWildcard) {
            return wildcardsCompatible(wildcard, candidateWildcard, codeModel);
        }

        return wildcard.upperBound()
                .map(bound -> isAssignable(candidate, bound, codeModel))
                .orElse(true)
            && wildcard.lowerBound()
                .map(bound -> isAssignable(bound, candidate, codeModel))
                .orElse(true);
    }

    /**
     * Determines if {@code candidate} is <i>contained by</i> {@code requested} per the type argument
     * containment rules of
     * <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.5.1">JLS 4.5.1</a> - i.e.
     * whether a {@code List<candidate>} usage would be assignable wherever a {@code List<requested>} usage is
     * expected. Containment is asymmetric.
     *
     * <p>When {@code requested} is {@code ? super S}, only a {@code candidate} that is itself
     * {@code ? super C} can be contained, and only when {@code S} is assignable to {@code C} (candidate's
     * lower bound reaches at least as low as {@code S}); an {@code extends}-bounded or unbounded
     * {@code candidate} has no guaranteed lower bound at all and can never satisfy a {@code super}
     * requirement, per JLS - not even one with a {@code super Object} bound.
     *
     * <p>When {@code requested} is {@code ? extends S} - including the unbounded {@code ?}, which JLS treats
     * as {@code ? extends Object} - containment reduces to comparing each side's <i>effective upper bound</i>:
     * {@code S} for {@code requested}, and for {@code candidate} either its own {@code extends} bound, or
     * {@code Object} if {@code candidate} is unbounded or only {@code super}-bounded (since neither guarantees
     * anything tighter) - requiring {@code candidate}'s effective upper bound to be assignable to
     * {@code requested}'s.
     */
    private static boolean wildcardsCompatible(final WildcardTypeUsage requested,
                                               final WildcardTypeUsage candidate,
                                               final JDKCodeModel codeModel) {

        if (requested.lowerBound().isPresent()) {
            final var requestedLower = requested.lowerBound().orElseThrow();

            return candidate.lowerBound()
                .map(candidateLower -> isAssignable(requestedLower, candidateLower, codeModel))
                .orElse(false);
        }

        final var requestedUpper = requested.upperBound()
            .orElseGet(() -> codeModel.getTypeUsage(Object.class));
        final var candidateUpper = candidate.upperBound()
            .orElseGet(() -> codeModel.getTypeUsage(Object.class));

        return isAssignable(candidateUpper, requestedUpper, codeModel);
    }

    /**
     * Determines if {@code from} is assignable to {@code to}, scanning either side into {@code codeModel} on
     * demand via {@link JDKCodeModel#getJDKTypeDescriptor(TypeName)} if not already present, so callers never
     * need to have pre-scanned the participating types themselves. Treats a type that cannot be resolved to a
     * loadable {@link Class} at all (for example a purely source-modeled type with no corresponding runtime
     * class) as not assignable, rather than failing the whole lookup. Identical {@link TypeName}s are treated
     * as assignable without a model lookup, short-circuiting before either side needs to be resolved.
     *
     * @param from      the {@link TypeUsage} to check assignability from
     * @param to        the {@link TypeUsage} to check assignability to
     * @param codeModel the {@link JDKCodeModel} used to scan either side into the model on demand
     * @return {@code true} if {@code from} is assignable to {@code to}, {@code false} otherwise
     */
    public static boolean isAssignable(final TypeUsage from,
                                       final TypeUsage to,
                                       final JDKCodeModel codeModel) {

        if (!(from instanceof NamedTypeUsage fromNamed) || !(to instanceof NamedTypeUsage toNamed)) {
            return from.canonicalName().equals(to.canonicalName());
        }

        if (fromNamed.typeName().equals(toNamed.typeName())) {
            return true;
        }

        final var fromDescriptor = codeModel.getJDKTypeDescriptor(fromNamed.typeName());
        final var toDescriptor = codeModel.getJDKTypeDescriptor(toNamed.typeName());

        return fromDescriptor.isPresent()
            && toDescriptor.isPresent()
            && fromDescriptor.get().isAssignableTo(toDescriptor.get());
    }
}
