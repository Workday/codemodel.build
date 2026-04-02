package build.codemodel.foundation.transport;

import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeNameTransformer}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
class TypeNameTransformerTests {

    /**
     * Ensure the {@link TypeName} for a {@link String} can be transformed into a {@link String}.
     */
    @Test
    void shouldTransformTypeNameIntoString() {
        final var nameProvider = new NonCachingNameProvider();
        final var transformer = new TypeNameTransformer(nameProvider);

        final var stringTypeName = nameProvider.getTypeName(String.class);

        final var stringName = transformer.transform(null, stringTypeName);

        assertThat(stringName)
            .isNotNull();

        assertThat(stringName)
            .isEqualTo("java.base/java.lang.String");
    }

    /**
     * Ensure the {@link String} representing the {@link TypeName} for a {@link String} can be transformed into a
     * {@link TypeName}.
     */
    @Test
    void shouldTransformStringIntoATypeName() {
        final var nameProvider = new NonCachingNameProvider();
        final var transformer = new TypeNameTransformer(nameProvider);

        final var stringTypeName = transformer.reform(null, null, "java.base/java.lang.String");

        assertThat(stringTypeName)
            .isNotNull();

        assertThat(stringTypeName.enclosingTypeName())
            .isEmpty();

        assertThat(stringTypeName.moduleName())
            .map(ModuleName::toString)
            .contains("java.base");

        assertThat(stringTypeName.namespace())
            .map(Namespace::toString)
            .contains("java.lang");

        assertThat(stringTypeName.canonicalName())
            .isEqualTo("java.lang.String");
    }
}
