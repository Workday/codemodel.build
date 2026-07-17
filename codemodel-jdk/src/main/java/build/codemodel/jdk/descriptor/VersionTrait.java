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

import build.base.version.Version;
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;

/**
 * A {@link Trait} carrying the {@link Version} of a module, as declared in a {@code module-info.class}
 * or provided by a build system.
 *
 * @param version the module {@link Version}
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@Singular
public record VersionTrait(Version version) implements Trait {

    public static VersionTrait of(final Version version) {
        return new VersionTrait(version);
    }
}
