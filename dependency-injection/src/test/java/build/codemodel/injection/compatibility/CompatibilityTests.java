package build.codemodel.injection.compatibility;

import build.codemodel.injection.Context;
import build.codemodel.injection.ContextualTesting;
import build.codemodel.injection.ProviderResolver;
import build.codemodel.injection.compatibility.accessories.Cupholder;
import build.codemodel.injection.compatibility.accessories.RoundThing;
import build.codemodel.injection.compatibility.accessories.SpareTire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quark Injection Jakarta Dependency Injection Compatibility Tests.
 * <p>
 * These tests are based on those defined by the Jakarta Dependency Injection.
 * They have been refactored <strong>but not changed</strong> to support modern JUnit (5+), remove optional
 * static tests (which are not supported), and reformatted to be compliant with Workday coding conventions.
 * <p>
 * <a href="https://github.com/eclipse-ee4j/injection-api">See Jakarta Dependency Injection</a> for more details.
 *
 * @author brian.oliver
 * @since May-2020
 */
class CompatibilityTests
    implements ContextualTesting {

    private Convertible car;
    private Cupholder cupholder;
    private SpareTire spareTire;
    private Tire plainTire;
    private Engine engine;

    @BeforeEach
    void setup() {
        // establish a Context to use for Injection
        final Context context = createInjectionFramework().newContext();

        // establish a Resolver for Provider-based InjectionPoints
        context.addResolver(ProviderResolver::new);

        // establish Bindings for the Context
        context.bind(Car.class).to(Convertible.class);
        context.bind(Seat.class).with(Drivers.class).to(DriversSeat.class);
        context.bind(Seat.class).to(Seat.class);
        context.bind(Tire.class).as("spare").to(SpareTire.class);
        context.bind(Tire.class).to(Tire.class);
        context.bind(Engine.class).to(V8Engine.class);
        context.bind(Cupholder.class).to(Cupholder.class);
        context.bind(SpareTire.class).to(SpareTire.class);
        context.bind(FuelTank.class).to(FuelTank.class);

        // create the Convertible Car
        this.car = context.create(Convertible.class);

        this.cupholder = this.car.cupholder;
        this.spareTire = this.car.spareTire;
        this.plainTire = this.car.fieldPlainTire;
        this.engine = this.car.engineProvider.get();
    }

    @Test
    void shouldInjectIntoFields() {
        assertTrue(this.cupholder != null && this.spareTire != null);
    }

    @Test
    void shouldInjectIntoProvider() {
        assertNotNull(this.engine);
    }

    @Test
    void shouldInjectIntoMethodWithZeroParameters() {
        assertTrue(this.car.methodWithZeroParamsInjected);
    }

    @Test
    void shouldInjectIntoMethodWithMultipleParameters() {
        assertTrue(this.car.methodWithMultipleParamsInjected);
    }

    @Test
    void shouldInjectIntoNonVoidMethodInjected() {
        assertTrue(this.car.methodWithNonVoidReturnInjected);
    }

    @Test
    void shouldInjectIntoPublicNoArgsConstructor() {
        assertTrue(this.engine.publicNoArgsConstructorInjected);
    }

    @Test
    void shouldInjectIntoSubtypeFields() {
        assertTrue(this.spareTire.hasSpareTireBeenFieldInjected());
    }

    @Test
    void shouldInjectIntoSubtypeMethods() {
        assertTrue(this.spareTire.hasSpareTireBeenMethodInjected());
    }

    @Test
    void shouldInjectIntoSupertypeFields() {
        assertTrue(this.spareTire.hasTireBeenFieldInjected());
    }

    @Test
    void shouldInjectIntoSupertypeMethods() {
        assertTrue(this.spareTire.hasTireBeenMethodInjected());
    }

    @Test
    void shouldInjectIntoTwiceOverriddenMethodWhenMiddleLacksAnnotation() {
        assertTrue(this.engine.overriddenTwiceWithOmissionInMiddleInjected);
    }

    @Test
    void shouldInjectIntoMethodWhenQualifiersNotInheritedFromOverriddenMethod() {
        assertFalse(this.engine.qualifiersInheritedFromOverriddenMethod);
    }

    @Test
    void shouldInjectIntoConstructorWithValues() {
        assertFalse(this.car.constructorPlainSeat instanceof DriversSeat);
        assertFalse(this.car.constructorPlainTire instanceof SpareTire);
        assertTrue(this.car.constructorDriversSeat instanceof DriversSeat);
        assertTrue(this.car.constructorSpareTire instanceof SpareTire);
    }

    @Test
    void shouldInjectFieldWithValues() {
        assertFalse(this.car.fieldPlainSeat instanceof DriversSeat);
        assertFalse(this.car.fieldPlainTire instanceof SpareTire);
        assertTrue(this.car.fieldDriversSeat instanceof DriversSeat);
        assertTrue(this.car.fieldSpareTire instanceof SpareTire);
    }

    @Test
    void shouldInjectMethodWithValues() {
        assertFalse(this.car.methodPlainSeat instanceof DriversSeat);
        assertFalse(this.car.methodPlainTire instanceof SpareTire);
        assertTrue(this.car.methodDriversSeat instanceof DriversSeat);
        assertTrue(this.car.methodSpareTire instanceof SpareTire);
    }

    @Test
    void shouldInjectIntoConstructorWithProviders() {
        assertFalse(this.car.constructorPlainSeatProvider.get() instanceof DriversSeat);
        assertFalse(this.car.constructorPlainTireProvider.get() instanceof SpareTire);
        assertTrue(this.car.constructorDriversSeatProvider.get() instanceof DriversSeat);
        assertTrue(this.car.constructorSpareTireProvider.get() instanceof SpareTire);
    }

    @Test
    void shouldInjectIntoFieldsWithProviders() {
        assertFalse(this.car.fieldPlainSeatProvider.get() instanceof DriversSeat);
        assertFalse(this.car.fieldPlainTireProvider.get() instanceof SpareTire);
        assertTrue(this.car.fieldDriversSeatProvider.get() instanceof DriversSeat);
        assertTrue(this.car.fieldSpareTireProvider.get() instanceof SpareTire);
    }

    @Test
    void shouldInjectIntoMethodsWithProviders() {
        assertFalse(this.car.methodPlainSeatProvider.get() instanceof DriversSeat);
        assertFalse(this.car.methodPlainTireProvider.get() instanceof SpareTire);
        assertTrue(this.car.methodDriversSeatProvider.get() instanceof DriversSeat);
        assertTrue(this.car.methodSpareTireProvider.get() instanceof SpareTire);
    }

    @Test
    void shouldInjectIntoConstructorWithProviderYieldsSingleton() {
        assertSame(this.car.constructorPlainSeatProvider.get(), this.car.constructorPlainSeatProvider.get());
    }

    @Test
    void shouldInjectIntoFieldWithProviderYieldsSingleton() {
        assertSame(this.car.fieldPlainSeatProvider.get(), this.car.fieldPlainSeatProvider.get());
    }

    @Test
    void shouldInjectIntoMethodWithProviderYieldsSingleton() {
        assertSame(this.car.methodPlainSeatProvider.get(), this.car.methodPlainSeatProvider.get());
    }

    @Test
    void shouldInjectIntoCircularlyDependentWithSingletons() {
        // uses provider.get() to get around circular deps
        assertSame(this.cupholder.seatProvider.get().getCupholder(), this.cupholder);
    }

    @Test
    void shouldInjectSingletonWithAnnotationNotInheritedFromSupertype() {
        assertNotSame(this.car.driversSeatA, this.car.driversSeatB);
    }

    @Test
    void shouldInjectConstructorWithProviderYieldsDistinctValues() {
        assertNotSame(this.car.constructorDriversSeatProvider.get(), this.car.constructorDriversSeatProvider.get());
        assertNotSame(this.car.constructorPlainTireProvider.get(), this.car.constructorPlainTireProvider.get());
        assertNotSame(this.car.constructorSpareTireProvider.get(), this.car.constructorSpareTireProvider.get());
    }

    @Test
    void shouldInjectFieldWithProviderYieldsDistinctValues() {
        assertNotSame(this.car.fieldDriversSeatProvider.get(), this.car.fieldDriversSeatProvider.get());
        assertNotSame(this.car.fieldPlainTireProvider.get(), this.car.fieldPlainTireProvider.get());
        assertNotSame(this.car.fieldSpareTireProvider.get(), this.car.fieldSpareTireProvider.get());
    }

    @Test
    void shouldInjectMethodWithProviderYieldsDistinctValues() {
        assertNotSame(this.car.methodDriversSeatProvider.get(), this.car.methodDriversSeatProvider.get());
        assertNotSame(this.car.methodPlainTireProvider.get(), this.car.methodPlainTireProvider.get());
        assertNotSame(this.car.methodSpareTireProvider.get(), this.car.methodSpareTireProvider.get());
    }

    @Test
    void shouldInjectIntoPackagePrivateMethodInDifferentPackages() {
        assertTrue(this.spareTire.subPackagePrivateMethodInjected);
        assertTrue(this.spareTire.superPackagePrivateMethodInjected);
    }

    @Test
    void shouldInjectOverriddenProtectedMethod() {
        assertTrue(this.spareTire.subProtectedMethodInjected);
        assertFalse(this.spareTire.superProtectedMethodInjected);
    }

    @Test
    void shouldNotInjectOverriddenPublicMethod() {
        assertTrue(this.spareTire.subPublicMethodInjected);
        assertFalse(this.spareTire.superPublicMethodInjected);
    }

    @Test
    void shouldInjectFieldsBeforeMethods() {
        assertFalse(this.spareTire.methodInjectedBeforeFields);
    }

    @Test
    void shouldInjectSupertypeMethodsBeforeSubtypeFields() {
        assertFalse(this.spareTire.subtypeFieldInjectedBeforeSupertypeMethods);
    }

    @Test
    void shouldInjectSupertypeMethodBeforeSubtypeMethods() {
        assertFalse(this.spareTire.subtypeMethodInjectedBeforeSupertypeMethods);
    }

    @Test
    void shouldInjectPackagePrivateMethodEvenWhenSimilarMethodLacksAnnotation() {
        assertTrue(this.spareTire.subPackagePrivateMethodForOverrideInjected);
    }

    @Test
    void shouldNotInjectPrivateMethodInjectedWhenSupertypeHasAnnotatedSimilarMethod() {
        assertFalse(this.spareTire.superPrivateMethodForOverrideInjected);
    }

    @Test
    void shouldNotInjectPackagePrivateMethodWhenOverrideLacksAnnotation() {
        assertFalse(this.engine.subPackagePrivateMethodForOverrideInjected);
        assertFalse(this.engine.superPackagePrivateMethodForOverrideInjected);
    }

    @Test
    void shouldNotInjectPackagePrivateMethodWhenSupertypeHasAnnotatedSimilarMethod() {
        assertFalse(this.spareTire.superPackagePrivateMethodForOverrideInjected);
    }

    @Test
    void shouldNotInjectProtectedMethodWhenOverrideLacksAnnotation() {
        assertFalse(this.spareTire.protectedMethodForOverrideInjected);
    }

    @Test
    void shouldNotInjectPublicMethodWhenOverrideLacksAnnotation() {
        assertFalse(this.spareTire.publicMethodForOverrideInjected);
    }

    @Test
    void shouldNotInjectTwiceOverriddenMethodWhenOverrideLacksAnnotation() {
        assertFalse(this.engine.overriddenTwiceWithOmissionInSubclassInjected);
    }

    @Test
    void shouldInjectOverriddingMixedWithPackagePrivate2() {
        assertTrue(this.spareTire.packagePrivateMethod2Injected);
        assertTrue(((Tire) this.spareTire).packagePrivateMethod2Injected);
        assertFalse(((RoundThing) this.spareTire).packagePrivateMethod2Injected);

        assertTrue(this.plainTire.packagePrivateMethod2Injected);
        assertTrue(((RoundThing) this.plainTire).packagePrivateMethod2Injected);
    }

    @Test
    void shouldInjectOverriddingMixedWithPackagePrivate3() {
        assertFalse(this.spareTire.packagePrivateMethod3Injected);
        assertTrue(((Tire) this.spareTire).packagePrivateMethod3Injected);
        assertFalse(((RoundThing) this.spareTire).packagePrivateMethod3Injected);

        assertTrue(this.plainTire.packagePrivateMethod3Injected);
        assertTrue(((RoundThing) this.plainTire).packagePrivateMethod3Injected);
    }

    @Test
    void shouldInjectOverriddingMixedWithPackagePrivate4() {
        assertFalse(this.plainTire.packagePrivateMethod4Injected);
        assertTrue(((RoundThing) this.plainTire).packagePrivateMethod4Injected);
    }

    @Test
    void shouldInjectOverriddenPackagePrivateMethodOnlyOnce() {
        assertFalse(this.engine.overriddenPackagePrivateMethodInjectedTwice);
    }

    @Test
    void shouldSimilarPackagePrivateMethodOnlyOnce() {
        assertFalse(this.spareTire.similarPackagePrivateMethodInjectedTwice);
    }

    @Test
    void shouldInjectIntoOverriddenProtectedMethodOnlyOnce() {
        assertFalse(this.spareTire.overriddenProtectedMethodInjectedTwice);
    }

    @Test
    void shouldInjectIntoOverriddenPublicMethodOnlyOnce() {
        assertFalse(this.spareTire.overriddenPublicMethodInjectedTwice);
    }

    @Test
    void shouldInjectIntoSupertypePrivateMethod() {
        assertTrue(this.spareTire.superPrivateMethodInjected);
        assertTrue(this.spareTire.subPrivateMethodInjected);
    }

    @Test
    void shouldInjectIntoPackagePrivateMethodInTheSamePackage() {
        assertTrue(this.engine.subPackagePrivateMethodInjected);
        assertFalse(this.engine.superPackagePrivateMethodInjected);
    }

    @Test
    void shouldInjectIntoPrivateMethodEvenWhenSimilarMethodLacksAnnotation() {
        assertTrue(this.spareTire.subPrivateMethodForOverrideInjected);
    }

    @Test
    void shouldInjectIntoSimilarPrivateMethodOnlyOnce() {
        assertFalse(this.spareTire.similarPrivateMethodInjectedTwice);
    }
}
