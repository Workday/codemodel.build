package build.codemodel.injection;

import build.codemodel.foundation.usage.TypeUsage;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UnsatisfiedDependencyException}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class UnsatisfiedDependencyExceptionTests {

    /**
     * A minimal {@link Dependency} for use in tests.
     */
    private static Dependency dependency(final String signature) {
        return new Dependency() {
            @Override
            public TypeUsage typeUsage() {
                return null;
            }

            @Override
            public String signature() {
                return signature;
            }

            @Override
            public String toString() {
                return signature;
            }
        };
    }

    /**
     * Ensures the two-arg constructor preserves the cause.
     */
    @Test
    void shouldPreserveCauseInTwoArgConstructor() {
        final var cause = new RuntimeException("root cause");
        final var exception = new UnsatisfiedDependencyException(dependency("some.Type"), cause);

        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getMessage()).contains("some.Type");
    }

    /**
     * Ensures the three-arg constructor (dependency, message, cause) preserves the cause.
     * Previously the cause was silently dropped.
     */
    @Test
    void shouldPreserveCauseInThreeArgConstructor() {
        final var cause = new RuntimeException("root cause");
        final var exception = new UnsatisfiedDependencyException(
            dependency("some.Type"), "extra detail", cause);

        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getMessage()).contains("some.Type");
        assertThat(exception.getMessage()).contains("extra detail");
    }

    /**
     * Ensures the dependency is always accessible via {@link UnsatisfiedDependencyException#dependency()}.
     */
    @Test
    void shouldExposeTheDependency() {
        final var dep = dependency("some.Type");
        final var exception = new UnsatisfiedDependencyException(dep, "detail", new RuntimeException());

        assertThat(exception.dependency()).isSameAs(dep);
    }
}
