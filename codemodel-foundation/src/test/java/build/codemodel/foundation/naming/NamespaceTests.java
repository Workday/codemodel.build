package build.codemodel.foundation.naming;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Namespace}s.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
class NamespaceTests {

    /**
     * Provides a {@link NameProvider} for the tests.
     *
     * @return a {@link NameProvider}
     */
    private NameProvider nameProvider() {
        return new NonCachingNameProvider();
    }

    /**
     * Ensure empty {@link Namespace}s can't be created.
     */
    @Test
    void shouldNotCreateEmptyNamespaces() {

        assertThat(Namespace.of(IrreducibleName.of("")))
            .isEmpty();

        assertThat(Namespace.of(IrreducibleName.of(null)))
            .isEmpty();

        assertThat(Namespace.of(IrreducibleName.of(" ")))
            .isEmpty();
    }

    /**
     * Ensure same {@link Namespace}s for base packages are equal.
     */
    @Test
    void shouldCompareSameBaseNamespacesAsEqual() {

        final var bruceOne = Namespace.of(IrreducibleName.of("bruce"));
        final var bruceTwo = Namespace.of(IrreducibleName.of("bruce"));

        // ensure reflexive equality
        assertThat(bruceOne).isEqualTo(bruceTwo);
        assertThat(bruceTwo).isEqualTo(bruceOne);

        // ensure hashcode reflexively holds
        assertThat(bruceOne.hashCode()).isEqualTo(bruceTwo.hashCode());
        assertThat(bruceTwo.hashCode()).isEqualTo(bruceOne.hashCode());
    }

    /**
     * Ensure same {@link Namespace}s for some packages are equal.
     */
    @Test
    void shouldCompareSameNamespacesAsEqual() {
        final var nameProvider = nameProvider();

        final var buildCodeModelOne = nameProvider.getNamespace("build.codemodel");
        final var buildCodeModelTwo = nameProvider.getNamespace("build.codemodel");

        // ensure reflexive equality
        assertThat(buildCodeModelOne).isEqualTo(buildCodeModelTwo);
        assertThat(buildCodeModelTwo).isEqualTo(buildCodeModelOne);

        // ensure hashcode reflexively holds
        assertThat(buildCodeModelOne.hashCode()).isEqualTo(buildCodeModelTwo.hashCode());
        assertThat(buildCodeModelTwo.hashCode()).isEqualTo(buildCodeModelOne.hashCode());
    }

    /**
     * Ensure different {@link Namespace}s for packages are not equal.
     */
    @Test
    void shouldNotCompareDifferentSameNamespacesAsEqual() {
        final var nameProvider = nameProvider();

        final var buildCodeModel = nameProvider.getNamespace("build.codemodel");
        final var buildCodeModelExpression = nameProvider.getNamespace("build.codemodel.expression");

        // ensure reflexive inequality
        assertThat(buildCodeModel).isNotEqualTo(buildCodeModelExpression);
        assertThat(buildCodeModelExpression).isNotEqualTo(buildCodeModel);

        // ensure hashcode reflexively doesn't hold
        assertThat(buildCodeModel.hashCode()).isNotEqualTo(buildCodeModelExpression.hashCode());
        assertThat(buildCodeModelExpression.hashCode()).isNotEqualTo(buildCodeModel.hashCode());
    }
}
