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

import build.base.foundation.Lazy;

import java.util.Objects;

/**
 * A {@link ClassBinding} for concrete {@link Class} for which only a single non-{@code null} instance may be produced
 *
 * @param <T> the type of {@link Class}
 * @author brian.oliver
 * @since Oct-2024
 */
class LazySingletonClassBinding<T>
    extends AbstractBinding<T>
    implements ClassBinding<T> {

    /**
     * The concrete {@link Class}.
     */
    private final Class<? extends T> concreteClass;

    /**
     * The {@link Lazy} produced <i>Singleton</i> value.
     */
    private final Lazy<T> value;

    /**
     * Constructs a {@link LazySingletonClassBinding}.
     *
     * @param dependency    the {@link Dependency} defining the type of {@link Binding}
     * @param concreteClass the concrete {@link Class}
     */
    LazySingletonClassBinding(final Dependency dependency,
                              final Class<? extends T> concreteClass) {

        super(dependency);
        this.concreteClass = Objects.requireNonNull(concreteClass, "The concrete Class must not be null");
        this.value = Lazy.empty();
    }

    @Override
    public Class<? extends T> concreteClass() {
        return this.concreteClass;
    }

    /**
     * Obtains the {@link Lazy} value.
     *
     * @return the {@link Lazy}
     */
    public Lazy<T> value() {
        return this.value;
    }
}
