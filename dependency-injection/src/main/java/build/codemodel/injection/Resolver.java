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

import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;
import java.util.function.Function;

/**
 * A facility to resolve {@link Binding}s for {@link Dependency}s required for injection.
 * <p>
 * A {@link Resolver} may be defined for a single type of {@link Binding} for a {@link Dependency}
 * (monomorphic) or for numerous types of {@link Binding}s for {@link Dependency}s (polymorphic).  When a
 * {@link Resolver} implementation is monomorphic, the resolvable type should be specified using the generic parameter
 * &lt;T&gt; for the {@link Resolver}.  When a {@link Resolver} implementation is polymorphic, the generic parameter
 * &lt;T&gt; for the {@link Resolver} should be defined as {@link Object}, and not left as a <i>raw type</i>.
 *
 * @param <T> the type of {@link Binding} value
 * @author brian.oliver
 * @see TypeUsage
 * @since Oct-2024
 */
@FunctionalInterface
public interface Resolver<T> {

    /**
     * Attempts to resolve a value for the type specified by the {@link Dependency}.
     *
     * @param dependency the {@link Dependency}
     * @return the {@link Optional} value if resolvable, {@link Optional#empty()} otherwise
     */
    Optional<? extends Binding<T>> resolve(Dependency dependency);

    /**
     * Creates a {@link Resolver} of values using the specified {@link Function} from a {@link Dependency}
     * to an {@link Optional} {@link Binding}.
     *
     * @param function the {@link Function}
     * @param <T>      the type of value to resolve
     * @return the {@link Optional}ly resolved value
     */
    static <T> Resolver<T> of(final Function<? super Dependency, Optional<? extends Binding<T>>> function) {
        return function::apply;
    }
}
