package build.codemodel.dependency.injection;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for binding and injecting fully-parameterized generic types via {@link TypeLiteral}.
 *
 * @author brian.oliver
 * @since Apr-2026
 */
class TypeLiteralBindingTests
    implements ContextualTesting {

    // ---- test fixtures ----

    record Person(String name) {
    }

    record Car(String make) {
    }

    static class PeopleHolder {
        @Inject
        List<Person> people;
    }

    static class PeopleAndCarsHolder {
        @Inject
        List<Person> people;

        @Inject
        List<Car> cars;
    }

    @SuppressWarnings("rawtypes")
    static class RawTypeLiteral
        extends TypeLiteral {
    }

    // ---- binding and injecting a parameterized type ----

    /**
     * Ensures a value bound via {@link TypeLiteral} is injectable into a matching {@code List<Person>}
     * field, matching the exact usage described in the originating issue.
     */
    @Test
    void shouldInjectValueBoundViaTypeLiteral() {
        final var context = createInjectionFramework().newContext();
        final var people = List.of(new Person("Alice"), new Person("Bob"));

        context.bind(new TypeLiteral<List<Person>>() {
        }).to(people);

        final var holder = context.inject(new PeopleHolder());

        assertThat(holder.people)
            .isSameAs(people);
    }

    /**
     * Ensures differently-parameterized usages of the same raw type ({@code List<Person>} vs
     * {@code List<Car>}) are treated as distinct bindings, rather than colliding on their shared raw type.
     */
    @Test
    void shouldDistinguishDifferentlyParameterizedBindings() {
        final var context = createInjectionFramework().newContext();
        final var people = List.of(new Person("Alice"));
        final var cars = List.of(new Car("Tesla"));

        context.bind(new TypeLiteral<List<Person>>() {
        }).to(people);
        context.bind(new TypeLiteral<List<Car>>() {
        }).to(cars);

        final var holder = context.inject(new PeopleAndCarsHolder());

        assertThat(holder.people)
            .isSameAs(people);
        assertThat(holder.cars)
            .isSameAs(cars);
    }

    /**
     * Ensures binding the same {@link TypeLiteral} twice throws {@link BindingAlreadyExistsException}, just
     * as {@link Binder#bind(Class)} does.
     */
    @Test
    void shouldThrowWhenBindingSameTypeLiteralTwice() {
        final var context = createInjectionFramework().newContext();
        context.bind(new TypeLiteral<List<Person>>() {
        }).to(List.of());

        assertThatThrownBy(() -> context.bind(new TypeLiteral<List<Person>>() {
        }).to(List.of()))
            .isInstanceOf(BindingAlreadyExistsException.class);
    }

    // ---- TypeLiteral construction guard ----

    /**
     * Ensures constructing a {@link TypeLiteral} without a concrete type argument (a raw subclass) throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void shouldThrowWhenConstructedWithoutTypeArgument() {
        assertThatThrownBy(RawTypeLiteral::new)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
