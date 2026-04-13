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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Module} system, {@link Binder#install(Module)}, and {@link Modules#override}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class ModuleTests
    implements ContextualTesting {

    /**
     * Ensures a {@link Module} installed via {@link Binder#install} contributes its bindings to the context.
     */
    @Test
    void shouldInstallModuleBindingsOntoContext() {
        final var context = createInjectionFramework().newContext();

        context.install(binder -> binder.bind(String.class).to("hello"));

        assertThat(context.create(String.class)).isEqualTo("hello");
    }

    /**
     * Ensures {@link InjectionFramework#newContext(Module...)} installs modules at construction time.
     */
    @Test
    void shouldCreateContextWithModules() {
        final Module module = binder -> binder.bind(String.class).to("hello");
        final var context = createInjectionFramework().newContext(module);

        assertThat(context.create(String.class)).isEqualTo("hello");
    }

    /**
     * Ensures multiple {@link Module}s are each installed when passed to {@link InjectionFramework#newContext}.
     */
    @Test
    void shouldInstallMultipleModules() {
        final Module strings = binder -> binder.bind(String.class).to("hello");
        final Module integers = binder -> binder.bind(Integer.class).to(42);
        final var context = createInjectionFramework().newContext(strings, integers);

        assertThat(context.create(String.class)).isEqualTo("hello");
        assertThat(context.create(Integer.class)).isEqualTo(42);
    }

    /**
     * Ensures {@link Modules#override} uses the override binding when base and overrides conflict.
     */
    @Test
    void shouldOverrideConflictingBaseBinding() {
        final Module base = binder -> binder.bind(String.class).to("base");
        final Module overrides = binder -> binder.bind(String.class).to("override");

        final var context = createInjectionFramework()
            .newContext(Modules.override(base, overrides));

        assertThat(context.create(String.class)).isEqualTo("override");
    }

    /**
     * Ensures {@link Modules#override} preserves base bindings that the override module does not touch.
     */
    @Test
    void shouldPreserveNonConflictingBaseBindings() {
        final Module base = binder -> {
            binder.bind(String.class).to("base-string");
            binder.bind(Integer.class).to(42);
        };
        final Module overrides = binder -> binder.bind(String.class).to("override-string");

        final var context = createInjectionFramework()
            .newContext(Modules.override(base, overrides));

        assertThat(context.create(String.class)).isEqualTo("override-string");
        assertThat(context.create(Integer.class)).isEqualTo(42);
    }
}
