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

/**
 * Defines the interfaces for the <i>Code Model Framework</i> to configure and build {@link CodeModel}s.
 *
 * @author brian.oliver
 * @since Apr-2024
 */
module build.codemodel.framework {
    requires build.base.foundation;
    requires build.base.telemetry;

    requires build.codemodel.foundation;

    exports build.codemodel.framework;
    exports build.codemodel.framework.compiler;
    exports build.codemodel.framework.completer;
    exports build.codemodel.framework.initialization;
}
