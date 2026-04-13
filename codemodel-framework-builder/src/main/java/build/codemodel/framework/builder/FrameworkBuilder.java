package build.codemodel.framework.builder;

/*-
 * #%L
 * Code Model Framework Builder
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.telemetry.TelemetryRecorder;
import build.base.telemetry.foundation.NoOpTelemetryRecorder;
import build.base.telemetry.foundation.ObservableTelemetryRecorder;
import build.codemodel.foundation.naming.CachingNameProvider;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.framework.Framework;
import build.codemodel.framework.Plugin;
import build.codemodel.injection.Binder;
import build.codemodel.injection.BindingBuilder;
import build.codemodel.injection.Context;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.injection.MultiBinder;
import build.codemodel.jdk.JDKCodeModel;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A builder of {@link Framework}s.
 *
 * @author brian.oliver
 * @since Apr-2024
 */
public class FrameworkBuilder
    implements Binder {

    /**
     * A {@link Function} to establish a {@link TelemetryRecorder}.
     */
    private Function<? super Context, ? extends TelemetryRecorder> telemetryRecorderBuilder;

    /**
     * A {@link Function} to establish a {@link FileSystem}.
     */
    private Function<? super Context, ? extends FileSystem> fileSystemBuilder;

    /**
     * A {@link Function} to establish a {@link NameProvider}.
     */
    private Function<? super Context, ? extends NameProvider> nameProviderBuilder;

    /**
     * {@link Function}s to establish {@link Plugin}s.
     */
    private final LinkedHashSet<Function<? super Context, ? extends Plugin>> pluginBuilders;

    /**
     * The {@link Context} with which to create {@link Plugin}s and the {@link Framework}.
     */
    private final Context context;

    /**
     * Constructs a default {@link FrameworkBuilder}.
     */
    public FrameworkBuilder() {
        this.telemetryRecorderBuilder = null;
        this.fileSystemBuilder = null;
        this.nameProviderBuilder = null;
        this.pluginBuilders = new LinkedHashSet<>();

        // establish a Context with which to create Plugins and Frameworks
        final var javaNameProvider = new CachingNameProvider(new NonCachingNameProvider());
        final var codeModel = new JDKCodeModel(javaNameProvider);
        final var injectionFramework = new InjectionFramework(codeModel);
        this.context = injectionFramework.newContext();
    }

    @Override
    public <T> BindingBuilder<T> bind(final Class<T> bindingClass) {
        return this.context.bind(bindingClass);
    }

    @Override
    public <T> MultiBinder<T> bindSet(final Class<T> type) {
        return this.context.bindSet(type);
    }

    /**
     * Specifies a {@link Function} to establish a {@link TelemetryRecorder} using a {@link Context}.
     *
     * @param builder the {@link Function} to establish a {@link TelemetryRecorder}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withTelemetryRecorder(final Function<? super Context, ? extends TelemetryRecorder> builder) {
        if (builder != null) {
            this.telemetryRecorderBuilder = builder;
        }
        return this;
    }

    /**
     * Specifies a {@link Supplier} to establish a {@link TelemetryRecorder}.
     *
     * @param supplier the {@link TelemetryRecorder} {@link Supplier}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withTelemetryRecorder(final Supplier<? extends TelemetryRecorder> supplier) {
        return supplier == null
            ? this
            : withTelemetryRecorder(context -> supplier.get());
    }

    /**
     * Specifies a {@link Function} to establish a {@link FileSystem} using a {@link Context}.
     *
     * @param builder the {@link Function} to establish a {@link FileSystem}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withFileSystem(final Function<? super Context, ? extends FileSystem> builder) {
        if (builder != null) {
            this.fileSystemBuilder = builder;
        }
        return this;
    }

    /**
     * Specifies a {@link Supplier} to establish a {@link FileSystem}.
     *
     * @param supplier the {@link FileSystem} {@link Supplier}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withFileSystem(final Supplier<? extends FileSystem> supplier) {
        return supplier == null
            ? this
            : withFileSystem(context -> supplier.get());
    }

    /**
     * Specifies the {@link FileSystem}.
     *
     * @param fileSystem the {@link FileSystem}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withFileSystem(final FileSystem fileSystem) {
        return withFileSystem(fileSystem == null ? null : () -> fileSystem);
    }

    /**
     * Specifies a {@link Function} to establish a {@link NameProvider} using a {@link Context}.
     *
     * @param builder the {@link Function} to establish a {@link NameProvider}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withNameProvider(final Function<? super Context, ? extends NameProvider> builder) {
        if (builder != null) {
            this.nameProviderBuilder = builder;
        }
        return this;
    }

    /**
     * Specifies a {@link Supplier} to establish a {@link NameProvider}.
     *
     * @param supplier the {@link NameProvider} {@link Supplier}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withNameProvider(final Supplier<? extends NameProvider> supplier) {
        return supplier == null
            ? this
            : withNameProvider(context -> supplier.get());
    }

    /**
     * Specifies the {@link NameProvider}.
     *
     * @param nameProvider the {@link NameProvider}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withNameProvider(final NameProvider nameProvider) {
        return withNameProvider(nameProvider == null ? null : () -> nameProvider);
    }

    /**
     * Specifies a {@link Function} to establish a {@link Plugin} using a {@link Context}.
     *
     * @param builder a {@link Plugin} {@link Function}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withPlugin(final Function<? super Context, ? extends Plugin> builder) {
        if (builder != null) {
            this.pluginBuilders.add(builder);
        }
        return this;
    }

    /**
     * Specifies a {@link Supplier} to establish a {@link Plugin}.
     *
     * @param supplier the {@link Plugin} {@link Supplier}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withPlugin(final Supplier<? extends Plugin> supplier) {
        return supplier == null
            ? this
            : withPlugin(context -> supplier.get());
    }

    /**
     * Specifies a {@link Class} of {@link Plugin}, to be established when a {@link Framework} is built.
     *
     * @param pluginClass the {@link Class} {@link Plugin}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withPlugin(final Class<? extends Plugin> pluginClass) {
        return pluginClass == null
            ? this
            : withPlugin(context -> context.create(pluginClass));
    }

    /**
     * Specifies the {@link ServiceLoader} from which to obtain {@link Plugin}s.
     *
     * @param serviceLoader the {@link ServiceLoader}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     * @see #withPlugin(Class)
     */
    public FrameworkBuilder withPlugins(final ServiceLoader<Plugin> serviceLoader) {
        if (serviceLoader != null) {
            serviceLoader.stream()
                .forEach(provider -> withPlugin(provider.type()));
        }

        return this;
    }

    /**
     * Specifies a {@link Plugin}.
     *
     * @param plugin a {@link Plugin}
     * @return this {@link FrameworkBuilder} to permit fluent-method invocation
     */
    public FrameworkBuilder withPlugin(final Plugin plugin) {
        return plugin == null
            ? this
            : withPlugin(() -> plugin);
    }

    /**
     * Builds a new {@link Framework} given the current state of the {@link FrameworkBuilder}.
     *
     * @return a new {@link Framework}
     */
    public Framework build() {

        // establish a new Context with which to create the Plugins
        final var context = this.context.newContext();

        // establish the FileSystem
        final var fileSystem = this.fileSystemBuilder == null
            ? FileSystems.getDefault()
            : this.fileSystemBuilder.apply(context);

        // establish the TelemetryRecorder
        final var telemetryRecorder = this.telemetryRecorderBuilder == null
            ? ObservableTelemetryRecorder.of(NoOpTelemetryRecorder.create())
            : this.telemetryRecorderBuilder.apply(context);

        // establish the NameProvider
        final var nameProvider = this.nameProviderBuilder == null
            ? new CachingNameProvider(new NonCachingNameProvider())
            : this.nameProviderBuilder.apply(context);

        context.bind(FileSystem.class).to(fileSystem);
        context.bind(NameProvider.class).to(nameProvider);

        // establish the Plugins
        final var plugins = this.pluginBuilders
            .stream()
            .filter(Objects::nonNull)
            .map(builder -> builder.apply(context));

        return new InternalFramework(fileSystem, nameProvider, plugins);
    }
}
