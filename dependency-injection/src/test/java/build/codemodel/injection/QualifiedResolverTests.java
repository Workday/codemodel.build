package build.codemodel.injection;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link QualifiedResolver}.
 *
 * @author spencer.firestone
 * @since Jan-2020
 */
class QualifiedResolverTest
    implements ContextualTesting {

    /**
     * The {@link Context} used for testing qualified injection.
     */
    private Context context;

    /**
     * The {@link Foo} for testing qualified injection.
     */
    private Foo foo;

    /**
     * Establishes the {@link Context} for each test.
     */
    @BeforeEach
    void createContext() {
        this.context = createInjectionFramework()
            .newContext();

        this.foo = new Foo.Impl("Foobar?");
        this.context.addResolver(QualifiedResolver.of(FooQualifier.class, this.foo));
    }

    /**
     * Ensure that a qualified {@link Object} can be resolved.
     */
    @Test
    void shouldResolveQualifiedObject() {
        final var instance = this.context.create(ClassWithInjectedFooObject.class);

        assertThat(this.foo)
            .isSameAs(instance.foo);
    }

    /**
     * Ensure that a qualified interface can be resolved.
     */
    @Test
    void shouldResolveQualifiedInterface() {
        final var instance = this.context.create(ClassWithInjectedFooInterface.class);

        assertThat(this.foo)
            .isSameAs(instance.foo);
    }

    /**
     * Ensure that a qualified implementation can be resolved.
     */
    @Test
    void shouldResolveQualifiedImplementation() {
        final var instance = this.context.create(ClassWithInjectedFooImplementation.class);

        assertThat(this.foo)
            .isSameAs(instance.foo);
    }

    /**
     * Ensure that an invalid qualified injection point will fail fast.
     */
    @Test
    void shouldFailFastOnResolvingInvalidQualifiedInjectionIfSetToFailFast() {
        assertThrows(InjectionException.class, () -> this.context.create(ClassWithInvalidFooInjection.class),
            "Binding to invalid object should fail");
    }

    /**
     * Ensure that multiple qualified injections and a non-qualified injection of the same type get resolved properly
     * (this IS dependent on the ordering of the resolvers).
     */
    @Test
    void shouldResolveMultipleQualifiers() {
        final var injectedString = "Hello world";
        this.context.addResolver(QualifiedResolver.of(StringQualifier.class, injectedString));

        final var injectedLong = 10L;
        this.context.bind(long.class).to(injectedLong);

        final var instance = this.context.create(ClassWithMultipleInjections.class);

        assertThat(this.foo)
            .isSameAs(instance.injectedFoo);

        assertThat(injectedString)
            .isSameAs(instance.injectedString);

        assertThat(injectedLong)
            .isEqualTo(instance.injectedLong);
    }

    /**
     * A simple class which contains a {@link FooQualifier} annotation as an {@link Object}.
     */
    private static class ClassWithInjectedFooObject {

        @Inject
        @FooQualifier
        private Object foo;
    }

    /**
     * A simple class which contains a {@link FooQualifier} annotation as a {@link Foo} interface.
     */
    private static class ClassWithInjectedFooInterface {

        @Inject
        @FooQualifier
        private Foo foo;
    }

    /**
     * A simple class which contains a {@link FooQualifier} annotation as a {@link Foo.Impl} implementation.
     */
    private static class ClassWithInjectedFooImplementation {

        @Inject
        @FooQualifier
        private Foo.Impl foo;
    }

    /**
     * A simple class which contains a {@link FooQualifier} annotation as a {@link Number}.
     */
    private static class ClassWithInvalidFooInjection {

        @Inject
        @FooQualifier
        private Number number;
    }

    /**
     * A simple class which contains multiple qualified injections and one non-qualified injection.
     */
    private static class ClassWithMultipleInjections {

        @Inject
        private long injectedLong;

        @Inject
        @FooQualifier
        private Object injectedFoo;

        @Inject
        @StringQualifier
        private Object injectedString;
    }

    /**
     * {@link Qualifier} for {@link Foo}.
     */
    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ METHOD, CONSTRUCTOR, FIELD })
    @Qualifier
    private @interface FooQualifier {

    }

    /**
     * {@link Qualifier} for {@link String}.
     */
    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ METHOD, CONSTRUCTOR, FIELD })
    @Qualifier
    private @interface StringQualifier {

    }

    private interface Foo {

        String getValue();

        class Impl
            implements Foo {

            private String value;

            Impl(final String value) {
                this.value = value;
            }

            @Override
            public String getValue() {
                return this.value;
            }
        }
    }
}