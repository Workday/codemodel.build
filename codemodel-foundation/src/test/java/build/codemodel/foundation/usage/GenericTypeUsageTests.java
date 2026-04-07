package build.codemodel.foundation.usage;

import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.pattern.GenericTypeUsagePattern;
import build.codemodel.foundation.usage.pattern.NamedTypeUsagePattern;
import build.codemodel.foundation.usage.pattern.TypeUsagePattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GenericTypeUsage}s.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
class GenericTypeUsageTests {

    /**
     * The {@link NameProvider}.
     */
    private NameProvider naming;

    @BeforeEach
    void onBeforeEach() {
        this.naming = new NonCachingNameProvider();
    }

    /**
     * Ensure two {@link GenericTypeUsage}s of the same type compare.
     */
    @Test
    void shouldCompareSameTypes() {
        final var codeModel = new ConceptualCodeModel(this.naming);

        final var optionalTypeName = this.naming.getTypeName(Optional.class);
        final var stringTypeName = this.naming.getTypeName(String.class);

        final var typeUsage = SpecificTypeUsage.of(codeModel, stringTypeName);
        final var type1 = GenericTypeUsage.of(codeModel, optionalTypeName, typeUsage);
        final var type2 = GenericTypeUsage.of(codeModel, optionalTypeName, typeUsage);

        assertThat(type1)
            .isEqualTo(type1);

        assertThat(type2)
            .isEqualTo(type2);

        assertThat(type1)
            .isEqualTo(type2);

        assertThat(type2)
            .isEqualTo(type1);
    }

    /**
     * Ensure two {@link GenericTypeUsage}s with different type parameters are not equal.
     * Previously equals() returned true regardless of parameters (bug).
     */
    @Test
    void shouldNotBeEqualWhenParametersAreDifferent() {
        final var codeModel = new ConceptualCodeModel(this.naming);

        final var listTypeName = this.naming.getTypeName(List.class);
        final var stringTypeName = this.naming.getTypeName(String.class);
        final var integerTypeName = this.naming.getTypeName(Integer.class);

        final var listOfString = GenericTypeUsage.of(codeModel, listTypeName,
            SpecificTypeUsage.of(codeModel, stringTypeName));
        final var listOfInteger = GenericTypeUsage.of(codeModel, listTypeName,
            SpecificTypeUsage.of(codeModel, integerTypeName));

        assertThat(listOfString).isNotEqualTo(listOfInteger);
        assertThat(listOfInteger).isNotEqualTo(listOfString);
    }

    /**
     * Ensure two {@link GenericTypeUsage}s with the same raw type but no parameters are equal.
     */
    @Test
    void shouldBeEqualWhenBothHaveNoParameters() {
        final var codeModel = new ConceptualCodeModel(this.naming);

        final var listTypeName = this.naming.getTypeName(List.class);

        final var rawList1 = GenericTypeUsage.of(codeModel, listTypeName);
        final var rawList2 = GenericTypeUsage.of(codeModel, listTypeName);

        assertThat(rawList1).isEqualTo(rawList2);
    }

    /**
     * Ensure a {@link TypeName} can be established for a nested generic {@link Class}.
     */
    @Test
    void shouldCreateGenericTypeName() {
        this.naming.getTypeName(Spliterator.OfPrimitive.class);
    }

    /**
     * Ensure a {@link GenericTypeUsagePattern} can match a {@link GenericTypeUsage} when not specifying
     * parameter {@link TypeUsagePattern}s.
     */
    @Test
    void shouldMatchGenericTypeUsagesWithoutParameterPatterns() {
        final var codeModel = new ConceptualCodeModel(this.naming);

        final var optionalTypeName = this.naming.getTypeName(Optional.class);
        final var stringTypeName = this.naming.getTypeName(String.class);

        final var typeUsage = SpecificTypeUsage.of(codeModel, stringTypeName);
        final var genericTypeUsage = GenericTypeUsage.of(codeModel, optionalTypeName, typeUsage);

        final var pattern = GenericTypeUsagePattern.of(optionalTypeName);
        final var match = pattern.match(genericTypeUsage);

        assertThat(match.isPresent())
            .isTrue();

        assertThat(match.orElseThrow())
            .isSameAs(genericTypeUsage);

        assertThat(match.parameters())
            .isEmpty();

        assertThat(TypeUsagePattern.any()
            .match(genericTypeUsage)
            .isPresent())
            .isTrue();

        assertThat(TypeUsagePattern.none()
            .match(genericTypeUsage)
            .isEmpty())
            .isTrue();
    }

    /**
     * Ensure a {@link GenericTypeUsagePattern} can match a {@link GenericTypeUsage} when specifying
     * parameter {@link TypeUsagePattern}s.
     */
    @Test
    void shouldMatchGenericTypeUsagesWithParameterPatterns() {
        final var codeModel = new ConceptualCodeModel(this.naming);

        final var optionalTypeName = this.naming.getTypeName(Optional.class);
        final var stringTypeName = this.naming.getTypeName(String.class);

        final var typeUsage = SpecificTypeUsage.of(codeModel, stringTypeName);
        final var genericTypeUsage = GenericTypeUsage.of(codeModel, optionalTypeName, typeUsage);

        final var pattern = GenericTypeUsagePattern.of(optionalTypeName, NamedTypeUsagePattern.of(stringTypeName));
        final var match = pattern.match(genericTypeUsage);

        assertThat(match.isPresent())
            .isTrue();

        assertThat(match.orElseThrow())
            .isSameAs(genericTypeUsage);

        assertThat(match.parameters())
            .isNotEmpty();

        assertThat(match.parameters())
            .hasSize(1);

        assertThat(TypeUsagePattern.any()
            .match(genericTypeUsage)
            .isPresent())
            .isTrue();

        assertThat(TypeUsagePattern.none()
            .match(genericTypeUsage)
            .isEmpty())
            .isTrue();

        assertThat(TypeUsagePattern.isInstanceOf(GenericTypeUsage.class)
            .match(genericTypeUsage)
            .isPresent())
            .isTrue();
    }
}
