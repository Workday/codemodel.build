package build.codemodel.foundation.usage;

import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.pattern.NamedTypeUsagePattern;
import build.codemodel.foundation.usage.pattern.OptionalTypeUsagePattern;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GenericTypeUsage}s with {@link Optional} types.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
class OptionalTypeUsageTests {

    private NameProvider naming;

    @BeforeEach
    void onBeforeEach() {
        this.naming = new NonCachingNameProvider();
    }

    /**
     * Ensure two {@link GenericTypeUsage}s of the same {@link Optional} type compare.
     */
    @Test
    void shouldCompareSameTypes() {

        final var codeModel = new ConceptualCodeModel(this.naming);

        final var type1 = GenericTypeUsage.of(
            codeModel,
            this.naming.getTypeName(Optional.class),
            SpecificTypeUsage.of(codeModel, this.naming.getTypeName(String.class)));

        final var type2 = GenericTypeUsage.of(
            codeModel,
            this.naming.getTypeName(Optional.class),
            SpecificTypeUsage.of(codeModel, this.naming.getTypeName(String.class)));

        assertThat(type1)
            .isEqualTo(type1);

        assertThat(type2)
            .isEqualTo(type2);

        assertThat(type1)
            .isEqualTo(type2);

        assertThat(type2)
            .isEqualTo(type1);

        assertThat(OptionalTypeUsagePattern.any()
            .match(type1)
            .isPresent())
            .isTrue();

        assertThat(OptionalTypeUsagePattern.of(NamedTypeUsagePattern.of(this.naming.getTypeName(String.class)))
            .match(type1)
            .isPresent())
            .isTrue();
    }
}
