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

import build.codemodel.foundation.usage.AnnotationTypeUsage;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * Factory methods for composing {@link Module}s.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public final class Modules {

    private Modules() {
    }

    /**
     * Returns a {@link Module} whose bindings are {@code base}'s bindings with {@code overrides} winning on
     * any conflicts. {@link BindingAlreadyExistsException} is suppressed for base bindings that are
     * superseded by the override module, so callers do not need to remove conflicting base bindings manually.
     *
     * <p>Example usage in tests:
     * <pre>{@code
     * context = framework.newContext(Modules.override(new ProductionModule(), new TestModule()));
     * }</pre>
     *
     * @param base      the base {@link Module} whose bindings are installed first
     * @param overrides the override {@link Module} whose bindings win on conflicts
     * @return a composed {@link Module}
     */
    public static Module override(final Module base, final Module overrides) {
        return binder -> {
            overrides.configure(binder);
            base.configure(new SuppressingBinder(binder));
        };
    }

    /**
     * A {@link Binder} that delegates all calls but silently swallows {@link BindingAlreadyExistsException}
     * on terminal {@link BindingBuilder} operations. Used by {@link #override} to install base bindings
     * without failing when the override module has already registered a binding for the same type.
     */
    private static class SuppressingBinder
        implements Binder {

        private final Binder delegate;

        SuppressingBinder(final Binder delegate) {
            this.delegate = delegate;
        }

        @Override
        public <T> BindingBuilder<T> bind(final Class<T> bindingClass) {
            return new SuppressingBindingBuilder<>(this.delegate.bind(bindingClass));
        }

        @Override
        public <T> MultiBinder<T> bindSet(final Class<T> type) {
            // multibindings accumulate naturally — no conflict suppression needed
            return this.delegate.bindSet(type);
        }
    }

    /**
     * A {@link BindingBuilder} that delegates all operations but silently swallows
     * {@link BindingAlreadyExistsException} when calling the terminal {@code to(...)} methods.
     *
     * @param <T> the type being bound
     */
    private record SuppressingBindingBuilder<T>(BindingBuilder<T> delegate)
        implements BindingBuilder<T> {

        @Override
        public Binding<T> to(final T value) {
            try {
                return this.delegate.to(value);
            } catch (final BindingAlreadyExistsException ignored) {
                return null;
            }
        }

        @Override
        public Binding<T> to(final Class<? extends T> bindingValueClass) {
            try {
                return this.delegate.to(bindingValueClass);
            } catch (final BindingAlreadyExistsException ignored) {
                return null;
            }
        }

        @Override
        public Binding<T> to(final Supplier<T> supplier) {
            try {
                return this.delegate.to(supplier);
            } catch (final BindingAlreadyExistsException ignored) {
                return null;
            }
        }

        @Override
        public Binding<T> toOverriding(final T value) {
            return this.delegate.toOverriding(value);
        }

        @Override
        public Binding<T> toOverriding(final Class<? extends T> implementationClass) {
            return this.delegate.toOverriding(implementationClass);
        }

        @Override
        public Binding<T> toOverriding(final Supplier<T> supplier) {
            return this.delegate.toOverriding(supplier);
        }

        @Override
        public BindingBuilder<T> as(final String name) {
            this.delegate.as(name);
            return this;
        }

        @Override
        public BindingBuilder<T> with(final Class<? extends Annotation> annotationClass) {
            this.delegate.with(annotationClass);
            return this;
        }

        @Override
        public BindingBuilder<T> with(final Annotation annotation) {
            this.delegate.with(annotation);
            return this;
        }

        @Override
        public BindingBuilder<T> with(final AnnotationTypeUsage qualifier) {
            this.delegate.with(qualifier);
            return this;
        }
    }
}
