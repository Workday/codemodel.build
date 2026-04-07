package build.codemodel.injection;

import build.base.configuration.AbstractValueOption;
import build.base.configuration.Default;
import build.base.configuration.Option;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultOptionResolver}.
 *
 * @author reed.vonredwitz
 */
class DefaultOptionResolverTests
    implements ContextualTesting {

    /**
     * Verifies that an {@link Option} with a {@link Default}-annotated static factory method is
     * instantiated and injected.
     */
    @Test
    void shouldResolveOptionWithDefaultStaticMethod() {
        final var framework = createInjectionFramework();
        final var context = framework.newContext(DefaultOptionResolver.of(framework));
        context.bind(ServiceWithMethodDefault.class).to(ServiceWithMethodDefault.class);

        final var service = context.create(ServiceWithMethodDefault.class);

        assertThat(service.option).isNotNull();
        assertThat(service.option.get()).isEqualTo("method-default");
    }

    /**
     * Verifies that an {@link Option} with a {@link Default}-annotated no-arg constructor is
     * instantiated and injected.
     */
    @Test
    void shouldResolveOptionWithDefaultConstructor() {
        final var framework = createInjectionFramework();
        final var context = framework.newContext(DefaultOptionResolver.of(framework));
        context.bind(ServiceWithConstructorDefault.class).to(ServiceWithConstructorDefault.class);

        final var service = context.create(ServiceWithConstructorDefault.class);

        assertThat(service.option).isNotNull();
        assertThat(service.option.get()).isEqualTo("constructor-default");
    }

    /**
     * Verifies that an {@link Option} with a {@link Default}-annotated static field is
     * instantiated and injected.
     */
    @Test
    void shouldResolveOptionWithDefaultStaticField() {
        final var framework = createInjectionFramework();
        final var context = framework.newContext(DefaultOptionResolver.of(framework));
        context.bind(ServiceWithFieldDefault.class).to(ServiceWithFieldDefault.class);

        final var service = context.create(ServiceWithFieldDefault.class);

        assertThat(service.option).isNotNull();
        assertThat(service.option.get()).isEqualTo("field-default");
    }

    /**
     * Verifies that an {@link Option} with no {@link Default} factory causes {@link DefaultOptionResolver}
     * to return empty, leaving the dependency unresolved.
     */
    @Test
    void shouldReturnEmptyWhenNoDefaultAnnotationPresent() {
        final var framework = createInjectionFramework();
        final var resolver = DefaultOptionResolver.of(framework);

        final var typeUsage = framework.codeModel().getTypeUsage(OptionWithNoDefault.class);
        final var dependency = IndependentDependency.of(typeUsage, _ -> Stream.empty());

        assertThat(resolver.resolve(dependency)).isEmpty();
    }

    /**
     * Verifies that non-{@link Option} types are not resolved by {@link DefaultOptionResolver}.
     */
    @Test
    void shouldNotResolveNonOptionTypes() {
        final var framework = createInjectionFramework();

        // add a fallback for String so creation doesn't fail
        final var context = framework.newContext(
            DefaultOptionResolver.of(framework),
            dep -> Optional.of(new ValueBinding<Object>() {
                @Override public Object value() { return "fallback"; }
                @Override public Dependency dependency() { return dep; }
            }));

        context.bind(ServiceWithString.class).to(ServiceWithString.class);

        // should succeed via fallback resolver
        final var service = context.create(ServiceWithString.class);
        assertThat(service.value).isEqualTo("fallback");
    }

    // --- fixtures ---

    static class ServiceWithFieldDefault {
        @Inject
        OptionWithFieldDefault option;
    }

    static class ServiceWithMethodDefault {
        @Inject
        OptionWithMethodDefault option;
    }

    static class ServiceWithConstructorDefault {
        @Inject
        OptionWithConstructorDefault option;
    }

    static class ServiceWithString {
        @Inject
        String value;
    }

    public static class OptionWithMethodDefault
        extends AbstractValueOption<String> {

        OptionWithMethodDefault(final String value) {
            super(value);
        }

        @Default
        public static OptionWithMethodDefault create() {
            return new OptionWithMethodDefault("method-default");
        }
    }

    public static class OptionWithConstructorDefault
        extends AbstractValueOption<String> {

        @Default
        public OptionWithConstructorDefault() {
            super("constructor-default");
        }
    }

    public static class OptionWithFieldDefault
        extends AbstractValueOption<String> {

        @Default
        public static final OptionWithFieldDefault INSTANCE = new OptionWithFieldDefault("field-default");

        OptionWithFieldDefault(final String value) {
            super(value);
        }
    }

    public static class OptionWithNoDefault
        extends AbstractValueOption<String> {

        public OptionWithNoDefault(final String value) {
            super(value);
        }
    }
}
