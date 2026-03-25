package build.codemodel.foundation.descriptor;

/*-
 * #%L
 * Code Model Foundation
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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.ModuleName;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides information concerning the <i>definition</i> of a <i>Module</i>.
 *
 * @author brian.oliver
 * @see ModuleName
 * @since Jun-2025
 */
public interface ModuleDescriptor
    extends Traitable {

    /**
     * The {@link ModuleName} for the <i>Module</i>.
     *
     * @return the {@link ModuleName}
     */
    ModuleName moduleName();

    /**
     * Obtains a {@link Stream} of the required {@link ModuleName}s for this <i>Module</i>.
     *
     * @return a {@link Stream} of the required {@link ModuleName}s
     */
    default Stream<ModuleName> requiresModuleNames() {
        return traits(RequiresModuleDescriptor.class)
            .map(RequiresModuleDescriptor::requiresModuleName);
    }

    /**
     * Obtains a {@link Stream} of the available required {@link ModuleDescriptor}s defined in the {@link CodeModel}
     * for this <i>Module</i>.
     *
     * @return a {@link Stream} of the available required {@link ModuleDescriptor}s
     */
    default Stream<ModuleDescriptor> requires() {
        return requiresModuleNames()
            .map(moduleName -> codeModel().getModuleDescriptor(moduleName))
            .filter(Optional::isPresent)
            .map(Optional::get);
    }
}
