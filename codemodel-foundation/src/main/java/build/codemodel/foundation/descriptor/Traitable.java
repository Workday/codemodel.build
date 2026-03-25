package build.codemodel.foundation.descriptor;

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

import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides access to add, remove and query the {@link Trait}s-by-{@link Class} defined for a type of {@link Object}.
 * <p>
 * To simplify implementation, types are <strong>strongly recommended</strong> to extend the
 * {@link AbstractTraitable} instead of implementing this interface directly.
 *
 * @author brian.oliver
 * @see Trait
 * @see AbstractTraitable
 * @since Feb-2024
 */
public interface Traitable
    extends Composite {

    /**
     * Obtains the {@link CodeModel} in which the {@link Traitable} is being used.
     *
     * @return the {@link CodeModel}
     */
    CodeModel codeModel();

    /**
     * Determines if the {@link Traitable} has any {@link Trait}s.
     *
     * @return {@code true} when the {@link Traitable} has one or more {@link Trait}s, otherwise {@code false}
     */
    boolean hasTraits();

    /**
     * Determines if the {@link Traitable} has one or more {@link Trait}s assignable to the specified {@link Class}.
     *
     * @param requiredClass the required {@link Class}
     * @return {@code true} when the {@link Traitable} has one or more {@link Trait}s assignable to the specified
     * {@link Class}, otherwise {@code false}
     */
    boolean hasTrait(Class<?> requiredClass);

    /**
     * Adds the specified {@link Trait} to the {@link Traitable}.
     *
     * @param trait the {@link Trait}
     * @throws IllegalArgumentException when a {@link Singular} {@link Trait} of the specified {@link Class} already exists
     * @see #createTrait(Function)
     */
    default <T extends Trait> void addTrait(final T trait)
        throws IllegalArgumentException {

        Objects.requireNonNull(trait, "The Trait must not be null");

        createTrait(_ -> trait);
    }

    /**
     * Adds the {@link Trait} produced by the specified {@link Function} to the {@link Traitable}.
     *
     * @param <C>           the type of {@link Traitable}, typically the {@link Traitable} type itself as {@code this}
     * @param <T>           the type of {@link Trait}
     * @param traitSupplier the {@link Function} to produce a {@link Trait} for the specified {@link Traitable}.
     * @return the created {@link Trait}
     * @throws IllegalArgumentException when a {@link Singular} {@link Trait} of the specified {@link Class} already exists
     * @see #addTrait(Trait)
     */
    <C extends Traitable, T extends Trait> T createTrait(Function<C, T> traitSupplier)
        throws IllegalArgumentException;

    /**
     * Removes the specified {@link Trait}.
     *
     * @param trait the {@link Trait}
     * @return {@code true} if the {@link Trait} was removed, {@code false} otherwise
     */
    <T extends Trait> boolean removeTrait(T trait);

    /**
     * Attempts to compute and add a {@link Trait} produced by the specified {@link Function} if a {@link Trait} of the
     * specified {@link Class} doesn't already exist.
     * <p>
     * The {@link Function} invocation is guaranteed to be <i>atomic</i>, thus ensuring only a single {@link Trait}
     * of the specified {@link Class} will be added to the {@link Traitable}.
     * <p>
     * Should the {@link Function} produce a {@code null}, {@link Optional#empty()} is returned.
     *
     * @param <C>        the type of {@link Traitable}, typically the {@link Traitable} type itself as {@code this}
     * @param <T>        the type of {@link Trait}
     * @param traitClass the {@link Class} of {@link Trait}
     * @param function   the {@link Function} to produce the {@link Trait} with this {@link Traitable}
     * @return the {@link Optional} {@link Trait} if the {@link Class} of {@link Trait} already exists, or
     * the {@link Optional} {@link Trait} produced by the {@link Function}, otherwise {@link Optional#empty()}
     */
    <C extends Traitable, T extends Trait> Optional<T> computeIfAbsent(Class<T> traitClass,
                                                                       Function<C, T> function);

    /**
     * Attempts to compute and replace an existing {@link Trait} with that produced by the provided {@link BiFunction},
     * if a {@link Trait} of the specified {@link Class} exists.
     * <p>
     * The {@link BiFunction} invocation is guaranteed to be <i>atomic</i>, thus ensuring only a single {@link Trait}
     * of the specified {@link Class} will be replaced in the {@link Traitable}.
     * <p>
     * Should the {@link BiFunction} produce a {@code null}, the existing {@link Trait} is removed and
     * {@link Optional#empty()} is returned.
     *
     * @param <C>        the type of {@link Traitable}, typically the {@link Traitable} type itself as {@code this}
     * @param <T>        the type of {@link Trait}
     * @param traitClass the {@link Class} of {@link Trait}
     * @param biFunction the {@link BiFunction} to produce the new {@link Trait} with this {@link Traitable} and
     *                   the existing {@link Trait} as parameters
     * @return the {@link Optional}ly produced new {@link Trait}, or {@link Optional#empty()} if there's no exist
     * {@link Trait} or the {@link BiFunction} returned {@code null}
     */
    <C extends Traitable, T extends Trait> Optional<T> computeIfPresent(Class<T> traitClass,
                                                                        BiFunction<C, T, T> biFunction);

    /**
     * Obtains the {@link Trait}s.
     *
     * @return the {@link Stream} of {@link Trait}s
     */
    Stream<Trait> traits();

    /**
     * Obtains the {@link Trait}s of the specified {@link Class}.
     *
     * @param requiredClass the required {@link Class}
     * @param <T>           the type of required {@link Class}
     * @return the {@link Stream} of {@link Trait}s assignable to the specified {@link Class}
     */
    <T> Stream<T> traits(Class<T> requiredClass);

    /**
     * Obtains the {@link Trait}s of one or more of the specified {@link Class}es.
     *
     * @param requiredClasses the required {@link Class}
     * @return the {@link Stream} of {@link Trait}s assignable to one of the specified {@link Class}es
     */
    Stream<Object> traits(Class<?>... requiredClasses);

    /**
     * Obtains the {@link Trait}s of the specified {@link Class}, where the extracted values satisfy the
     * specified predicate.
     *
     * @param requiredClass the required {@link Class}
     * @param extractor     the extractor {@link Function}
     * @param predicate     the {@link Predicate}
     * @param <T>           the type of requires {@link Class}
     * @param <V>           the type of extracted value
     * @return the {@link Stream} of {@link Trait}s assignable to the specified {@link Class}
     */
    default <T, V> Stream<T> traits(final Class<T> requiredClass,
                                    final Function<? super T, V> extractor,
                                    final Predicate<? super V> predicate) {

        return requiredClass == null || extractor == null || predicate == null || !hasTraits()
            ? Stream.empty()
            : traits(requiredClass)
                .filter(trait -> predicate.test(extractor.apply(trait)));
    }

    /**
     * Obtains the {@link Trait}s of the specified {@link Class}, where the extracted value equals the provided value.
     *
     * @param requiredClass the required {@link Class}
     * @param extractor     the extractor {@link Function}
     * @param value         the value
     * @param <T>           the type of requires {@link Class}
     * @param <V>           the type of extracted value
     * @return the {@link Stream} of {@link Trait}s assignable to the specified {@link Class}
     */
    default <T, V> Stream<T> traits(final Class<T> requiredClass,
                                    final Function<? super T, V> extractor,
                                    final V value) {

        return requiredClass == null || extractor == null || !hasTraits()
            ? Stream.empty()
            : traits(requiredClass)
                .filter(trait -> (value == null && extractor.apply(trait) == null)
                    || (value != null && Objects.equals(value, extractor.apply(trait))));
    }

    /**
     * Obtains the <strong>one-and-only-one</strong> {@link Trait} of the specified {@link Class}.
     *
     * @param requiredClass the required {@link Class}
     * @param <T>           the type of required {@link Class}
     * @return the {@link Optional} {@link Trait} assignable to the specified {@link Class}, otherwise {@link Optional#empty()}
     * @throws IllegalArgumentException should there be more than one {@link Trait} assignable to the specified {@link Class}
     */
    <T> Optional<T> getTrait(Class<T> requiredClass)
        throws IllegalArgumentException;

    /**
     * Obtains the {@link Trait} of the specified {@link Class}, where the extracted value satisfies the
     * specified predicate.
     *
     * @param requiredClass the required {@link Class}
     * @param extractor     the extractor {@link Function}
     * @param predicate     the {@link Predicate}
     * @param <T>           the type of requires {@link Class}
     * @param <V>           the type of extracted value
     * @return the {@link Optional} {@link Trait}, otherwise {@link Optional#empty()}
     * @see #getTrait(Class)
     */
    default <T, V> Optional<T> getTrait(final Class<T> requiredClass,
                                        final Function<? super T, V> extractor,
                                        final Predicate<? super V> predicate) {

        return hasTraits()
            ? traits(requiredClass, extractor, predicate).findFirst()
            : Optional.empty();
    }

    /**
     * Obtains the {@link Trait} of the specified {@link Class}, where the extracted value equals the
     * provided value.
     *
     * @param requiredClass the required {@link Class}
     * @param extractor     the extractor {@link Function}
     * @param value         the value
     * @param <T>           the type of requires {@link Class}
     * @param <V>           the type of extracted value
     * @return the {@link Optional} {@link Trait}, otherwise {@link Optional#empty()}
     * @see #getTrait(Class)
     */
    default <T, V> Optional<T> getTrait(final Class<T> requiredClass,
                                        final Function<? super T, V> extractor,
                                        final V value) {

        return hasTraits()
            ? traits(requiredClass, extractor, value).findFirst()
            : Optional.empty();
    }

    /**
     * Obtains the {@link Trait} of the specified {@link Class}.
     *
     * @param requiredClass the required {@link Class}
     * @param <T>           the type of required {@link Class}
     * @return the {@link Trait} assignable to the specified {@link Class}
     * @throws NoSuchElementException if no such assignable {@link Trait} exists
     * @throws IllegalStateException  if more than one {@link Trait} of the specified {@link Class}
     * @see #getTrait(Class)
     */
    default <T> T trait(final Class<T> requiredClass)
        throws NoSuchElementException, IllegalStateException {

        return getTrait(requiredClass).orElseThrow();
    }

    /**
     * Produces a {@link String} representation of the {@link Trait}s defined by the specified {@link Traitable}.
     *
     * @param traitable the {@link Traitable}
     * @return a {@link String}
     */
    static String toString(final Traitable traitable) {
        if (traitable == null || !traitable.hasTraits()) {
            return "";
        }

        return traitable.traits()
            .map(Object::toString)
            .collect(Collectors.joining(",", " [", "]"));
    }
}
