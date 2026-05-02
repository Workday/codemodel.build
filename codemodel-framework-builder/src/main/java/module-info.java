/*-
 * #%L
 * Code Model Framework Builder
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
import build.codemodel.framework.Framework;

/**
 * Defines a mechanism to programmatically build <i>Code Model</i> {@link Framework}s.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
module build.codemodel.framework.builder {
    requires jakarta.inject;

    requires build.base.foundation;

    requires build.base.telemetry;
    requires build.base.telemetry.foundation;

    requires build.codemodel.foundation;
    requires build.codemodel.expression;
    requires build.codemodel.imperative;
    requires build.codemodel.objectoriented;
    requires build.codemodel.jdk;

    requires build.codemodel.injection;
    requires build.codemodel.framework;
    requires build.base.mereology;

    exports build.codemodel.framework.builder;
}
