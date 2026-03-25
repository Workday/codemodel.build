package build.codemodel.injection;

import build.codemodel.injection.example.SystemPropertyInjectablePerson;
import jakarta.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SystemProperty} injection.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
class SystemPropertyInjectionTests
    implements ContextualTesting {

    /**
     * Ensure a {@link Context} can create and inject values into a class that has no default constructor and only
     * private {@link Inject} {@link SystemProperty} annotated fields.
     */
    @Test
    void shouldCreateAndInjectIntoFieldsWithoutDefaultConstructorUsingSystemProperties() {
        // establish a context for injection
        final var injectionFramework = createInjectionFramework();
        final var context = injectionFramework.newContext();

        // include the ability to resolve and inject SystemProperty-based values
        context.addResolver(SystemPropertyResolver.of(injectionFramework.codeModel()));

        System.setProperty("FirstName", "Harry");
        System.setProperty("LastName", "Potter");

        final var person = context.create(SystemPropertyInjectablePerson.class);

        assertThat(person.firstName())
            .isEqualTo("Harry");

        assertThat(person.lastName())
            .isEqualTo("Potter");

        assertThat(person.age())
            .isEqualTo(42);

        System.clearProperty("FirstName");
        System.clearProperty("LastName");
    }

    /**
     * Ensure a {@link Context} will fail to inject values into a class when required system properties are unavailable.
     */
    @Test
    void shouldFailToInjectWithMissingSystemProperty() {
        // establish a context for injection
        final var injectionFramework = createInjectionFramework();
        final var context = injectionFramework.newContext();

        // include the ability to resolve and inject SystemProperty-based values
        context.addResolver(SystemPropertyResolver.of(injectionFramework.codeModel()));

        assertThrows(UnsatisfiedDependencyException.class, () -> {

            System.setProperty("LastName", "Potter");

            final var person = context.create(SystemPropertyInjectablePerson.class);

            System.clearProperty("LastName");

            fail();
        });
    }
}
