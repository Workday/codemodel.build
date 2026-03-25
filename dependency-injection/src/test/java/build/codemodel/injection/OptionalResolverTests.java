package build.codemodel.injection;

import jakarta.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OptionalResolver}.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
class OptionalResolverTests
    implements ContextualTesting {

    /**
     * Ensure that {@link Optional} fields are injected.
     */
    @Test
    void shouldInjectOptionalFields() {

        final var context = createInjectionFramework()
            .newContext();

        context.addResolver(OptionalResolver.of(String.class, "Hello"));
        context.addResolver(OptionalResolver.of(Integer.class, 42));
        context.addResolver(OptionalResolver.empty(Double.class));

        final var injectable = context.create(Injectable.class);

        assertThat(injectable.optionalString)
            .isPresent()
            .hasValue("Hello");

        assertThat(injectable.optionalInteger)
            .isPresent()
            .hasValue(42);

        assertThat(injectable.optionalEmpty)
            .isEmpty();
    }

    /**
     * An simple {@link Inject}able type containing various {@link Optional} fields.
     */
    static class Injectable {

        @Inject
        public Optional<String> optionalString;

        @Inject
        public Optional<Integer> optionalInteger;

        @Inject
        public Optional<Double> optionalEmpty;
    }
}