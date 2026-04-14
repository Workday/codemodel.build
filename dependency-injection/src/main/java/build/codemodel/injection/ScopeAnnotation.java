package build.codemodel.injection;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that marks an annotation as a scope annotation. Scope annotations are applied to
 * types and control how instances are created and cached by the {@link InjectionFramework}.
 *
 * <p>Register a {@link Scope} implementation for a scope annotation via
 * {@link InjectionFramework#bindScope(Class, Scope)}.
 *
 * <p>The built-in scope implementation is {@link ScopedValueScope}. Users define their own scope
 * annotations (e.g. {@code @RequestScoped}) and register them via
 * {@link InjectionFramework#bindScope(Class, Scope)}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ScopeAnnotation {
}
