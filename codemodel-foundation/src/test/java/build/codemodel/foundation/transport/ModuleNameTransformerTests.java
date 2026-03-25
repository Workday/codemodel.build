package build.codemodel.foundation.transport;

import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ModuleNameTransformer}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
class ModuleNameTransformerTests {

    /**
     * Ensure the {@link ModuleName} can be transformed into a {@link String}.
     */
    @Test
    void shouldTransformModuleNameIntoString() {
        final var nameProvider = new NonCachingNameProvider();
        final var transformer = new ModuleNameTransformer(nameProvider);

        final var moduleName = nameProvider.getModuleName("java.base")
            .orElseThrow();

        final var stringName = transformer.transform(null, moduleName);

        assertThat(stringName)
            .isNotNull();

        assertThat(stringName)
            .isEqualTo("java.base");
    }

    /**
     * Ensure the {@link String} representing a {@link ModuleName} can be transformed into a {@link ModuleName}.
     */
    @Test
    void shouldTransformStringIntoAModuleName() {
        final var nameProvider = new NonCachingNameProvider();
        final var transformer = new TypeNameTransformer(nameProvider);

        final var moduleName = transformer.reform(null, null, "java.base");

        assertThat(moduleName)
            .isNotNull();

        assertThat(moduleName.toString())
            .isEqualTo("java.base");
    }
}
