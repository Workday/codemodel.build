package build.codemodel.jdk;

import build.base.foundation.Lazy;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeUsages}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
class TypeUsagesTests {

    /**
     * Creates a new {@link JDKCodeModel} for testing.
     *
     * @return a new {@link JDKCodeModel}
     */
    protected JDKCodeModel createCodeModel() {
        final var nameProvider = new NonCachingNameProvider();
        return new JDKCodeModel(nameProvider);
    }

    /**
     * Ensure a {@link Class} can be obtained for a {@link TypeUsage}.
     */
    @Test
    void shouldObtainClassForTypeUsage() {
        final var codeModel = createCodeModel();

        final var typeUsage = codeModel.getTypeUsage(String.class);

        final var typeUsageClass = TypeUsages.getSystemClass(typeUsage)
            .orElseThrow();

        assertThat(typeUsageClass)
            .isEqualTo(String.class);
    }

    /**
     * Ensure a {@link Class} can be obtained for a {@link GenericTypeUsage}.
     */
    @Test
    void shouldObtainClassForGenericTypeUsage() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeUsage = GenericTypeUsage.of(codeModel, nameProvider.getTypeName(Optional.class));

        final var typeUsageClass = TypeUsages.getSystemClass(typeUsage)
            .orElseThrow();

        assertThat(typeUsageClass)
            .isEqualTo(Optional.class);
    }

    /**
     * Ensure a {@link Class} can be obtained for a {@link ArrayTypeUsage}.
     */
    @Test
    void shouldObtainClassForArrayTypeUsage() {
        final var codeModel = createCodeModel();

        final var typeUsage = ArrayTypeUsage.of(codeModel,
            Lazy.of(codeModel.getTypeUsage(String.class)));

        final var typeUsageClass = TypeUsages.getSystemClass(typeUsage)
            .orElseThrow();

        assertThat(typeUsageClass)
            .isEqualTo(String.class);
    }

    // ---- fixtures for isCompatible / isAssignable ----

    static class Base {
    }

    static class Impl
        extends Base {
    }

    static class Unrelated {
    }

    static class Impl2
        extends Base {
    }

    /**
     * A raw {@code Class} usage (no reified generic parameter) carries no type argument to conflict with a
     * requested wildcard bound, so it should be compatible with a {@code Class<? extends Base>} request.
     */
    @Test
    void shouldBeCompatibleWhenCandidateIsRawUsage() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))));
        final var candidate = GenericTypeUsage.of(codeModel, classTypeName);

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isTrue();
    }

    /**
     * A concretely-parameterized {@code Class<Impl>} usage should be compatible with a
     * {@code Class<? extends Base>} request when {@code Impl} is actually assignable to {@code Base}, and
     * incompatible when it isn't.
     */
    @Test
    void shouldBeCompatibleOnlyWhenConcreteParameterSatisfiesWildcardBound() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))));

        final var assignableCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Impl.class));
        final var unrelatedCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Unrelated.class));

        assertThat(TypeUsages.isCompatible(requested, assignableCandidate, codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(requested, unrelatedCandidate, codeModel))
            .isFalse();
    }

    /**
     * A candidate wildcard's bound is checked against the requested wildcard's bound rather than requiring
     * the two to be identical - a {@code Class<? extends Impl>} candidate should be compatible with a
     * {@code Class<? extends Base>} request since every {@code Impl} is a {@code Base}.
     */
    @Test
    void shouldBeCompatibleWhenCandidateWildcardBoundSatisfiesRequestedWildcardBound() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))));
        final var candidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class)))));

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isTrue();
    }

    /**
     * An unbounded wildcard ({@code Class<?>}) imposes no constraint, so any candidate parameterization -
     * including one bearing no relation at all to the other fixtures - should be compatible.
     */
    @Test
    void shouldBeCompatibleWithUnboundedWildcard() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(), Optional.empty()));
        final var candidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Unrelated.class));

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isTrue();
    }

    /**
     * A {@code ? super Impl} requested wildcard should be compatible with a concrete candidate parameter only
     * when {@code Impl} is assignable <strong>to</strong> the candidate (the candidate is a supertype of the
     * lower bound), not the other way around as with an {@code extends} bound.
     */
    @Test
    void shouldBeCompatibleOnlyWhenConcreteParameterSatisfiesWildcardLowerBound() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class))),
                Optional.empty()));

        final var supertypeCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Base.class));
        final var unrelatedCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Unrelated.class));

        assertThat(TypeUsages.isCompatible(requested, supertypeCandidate, codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(requested, unrelatedCandidate, codeModel))
            .isFalse();
    }

    /**
     * Per <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.5.1">JLS 4.5.1</a> type
     * argument containment, an {@code extends}-bounded wildcard can never be contained by a {@code super}
     * requirement, and a {@code super}-bounded wildcard can never be contained by an {@code extends}
     * requirement - regardless of how the two bounds relate to each other. {@code ? extends Base} is
     * incompatible with a {@code ? super Impl} candidate even though {@code Impl <: Base}, and
     * {@code ? super Impl} is incompatible with a {@code ? extends Base} candidate for the same reason.
     */
    @Test
    void shouldBeIncompatibleWhenExtendsAndSuperWildcardsArePaired() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var extendsBaseRequested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))));
        final var superImplCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class))),
                Optional.empty()));

        final var superImplRequested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class))),
                Optional.empty()));
        final var extendsBaseCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))));

        assertThat(TypeUsages.isCompatible(extendsBaseRequested, superImplCandidate, codeModel))
            .isFalse();
        assertThat(TypeUsages.isCompatible(superImplRequested, extendsBaseCandidate, codeModel))
            .isFalse();
    }

    /**
     * A {@code ? super} requested wildcard is only contained by a {@code candidate} that is itself
     * {@code super}-bounded and whose lower bound is reachable from the requested one - {@code ? super Impl}
     * is compatible with {@code ? super Base} (candidate's {@code super Base} accepts a strict superset of
     * what {@code super Impl} accepts, since {@code Impl <: Base}), but is incompatible with a
     * {@code ? super Base} requested against a narrower {@code ? super Impl} candidate reversed, or against an
     * unbounded candidate, since an unbounded wildcard has no guaranteed lower bound at all.
     */
    @Test
    void shouldBeCompatibleForSuperWildcardsOnlyWhenCandidateLowerBoundIsReachable() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class))),
                Optional.empty()));

        final var reachableCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class))),
                Optional.empty()));
        final var unreachableCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Unrelated.class))),
                Optional.empty()));
        final var unboundedCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.create(codeModel));

        assertThat(TypeUsages.isCompatible(requested, reachableCandidate, codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(requested, unreachableCandidate, codeModel))
            .isFalse();
        assertThat(TypeUsages.isCompatible(requested, unboundedCandidate, codeModel))
            .isFalse();
    }

    /**
     * Containment of two {@code extends} bounds is directional, per JLS 4.5.1: {@code candidate}'s bound must
     * itself be assignable to {@code requested}'s bound, not merely related to it - sibling subtypes of a
     * common supertype (neither assignable to the other) must be treated as incompatible, since neither bound
     * is narrower than the other.
     */
    @Test
    void shouldBeIncompatibleForDisjointExtendsWildcards() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class)))));
        final var candidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Impl2.class)))));

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isFalse();
    }

    /**
     * A wildcard can only ever appear on the candidate side when {@code requested} is itself a wildcard - if
     * {@code requested} is a plain concrete type, a wildcard candidate can never satisfy it and must be
     * treated as incompatible rather than falling through to a canonical-name comparison.
     */
    @Test
    void shouldBeIncompatibleWhenCandidateIsWildcardButRequestedIsNot() {
        final var codeModel = createCodeModel();

        final var requested = codeModel.getTypeUsage(Base.class);
        final var candidate = WildcardTypeUsage.create(codeModel);

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isFalse();
    }

    /**
     * {@link TypeUsages#isAssignable(TypeUsage, TypeUsage, JDKCodeModel)} should report a subtype as
     * assignable to its supertype, and two unrelated types as not assignable.
     */
    @Test
    void shouldDetermineAssignabilityBetweenNamedTypeUsages() {
        final var codeModel = createCodeModel();

        assertThat(TypeUsages.isAssignable(
            codeModel.getTypeUsage(Impl.class), codeModel.getTypeUsage(Base.class), codeModel))
            .isTrue();
        assertThat(TypeUsages.isAssignable(
            codeModel.getTypeUsage(Unrelated.class), codeModel.getTypeUsage(Base.class), codeModel))
            .isFalse();
    }

    /**
     * A requested type that isn't itself a {@link GenericTypeUsage} carries no type argument for JLS
     * invariance to apply to, so ordinary (covariant) assignability governs the whole comparison: a subtype
     * candidate is compatible with its supertype request, and an unrelated candidate isn't.
     */
    @Test
    void shouldUseOrdinaryAssignabilityForNonGenericRequestedTypes() {
        final var codeModel = createCodeModel();

        final var requested = codeModel.getTypeUsage(Base.class);

        assertThat(TypeUsages.isCompatible(requested, codeModel.getTypeUsage(Base.class), codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(requested, codeModel.getTypeUsage(Impl.class), codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(requested, codeModel.getTypeUsage(Unrelated.class), codeModel))
            .isFalse();
    }

    /**
     * Within a generic type argument, invariance still applies even though the same two types are
     * compatible at the top level - {@code List<Impl>} is never compatible with a requested
     * {@code List<Base>} type argument per JLS 4.5.1, even though {@code Impl} is itself compatible with
     * {@code Base} when compared directly (as in
     * {@link #shouldUseOrdinaryAssignabilityForNonGenericRequestedTypes()}).
     */
    @Test
    void shouldRemainInvariantForConcreteTypeArgumentsDespiteTopLevelAssignability() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName, codeModel.getTypeUsage(Base.class));
        final var candidate = GenericTypeUsage.of(codeModel, classTypeName, codeModel.getTypeUsage(Impl.class));

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isFalse();
    }

    /**
     * A candidate that isn't a {@link NamedTypeUsage} at all, or is a {@link NamedTypeUsage} of a different
     * raw type than the requested {@link GenericTypeUsage}, can never be compatible - matching by raw type
     * name is a prerequisite before any type argument is even considered.
     */
    @Test
    void shouldBeIncompatibleWhenCandidateRawTypeDiffersOrIsNotNamed() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);
        final var optionalTypeName = nameProvider.getTypeName(Optional.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName, codeModel.getTypeUsage(Base.class));

        final var differentRawTypeCandidate = GenericTypeUsage.of(codeModel, optionalTypeName,
            codeModel.getTypeUsage(Base.class));
        final var notNamedCandidate = ArrayTypeUsage.of(codeModel, Lazy.of(codeModel.getTypeUsage(Base.class)));

        assertThat(TypeUsages.isCompatible(requested, differentRawTypeCandidate, codeModel))
            .isFalse();
        assertThat(TypeUsages.isCompatible(requested, notNamedCandidate, codeModel))
            .isFalse();
    }

    /**
     * A candidate that is a subtype of {@code requested}'s raw type but under a <em>different</em> raw type
     * of its own (e.g. {@code ArrayList} against a requested {@code List}) can't have its own type argument
     * checked positionally against {@code requested}'s, since there's no shared parameter list to compare -
     * doing so would require substituting type arguments through the intervening supertypes (e.g.
     * {@code ArrayList<Impl>}'s {@code E} through {@code AbstractList<E>} to {@code List<E>}), which isn't
     * modeled. Rather than ignore the candidate's own argument entirely and risk a false positive, this case
     * is conservatively treated as incompatible even though {@code ArrayList} actually implements {@code List}.
     */
    @Test
    void shouldBeIncompatibleWhenDifferentRawTypeCandidateHasItsOwnTypeArgument() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var listTypeName = nameProvider.getTypeName(java.util.List.class);
        final var arrayListTypeName = nameProvider.getTypeName(java.util.ArrayList.class);

        final var requested = GenericTypeUsage.of(codeModel, listTypeName, codeModel.getTypeUsage(Base.class));
        final var candidate = GenericTypeUsage.of(codeModel, arrayListTypeName, codeModel.getTypeUsage(Impl.class));

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isFalse();
    }

    /**
     * A candidate that is a subtype of {@code requested}'s raw type under a different raw type of its own, but
     * is itself a <em>raw</em> usage with no type argument, carries nothing that could conflict with
     * {@code requested}'s type argument - ordinary raw (erasure) assignability of the whole type still governs.
     */
    @Test
    void shouldFallBackToErasureAssignabilityWhenDifferentRawTypeCandidateIsRaw() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var listTypeName = nameProvider.getTypeName(java.util.List.class);
        final var arrayListTypeName = nameProvider.getTypeName(java.util.ArrayList.class);

        final var requested = GenericTypeUsage.of(codeModel, listTypeName, codeModel.getTypeUsage(Base.class));
        final var candidate = GenericTypeUsage.of(codeModel, arrayListTypeName);

        assertThat(TypeUsages.isCompatible(requested, candidate, codeModel))
            .isTrue();
    }

    /**
     * Type argument comparison is positional and requires equal arity - a candidate with a different number
     * of type arguments than requested can never be compatible, even when every one of its arguments would
     * individually satisfy the corresponding requested argument. When arity does match, every positional
     * pair must be compatible - a single mismatched pair fails the whole comparison regardless of how many
     * other pairs match.
     */
    @Test
    void shouldRequireMatchingArityAndAllPositionalParametersToBeCompatible() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var requested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))),
            codeModel.getTypeUsage(Impl.class));

        final var mismatchedArityCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Impl.class));
        final var allMatchingCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Impl.class), codeModel.getTypeUsage(Impl.class));
        final var secondParameterMismatchCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            codeModel.getTypeUsage(Impl.class), codeModel.getTypeUsage(Impl2.class));

        assertThat(TypeUsages.isCompatible(requested, mismatchedArityCandidate, codeModel))
            .isFalse();
        assertThat(TypeUsages.isCompatible(requested, allMatchingCandidate, codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(requested, secondParameterMismatchCandidate, codeModel))
            .isFalse();
    }

    /**
     * When neither the requested nor candidate wildcard has a {@code super} bound, containment falls back to
     * comparing each side's <i>effective upper bound</i> - {@code Object} for an unbounded or
     * {@code super}-only wildcard, since neither guarantees anything tighter. A requested
     * {@code ? extends Base} rejects a candidate with no {@code extends} bound of its own (whether fully
     * unbounded or only {@code super}-bounded), since {@code Object} isn't assignable to {@code Base}; but an
     * unbounded {@code ?} request - itself {@code ? extends Object} - accepts either, since {@code Object} is
     * assignable to {@code Object}.
     */
    @Test
    void shouldCompareEffectiveUpperBoundsWhenNeitherWildcardHasASuperBound() {
        final var codeModel = createCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var classTypeName = nameProvider.getTypeName(Class.class);

        final var extendsBaseRequested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.empty(),
                Optional.of(Lazy.of(codeModel.getTypeUsage(Base.class)))));
        final var unboundedRequested = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.create(codeModel));

        final var unboundedCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.create(codeModel));
        final var superOnlyCandidate = GenericTypeUsage.of(codeModel, classTypeName,
            WildcardTypeUsage.of(codeModel, Optional.of(Lazy.of(codeModel.getTypeUsage(Impl.class))),
                Optional.empty()));

        assertThat(TypeUsages.isCompatible(extendsBaseRequested, unboundedCandidate, codeModel))
            .isFalse();
        assertThat(TypeUsages.isCompatible(extendsBaseRequested, superOnlyCandidate, codeModel))
            .isFalse();
        assertThat(TypeUsages.isCompatible(unboundedRequested, unboundedCandidate, codeModel))
            .isTrue();
        assertThat(TypeUsages.isCompatible(unboundedRequested, superOnlyCandidate, codeModel))
            .isTrue();
    }

    /**
     * Identical {@link build.codemodel.foundation.naming.TypeName}s short-circuit to {@code true} without
     * consulting the {@link JDKCodeModel} at all.
     */
    @Test
    void shouldShortCircuitAssignabilityForIdenticalTypeNames() {
        final var codeModel = createCodeModel();

        assertThat(TypeUsages.isAssignable(
            codeModel.getTypeUsage(Base.class), codeModel.getTypeUsage(Base.class), codeModel))
            .isTrue();
    }

    /**
     * A {@link TypeUsage} that isn't a {@link NamedTypeUsage} - such as an array type - falls back to plain
     * canonical-name equality rather than {@link JDKCodeModel} hierarchy assignability.
     */
    @Test
    void shouldFallBackToCanonicalNameForNonNamedTypeUsages() {
        final var codeModel = createCodeModel();

        final var baseArray = ArrayTypeUsage.of(codeModel, Lazy.of(codeModel.getTypeUsage(Base.class)));
        final var otherBaseArray = ArrayTypeUsage.of(codeModel, Lazy.of(codeModel.getTypeUsage(Base.class)));
        final var implArray = ArrayTypeUsage.of(codeModel, Lazy.of(codeModel.getTypeUsage(Impl.class)));

        assertThat(TypeUsages.isAssignable(baseArray, otherBaseArray, codeModel))
            .isTrue();
        assertThat(TypeUsages.isAssignable(baseArray, implArray, codeModel))
            .isFalse();
    }

    /**
     * A {@link TypeName} that can't be resolved to a loadable {@link Class} - for example a purely
     * source-modeled type with no corresponding runtime class - is treated as not assignable rather than
     * failing the lookup outright.
     */
    @Test
    void shouldTreatUnresolvableTypeAsNotAssignable() {
        final var codeModel = createCodeModel();

        final var bogusNamespace = Namespace.of(IrreducibleName.of("bogus")).orElseThrow();
        final var bogusTypeName = TypeName.of(Optional.empty(), Optional.empty(), Optional.of(bogusNamespace),
            IrreducibleName.of("Nonexistent"));
        final var unresolvable = SpecificTypeUsage.of(codeModel, bogusTypeName);

        assertThat(TypeUsages.isAssignable(unresolvable, codeModel.getTypeUsage(Base.class), codeModel))
            .isFalse();
    }
}
