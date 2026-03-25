package build.codemodel.framework.completer;

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

import build.codemodel.framework.compiler.Compilation;
import build.codemodel.foundation.CodeModel;

/**
 * Represent a successfully completed {@link CodeModel} {@link Compilation}.
 *
 * @author brian.oliver
 * @see Compilation
 * @since Apr-2024
 */
public interface Completion {

    /**
     * Obtains the {@link CodeModel} that was completed.
     *
     * @return the {@link CodeModel}
     */
    CodeModel codeModel();
}
