package build.codemodel.injection;

import jakarta.inject.Inject;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for Injection Cyclic Dependencies.
 *
 * @author andrew.wilson
 * @since Aug-2020
 */
class CyclicDependencyTests
    implements ContextualTesting {

    /**
     * A TriCycle where A -> B -> C -> A -> ... , should throw a {@link CyclicDependencyException}.
     */
    @Test
    void shouldNotInjectWhenEncounteringACyclicTransitiveDependency() {
        final Context context = createInjectionFramework().newContext();

        context.bind(A.class).to(A.class);
        context.bind(A.B.class).to(A.B.class);
        context.bind(A.C.class).to(A.C.class);

        final CyclicDependencyException exception =
            assertThrows(CyclicDependencyException.class, () -> context.create(A.class));
    }

    /**
     * A class that depends on itself should throw a {@link CyclicDependencyException}.
     */
    @Test
    void shouldNotInjectWhenEncounteringASelfDependency() {
        final Context context = createInjectionFramework().newContext();

        context.bind(SelfDependencyCycleCase.class).to(SelfDependencyCycleCase.class);

        assertThrows(CyclicDependencyException.class,
            () -> context.create(SelfDependencyCycleCase.class));
    }

    /**
     * A cycle with constructor injection that should throw a {@link CyclicDependencyException}.
     */
    @Test
    void shouldNotInjectIntoConstructorWhenEncounteringCyclicDependency() {
        final Context context = createInjectionFramework().newContext();

        context.bind(ConstructorInjectionCycleCase.class)
            .to(ConstructorInjectionCycleCase.class);
        context.bind(ConstructorInjectionCycleCase.AnotherChild.class)
            .to(ConstructorInjectionCycleCase.AnotherChild.class);

        assertThrows(CyclicDependencyException.class,
            () -> context.create(ConstructorInjectionCycleCase.class));
    }

    /**
     * If we inject an object, that then contains a cycle, we should throw a {@link CyclicDependencyException}.
     */
    @Test
    void shouldDetectCycleInInjectionCase() {
        final Context context = createInjectionFramework().newContext();

        final ParentCase aCase = new ParentCase();
        context.bind(SelfDependencyCycleCase.class).to(SelfDependencyCycleCase.class);

        assertThrows(CyclicDependencyException.class,
            () -> context.inject(aCase));
    }

    /**
     * This should be ok, but necessary to test.
     */
    @Test
    void shouldInjectIntoChildWithParent() {
        final Context context = createInjectionFramework().newContext();

        context.bind(MultipleChildValidCase.class).to(MultipleChildValidCase.class);
        context.bind(MultipleChildValidCase.Child.class).to(MultipleChildValidCase.Child.class);

        final MultipleChildValidCase aCase = context.create(MultipleChildValidCase.class);

        assertNotNull(aCase.a);
        assertNotNull(aCase.b);
    }

    /**
     * A TriCycle where A -> B -> C -> A -> ... , should throw a {@link CyclicDependencyException}.
     */
    class A {

        @Inject
        B b;

        class B {

            @Inject
            C c;
        }

        class C {

            @Inject
            A a;
        }
    }

    /**
     * A parent that we can inject a cycle into should throw a {@link CyclicDependencyException}.
     */
    class ParentCase {

        @Inject
        SelfDependencyCycleCase a;
    }

    /**
     * A class that depends on itself should throw a {@link CyclicDependencyException}.
     */
    class SelfDependencyCycleCase {

        @Inject
        SelfDependencyCycleCase a;
    }

    /**
     * A cycle with constructor injection that should throw a {@link CyclicDependencyException}.
     */
    class ConstructorInjectionCycleCase {

        @Inject
        AnotherChild b;

        class AnotherChild {

            final ConstructorInjectionCycleCase a;

            @Inject
            AnotherChild(final ConstructorInjectionCycleCase a) {
                this.a = a;
            }
        }
    }

    /**
     * This is a perfectly valid case.
     */
    static class MultipleChildValidCase {

        @Inject
        Child a;

        @Inject
        Child b;

        static class Child {

        }
    }
}


