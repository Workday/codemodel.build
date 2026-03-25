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

import build.base.foundation.stream.Streams;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * An immutable {@link Resolver} defined by zero or more other {@link Resolver}s, to be consulted in-order of definition.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public class ChainedResolver
    implements Resolver<Object> {

    /**
     * The {@link Resolver}s used to resolve values.
     */
    private final CopyOnWriteArrayList<Resolver<Object>> resolvers;

    /**
     * Constructs an {@link ChainedResolver} given zero or more {@link Resolver}s.
     *
     * @param resolvers the {@link Resolver}s
     */
    @SuppressWarnings("unchecked")
    private ChainedResolver(final Resolver<?>... resolvers) {
        this.resolvers = Streams.of(resolvers)
            .filter(Objects::nonNull)
            .map(resolver -> (Resolver<Object>) resolver)
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    @Override
    public Optional<? extends Binding<Object>> resolve(final Dependency dependency) {
        for (final Resolver<Object> resolver : this.resolvers) {
            final var resolved = resolver.resolve(dependency);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        return Optional.empty();
    }

    /**
     * Adds the specified {@link Resolver}.
     *
     * @param resolver the {@link Resolver}
     * @return this {@link ChainedResolver} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public ChainedResolver addResolver(final Resolver<?> resolver) {
        if (resolver != null) {
            this.resolvers.add((Resolver<Object>) resolver);
        }
        return this;
    }

    /**
     * Creates a new {@link ChainedResolver}.
     *
     * @param resolvers the {@link Resolver}s
     * @return an {@link ChainedResolver}
     */
    public static ChainedResolver create(final Resolver<?>... resolvers) {
        return new ChainedResolver(resolvers);
    }
}
