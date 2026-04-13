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
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Context#validate()} and {@link Context#initializeEagerSingletons()}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class ValidationTests
    implements ContextualTesting {

    // ---- fixture types ----

    @Singleton
    static class Alpha {
        @Inject
        Beta beta;
    }

    @Singleton
    static class Beta {
        @Inject
        Alpha alpha; // cycle: Alpha → Beta → Alpha
    }

    @Singleton
    static class ValidRoot {
        @Inject
        ValidLeaf leaf;
    }

    @Singleton
    static class ValidLeaf {
    }

    @Singleton
    static class NeedsMissing {
        @Inject
        MissingDep missing;
    }

    // Not bound — used to exercise unsatisfied-dependency detection
    static class MissingDep {
    }

    @Singleton
    static class SingletonConsumer {
        @Inject
        PrototypeDep prototype;
    }

    // Prototype-scoped (no @Singleton): when explicitly bound via ClassBinding this is a scope violation
    static class PrototypeDep {
    }

    static final AtomicInteger eagerInitCount = new AtomicInteger(0);

    @Singleton
    static class EagerService {
        @PostInject
        void init() {
            eagerInitCount.incrementAndGet();
        }
    }

    @Singleton
    static class EagerDep {
    }

    @Singleton
    static class EagerConsumer {
        @Inject
        EagerDep dep;
    }

    // ---- happy path ----

    /**
     * Ensures {@link Context#validate()} returns the context when bindings are fully satisfied with no cycles.
     */
    @Test
    void shouldPassValidationForValidBindings() {
        final var context = createInjectionFramework().newContext();
        context.bind(ValidRoot.class).to(ValidRoot.class);
        context.bind(ValidLeaf.class).to(ValidLeaf.class);

        assertThatCode(context::validate).doesNotThrowAnyException();
        assertThat(context.validate()).isSameAs(context);
    }

    // ---- cycle detection ----

    /**
     * Ensures {@link Context#validate()} throws {@link CyclicDependencyException} for a two-class cycle before
     * any object is created.
     */
    @Test
    void shouldDetectCycleAtValidation() {
        final var context = createInjectionFramework().newContext();
        context.bind(Alpha.class).to(Alpha.class);
        context.bind(Beta.class).to(Beta.class);

        assertThatThrownBy(context::validate)
            .isInstanceOf(CyclicDependencyException.class);
    }

    // ---- unsatisfied dependency detection ----

    /**
     * Ensures {@link Context#validate()} throws when a registered class binding has a dependency on an
     * unregistered type.
     */
    @Test
    void shouldDetectUnsatisfiedDependencyAtValidation() {
        final var context = createInjectionFramework().newContext();
        context.bind(NeedsMissing.class).to(NeedsMissing.class);
        // MissingDep is NOT bound

        assertThatThrownBy(context::validate)
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("MissingDep");
    }

    // ---- scope violation detection ----

    /**
     * Ensures {@link Context#validate()} throws when a {@link Singleton}-scoped class binding depends on a
     * prototype-scoped (non-singleton) class binding.
     */
    @Test
    void shouldDetectScopeViolation() {
        final var context = createInjectionFramework().newContext();
        context.bind(SingletonConsumer.class).to(SingletonConsumer.class);
        context.bind(PrototypeDep.class).to(PrototypeDep.class); // NonSingletonClassBinding

        assertThatThrownBy(context::validate)
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Scope violation");
    }

    // ---- initializeEagerSingletons ----

    /**
     * Ensures {@link Context#initializeEagerSingletons()} pre-creates all {@link Singleton}-scoped class
     * bindings, invoking {@link PostInject} methods, without requiring an explicit {@link Context#create} call.
     */
    @Test
    void shouldInitializeEagerSingletons() {
        eagerInitCount.set(0);
        final var context = createInjectionFramework().newContext();
        context.bind(EagerService.class).to(EagerService.class);

        context.validate().initializeEagerSingletons();

        // @PostInject should have fired during eager init
        assertThat(eagerInitCount.get()).isEqualTo(1);

        // subsequent create() must return the same instance without re-initializing
        context.create(EagerService.class);
        assertThat(eagerInitCount.get()).isEqualTo(1);
    }

    /**
     * Ensures eager singletons with dependencies are initialized in correct dependency order so that
     * transitive dependencies are available when the dependents are constructed.
     */
    @Test
    void shouldInitializeEagerSingletonsInDependencyOrder() {
        final var context = createInjectionFramework().newContext();
        context.bind(EagerConsumer.class).to(EagerConsumer.class);
        context.bind(EagerDep.class).to(EagerDep.class);

        assertThatCode(() -> context.validate().initializeEagerSingletons())
            .doesNotThrowAnyException();

        // both should be resolvable and the same instances
        final var consumer1 = context.create(EagerConsumer.class);
        final var consumer2 = context.create(EagerConsumer.class);
        assertThat(consumer1).isSameAs(consumer2);
    }
}
