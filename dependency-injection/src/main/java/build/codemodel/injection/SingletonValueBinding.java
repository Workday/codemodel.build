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

import java.util.Objects;

/**
 * A {@link ValueBinding} that produces a non-{@code null} <i>Singleton</i> value.
 *
 * @param <T> the type of <i>Singleton</i> value
 * @author brian.oliver
 * @since Oct-2024
 */
class SingletonValueBinding<T>
    extends AbstractBinding<T>
    implements ValueBinding<T> {

    /**
     * The non-{@code null} <i>Singleton</i> value.
     */
    private final T value;

    /**
     * Constructs a {@link SingletonValueBinding}.
     *
     * @param dependency the {@link Dependency} defining the type of {@link Binding}
     * @param value      the non-{@code null} <i>Singleton</i> value
     */
    private SingletonValueBinding(final Dependency dependency,
                                  final T value) {

        super(dependency);
        this.value = Objects.requireNonNull(value, "The Value must not be null");
    }

    @Override
    public T value() {
        return this.value;
    }

    /**
     * Obtains the {@link Class} of value.
     *
     * @return the {@link Class} of value
     */
    @SuppressWarnings("unchecked")
    public Class<? extends T> valueClass() {
        return (Class<? extends T>) this.value.getClass();
    }

    /**
     * Constructs a {@link SingletonValueBinding}.
     *
     * @param <T>        the type of value
     * @param dependency the {@link Dependency} defining the type of {@link Binding}
     * @param value      the non-{@code null} <i>Singleton</i> value
     */
    public static <T> SingletonValueBinding<T> of(final Dependency dependency,
                                                  final T value) {

        return new SingletonValueBinding<>(dependency, value);
    }
}
