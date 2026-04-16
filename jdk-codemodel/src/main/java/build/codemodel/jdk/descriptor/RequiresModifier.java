package build.codemodel.jdk.descriptor;

/*-
 * #%L
 * JDK Code Model
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

import build.base.marshalling.Marshalling;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;

/**
 * A {@link Trait} qualifying a {@link build.codemodel.foundation.descriptor.RequiresModuleDescriptor}
 * with a JPMS modifier keyword.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public enum RequiresModifier implements Trait {

    /**
     * {@code requires transitive} — the dependency is re-exported to consumers of this module.
     */
    TRANSITIVE,

    /**
     * {@code requires static} — the dependency is required at compile time only.
     */
    STATIC;

    static {
        Marshalling.registerEnum(RequiresModifier.class);
    }
}
