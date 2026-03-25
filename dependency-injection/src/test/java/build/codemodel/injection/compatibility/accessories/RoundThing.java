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

import jakarta.inject.Inject;

public class RoundThing {

    public boolean packagePrivateMethod2Injected;

    public boolean packagePrivateMethod3Injected;

    public boolean packagePrivateMethod4Injected;

    @Inject
    void injectPackagePrivateMethod2() {
        this.packagePrivateMethod2Injected = true;
    }

    @Inject
    void injectPackagePrivateMethod3() {
        this.packagePrivateMethod3Injected = true;
    }

    @Inject
    void injectPackagePrivateMethod4() {
        this.packagePrivateMethod4Injected = true;
    }
}
