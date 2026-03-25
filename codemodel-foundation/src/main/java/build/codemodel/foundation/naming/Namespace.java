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

import build.base.foundation.stream.Streams;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link Comparable} {@link Name} consisting of multiple {@link IrreducibleName}s parts, representing the structural
 * composition of a namespace for a {@link TypeName}.
 * <p>
 * The {@link #parts()} of a {@link Namespace} thus have a semantic meaning, as they define in order that a part is a
 * sub-part of another part.  When a {@link Namespace} has only a single part, it is considered the <i>root</i>
 * {@link Namespace}.  When a {@link Namespace} has multiple parts, the first-most part is considered
 * the <i>root</i> {@link Namespace} and the last-most part is considered the <i>leaf</i> {@link Namespace}.
 *
 * @author brian.oliver
 * @see TypeName
 * @since Jan-2024
 */
public final class Namespace
    implements Name, Comparable<Namespace> {

    /**
     * The parent {@link Namespace} in which this {@link Namespace} is defined.
     */
    private Optional<Namespace> parent;

    /**
     * The {@link IrreducibleName} for this {@link Namespace} within the parent {@link Namespace}.
     */
    private IrreducibleName name;

    /**
     * Constructs a {@link Namespace} given its {@link Optional} parent {@link Namespace} and an {@link IrreducibleName}.
     *
     * @param parent the {@link Optional} parent {@link Namespace} in which this {@link Namespace} is defined
     * @param name   the {@link IrreducibleName} for this {@link Namespace} within the parent {@link Namespace}
     */
    private Namespace(final Optional<Namespace> parent,
                      final IrreducibleName name) {

        this.parent = parent == null
            ? Optional.empty()
            : parent;

        this.name = name;
    }

    /**
     * Constructs a {@link Namespace} given its parent {@link Namespace} and an {@link IrreducibleName}.
     *
     * @param parent the parent {@link Namespace} in which this {@link Namespace} is defined, or {@code null}
     * @param name   the {@link IrreducibleName} for this {@link Namespace} within the parent {@link Namespace}
     */
    private Namespace(final Namespace parent,
                      final IrreducibleName name) {

        this(Optional.ofNullable(parent), name);
    }

    /**
     * Constructs a {@link Namespace} given an {@link IrreducibleName} and no parent {@link Namespace}.
     *
     * @param name the {@link IrreducibleName} for this {@link Namespace}
     */
    private Namespace(final IrreducibleName name) {
        this(Optional.empty(), name);
    }

    /**
     * Obtains the {@link Optional} parent {@link Namespace} in which this {@link Namespace} is defined.
     *
     * @return the {@link Optional} parent {@link Namespace}, or an empty {@link Optional} if this is a root {@link Namespace}
     */
    public Optional<Namespace> parent() {
        return this.parent;
    }

    /**
     * Obtains the level of this {@link Namespace} within the hierarchy of {@link Namespace}s, where the <i>root</i>
     * {@link Namespace} is at level 0.
     *
     * @return the level of this {@link Namespace}
     */
    public int level() {
        return this.parent.map(Namespace::level).orElse(0) + 1;
    }

    /**
     * Obtains a {@link Stream} containing the {@link IrreducibleName} parts of the entire {@link Namespace}, from and
     * including the <i>root</i> {@link Namespace} to {@code this} {@link Namespace}.
     *
     * @return a {@link Stream} of {@link IrreducibleName}d parts.
     */
    public Stream<IrreducibleName> parts() {
        return this.parent
            .map(parent -> Streams.concat(parent.parts(), Stream.of(this.name)))
            .orElseGet(() -> Stream.of(this.name));
    }

    /**
     * Obtains the <i>root</i> {@link IrreducibleName} of the {@link Namespace}.
     *
     * @return the <i>root</i> {@link IrreducibleName} of the {@link Namespace}
     */
    public IrreducibleName root() {
        return this.parent.isEmpty()
            ? this.name
            : this.parent.get().root();
    }

    /**
     * Determines if the {@link Namespace} is a <i>root</i> {@link Namespace}.
     *
     * @return {@code true} if the {@link Namespace} is a <i>root</i> {@link Namespace}, otherwise {@code false}
     */
    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    @Override
    public boolean matches(final String regularExpression) {
        return toString().matches(regularExpression);
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof Namespace that
            && Objects.equals(toString(), that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public String toString() {
        return this.parent.map(parent -> parent + "." + this.name.toString()).
            orElseGet(() -> this.name.toString());
    }

    @Override
    public int compareTo(final Namespace other) {
        return toString().compareTo(other.toString());
    }

    /**
     * Attempts to create a {@link Namespace} given its {@link Optional} parent {@link Namespace} and an {@link IrreducibleName}.
     *
     * @param parent the {@link Optional} parent {@link Namespace} in which this {@link Namespace} is defined
     * @param name   the {@link IrreducibleName} for this {@link Namespace} within the parent {@link Namespace}
     * @return the {@link Optional} {@link Namespace} with the specified parent and name
     */
    public static Optional<Namespace> of(final Optional<Namespace> parent,
                                         final IrreducibleName name) {

        return name == null || name.isEmpty()
            ? Optional.empty()
            : Optional.of(new Namespace(parent, name));
    }

    /**
     * Attempts to create a {@link Namespace} given its parent {@link Namespace} and an {@link IrreducibleName}.
     *
     * @param parent the parent {@link Namespace} in which this {@link Namespace} is defined, or {@code null}
     * @param name   the {@link IrreducibleName} for this {@link Namespace} within the parent {@link Namespace}
     * @return the {@link Optional}  {@link Namespace} with the specified parent and name
     */
    public static Optional<Namespace> of(final Namespace parent,
                                         final IrreducibleName name) {

        return name == null || name.isEmpty()
            ? Optional.empty()
            : Optional.of(new Namespace(parent, name));
    }

    /**
     * Attempts to create a {@link Namespace} given an {@link IrreducibleName} and no parent {@link Namespace}.
     *
     * @param name the {@link IrreducibleName} for this {@link Namespace}
     * @return the {@link Optional}  {@link Namespace} with the specified name and no parent
     */
    public static Optional<Namespace> of(final IrreducibleName name) {
        return name == null || name.isEmpty()
            ? Optional.empty()
            : Optional.of(new Namespace(name));
    }

    /**
     * Creates a {@link Namespace} given a {@link CharSequence} using the specified {@link NameProvider}.
     *
     * @param characters   the {@link CharSequence}
     * @param nameProvider the {@link NameProvider}
     * @return an {@link Optional} {@link Namespace}
     */
    public static Optional<Namespace> of(final CharSequence characters,
                                         final NameProvider nameProvider) {

        if (Objects.isNull(characters) || Objects.isNull(nameProvider)) {
            return Optional.empty();
        }

        return nameProvider.getNamespace(characters);
    }
}
