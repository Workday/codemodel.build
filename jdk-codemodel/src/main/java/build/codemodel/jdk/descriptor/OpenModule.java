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
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;

/**
 * A {@link Trait} indicating that a module is declared {@code open}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@Singular
public enum OpenModule implements Trait {

    /**
     * Indicates the module is an open module.
     */
    OPEN;

    static {
        Marshalling.registerEnum(OpenModule.class);
    }
}
