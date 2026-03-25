/*-
 * #%L
 * Dependency Injection
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
 * An implementation of the Jakarta Dependency Injection specification using the JDKCodeModel.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
module build.codemodel.injection {
    requires jakarta.inject;
    requires transitive build.base.foundation;
    requires transitive build.base.mereology;
    requires transitive build.base.query;
    requires transitive build.base.marshalling;
    requires transitive build.base.transport;
    requires transitive build.base.telemetry;
    requires build.base.configuration;

    requires transitive build.codemodel.foundation;
    requires build.codemodel.expression;
    requires build.codemodel.imperative;
    requires build.codemodel.objectoriented;
    requires transitive build.codemodel.jdk;

    exports build.codemodel.injection;
}
