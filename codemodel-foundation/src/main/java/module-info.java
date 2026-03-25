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
/**
 * Foundation types for the specification of <i>Code Models</i>.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
open module build.codemodel.foundation {
    requires build.base.foundation;
    requires build.base.mereology;
    requires build.base.query;
    requires build.base.marshalling;
    requires build.base.transport;
    requires build.base.telemetry;

    requires java.desktop;
    requires jakarta.inject;

    exports build.codemodel.foundation;
    exports build.codemodel.foundation.descriptor;
    exports build.codemodel.foundation.naming;
    exports build.codemodel.foundation.usage;
    exports build.codemodel.foundation.usage.pattern;
    exports build.codemodel.foundation.transport;
}
