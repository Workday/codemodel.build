package build.framework.implementation.compliance;

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.framework.Framework;
import build.codemodel.framework.builder.FrameworkBuilder;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FrameworkBuilder} compliance tests.
 *
 * @author brian.oliver
 * @since May-2024
 */
class FrameworkBuilderTests {

    /**
     * Establishes a default {@link FrameworkBuilder} to use for testing.
     *
     * @return a new {@link FrameworkBuilder}
     */
    FrameworkBuilder createDefaultFrameworkBuilder() {
        return new FrameworkBuilder()
            .withNameProvider(() -> new NonCachingNameProvider());
    }

    /**
     * Ensure a newly created {@link FrameworkBuilder} produces a default {@link Framework}.
     */
    @Test
    public void shouldCreateDefaultFramework() {
        final var framework = createDefaultFrameworkBuilder().build();

        // there should be no plugins
        assertThat(framework.plugins()).isEmpty();

        // there should be a NameProvider
        assertThat(framework.nameProvider()).isNotNull();

        // the FileSystem should be the system default
        assertThat(framework.fileSystem()).isEqualTo(FileSystems.getDefault());
    }

    /**
     * Ensure a newly created {@link Framework} produces a default {@link CodeModel}.
     */
    @Test
    public void shouldCreateDefaultCodeModel() {
        final var framework = createDefaultFrameworkBuilder()
            .build();

        final var codeModel = framework.newCodeModel();

        assertThat(codeModel).isNotNull();

        // the underlying NameProvider should be the same as the Framework
        assertThat(codeModel.getNameProvider())
            .isSameAs(framework.nameProvider());

        // there should be no TypeDescriptors
        assertThat(codeModel.typeDescriptors()).isEmpty();
    }
}
