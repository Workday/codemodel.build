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

import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

/**
 * A {@link Trait} representing a {@code uses} directive in a {@code module-info.java}.
 *
 * @param serviceType the service type consumed by this module
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public record UsesDescriptor(TypeUsage serviceType) implements Trait {

    public static UsesDescriptor of(final TypeUsage serviceType) {
        return new UsesDescriptor(serviceType);
    }
}
