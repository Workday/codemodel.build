package build.codemodel.foundation.usage.pattern;

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

import build.codemodel.foundation.usage.TypeUsage;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents the result of attempting to match a {@link TypeUsage} against a {@link TypeUsagePattern}.
 *
 * @param <T> the type of {@link TypeUsage}
 * @author brian.oliver
 * @since Jul-2025
 */
public interface TypeUsageMatch<T extends TypeUsage> {

    /**
     * Obtains the {@link Optional}ly matched {@link TypeUsage}.
     *
     * @return {@link Optional} {@link TypeUsage} when the match was successful, otherwise {@link Optional#empty()}
     */
    Optional<T> typeUsage();

    /**
     * Maps the matched {@link TypeUsage} to another type using the provided {@link Function}.
     *
     * @param mapper the {@link Function} to map the matched {@link TypeUsage} to another type
     * @param <U>    the type of the mapped value
     * @return an {@link Optional} containing the mapped value when the match was successful,
     * otherwise {@link Optional#empty()}
     */
    default <U> Optional<U> map(final Function<? super T, ? extends U> mapper) {
        return mapper == null
            ? Optional.empty()
            : typeUsage()
                .map(mapper);
    }

    /**
     * Maps the matched {@link TypeUsage} to another {@link Optional} type using the provided {@link Function}.
     *
     * @param mapper the {@link Function} to map the matched {@link TypeUsage} to another {@link Optional} type
     * @param <U>    the type of the {@link Optional} mapped value
     * @return an {@link Optional} containing the mapped value when the match was successful,
     * otherwise {@link Optional#empty()}
     */
    default <U> Optional<U> flatMap(final Function<? super T, Optional<? extends U>> mapper) {
        return mapper == null
            ? Optional.empty()
            : typeUsage()
                .flatMap(mapper);
    }

    /**
     * Filters the matched {@link TypeUsage} using the specified {@link Predicate}.
     *
     * @param filter the {@link Predicate}
     * @return the matched {@link TypeUsage} if it satisfies the {@link Predicate}, otherwise {@link Optional#empty()}
     */
    default Optional<T> filter(final Predicate<? super T> filter) {
        return filter == null
            ? Optional.empty()
            : typeUsage()
                .filter(filter);
    }

    /**
     * Obtains the matched {@link TypeUsage} or throws a {@link NoSuchElementException} if the match was not successful.
     *
     * @return the matched {@link TypeUsage}
     * @throws NoSuchElementException if the match was not successful
     */
    default T orElseThrow() {
        return typeUsage()
            .orElseThrow(() -> new NoSuchElementException("No TypeUsage matched"));
    }

    /**
     * Obtains the matched {@link TypeUsage} or throws an exception supplied by the provided {@link Supplier}.
     *
     * @param <X>      the type of the exception to be thrown
     * @param supplier the {@link Supplier} that produces an exception to be thrown
     * @return the matched {@link TypeUsage}, if present
     * @throws X                    if no {@link TypeUsage} is present
     * @throws NullPointerException if no {@link TypeUsage} is present and the exception
     *                              {@link Supplier} is {@code null}
     */
    default <X extends Throwable> T orElseThrow(final Supplier<? extends X> supplier)
        throws X {

        if (isPresent()) {
            return orElseThrow();
        }
        else {
            throw supplier.get();
        }
    }

    /**
     * Determines if the match was successful and thus a {@link #typeUsage()} is present.
     *
     * @return {@code true} if the match was successful, otherwise {@code false}
     * @see #typeUsage()
     */
    default boolean isPresent() {
        return typeUsage()
            .isPresent();
    }

    /**
     * Determines if the match was not successful and thus a {@link #typeUsage()} is not present.
     *
     * @return {@code true} if the match was not successful, otherwise {@code false}
     * @see #typeUsage()
     */
    default boolean isEmpty() {
        return typeUsage()
            .isEmpty();
    }

    /**
     * If the match was successful, consumes the matched value using the provided {@link Consumer},
     * otherwise does nothing.
     *
     * @param consumer the {@link Consumer} of the matched value
     */
    default void ifPresent(final Consumer<? super T> consumer) {
        if (isPresent() && consumer != null) {
            typeUsage().ifPresent(consumer);
        }
    }

    /**
     * If the match was successful, consumes the matched value using the provided {@link Consumer},
     * otherwise runs the provided {@link Runnable}.
     *
     * @param consumer the {@link Consumer} of the matched value
     * @param runnable the {@link Runnable} to run when the match was not successful
     */
    default void ifPresentOrElse(final Consumer<? super T> consumer,
                                 final Runnable runnable) {

        if (isPresent()) {
            if (consumer != null) {
                typeUsage().ifPresent(consumer);
            }
        }
        else if (runnable != null) {
            runnable.run();
        }
    }

    /**
     * Obtains a {@link Stream} of the matched {@link TypeUsage}.
     *
     * @return a {@link Stream} containing the matched {@link TypeUsage} if present, otherwise an empty {@link Stream}
     */
    default Stream<T> stream() {
        return typeUsage()
            .stream();
    }

    /**
     * Attempts to perform further matching against the currently matched {@link TypeUsage} using the provided
     * {@link TypeUsagePattern}.
     *
     * @param pattern the {@link TypeUsagePattern} to match against the currently matched {@link TypeUsage}
     * @param <U>     the type of {@link TypeUsage} expected by the pattern
     * @param <M>     the type of {@link TypeUsageMatch} expected by the pattern
     * @return the {@link TypeUsageMatch} as a result of the match
     */
    default <U extends TypeUsage, M extends TypeUsageMatch<U>> M match(final TypeUsagePattern<U, M> pattern) {
        return isPresent()
            ? pattern.match(orElseThrow())
            : pattern.failure();
    }
}
