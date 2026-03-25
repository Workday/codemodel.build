package build.framework.builder;

import build.codemodel.framework.initialization.Enricher;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Enricher}s.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
class EnricherTests {

    /**
     * A no-operation {@link Trait} for testing.
     */
    public static class NoopTrait
        implements Trait {

    }

    /**
     * A {@link Class}-based {@link Enricher}
     */
    public static class ClassBasedEnricher
        implements Enricher<Traitable, NoopTrait> {

        @Override
        public Stream<NoopTrait> create(final Traitable target) {
            return Stream.empty();
        }
    }

    /**
     * A <i>raw-type</i> based {@link Enricher}
     */
    @SuppressWarnings("rawtypes")
    public static class RawTypeBasedEnricher
        implements Enricher {

        @Override
        public Stream create(final Traitable target) {
            return Stream.empty();
        }
    }

    /**
     * A <i>generic-type</i> based {@link Enricher}
     */
    public static class ParameterizedEnricher<T extends Traitable, E extends Trait>
        implements Enricher<T, E> {

        @Override
        public Stream<E> create(final T target) {
            return Stream.empty();
        }
    }

    /**
     * Ensure the {@link Enricher#getTargetClass()} can be automatically determined.
     */
    @Test
    void shouldAutomaticallyDetermineTargetClassForConcreteClass() {
        final var enricher = new ClassBasedEnricher();

        assertTrue(enricher.getTargetClass().isPresent());
        assertEquals(enricher.getTargetClass().orElseThrow(), Traitable.class);
    }

    /**
     * Ensure the {@link Enricher#getTargetClass()} cannot be determined for <i>raw-type</i>.
     */
    @Test
    void shouldNotAutomaticallyDetermineTargetClassForRawClass() {
        final var enricher = new RawTypeBasedEnricher();

        assertTrue(enricher.getTargetClass().isEmpty());
    }

    /**
     * Ensure the {@link Enricher#getTargetClass()} cannot be determined for <i>parameterized-type</i>.
     */
    @Test
    void shouldNotAutomaticallyDetermineTargetClassForParameterizedClass() {
        final var enricher = new ParameterizedEnricher<>();

        assertTrue(enricher.getTargetClass().isEmpty());
    }
}
