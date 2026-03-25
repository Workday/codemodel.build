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
 * A {@link Binding} that produces the same non-{@code null} value when requested.
 *
 * @param <T> the value produced by the {@link Binding}
 * @author brian.oliver
 * @since Oct-2024
 */
public interface ValueBinding<T>
    extends Binding<T> {

    /**
     * Obtains the non-{@code null} value.
     *
     * @return the non-{@code null} value
     */
    T value();

    /**
     * Creates a {@link ValueBinding} for the specified {@link Dependency} and value.
     *
     * @param dependency the {@link Dependency}
     * @param value      the value
     * @param <T>        the type of value
     * @return a new {@link ValueBinding}
     */
    static <T> ValueBinding<T> of(final Dependency dependency, final T value) {
        Objects.requireNonNull(dependency, "The Dependency must not be null");
        Objects.requireNonNull(value, "The value must not be null");
        return new ValueBinding<>() {
            @Override
            public T value() {
                return value;
            }

            @Override
            public Dependency dependency() {
                return dependency;
            }
        };
    }
}
