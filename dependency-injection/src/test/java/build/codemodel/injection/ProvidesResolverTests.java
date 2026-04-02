package build.codemodel.injection;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProvidesResolver}.
 *
 * @author reed.vonredwitz
 */
class ProvidesResolverTests
    implements ContextualTesting {

    /**
     * Verifies that a value produced by a {@link Provides}-annotated method is resolvable and injected.
     */
    @Test
    void shouldResolveValueFromProvidesMethod() {
        final var framework = createInjectionFramework();

        final var context = framework.newContext(ProvidesResolver.of(new GreetingProvider(), framework));
        context.bind(GreetingService.class).to(GreetingService.class);

        final var service = context.create(GreetingService.class);

        assertThat(service.greeting).isEqualTo("Hello from @Provides");
    }

    /**
     * Verifies that a superclass {@link Provides} method is discovered when scanning the hierarchy.
     */
    @Test
    void shouldResolveValueFromSuperclassProvidesMethod() {
        final var framework = createInjectionFramework();

        final var context = framework.newContext(ProvidesResolver.of(new ExtendedProvider(), framework));
        context.bind(GreetingService.class).to(GreetingService.class);

        final var service = context.create(GreetingService.class);

        assertThat(service.greeting).isEqualTo("Hello from @Provides");
    }

    /**
     * Verifies that a {@link ProvidesResolver} with no matching method returns empty.
     */
    @Test
    void shouldReturnEmptyWhenNoProvidesMethodMatchesDependency() {
        final var framework = createInjectionFramework();

        final var resolver = ProvidesResolver.of(new EmptyProvider(), framework);

        // EmptyProvider has no @Provides for String
        final var dependency = IndependentDependency.of(
            framework.codeModel().getTypeUsage(String.class),
            _ -> java.util.stream.Stream.empty());

        final Optional<?> result = resolver.resolve(dependency);
        assertThat(result).isEmpty();
    }

    /**
     * Verifies that a {@link Provides}-annotated method with a {@code void} return type is silently
     * ignored during construction and does not cause an error or a spurious registration.
     */
    @Test
    void shouldIgnoreVoidProvidesMethod() {
        final var framework = createInjectionFramework();

        // construction must not throw even though @Provides is on a void method
        final var resolver = ProvidesResolver.of(new VoidProvider(), framework);

        // nothing should be registered, so any dependency comes back empty
        final var dependency = IndependentDependency.of(
            framework.codeModel().getTypeUsage(String.class),
            _ -> java.util.stream.Stream.empty());

        assertThat(resolver.resolve(dependency)).isEmpty();
    }

    // --- fixtures ---

    static class GreetingService {
        @Inject
        String greeting;
    }

    static class GreetingProvider {
        @Provides
        public String greeting() {
            return "Hello from @Provides";
        }

        public String notProvides() {
            return "not annotated";
        }
    }

    static class BaseProvider {
        @Provides
        public String greeting() {
            return "Hello from @Provides";
        }
    }

    static class ExtendedProvider extends BaseProvider {
        // inherits the @Provides method
    }

    static class EmptyProvider {
        public String notAnnotated() {
            return "no @Provides here";
        }
    }

    static class VoidProvider {
        @Provides
        public void doNothing() {
            // void return type — must be silently ignored by ProvidesResolver
        }
    }
}
