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

import java.util.function.Consumer;

/**
 * A {@link Binder} provides the ability to create {@link Binding}s.
 *
 * @author brian.oliver
 * @see Injector
 * @see Resolver
 * @since Oct-2024
 */
public interface Binder {

    /**
     * Creates a new fluent {@link BindingBuilder} for the specified {@link Class} of {@link Binding},
     * allowing definition, configuration and construction of a {@link Binding}.
     *
     * @param <T>          the type of {@link Binding}
     * @param bindingClass the {@link Class} for which to create a {@link Binding}
     * @return a new {@link BindingBuilder}
     */
    <T> BindingBuilder<T> bind(Class<T> bindingClass);

    /**
     * Configures the {@link Binder} using the specified {@link Consumer}.
     *
     * @param consumer the {@link Consumer}
     * @return this {@link Binder} to permit fluent-style method invocation
     */
    default Binder configure(final Consumer<? super Binder> consumer) {
        if (consumer != null) {
            consumer.accept(this);
        }

        return this;
    }
}
