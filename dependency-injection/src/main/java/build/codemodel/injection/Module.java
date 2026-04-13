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
 * A reusable unit of binding configuration. Modules can be installed into a {@link Binder} (and therefore
 * into any {@link Context}) via {@link Binder#install(Module)} or
 * {@link InjectionFramework#newContext(Module...)}.
 *
 * <p>Modules are composable: install multiple modules to combine their bindings. Use
 * {@link Modules#override(Module, Module)} to compose two modules where one overrides conflicting
 * bindings from the other.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 * @see Modules
 * @see Binder#install(Module)
 */
@FunctionalInterface
public interface Module {

    /**
     * Contributes bindings to the given {@link Binder}.
     *
     * @param binder the {@link Binder} to configure
     */
    void configure(Binder binder);
}
