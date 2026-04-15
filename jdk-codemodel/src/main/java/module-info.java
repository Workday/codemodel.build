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
/**
 * Provides <i>JDK</i>-based {@code Trait}s for {@code CodeModel}s.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
open module build.codemodel.jdk {
    requires jakarta.inject;

    requires build.codemodel.framework;

    requires transitive build.codemodel.foundation;
    requires transitive build.codemodel.expression;
    requires transitive build.codemodel.imperative;
    requires transitive build.codemodel.objectoriented;

    requires java.compiler;
    requires jdk.compiler;
    requires build.base.telemetry.foundation;

    requires build.base.marshalling;
    requires build.base.foundation;
    requires build.base.parsing;
    requires build.base.version;

    exports build.codemodel.jdk;
    exports build.codemodel.jdk.descriptor;
    exports build.codemodel.jdk.expression;
    exports build.codemodel.jdk.statement;
}
