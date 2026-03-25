package build.codemodel.framework.compiler;

/*-
 * #%L
 * Code Model Framework
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

import build.codemodel.framework.completer.Completer;
import build.codemodel.foundation.CodeModel;

/**
 * Provides access to the successfully compiled {@link CodeModel}, including the ability to <i>complete</i> it with
 * the known {@link Completer}s.
 *
 * @author brian.oliver
 * @see Compiler
 * @see Completer
 * @since Apr-2024
 */
public interface Compilation {

    /**
     * Obtains the {@link CodeModel} that was successfully compiled.
     *
     * @return the {@link CodeModel}
     */
    CodeModel codeModel();
}
