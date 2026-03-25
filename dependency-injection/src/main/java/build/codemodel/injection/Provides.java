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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a zero-args non-static non-void method whose return value is made available for injection
 * via a {@link ProvidesResolver}.
 *
 * <p>This annotation is {@link Inherited}, so it propagates from a superclass method to an overriding
 * subclass method.  Note that due to a JDK constraint, {@link Inherited} does <em>not</em> propagate
 * from an interface method to its implementors; annotate the concrete method directly in that case.
 *
 * <p>Each call to {@link ProvidesResolver#resolve} invokes the annotated method afresh.  The provider
 * object is responsible for any memoization if singleton semantics are required.
 *
 * @author reed.vonredwitz
 * @see ProvidesResolver
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Provides {

}
