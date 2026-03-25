package build.codemodel.jdk;

import build.base.foundation.Lazy;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

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
}
