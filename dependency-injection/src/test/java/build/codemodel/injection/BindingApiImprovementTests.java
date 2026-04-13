package build.codemodel.injection;

/*-
 * #%L
 * Dependency Injection
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

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the Binding API Improvements: {@link BindingBuilder#toOverriding} variants and
 * {@link BindingBuilder#asAllInterfaces} / {@link BindingBuilder#asAllInterfaces(java.util.function.Predicate)}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class BindingApiImprovementTests
    implements ContextualTesting {

    // ---- test fixtures ----

    interface Greeter {
        String greet();
    }

    interface Farewell {
        String farewell();
    }

    static class HelloWorld
        implements Greeter, Farewell, Comparable<HelloWorld> {

        @Override
        public String greet() {
            return "hello";
        }

        @Override
        public String farewell() {
            return "goodbye";
        }

        @Override
        public int compareTo(final HelloWorld o) {
            return 0;
        }
    }

    static class GreeterHolder {
        @Inject
        Greeter greeter;
    }

    static class FarewellHolder {
        @Inject
        Farewell farewell;
    }

    // ---- toOverriding: value ----

    /**
     * Ensures {@link BindingBuilder#toOverriding(Object)} replaces an existing binding without throwing.
     */
    @Test
    void shouldOverrideExistingBindingWithValue() {
        final var context = createInjectionFramework().newContext();
        context.bind(String.class).to("original");

        context.bind(String.class).toOverriding("override");

        assertThat(context.create(String.class)).isEqualTo("override");
    }

    /**
     * Ensures {@link BindingBuilder#toOverriding(Object)} acts as a normal bind when no prior binding exists.
     */
    @Test
    void shouldBindNormallyWhenNoExistingBindingForValueOverride() {
        final var context = createInjectionFramework().newContext();

        assertThatCode(() -> context.bind(String.class).toOverriding("value"))
            .doesNotThrowAnyException();

        assertThat(context.create(String.class)).isEqualTo("value");
    }

    // ---- toOverriding: class ----

    /**
     * Ensures {@link BindingBuilder#toOverriding(Class)} replaces an existing class binding without throwing.
     */
    @Test
    void shouldOverrideExistingBindingWithClass() {
        final var context = createInjectionFramework().newContext();
        context.bind(CharSequence.class).to(String.class);

        context.bind(CharSequence.class).toOverriding(StringBuilder.class);

        assertThat(context.create(CharSequence.class)).isInstanceOf(StringBuilder.class);
    }

    // ---- toOverriding: supplier ----

    /**
     * Ensures {@link BindingBuilder#toOverriding(Supplier)} replaces an existing binding without throwing.
     */
    @Test
    void shouldOverrideExistingBindingWithSupplier() {
        final var context = createInjectionFramework().newContext();
        context.bind(String.class).to("original");

        context.bind(String.class).toOverriding(() -> "supplied override");

        assertThat(context.create(String.class)).isEqualTo("supplied override");
    }

    // ---- toOverriding: existing bind still throws without override ----

    /**
     * Ensures that a plain {@link BindingBuilder#to(Object)} still throws when a binding exists,
     * so the toOverriding variants are genuinely distinct.
     */
    @Test
    void shouldStillThrowWithoutOverride() {
        final var context = createInjectionFramework().newContext();
        context.bind(String.class).to("original");

        assertThatThrownBy(() -> context.bind(String.class).to("duplicate"))
            .isInstanceOf(BindingAlreadyExistsException.class);
    }

    // ---- asAllInterfaces ----

    /**
     * Ensures {@link Context#bind(Object)} followed by {@link BindingBuilder#asAllInterfaces()} registers
     * a binding for each non-java interface of the instance's type.
     */
    @Test
    void shouldBindAllNonJavaInterfacesOfInstance() {
        final var context = createInjectionFramework().newContext();
        final var impl = new HelloWorld();

        context.bind(impl).asAllInterfaces();

        assertThat(context.inject(new GreeterHolder()).greeter).isSameAs(impl);
        assertThat(context.inject(new FarewellHolder()).farewell).isSameAs(impl);
    }

    /**
     * Ensures the default {@link BindingBuilder#asAllInterfaces()} filter excludes {@code java.*} interfaces
     * ({@link Comparable} in this case) from registration.
     */
    @Test
    void shouldNotBindJavaInterfacesByDefault() {
        final var context = createInjectionFramework().newContext();
        final var impl = new HelloWorld();

        context.bind(impl).asAllInterfaces();

        assertThatThrownBy(() -> context.create(Comparable.class))
            .isInstanceOf(UnsatisfiedDependencyException.class);
    }

    /**
     * Ensures {@link BindingBuilder#asAllInterfaces(java.util.function.Predicate)} respects a custom filter,
     * allowing only types matching the predicate.
     */
    @Test
    void shouldRespectCustomFilterForAsAllInterfaces() {
        final var context = createInjectionFramework().newContext();
        final var impl = new HelloWorld();

        // only bind Greeter, not Farewell
        context.bind(impl).asAllInterfaces(iface -> iface == Greeter.class);

        assertThat(context.inject(new GreeterHolder()).greeter).isSameAs(impl);
        assertThatThrownBy(() -> context.create(Farewell.class))
            .isInstanceOf(UnsatisfiedDependencyException.class);
    }
}
