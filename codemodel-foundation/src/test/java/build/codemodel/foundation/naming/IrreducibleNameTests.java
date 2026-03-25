package build.codemodel.foundation.naming;

import build.codemodel.foundation.naming.IrreducibleName;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IrreducibleName}s.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
class IrreducibleNameTests {

    /**
     * Ensure empty {@link IrreducibleName}s are the equal.
     */
    @Test
    void shouldCompareEmptyIrreducibleNamesAsEqual() {

        final var empty = IrreducibleName.empty();
        final var emptyStringBasedName = IrreducibleName.of("");
        final var nullBasedName = IrreducibleName.of(null);

        // ensure equality holds for oneself
        assertThat(empty)
            .isEqualTo(empty);

        // ensure equality reflexively holds
        assertThat(empty)
            .isEqualTo(emptyStringBasedName);

        assertThat(emptyStringBasedName)
            .isEqualTo(empty);

        assertThat(empty)
            .isEqualTo(nullBasedName);

        assertThat(nullBasedName)
            .isEqualTo(empty);

        assertThat(emptyStringBasedName)
            .isEqualTo(nullBasedName);

        assertThat(nullBasedName)
            .isEqualTo(emptyStringBasedName);
    }

    /**
     * Ensure empty {@link IrreducibleName}s hash-codes are the same.
     */
    @Test
    void shouldCompareSameEmptyIrreducibleNameHashCodesAsEqual() {

        final var empty = IrreducibleName.empty();
        final var emptyStringBasedName = IrreducibleName.of("");
        final var nullBasedName = IrreducibleName.of(null);

        // ensure hashcodes are stable
        assertThat(empty.hashCode())
            .isEqualTo(empty.hashCode());

        // ensure hashcode reflexively holds
        assertThat(empty.hashCode())
            .isEqualTo(emptyStringBasedName.hashCode());

        assertThat(emptyStringBasedName.hashCode())
            .isEqualTo(empty.hashCode());

        assertThat(empty.hashCode())
            .isEqualTo(nullBasedName.hashCode());

        assertThat(nullBasedName.hashCode())
            .isEqualTo(empty.hashCode());

        assertThat(emptyStringBasedName.hashCode())
            .isEqualTo(nullBasedName.hashCode());

        assertThat(nullBasedName.hashCode())
            .isEqualTo(emptyStringBasedName.hashCode());
    }

    /**
     * Ensure that the same {@link IrreducibleName}s compare equally.
     */
    @Test
    void shouldCompareSameIrreducibleNamesAsEqual() {
        final var bruceOne = IrreducibleName.of("Bruce");
        final var bruceTwo = IrreducibleName.of("Bruce");

        assertThat(bruceOne)
            .isEqualTo(bruceTwo);

        assertThat(bruceTwo)
            .isEqualTo(bruceOne);

        assertThat(bruceOne.hashCode())
            .isEqualTo(bruceTwo.hashCode());

        assertThat(bruceTwo.hashCode())
            .isEqualTo(bruceOne.hashCode());
    }

    /**
     * Ensure that different {@link IrreducibleName}s don't compare equally.
     */
    @Test
    void shouldNotCompareDifferentIrreducibleNamesAsEqual() {
        final var bruce = IrreducibleName.of("Bruce");
        final var brucette = IrreducibleName.of("Brucette");

        assertThat(bruce)
            .isNotEqualTo(brucette);

        assertThat(brucette)
            .isNotEqualTo(bruce);

        assertThat(bruce.hashCode())
            .isNotEqualTo(brucette.hashCode());

        assertThat(brucette.hashCode())
            .isNotEqualTo(bruce.hashCode());
    }

    /**
     * Ensure {@link IrreducibleName}s can be created using underscores.
     */
    @Test
    void shouldCreateIrreducibleNamesContainingUnderScores() {
        IrreducibleName.of("T_CONS");
        IrreducibleName.of("T_SPLITR");
    }
}
