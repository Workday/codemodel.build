package build.codemodel.framework.initialization;

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

import build.codemodel.foundation.CodeModel;
import build.codemodel.framework.Plugin;

/**
 * A {@link Plugin} to initialize {@link CodeModel}s.
 * <p>
 * {@link Initializer} implementations are typically located using the {@link java.util.ServiceLoader}
 * as a {@link Plugin} <i>service</i>.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public interface Initializer
    extends Plugin {

    /**
     * Initialize the specified {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel}
     */
    void initialize(CodeModel codeModel);
}
