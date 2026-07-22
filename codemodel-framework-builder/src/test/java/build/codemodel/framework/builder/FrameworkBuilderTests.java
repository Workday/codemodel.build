package build.codemodel.framework.builder;

import build.base.telemetry.TelemetryRecorder;
import build.base.telemetry.foundation.NoOpTelemetryRecorder;
import build.base.telemetry.foundation.ObservableTelemetryRecorder;
import build.codemodel.dependency.injection.Context;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.CachingNameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.framework.Framework;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.compiler.Compiler;
import build.codemodel.framework.initialization.Initializer;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.function.Function;
import java.util.stream.Stream;

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

    /**
     * Ensure {@link Plugin}s with an injected {@link Provider} of {@link CodeModel} resolve to the
     * {@link CodeModel} actually being processed, both immediately after creation and across a
     * subsequent {@link Framework#compile} call.
     */
    @Test
    public void shouldInjectCodeModelIntoPlugins() {
        final var initializer = new Initializer() {
            @Inject
            private Provider<CodeModel> codeModelProvider;

            @Override
            public void initialize(final CodeModel codeModel) {
            }

            public CodeModel injectedCodeModel() {
                return this.codeModelProvider.get();
            }
        };
        final var compiler1 = new Compiler<CodeModel>() {
            @Inject
            private Provider<CodeModel> codeModelProvider;

            @Override
            public void compile(final CodeModel target, final CodeModel codeModel, final TelemetryRecorder telemetryRecorder) {
            }

            public CodeModel injectedCodeModel() {
                return this.codeModelProvider.get();
            }
        };
        final var compiler2 = new Compiler<CodeModel>() {
            @Inject
            private Provider<CodeModel> codeModelProvider;

            @Override
            public void compile(final CodeModel target, final CodeModel codeModel, final TelemetryRecorder telemetryRecorder) {
            }

            public CodeModel injectedCodeModel() {
                return this.codeModelProvider.get();
            }
        };

        final var pluginBuilders = Stream.of(initializer, compiler1, compiler2)
            .map(plugin -> (Function<? super Context, Plugin>) context -> context.inject(plugin));

        final var telemetryRecorder = ObservableTelemetryRecorder.of(NoOpTelemetryRecorder.create());
        final var builder = new FrameworkBuilder()
            .withFileSystem(FileSystems.getDefault())
            .withNameProvider(new CachingNameProvider(new NonCachingNameProvider()));
        builder.bind(TelemetryRecorder.class).to(telemetryRecorder);
        pluginBuilders.forEach(builder::withPlugin);

        final var framework = builder.build();

        final var codeModel = framework.newCodeModel();
        assertThat(initializer.injectedCodeModel()).isSameAs(codeModel);
        assertThat(compiler1.injectedCodeModel()).isSameAs(codeModel);
        assertThat(compiler2.injectedCodeModel()).isSameAs(codeModel);

        framework.compile(codeModel, telemetryRecorder).orElseThrow();
        assertThat(initializer.injectedCodeModel()).isSameAs(codeModel);
        assertThat(compiler1.injectedCodeModel()).isSameAs(codeModel);
        assertThat(compiler2.injectedCodeModel()).isSameAs(codeModel);
    }

    /**
     * Ensure that when a {@link Framework} is used to create more than one {@link CodeModel}, a
     * subsequent {@link Framework#compile} call resolves an injected {@link Provider} of
     * {@link CodeModel} to the {@link CodeModel} passed to that call, not to a previously created one.
     */
    @Test
    public void shouldResolveCurrentCodeModelAcrossMultipleCodeModels() {
        final var compiler = new Compiler<CodeModel>() {
            @Inject
            private Provider<CodeModel> codeModelProvider;

            @Override
            public void compile(final CodeModel target, final CodeModel codeModel, final TelemetryRecorder telemetryRecorder) {
            }

            public CodeModel injectedCodeModel() {
                return this.codeModelProvider.get();
            }
        };

        final var telemetryRecorder = ObservableTelemetryRecorder.of(NoOpTelemetryRecorder.create());
        final var builder = new FrameworkBuilder()
            .withFileSystem(FileSystems.getDefault())
            .withNameProvider(new CachingNameProvider(new NonCachingNameProvider()))
            .withPlugin(context -> context.inject(compiler));
        builder.bind(TelemetryRecorder.class).to(telemetryRecorder);

        final var framework = builder.build();

        final var firstCodeModel = framework.newCodeModel();
        final var secondCodeModel = framework.newCodeModel();

        framework.compile(secondCodeModel, telemetryRecorder).orElseThrow();
        assertThat(compiler.injectedCodeModel()).isSameAs(secondCodeModel);

        framework.compile(firstCodeModel, telemetryRecorder).orElseThrow();
        assertThat(compiler.injectedCodeModel()).isSameAs(firstCodeModel);
    }
}
