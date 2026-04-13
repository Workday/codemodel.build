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

import java.util.Set;
import java.util.function.Supplier;

/**
 * Accumulates multiple bindings for the same type, making them injectable as {@link Set}, {@code Collection},
 * {@code Iterable}, {@code Stream}, or {@code List}. Obtain a {@link MultiBinder} via
 * {@link Binder#bindSet(Class)}.
 *
 * <p>Calling {@link Binder#bindSet} multiple times for the same element type (e.g. from two different
 * {@link Module}s) returns the same underlying binder so entries accumulate naturally across modules.
 *
 * @param <T> the element type
 * @author reed.vonredwitz
 * @see Binder#bindSet(Class)
 * @since Apr-2026
 */
public interface MultiBinder<T> {

    /**
     * Adds a pre-constructed value to the set.
     *
     * @param value the value
     * @return this {@link MultiBinder} for chaining
     */
    MultiBinder<T> add(T value);

    /**
     * Adds a binding by implementation class, which is instantiated via the {@link Context} at
     * injection time.
     *
     * @param implementationClass the implementation class
     * @return this {@link MultiBinder} for chaining
     */
    MultiBinder<T> add(Class<? extends T> implementationClass);

    /**
     * Adds a binding via supplier, which is called at injection time to produce the value.
     *
     * @param supplier the supplier
     * @return this {@link MultiBinder} for chaining
     */
    MultiBinder<T> add(Supplier<? extends T> supplier);
}
