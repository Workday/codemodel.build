package build.codemodel.framework.initialization;

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

import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.Targetable;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link Plugin} to enrich {@link Traitable}s with new {@link Trait}s.
 * <p>
 * {@link Enricher} implementations are typically located using the {@link java.util.ServiceLoader}
 * as a {@link Plugin} <i>service</i>.
 *
 * @param <T> the type of {@link Traitable}
 * @param <E> the type of {@link Trait}
 * @author brian.oliver
 * @see Traitable
 * @since Feb-2024
 */
public interface Enricher<T extends Traitable, E extends Trait>
    extends Plugin, Targetable<T> {

    @Override
    default Optional<? extends Class<T>> getTargetClass() {
        return getTargetClass(Enricher.class);
    }

    /**
     * Obtains the {@link Class} of {@link Trait} produced by the {@link Enricher}.
     *
     * @return the {@link Optional} {@link Class} of {@link Trait} or {@link Optional#empty()} when it can't be
     * determined
     */
    @SuppressWarnings("unchecked")
    default Optional<? extends Class<E>> getTraitClass() {
        // determine the Class type of E from the implementation of this
        return Arrays.stream(this.getClass().getGenericInterfaces())
            .filter(type -> type instanceof ParameterizedType)
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType().equals(Enricher.class))
            .findFirst()
            .map(type -> type.getActualTypeArguments()[1])
            .filter(type -> type instanceof Class<?>)
            .map(type -> (Class<E>) type);
    }

    /**
     * Determines if the {@link Enricher} may create one or more {@link Trait}s for the specified {@link Traitable}.
     * <p>
     * By default, only one {@link Trait} of the specified {@link #getTraitClass()} produced by the
     * {@link Enricher} is permitted for a {@link Traitable}.  However, this method may be overridden to
     * provide alternative semantics and constraints.
     *
     * @param target the {@link Traitable} target for a {@link Class} of {@link Trait}
     * @return {@code true} if the {@link Trait} may be created for the target, {@code false} otherwise
     */
    default boolean isTraitPermitted(final T target) {

        // traits can't be added to a null Traitable
        if (target == null) {
            return false;
        }

        // determine the class of Trait
        final var traitClass = getTraitClass();

        // only allow the class of Trait to be created when it's not already included in the target
        // (when the class of Trait can't be determined, we assume the Trait isn't permitted)
        return traitClass
            .map(c -> {
                try {
                    return target.getTrait(c).isEmpty();
                }
                catch (final IllegalStateException e) {
                    return true;
                }
            })
            .orElse(false);
    }

    /**
     * Attempts to create zero or more {@link Trait}s for the specified {@link Traitable} target.
     *
     * @param target the target {@link Traitable} for which to create the {@link Trait}s
     * @return a {@link Stream} {@link Trait}s, or {@link Stream#empty()} when none can be created
     */
    Stream<E> create(T target);
}
