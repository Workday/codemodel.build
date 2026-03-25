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

import build.codemodel.foundation.usage.pattern.OptionalTypeUsagePattern;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link Resolver} that will resolve a specific {@link Optional} {@link Object} that is assignable to a
 * specified {@link Class}. The {@link Optional} may also resolve as an empty {@link Optional}.
 *
 * @param <T> the type of the {@link Optional} {@link Object} to resolve.
 * @author spencer.firestone
 * @author brian.oliver
 * @since Jul-2025
 */
public class OptionalResolver<T>
    implements Resolver<Optional<T>> {

    /**
     * The {@link Class} of the {@link Object} to resolve as an {@link Optional}.
     */
    private final Class<? extends T> resolvableClass;

    /**
     * The {@link Optional} to be returned when the {@link InjectionPoint} is resolvable.
     */
    private final Optional<T> optional;

    /**
     * Creates an {@link OptionalResolver}.
     *
     * @param <O>             the type of the {@link Object} to resolve
     * @param resolvableClass the {@link Class} of the {@link Object} to resolve as an {@link Optional}
     * @param object          the {@link Object} to resolve as an {@link Optional}. May be {@code null}
     */
    private <O extends T> OptionalResolver(final Class<? extends T> resolvableClass,
                                           final O object) {

        this.resolvableClass = Objects
            .requireNonNull(resolvableClass, "The Resolvable Optional Class must not be null");
        this.optional = Optional.ofNullable(object);
    }

    @Override
    public Optional<? extends Binding<Optional<T>>> resolve(final Dependency dependency) {

        final var nameProvider = dependency.typeUsage()
            .codeModel()
            .getNameProvider();

        return OptionalTypeUsagePattern
            .of(nameProvider.getTypeName(this.resolvableClass))
            .match(dependency.typeUsage())
            .map(_ -> new ValueBinding<Optional<T>>() {
                @Override
                public Optional<T> value() {
                    return OptionalResolver.this.optional;
                }

                @Override
                public Dependency dependency() {
                    return dependency;
                }
            });
    }

    /**
     * Creates an {@link OptionalResolver}.
     *
     * @param <T>         the type of the {@link Class} to resolve
     * @param <O>         the type of the {@link Object} to resolve
     * @param objectClass the class of the object to resolve in an {@link Optional}
     * @param object      the object to resolve in an {@link Optional}. May be {@code null}
     * @return an {@link OptionalResolver}
     */
    public static <T, O extends T> OptionalResolver<T> of(final Class<? extends T> objectClass,
                                                          final O object) {

        return new OptionalResolver<>(objectClass, object);
    }

    /**
     * Creates an {@link OptionalResolver}.
     *
     * @param <T>            the type of the {@link Class} to resolve
     * @param <O>            the type of the {@link Object} to resolve
     * @param objectClass    the {@link Class} of the {@link Object} to resolve as an {@link Optional}
     * @param objectSupplier a {@link Supplier} for the {@link Object} to resolve as an {@link Optional}.
     *                       May supplier may return a {@code null}
     * @return an {@link OptionalResolver}
     */
    public static <T, O extends T> OptionalResolver<T> of(final Class<? extends T> objectClass,
                                                          final Supplier<O> objectSupplier) {

        return new OptionalResolver<>(objectClass, objectSupplier.get());
    }

    /**
     * Creates an empty {@link OptionalResolver}.
     *
     * @param <T>         the type of the {@link Class} to resolve
     * @param objectClass the {@link Class} of the {@link Object} to resolve as an {@link Optional}
     * @return an {@link OptionalResolver}
     */
    public static <T> OptionalResolver<T> empty(final Class<? extends T> objectClass) {
        return new OptionalResolver<>(objectClass, null);
    }
}
