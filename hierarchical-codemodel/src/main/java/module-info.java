/*-
 * #%L
 * Hierarchical Code Model
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
 * Provides <i>Hierarchical</i> Concepts and {@code Trait}s for {@code CodeModel}s
 *
 * @author brian.oliver
 * @since Mar-2025
 */
open module build.codemodel.hierarchical {

    requires transitive build.codemodel.foundation;

    requires build.base.foundation;
    requires build.base.marshalling;

    exports build.codemodel.hierarchical;
    exports build.codemodel.hierarchical.descriptor;
}
