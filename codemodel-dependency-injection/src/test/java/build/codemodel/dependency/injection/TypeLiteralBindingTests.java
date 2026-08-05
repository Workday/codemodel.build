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
import jakarta.inject.Named;
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

    // ---- resolution against a wildcard-bounded dependency ----

    static class Base {
    }

    static class Impl
        extends Base {
    }

    static class Sibling
        extends Base {
    }

    static class Unrelated {
    }

    static class WildcardHolder {
        @Inject
        Class<? extends Base> type;
    }

    /**
     * An exact match on the wildcard-bearing dependency itself must win outright, even when a separate,
     * otherwise wildcard-compatible binding (e.g. {@code Class<Impl>}) is also registered.
     */
    @Test
    void shouldPreferExactMatchOverWildcardCompatibleCandidate() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<? extends Base>>() {
        }).to(Sibling.class);
        context.bind(new TypeLiteral<Class<Impl>>() {
        }).to(Impl.class);

        final var holder = context.inject(new WildcardHolder());

        assertThat(holder.type)
            .isEqualTo(Sibling.class);
    }

    /**
     * A raw {@code Class} binding carries no type argument to conflict with a requested wildcard bound, so it
     * satisfies a {@code Class<? extends Base>} injection point.
     */
    @Test
    void shouldSatisfyWildcardWithRawClassBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(Class.class).to(Impl.class);

        final var holder = context.inject(new WildcardHolder());

        assertThat(holder.type)
            .isEqualTo(Impl.class);
    }

    /**
     * A binding for a concrete {@code Class<Impl>} satisfies a {@code Class<? extends Base>} injection point
     * when {@code Impl} is assignable to {@code Base}; an unrelated concrete binding is filtered out.
     */
    @Test
    void shouldSatisfyWildcardWithAssignableConcreteBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<Impl>>() {
        }).to(Impl.class);
        context.bind(new TypeLiteral<Class<Unrelated>>() {
        }).to(Unrelated.class);

        final var holder = context.inject(new WildcardHolder());

        assertThat(holder.type)
            .isEqualTo(Impl.class);
    }

    /**
     * A binding registered against a wildcard-bearing type itself (e.g. {@code Class<? extends Impl>}) should
     * still satisfy a {@code Class<? extends Base>} injection point when the candidate's bound is assignable
     * to the requested bound.
     */
    @Test
    void shouldSatisfyWildcardWithWildcardCandidateBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<? extends Impl>>() {
        }).to(Impl.class);

        final var holder = context.inject(new WildcardHolder());

        assertThat(holder.type)
            .isEqualTo(Impl.class);
    }

    /**
     * A {@code super}-bounded candidate wildcard must never satisfy an {@code extends}-bounded request, per
     * <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.5.1">JLS 4.5.1</a> type
     * argument containment - even when the lower bound chains to the requested upper bound.
     */
    @Test
    void shouldNotSatisfyWildcardWithLowerBoundedWildcardCandidateBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<? super Impl>>() {
        }).to(Impl.class);

        assertThatThrownBy(() -> context.inject(new WildcardHolder()))
            .isInstanceOf(UnsatisfiedDependencyException.class);
    }

    /**
     * Reinforces {@link #shouldNotSatisfyWildcardWithLowerBoundedWildcardCandidateBinding} with a lower bound
     * unrelated to the requested upper bound, ruling out a coincidental pass.
     */
    @Test
    void shouldNotSatisfyWildcardWithIncompatibleLowerBoundedWildcardCandidateBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<? super Unrelated>>() {
        }).to(Unrelated.class);

        assertThatThrownBy(() -> context.inject(new WildcardHolder()))
            .isInstanceOf(UnsatisfiedDependencyException.class);
    }

    // ---- wildcard resolution combined with qualifiers ----

    static class QualifiedWildcardHolder {
        @Inject
        @Named("preferred")
        Class<? extends Base> type;
    }

    /**
     * The wildcard-compatibility fallback must still honor qualifiers: a {@code @Named("preferred")} request
     * is satisfied by a wildcard-compatible candidate that carries the *same* qualifier, even though neither
     * side's TypeUsage is an exact structural match on its own.
     */
    @Test
    void shouldSatisfyQualifiedWildcardWithMatchingQualifiedCompatibleBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<Impl>>() {
        }).as("preferred").to(Impl.class);

        final var holder = context.inject(new QualifiedWildcardHolder());

        assertThat(holder.type)
            .isEqualTo(Impl.class);
    }

    /**
     * A structurally wildcard-compatible candidate whose qualifiers don't match the request must never be
     * used to satisfy it - the differently-qualified {@code Class<Impl>} binding here must not silently stand
     * in for a {@code @Named("preferred")} request just because the raw types are compatible.
     */
    @Test
    void shouldNotSatisfyQualifiedWildcardWithDifferentlyQualifiedCompatibleBinding() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<Impl>>() {
        }).to(Impl.class);

        assertThatThrownBy(() -> context.inject(new QualifiedWildcardHolder()))
            .isInstanceOf(UnsatisfiedDependencyException.class);
    }

    /**
     * Two structurally wildcard-compatible candidates that carry <em>different</em> qualifiers must not be
     * treated as an ambiguous pair for an unqualified request - only the qualifier-matching (here,
     * unqualified) candidate is actually in contention, so the unqualified {@code Class<? extends Base>}
     * request must resolve cleanly to the sole unqualified candidate rather than throwing
     * {@link InjectionException} over a candidate it was never eligible to match in the first place.
     */
    @Test
    void shouldNotTreatDifferentlyQualifiedCompatibleCandidateAsAmbiguous() {
        final var context = createInjectionFramework().newContext();

        context.bind(new TypeLiteral<Class<Impl>>() {
        }).to(Impl.class);
        context.bind(new TypeLiteral<Class<Sibling>>() {
        }).as("named").to(Sibling.class);

        final var holder = context.inject(new WildcardHolder());

        assertThat(holder.type)
            .isEqualTo(Impl.class);
    }
}
