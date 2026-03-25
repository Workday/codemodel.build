/*-
 * #%L
 * Expression Code Model
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
 * Defines interfaces for the specification of <i>Code Model Expressions</i>.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
module build.codemodel.expression {
    requires build.codemodel.foundation;
    requires build.base.parsing;
    requires build.base.io;
    requires build.base.marshalling;
    requires build.base.foundation;

    exports build.codemodel.expression;
    exports build.codemodel.expression.naming;
    exports build.codemodel.expression.parsing;
    exports build.codemodel.expression.parsing.resolvers;
    exports build.codemodel.expression.parsing.tokenparsers;
}
