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

package build.codemodel.injection.compatibility.accessories;

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

import build.codemodel.injection.compatibility.FuelTank;
import build.codemodel.injection.compatibility.Tire;
import jakarta.inject.Inject;

public class SpareTire
    extends Tire {

    FuelTank constructorInjection = NEVER_INJECTED;
    @Inject
    FuelTank fieldInjection = NEVER_INJECTED;

    FuelTank methodInjection = NEVER_INJECTED;

    public boolean packagePrivateMethod2Injected;
    public boolean packagePrivateMethod3Injected;

    @Inject
    public SpareTire(final FuelTank forSupertype, final FuelTank forSubtype) {
        super(forSupertype);
        this.constructorInjection = forSubtype;
    }

    @Inject
    void subtypeMethodInjection(final FuelTank methodInjection) {
        if (!hasSpareTireBeenFieldInjected()) {
            this.methodInjectedBeforeFields = true;
        }
        this.methodInjection = methodInjection;
    }

    @Inject
    private void injectPrivateMethod() {
        if (this.subPrivateMethodInjected) {
            this.similarPrivateMethodInjectedTwice = true;
        }
        this.subPrivateMethodInjected = true;
    }

    @Inject
    void injectPackagePrivateMethod() {
        if (this.subPackagePrivateMethodInjected) {
            this.similarPackagePrivateMethodInjectedTwice = true;
        }
        this.subPackagePrivateMethodInjected = true;
    }

    @Inject
    protected void injectProtectedMethod() {
        if (this.subProtectedMethodInjected) {
            this.overriddenProtectedMethodInjectedTwice = true;
        }
        this.subProtectedMethodInjected = true;
    }

    @Inject
    public void injectPublicMethod() {
        if (this.subPublicMethodInjected) {
            this.overriddenPublicMethodInjectedTwice = true;
        }
        this.subPublicMethodInjected = true;
    }

    private void injectPrivateMethodForOverride() {
        this.superPrivateMethodForOverrideInjected = true;
    }

    void injectPackagePrivateMethodForOverride() {
        this.superPackagePrivateMethodForOverrideInjected = true;
    }

    protected void injectProtectedMethodForOverride() {
        this.protectedMethodForOverrideInjected = true;
    }

    public void injectPublicMethodForOverride() {
        this.publicMethodForOverrideInjected = true;
    }

    public boolean hasSpareTireBeenFieldInjected() {
        return this.fieldInjection != NEVER_INJECTED;
    }

    public boolean hasSpareTireBeenMethodInjected() {
        return this.methodInjection != NEVER_INJECTED;
    }

    @Inject
    void injectPackagePrivateMethod2() {
        this.packagePrivateMethod2Injected = true;
    }

    void injectPackagePrivateMethod3() {
        this.packagePrivateMethod3Injected = true;
    }
}
