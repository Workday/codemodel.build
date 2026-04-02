package build.codemodel.foundation.usage;

/*-
 * #%L
 * Code Model Foundation
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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

/**
 * Provides type information concerning the <i>usage</i> of a type in a {@link CodeModel}, for example the type of
 * field or attribute, the type of parameter or the type returned by a method, either generic or otherwise.
 *
 * @author brian.oliver
 * @see TypeDescriptor
 * @since Jan-2024
 */
public interface TypeUsage
    extends Dependent, Traitable {

    /**
     * Attempts to obtain an {@link Optional} representation of or the first part of the {@link TypeUsage} assignable
     * to the specified {@link Class}, or {@link Optional#empty()} if not possible.
     *
     * @param requiredClass the required {@link Class}
     * @param <T>           the type of the required {@link Class}
     * @return the {@link Optional}
     */
    default <T> Optional<T> as(final Class<T> requiredClass) {
        return Objects.nonNull(requiredClass) && requiredClass.isInstance(this)
            ? Optional.of(requiredClass.cast(this))
            : Optional.empty();
    }

    /**
     * Visits {@link TypeUsage} and the <a href="https://en.wikipedia.org/wiki/Transitive_closure">transitive closure</a>
     * of the {@link TypeUsage}s on which this {@link TypeUsage} directly and indirectly depends.
     *
     * @param visitor the {@link TypeUsageVisitor}
     * @see #dependencies()
     */
    default void visit(final TypeUsageVisitor visitor) {
        if (Objects.nonNull(visitor)) {
            final var pending = new LinkedHashSet<TypeUsage>();
            final var visited = new LinkedHashSet<TypeUsage>();
            pending.add(this);

            while (!pending.isEmpty()) {
                final var next = pending.removeFirst();

                if (!visited.contains(next)) {
                    visitor.visit(next);
                    visited.add(next);

                    next.dependencies()
                        .filter(typeUsage -> !visited.contains(typeUsage) && !pending.contains(typeUsage))
                        .forEach(pending::add);
                }
            }
        }
    }

    /**
     * Collects the {@link TypeUsage} and the <a href="https://en.wikipedia.org/wiki/Transitive_closure">transitive closure</a>
     * of {@link TypeUsage}s on which the {@link TypeUsage} depends.
     *
     * @param collector the {@link Collector}
     * @param <A>       the accumulator of {@link TypeUsage}s
     * @param <R>       the result of the {@link Collector}
     * @return the collected result
     * @see #visit(TypeUsageVisitor)
     */
    default <A, R> R collect(final Collector<? super TypeUsage, A, R> collector) {
        Objects.requireNonNull(collector, "The Collector must not be null");

        final var accumulator = collector.supplier().get();
        visit(typeUsage -> collector.accumulator().accept(accumulator, typeUsage));
        return collector.finisher().apply(accumulator);
    }
}
