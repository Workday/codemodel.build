package build.codemodel.injection;

import build.base.configuration.AbstractValueOption;
import build.base.configuration.Configuration;
import build.base.configuration.Default;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationResolver}.
 *
 * @author brian.oliver
 * @since Dec-2018
 */
class ConfigurationResolverTests
    implements ContextualTesting {

    /**
     * Test the creation and usage of a {@link Context} with a {@link ConfigurationResolver}.
     */
    @Test
    void shouldResolveAndInjectOption() {
        final var resourceName = "testResource";
        final Configuration configuration = Configuration.of(new Resource(resourceName));

        final Context context = createInjectionFramework()
            .newContext(ConfigurationResolver.of(configuration));

        context.bind(Service.class)
            .to(Service.class);

        final Service service = context.create(Service.class);

        assertThat(service.resource.get())
            .isEqualTo(resourceName);

        assertThat(service.configuration)
            .isSameAs(configuration);
    }

    /**
     * Tests that {@link ConfigurationResolver}s do resolve default options.
     */
    @Test
    void shouldResolveDefaultOptions() {
        final Context context = createInjectionFramework()
            .newContext(ConfigurationResolver.of(Configuration.empty()));

        context.bind(DefaultOptionService.class)
            .to(DefaultOptionService.class);

        assertThat(context.create(DefaultOptionService.class).optionWithDefault)
            .isEqualTo(OptionWithDefault.autodetect());
    }

    /**
     * A stub "service" that is initialized via constructor injection.
     */
    static class Service {

        /**
         * Injected {@link Resource} from a {@link Configuration}.
         */
        private final Resource resource;

        /**
         * Injected {@link Configuration}.
         */
        private final Configuration configuration;

        /**
         * Construct a Service.
         *
         * @param resource the resource this service depends on
         */
        @Inject
        Service(final Resource resource,
                final Configuration configuration) {

            this.resource = resource;
            this.configuration = configuration;
        }

        @Override
        public String toString() {
            return String.format("Service[%s]", this.resource);
        }
    }

    /**
     * A stub named "resource" for testing injection.
     */
    static class Resource
        extends AbstractValueOption<String> {

        /**
         * Construct a {@link Resource}.
         *
         * @param name resource mame
         */
        Resource(final String name) {
            super(name);
        }
    }

    /**
     * A {@link build.base.configuration.ValueOption} with a default.
     */
    public static class OptionWithDefault
        extends AbstractValueOption<String> {

        OptionWithDefault(final String value)
            throws NullPointerException {
            super(value);
        }

        @Default
        public static OptionWithDefault autodetect() {
            return new OptionWithDefault("default");
        }
    }

    /**
     * A class that injects an {@link OptionWithDefault}.
     */
    static class DefaultOptionService {

        @Inject
        private OptionWithDefault optionWithDefault;
    }
}
