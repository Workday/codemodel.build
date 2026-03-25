/*
 * Copyright (C) 2009 The JSR-330 Expert Group
 *
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
 */

package build.codemodel.injection.compatibility;

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

import build.codemodel.injection.compatibility.accessories.Cupholder;
import build.codemodel.injection.compatibility.accessories.SpareTire;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

public class Convertible
    implements Car {

    @Inject
    @Drivers
    Seat driversSeatA;

    @Inject
    @Drivers
    Seat driversSeatB;

    @Inject
    SpareTire spareTire;

    @Inject
    Cupholder cupholder;

    @Inject
    Provider<Engine> engineProvider;

    boolean methodWithZeroParamsInjected;
    boolean methodWithMultipleParamsInjected;
    boolean methodWithNonVoidReturnInjected;

    final Seat constructorPlainSeat;
    final Seat constructorDriversSeat;
    final Tire constructorPlainTire;
    final Tire constructorSpareTire;
    Provider<Seat> constructorPlainSeatProvider = nullProvider();
    Provider<Seat> constructorDriversSeatProvider = nullProvider();
    Provider<Tire> constructorPlainTireProvider = nullProvider();
    Provider<Tire> constructorSpareTireProvider = nullProvider();

    @Inject
    Seat fieldPlainSeat;

    @Inject
    @Drivers
    Seat fieldDriversSeat;

    @Inject
    Tire fieldPlainTire;

    @Inject
    @Named("spare")
    Tire fieldSpareTire;

    @Inject
    Provider<Seat> fieldPlainSeatProvider = nullProvider();

    @Inject
    @Drivers
    Provider<Seat> fieldDriversSeatProvider = nullProvider();

    @Inject
    Provider<Tire> fieldPlainTireProvider = nullProvider();

    @Inject
    @Named("spare")
    Provider<Tire> fieldSpareTireProvider = nullProvider();

    Seat methodPlainSeat;
    Seat methodDriversSeat;
    Tire methodPlainTire;
    Tire methodSpareTire;
    Provider<Seat> methodPlainSeatProvider = nullProvider();
    Provider<Seat> methodDriversSeatProvider = nullProvider();
    Provider<Tire> methodPlainTireProvider = nullProvider();
    Provider<Tire> methodSpareTireProvider = nullProvider();

    @Inject
    Convertible(final Seat plainSeat,
                final @Drivers Seat driversSeat,
                final Tire plainTire,
                final @Named("spare") Tire spareTire,
                final Provider<Seat> plainSeatProvider,
                final @Drivers Provider<Seat> driversSeatProvider,
                final Provider<Tire> plainTireProvider,
                final @Named("spare") Provider<Tire> spareTireProvider) {

        this.constructorPlainSeat = plainSeat;
        this.constructorDriversSeat = driversSeat;
        this.constructorPlainTire = plainTire;
        this.constructorSpareTire = spareTire;
        this.constructorPlainSeatProvider = plainSeatProvider;
        this.constructorDriversSeatProvider = driversSeatProvider;
        this.constructorPlainTireProvider = plainTireProvider;
        this.constructorSpareTireProvider = spareTireProvider;
    }

    Convertible() {
        throw new AssertionError("Unexpected call to non-injectable constructor");
    }

    void setSeat(final Seat unused) {
        throw new AssertionError("Unexpected call to non-injectable method");
    }

    @Inject
    void injectMethodWithZeroArgs() {
        this.methodWithZeroParamsInjected = true;
    }

    @Inject
    String injectMethodWithNonVoidReturn() {
        this.methodWithNonVoidReturnInjected = true;
        return "unused";
    }

    @Inject
    void injectInstanceMethodWithManyArgs(final Seat plainSeat,
                                          final @Drivers Seat driversSeat,
                                          final Tire plainTire,
                                          final @Named("spare") Tire spareTire,
                                          final Provider<Seat> plainSeatProvider,
                                          final @Drivers Provider<Seat> driversSeatProvider,
                                          final Provider<Tire> plainTireProvider,
                                          final @Named("spare") Provider<Tire> spareTireProvider) {

        this.methodWithMultipleParamsInjected = true;

        this.methodPlainSeat = plainSeat;
        this.methodDriversSeat = driversSeat;
        this.methodPlainTire = plainTire;
        this.methodSpareTire = spareTire;
        this.methodPlainSeatProvider = plainSeatProvider;
        this.methodDriversSeatProvider = driversSeatProvider;
        this.methodPlainTireProvider = plainTireProvider;
        this.methodSpareTireProvider = spareTireProvider;
    }

    /**
     * Returns a provider that always returns null. This is used as a default
     * value to avoid null checks for omitted provider injections.
     */
    private static <T> Provider<T> nullProvider() {
        return () -> null;
    }
}
