package build.codemodel.foundation.naming;

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

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Comparable} {@link Name} consisting of multiple {@link IrreducibleName}s, representing the name of a
 * <i>Module</i>.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public final class ModuleName
    implements Name, Comparable<ModuleName> {

    /**
     * The internal representation of the {@link ModuleName}, as list of {@link IrreducibleName}s, the
     * first-most being the base module name.
     */
    private final ArrayList<IrreducibleName> parts;

    /**
     * The internal representation of the {@link ModuleName} as a {@link String}.
     */
    private final String string;

    /**
     * Constructs a {@link ModuleName} given one or more {@link IrreducibleName}s (parts).
     *
     * @param parts the {@link IrreducibleName} parts
     */
    private ModuleName(final Stream<IrreducibleName> parts) {

        Objects.requireNonNull(parts,
            "The IrreducibleName parts must not be null");

        this.parts = parts
            .peek(part -> {
                if (part.isEmpty()) {
                    throw new IllegalArgumentException(
                        "The ModuleName must not contain empty parts");
                }
            })
            .collect(Collectors.toCollection(ArrayList::new));

        if (this.parts.isEmpty()) {
            throw new IllegalArgumentException("The ModuleName contained no parts");
        }

        // establish an optimized string representation using . as the separator
        this.string = this.parts.stream()
            .map(Object::toString)
            .collect(Collectors.joining("."));
    }

    /**
     * Constructs a {@link ModuleName} given a raw {@link String}, that is made up of one or more
     * {@code .} separated {@link IrreducibleName}s (parts).
     *
     * @param string                  the raw {@link String}
     * @param parts                   the {@link String} parts
     * @param irreducibleNameFunction the {@link Function} to produce {@link IrreducibleName}s from module name parts
     */
    private ModuleName(final String string,
                       final Stream<String> parts,
                       final Function<? super String, ? extends IrreducibleName> irreducibleNameFunction) {

        Objects.requireNonNull(irreducibleNameFunction,
            "The IrreducibleName Function must not be null");

        if (Objects.isNull(string)) {
            throw new IllegalArgumentException("The module name must not be null");
        }

        if (string.isEmpty()) {
            throw new IllegalArgumentException("The module name must not be empty");
        }

        this.string = string;

        this.parts = parts
            .peek(part -> {
                if (part.isEmpty()) {
                    throw new IllegalArgumentException(
                        "The module name must not contain empty parts [" + string + "]");
                }
            })
            .map(irreducibleNameFunction)
            .collect(Collectors.toCollection(ArrayList::new));

        if (this.parts.isEmpty()) {
            throw new IllegalArgumentException("No modules specified in [" + string + "]");
        }
    }

    /**
     * Obtains a {@link Stream} containing the parts of the {@link ModuleName}.
     *
     * @return a {@link Stream} of {@link Name}d parts.
     */
    public Stream<IrreducibleName> parts() {
        return this.parts.stream();
    }

    @Override
    public boolean matches(final String regularExpression) {
        return this.string.matches(regularExpression);
    }

    @Override
    public int length() {
        return this.string.length();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof ModuleName that
            && Objects.equals(this.string, that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.string);
    }

    @Override
    public String toString() {
        return this.string;
    }

    @Override
    public int compareTo(final ModuleName other) {
        return this.string.compareTo(other.toString());
    }

    /**
     * Creates a {@link ModuleName} given one or more {@link IrreducibleName}s (parts).
     *
     * @param parts the {@link IrreducibleName} parts
     * @return a new {@link ModuleName}
     */
    public static ModuleName of(final Stream<IrreducibleName> parts) {
        return new ModuleName(parts);
    }

    /**
     * Creates a {@link ModuleName} given a {@link CharSequence} using the specified {@link NameProvider}.
     *
     * @param characters   the {@link CharSequence}
     * @param nameProvider the {@link NameProvider}
     * @return an {@link Optional} {@link ModuleName}
     */
    public static Optional<ModuleName> of(final CharSequence characters,
                                          final NameProvider nameProvider) {

        if (Objects.isNull(characters) || Objects.isNull(nameProvider)) {
            return Optional.empty();
        }

        return nameProvider.getModuleName(characters);
    }
}
