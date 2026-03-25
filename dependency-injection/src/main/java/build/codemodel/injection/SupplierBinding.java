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
import java.util.function.Supplier;

/**
 * A {@link ValueBinding} that uses a {@link Supplier} to produce non-{@code null} values.
 *
 * @param <T> the type of value
 * @author brian.oliver
 * @since Oct-2024
 */
class SupplierBinding<T>
    extends AbstractBinding<T>
    implements ValueBinding<T> {

    /**
     * The non-{@code null} {@link Supplier}.
     */
    private final Supplier<T> supplier;

    /**
     * Constructs a {@link SupplierBinding} with the specified {@link java.util.function.Supplier}
     *
     * @param dependency the {@link Dependency} defining the type of {@link Binding}
     * @param supplier   the non-{@code null} {@link Supplier}
     */
    SupplierBinding(final Dependency dependency,
                    final Supplier<T> supplier) {

        super(dependency);
        this.supplier = Objects.requireNonNull(supplier, "The Supplier must not be null");
    }

    @Override
    public T value() {
        final var value = this.supplier.get();
        if (value == null) {
            throw new IllegalStateException("Supplier for " + dependency() + " returned null value");
        }
        return value;
    }
}
