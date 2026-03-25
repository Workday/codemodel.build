package build.codemodel.foundation.naming;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModuleName}s.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
class ModuleNameTests {

    /**
     * The {@link NameProvider} to obtain {@link ModuleName}s.
     */
    private NameProvider nameProvider;

    @BeforeEach
    void onBeforeEach() {
        this.nameProvider = new NonCachingNameProvider();
    }

    /**
     * Ensure empty {@link ModuleName}s can't be created.
     */
    @Test
    void shouldNotCreateEmptyModuleNames() {

        assertThat(ModuleName.of("", this.nameProvider))
            .isEmpty();

        assertThat(ModuleName.of(null, this.nameProvider))
            .isEmpty();

        assertThat(ModuleName.of(" ", this.nameProvider))
            .isEmpty();
    }

    /**
     * Ensure same {@link ModuleName}s for base modules are equal.
     */
    @Test
    void shouldCompareSameBaseModuleNamesAsEqual() {

        final var bruceOne = ModuleName.of("bruce", this.nameProvider);
        final var bruceTwo = ModuleName.of("bruce", this.nameProvider);

        // ensure reflexive equality
        assertThat(bruceOne)
            .isEqualTo(bruceTwo);

        assertThat(bruceTwo)
            .isEqualTo(bruceOne);

        // ensure hashcode reflexively holds
        assertThat(bruceOne.hashCode())
            .isEqualTo(bruceTwo.hashCode());

        assertThat(bruceTwo.hashCode())
            .isEqualTo(bruceOne.hashCode());
    }

    /**
     * Ensure same {@link ModuleName}s for some modules are equal.
     */
    @Test
    void shouldCompareSameModuleNamesAsEqual() {

        final var buildCodeModelsOne = ModuleName.of("build.codemodel", this.nameProvider);
        final var buildCodeModelTwo = ModuleName.of("build.codemodel", this.nameProvider);

        // ensure reflexive equality
        assertThat(buildCodeModelsOne)
            .isEqualTo(buildCodeModelTwo);

        assertThat(buildCodeModelTwo)
            .isEqualTo(buildCodeModelsOne);

        // ensure hashcode reflexively holds
        assertThat(buildCodeModelsOne.hashCode())
            .isEqualTo(buildCodeModelTwo.hashCode());
        assertThat(buildCodeModelTwo.hashCode())
            .isEqualTo(buildCodeModelsOne.hashCode());
    }

    /**
     * Ensure different {@link ModuleName}s for modules are not equal.
     */
    @Test
    void shouldNotCompareDifferentSameModuleNamesAsEqual() {

        final var buildCodeModel = ModuleName.of("build.codemodel", this.nameProvider);
        final var buildCodeModelAnnotationProcessing = ModuleName
            .of("build.codemodel.annotation.processing", this.nameProvider);

        // ensure reflexive inequality
        assertThat(buildCodeModel)
            .isNotEqualTo(buildCodeModelAnnotationProcessing);

        assertThat(buildCodeModelAnnotationProcessing)
            .isNotEqualTo(buildCodeModel);

        // ensure hashcode reflexively doesn't hold
        assertThat(buildCodeModel.hashCode())
            .isNotEqualTo(buildCodeModelAnnotationProcessing.hashCode());

        assertThat(buildCodeModelAnnotationProcessing.hashCode())
            .isNotEqualTo(buildCodeModel.hashCode());
    }
}
