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

import java.util.Objects;
import java.util.Optional;

/**
 * A representation for the name of a <i>Type</i>, optionally scoped by its {@link ModuleName} and {@link Namespace}.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public final class TypeName
    implements Name, Comparable<TypeName> {

    /**
     * A constant representing an empty {@link TypeName}.
     */
    private static final TypeName EMPTY = new TypeName(Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        IrreducibleName.of(""));

    /**
     * The {@link Optional} {@link ModuleName} that defines the {@link TypeName}.
     */
    private Optional<ModuleName> moduleName;

    /**
     * The {@link Optional} {@link Namespace} defining the {@link TypeName}.
     */
    private Optional<Namespace> namespace;

    /**
     * The {@link Optional} {@link TypeName} in which this {@link TypeName} is defined.
     */
    private Optional<TypeName> enclosingTypeName;

    /**
     * The {@link IrreducibleName} of the {@link TypeName}.
     */
    private final IrreducibleName irreducibleName;

    /**
     * The internal {@link String} representation of the {@link TypeName}
     */
    private final String string;

    /**
     * Constructs a {@link TypeName}.
     *
     * @param moduleName        the {@link Optional} {@link ModuleName}
     * @param namespace         the {@link Optional} {@link Namespace}
     * @param enclosingTypeName the {@link Optional} enclosing {@link TypeName}
     * @param irreducibleName   the {@link IrreducibleName} for the {@link TypeName}
     */
    private TypeName(final Optional<ModuleName> moduleName,
                     final Optional<Namespace> namespace,
                     final Optional<TypeName> enclosingTypeName,
                     final IrreducibleName irreducibleName) {

        this.moduleName = moduleName == null ? Optional.empty() : moduleName;
        this.namespace = namespace == null ? Optional.empty() : namespace;
        this.enclosingTypeName = enclosingTypeName == null ? Optional.empty() : enclosingTypeName;
        this.irreducibleName =
            Objects.requireNonNull(irreducibleName, "The name of the type must not be null");

        // construct the internal string representation of the TypeName
        final var builder = new StringBuilder();

        this.enclosingTypeName
            .ifPresent(enclosingType -> {
                builder.append(enclosingType.canonicalName());
                builder.append(":");
            });

        if (builder.isEmpty()) {
            this.moduleName
                .ifPresent(name -> {
                    builder.append(name);
                    builder.append("/");
                });
        }

        this.namespace
            .ifPresent(name -> {
                name.parts()
                    .forEach(part -> {
                        builder.append(part);
                        builder.append(".");
                    });
            });

        builder.append(this.irreducibleName);

        this.string = builder.toString();
    }

    /**
     * The {@link Optional} {@link ModuleName} in which the {@link TypeName} is defined.
     *
     * @return the {@link Optional} {@link ModuleName}
     */
    public Optional<ModuleName> moduleName() {
        return this.moduleName;
    }

    /**
     * The {@link Optional} {@link Namespace} in which the {@link TypeName} is defined.
     *
     * @return the {@link Optional} {@link Namespace}
     */
    public Optional<Namespace> namespace() {
        return this.namespace;
    }

    /**
     * Obtains the {@link Optional} {@link TypeName} in which this {@link TypeName} is defined (the
     * enclosing type).
     *
     * @return the {@link Optional} {@link TypeName}
     */
    public Optional<TypeName> enclosingTypeName() {
        return this.enclosingTypeName;
    }

    /**
     * The {@link IrreducibleName} name of the {@link TypeName}.
     *
     * @return the {@link IrreducibleName}
     */
    public IrreducibleName name() {
        return this.irreducibleName;
    }

    /**
     * Obtains the <i>canonical-name</i>.
     *
     * @return the canonical name
     */
    public String canonicalName() {
        return namespace()
            .map(p -> p + ".")
            .orElse("")
            + enclosingTypeName()
            .map(e -> e.name() + "$")
            .orElse("")
            + name().toString();
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

        return object instanceof TypeName that
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
    public int compareTo(final TypeName other) {
        return this.string.compareTo(other.toString());
    }

    /**
     * Creates a {@link TypeName}.
     *
     * @param moduleName        the {@link Optional} {@link ModuleName}
     * @param namespace         the {@link Optional} {@link Namespace}
     * @param enclosingTypeName the {@link Optional} enclosing {@link TypeName}
     * @param irreducibleName   the {@link IrreducibleName} for the {@link TypeName}
     * @return an {@link TypeName}
     */
    public static TypeName of(final Optional<ModuleName> moduleName,
                              final Optional<TypeName> enclosingTypeName,
                              final Optional<Namespace> namespace,
                              final IrreducibleName irreducibleName) {

        return new TypeName(moduleName, namespace, enclosingTypeName, irreducibleName);
    }

    /**
     * Obtains an empty {@link TypeName}.
     *
     * @return an empty {@link TypeName}
     */
    public static TypeName empty() {
        return EMPTY;
    }
}
