package build.codemodel.foundation.transport;

import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ModuleNameTransformer}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
class NamespaceTransformerTests {

    /**
     * Ensure the {@link Namespace} can be transformed into a {@link String}.
     */
    @Test
    void shouldTransformNamespaceIntoString() {
        final var nameProvider = new NonCachingNameProvider();
        final var transformer = new NamespaceTransformer(nameProvider);

        final var namespace = nameProvider.getNamespace("build.codemodel.foundation")
            .orElseThrow();

        final var string = transformer.transform(null, namespace);

        assertThat(string)
            .isNotNull();

        assertThat(string)
            .isEqualTo("build.codemodel.foundation");
    }

    /**
     * Ensure the {@link String} representing a {@link Namespace} can be transformed into a {@link Namespace}.
     */
    @Test
    void shouldTransformStringIntoANamespace() {
        final var nameProvider = new NonCachingNameProvider();
        final var transformer = new TypeNameTransformer(nameProvider);

        final var namespace = transformer.reform(null, null, "build.codemodel.foundation");

        assertThat(namespace)
            .isNotNull();

        assertThat(namespace.toString())
            .isEqualTo("build.codemodel.foundation");
    }
}
