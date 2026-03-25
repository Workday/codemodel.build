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

import build.codemodel.injection.compatibility.accessories.RoundThing;
import jakarta.inject.Inject;

import java.util.LinkedHashSet;
import java.util.Set;

public class Tire
    extends RoundThing {

    protected static final FuelTank NEVER_INJECTED = new FuelTank();

    protected static final Set<String> moreProblems = new LinkedHashSet<String>();

    FuelTank constructorInjection = NEVER_INJECTED;

    @Inject
    FuelTank fieldInjection = NEVER_INJECTED;

    FuelTank methodInjection = NEVER_INJECTED;

    boolean constructorInjected;

    protected boolean superPrivateMethodInjected;
    protected boolean superPackagePrivateMethodInjected;
    protected boolean superProtectedMethodInjected;
    protected boolean superPublicMethodInjected;
    protected boolean subPrivateMethodInjected;
    protected boolean subPackagePrivateMethodInjected;
    protected boolean subProtectedMethodInjected;
    protected boolean subPublicMethodInjected;

    protected boolean superPrivateMethodForOverrideInjected;
    protected boolean superPackagePrivateMethodForOverrideInjected;
    protected boolean subPrivateMethodForOverrideInjected;
    protected boolean subPackagePrivateMethodForOverrideInjected;
    protected boolean protectedMethodForOverrideInjected;
    protected boolean publicMethodForOverrideInjected;

    public boolean methodInjectedBeforeFields;
    public boolean subtypeFieldInjectedBeforeSupertypeMethods;
    public boolean subtypeMethodInjectedBeforeSupertypeMethods;
    public static boolean staticMethodInjectedBeforeStaticFields;
    public static boolean subtypeStaticFieldInjectedBeforeSupertypeStaticMethods;
    public static boolean subtypeStaticMethodInjectedBeforeSupertypeStaticMethods;
    public boolean similarPrivateMethodInjectedTwice;
    public boolean similarPackagePrivateMethodInjectedTwice;
    public boolean overriddenProtectedMethodInjectedTwice;
    public boolean overriddenPublicMethodInjectedTwice;

    boolean packagePrivateMethod2Injected;
    public boolean packagePrivateMethod3Injected;
    public boolean packagePrivateMethod4Injected;

    @Inject
    public Tire(final FuelTank constructorInjection) {
        this.constructorInjection = constructorInjection;
    }

    @Inject
    void supertypeMethodInjection(final FuelTank methodInjection) {
        if (!hasTireBeenFieldInjected()) {
            this.methodInjectedBeforeFields = true;
        }
        if (hasSpareTireBeenFieldInjected()) {
            this.subtypeFieldInjectedBeforeSupertypeMethods = true;
        }
        if (hasSpareTireBeenMethodInjected()) {
            this.subtypeMethodInjectedBeforeSupertypeMethods = true;
        }
        this.methodInjection = methodInjection;
    }

    @Inject
    private void injectPrivateMethod() {
        if (this.superPrivateMethodInjected) {
            this.similarPrivateMethodInjectedTwice = true;
        }
        this.superPrivateMethodInjected = true;
    }

    @Inject
    void injectPackagePrivateMethod() {
        if (this.superPackagePrivateMethodInjected) {
            this.similarPackagePrivateMethodInjectedTwice = true;
        }
        this.superPackagePrivateMethodInjected = true;
    }

    @Inject
    protected void injectProtectedMethod() {
        if (this.superProtectedMethodInjected) {
            this.overriddenProtectedMethodInjectedTwice = true;
        }
        this.superProtectedMethodInjected = true;
    }

    @Inject
    public void injectPublicMethod() {
        if (this.superPublicMethodInjected) {
            this.overriddenPublicMethodInjectedTwice = true;
        }
        this.superPublicMethodInjected = true;
    }

    @Inject
    private void injectPrivateMethodForOverride() {
        this.subPrivateMethodForOverrideInjected = true;
    }

    @Inject
    void injectPackagePrivateMethodForOverride() {
        this.subPackagePrivateMethodForOverrideInjected = true;
    }

    @Inject
    protected void injectProtectedMethodForOverride() {
        this.protectedMethodForOverrideInjected = true;
    }

    @Inject
    public void injectPublicMethodForOverride() {
        this.publicMethodForOverrideInjected = true;
    }

    protected final boolean hasTireBeenFieldInjected() {
        return this.fieldInjection != NEVER_INJECTED;
    }

    protected boolean hasSpareTireBeenFieldInjected() {
        return false;
    }

    protected final boolean hasTireBeenMethodInjected() {
        return this.methodInjection != NEVER_INJECTED;
    }

    protected boolean hasSpareTireBeenMethodInjected() {
        return false;
    }

    @Inject
    void injectPackagePrivateMethod2() {
        this.packagePrivateMethod2Injected = true;
    }

    @Inject
    void injectPackagePrivateMethod3() {
        this.packagePrivateMethod3Injected = true;
    }

    void injectPackagePrivateMethod4() {
        this.packagePrivateMethod4Injected = true;
    }
}
