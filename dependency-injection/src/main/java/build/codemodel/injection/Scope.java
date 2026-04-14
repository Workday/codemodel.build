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

/**
 * Controls the lifecycle and sharing of instances produced by a {@link Binding}. A {@link Scope}
 * wraps an unscoped {@link ValueBinding} and returns a new {@link Binding} that applies the scope's
 * caching or sharing strategy.
 *
 * <p>Register custom scopes via {@link InjectionFramework#bindScope(Class, Scope)}.
 *
 * <p>The built-in implementation is {@link ScopedValueScope}, which uses a {@link ScopedValue}
 * to cache one instance per structured scope invocation.
 *
 * @author reed.vonredwitz
 * @see ScopeAnnotation
 * @see ScopedValueScope
 * @since Apr-2026
 */
@FunctionalInterface
public interface Scope {

    /**
     * Wraps the given unscoped {@link ValueBinding} in a scoped {@link Binding}. The returned binding
     * may cache, share, or re-create instances according to the scope's strategy.
     *
     * <p>The signature uses wildcards so that implementations may be expressed as lambdas.
     * The framework guarantees that the returned {@link Binding} produces values that are
     * assignment-compatible with the type represented by {@link Binding#dependency()}.
     *
     * @param binding the unscoped {@link ValueBinding} whose {@link ValueBinding#value()} creates a
     *                fresh instance on each call
     * @return a scoped {@link Binding} implementing this scope's instance lifecycle
     */
    Binding<?> scope(ValueBinding<?> binding);
}
