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
 * A {@link Trait} qualifying a {@link JDKModuleDescriptor} with a module-level flag.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public enum ModuleModifier implements Trait {

    /**
     * Automatic module — derived from a plain JAR without a {@code module-info.class}.
     * Set by the module system at load time; does not appear in source.
     */
    AUTOMATIC,

    /**
     * Compiler-generated module — present only in bytecode, never in source.
     */
    SYNTHETIC,

    /**
     * Implicitly required by the platform — present only in bytecode, never in source.
     */
    MANDATED;

    static {
        Marshalling.registerEnum(ModuleModifier.class);
    }
}
