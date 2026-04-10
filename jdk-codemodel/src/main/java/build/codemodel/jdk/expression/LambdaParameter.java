package build.codemodel.jdk.expression;

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

import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;

/**
 * A parameter of a lambda expression, with its resolved {@link TypeUsage} and parameter name.
 *
 * <p>For explicitly-annotated parameters the type is always present (resolving to
 * {@link build.codemodel.foundation.usage.UnknownTypeUsage} on failure). For implicitly-typed
 * parameters the type is present when javac's inferred type is reachable, and empty when it
 * is not — distinguishing "no annotation and inference unavailable" from "annotation present
 * but unresolvable".
 *
 * @param type an {@link Optional} resolved {@link TypeUsage} of the parameter
 * @param name the parameter name
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public record LambdaParameter(Optional<TypeUsage> type, String name) {
}
