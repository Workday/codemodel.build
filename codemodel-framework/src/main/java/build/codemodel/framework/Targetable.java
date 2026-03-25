package build.codemodel.framework;

/*-
 * #%L
 * Code Model Framework
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

import build.base.foundation.Introspection;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * A type which is parameterized for a particular class.
 *
 * @param <T> the target type to be handled
 * @author reed.vonredwitz
 * @since Aug-2024
 */
public interface Targetable<T>
    extends Plugin {

    /**
     * Obtains the first actual type parameter of the specified parameterized {@link Class}.
     *
     * @param parameterizedClass the parameterized {@link Class}.
     *
     * @return the {@link Optional} {@link Class} of the target type, or {@link Optional#empty()}
     * if it can't be determined
     */
    @SuppressWarnings("unchecked")
    default Optional<? extends Class<T>> getTargetClass(final Class<?> parameterizedClass) {
        // determine the Class type of T from the implementation of this
        return Introspection.getAllGenericInterfaces(this.getClass())
            .filter(type -> type instanceof ParameterizedType)
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType().equals(parameterizedClass))
            .findFirst()
            .map(type -> type.getActualTypeArguments()[0])
            .filter(type -> type instanceof Class<?>)
            .map(type -> (Class<T>) type);
    }

    /**
     * Obtains the {@link Class} of the target type.
     *
     * @return the {@link Optional} {@link Class} of the target type, or {@link Optional#empty()}
     * if it can't be determined
     */
    Optional<? extends Class<T>> getTargetClass();
}
