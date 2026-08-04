package build.codemodel.dependency.injection;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

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
            _ -> Stream.empty());

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
            _ -> Stream.empty());

        assertThat(resolver.resolve(dependency)).isEmpty();
    }

    /**
     * Verifies that two {@link Provides} methods returning the same type but distinguished by different
     * {@code @Named} qualifiers are resolved independently, rather than the second registration being
     * dropped or the first method's value being returned for both qualifiers.
     */
    @Test
    void shouldResolveDistinctValuesForDifferentlyQualifiedProvidesMethods() {
        final var framework = createInjectionFramework();

        final var context = framework.newContext(ProvidesResolver.of(new QualifiedGreetingProvider(), framework));
        context.bind(QualifiedGreetingService.class).to(QualifiedGreetingService.class);

        final var service = context.create(QualifiedGreetingService.class);

        assertThat(service.englishGreeting).isEqualTo("Hello");
        assertThat(service.frenchGreeting).isEqualTo("Bonjour");
    }

    /**
     * Verifies that a concrete method overriding an {@code abstract} {@link Provides}-annotated method is
     * itself treated as {@link Provides}, even though it does not repeat the annotation. An {@code abstract}
     * method can't be "opted out of" the way an {@code @Inject} method can - every concrete subclass must
     * supply an override - so the {@link Provides} contract carries through to whichever override is
     * actually invoked.
     */
    @Test
    void shouldResolveValueFromConcreteOverrideOfAbstractProvidesMethod() {
        final var framework = createInjectionFramework();

        final var context = framework.newContext(ProvidesResolver.of(new ConcreteGreetingProvider(), framework));
        context.bind(GreetingService.class).to(GreetingService.class);

        final var service = context.create(GreetingService.class);

        assertThat(service.greeting).isEqualTo("Hello from concrete override");
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

    abstract static class AbstractGreetingProvider {
        @Provides
        public abstract String greeting();
    }

    static class ConcreteGreetingProvider extends AbstractGreetingProvider {
        @Override
        public String greeting() {
            return "Hello from concrete override";
        }
    }

    static class QualifiedGreetingService {
        @Inject
        @Named("English")
        String englishGreeting;

        @Inject
        @Named("French")
        String frenchGreeting;
    }

    static class QualifiedGreetingProvider {
        @Provides
        @Named("English")
        public String english() {
            return "Hello";
        }

        @Provides
        @Named("French")
        public String french() {
            return "Bonjour";
        }
    }
}
