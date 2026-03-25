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
 * A provider of {@link Name}s.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public interface NameProvider {

    /**
     * Obtains an {@link IrreducibleName} defined by the specified {@link String}.
     *
     * @param string the {@link String}
     * @return an {@link IrreducibleName}
     */
    IrreducibleName getIrreducibleName(String string);

    /**
     * Obtains an {@link IrreducibleName} defined by the specified {@link CharSequence}.
     *
     * @param charSequence the {@link CharSequence}
     * @return an {@link IrreducibleName}
     */
    default IrreducibleName getIrreducibleName(final CharSequence charSequence) {
        return getIrreducibleName(charSequence.toString());
    }

    /**
     * Obtains a {@link Namespace} defined by the specified {@link String}.
     *
     * @param string the {@link String}
     * @return an {@link Optional} {@link Namespace},
     * {@link Optional#empty()} when the {@link String} is not a valid namespace
     */
    Optional<Namespace> getNamespace(String string);

    /**
     * Obtains a {@link Namespace} defined by the specified {@link CharSequence}.
     *
     * @param charSequence the {@link CharSequence}
     * @return an {@link Optional} {@link Namespace},
     * {@link Optional#empty()} when the {@link String} is not a valid namespace
     */
    default Optional<Namespace> getNamespace(final CharSequence charSequence) {
        return charSequence == null
            ? Optional.empty()
            : getNamespace(charSequence.toString());
    }

    /**
     * Obtains an {@link ModuleName} defined by the specified {@link String}.
     *
     * @param string the {@link String}
     * @return a {@link Optional} {@link ModuleName},
     * {@link Optional#empty()} when the {@link String} is not a valid module name
     */
    Optional<ModuleName> getModuleName(String string);

    /**
     * Obtains an {@link ModuleName} defined by the specified {@link CharSequence}.
     *
     * @param charSequence the {@link CharSequence}
     * @return a {@link Optional} {@link ModuleName},
     * {@link Optional#empty()} when the {@link String} is not a valid module name
     */
    default Optional<ModuleName> getModuleName(final CharSequence charSequence) {
        return charSequence == null
            ? Optional.empty()
            : getModuleName(charSequence.toString());
    }

    /**
     * Obtains a {@link TypeName}.
     *
     * @param moduleName        the {@link ModuleName} of the {@link TypeName}
     * @param namespace         the {@link Namespace} of the {@link TypeName}
     * @param enclosingTypeName the {@link TypeName} of the type in which the {@link TypeName} is
     *                          defined (the enclosing type)
     * @param irreducibleName   the {@link IrreducibleName} of the {@link TypeName}
     * @return a {@link TypeName}
     */
    TypeName getTypeName(Optional<ModuleName> moduleName,
                         Optional<Namespace> namespace,
                         Optional<TypeName> enclosingTypeName,
                         IrreducibleName irreducibleName);

    /**
     * Obtains a {@link TypeName}.
     *
     * @param namespace       the {@link Namespace} of the {@link TypeName}
     * @param irreducibleName the {@link IrreducibleName} of the {@link TypeName}
     * @return a {@link TypeName}
     */
    default TypeName getTypeName(final Optional<Namespace> namespace,
                                 final IrreducibleName irreducibleName) {

        return getTypeName(Optional.empty(), namespace, Optional.empty(), irreducibleName);
    }

    /**
     * Obtains a {@link TypeName} given a fully-qualified modular JDK-based type name.
     *
     * @param moduleName             the {@link Optional} {@link ModuleName}
     * @param fullyQualifiedTypeName the fully-qualified-JDK-based type name
     * @return a {@link TypeName}
     */
    default TypeName getTypeName(final Optional<ModuleName> moduleName,
                                 final String fullyQualifiedTypeName) {

        Objects.requireNonNull(moduleName, "The ModuleName must not be null");
        Objects.requireNonNull(fullyQualifiedTypeName,
            "The fully-qualified-type-name must not be null");

        final var nameStart = fullyQualifiedTypeName.lastIndexOf('.');
        if (nameStart < 0) {
            return getTypeName(moduleName,
                Optional.empty(),
                Optional.empty(),
                getIrreducibleName(fullyQualifiedTypeName));
        }
        else {
            final var namespace = fullyQualifiedTypeName.substring(0, nameStart);
            final var typeName = fullyQualifiedTypeName.substring(nameStart + 1);
            return getTypeName(moduleName,
                getNamespace(namespace),
                Optional.empty(),
                getIrreducibleName(typeName));
        }
    }

    /**
     * Creates a {@link TypeName} for the specified {@link Class}.
     *
     * @param c the {@link Class}
     * @return an {@link TypeName}
     */
    default TypeName getTypeName(final Class<?> c) {

        if (Objects.isNull(c)) {
            throw new IllegalArgumentException("The class for the TypeName must not be null");
        }

        // determine the IrreducibleName from the simple name
        final var simpleName = c.getSimpleName();
        final var irreducibleName = getIrreducibleName(simpleName);

        final var namespace = c.getPackageName().isEmpty()
            ? Optional.<Namespace>empty()
            : getNamespace(c.getPackageName());

        final var moduleName = c.getModule().isNamed()
            ? getModuleName(c.getModule().getName())
            : Optional.<ModuleName>empty();

        final var enclosingTypeName = c.getDeclaringClass() == null
            ? Optional.<TypeName>empty()
            : Optional.of(getTypeName(c.getDeclaringClass()));

        return getTypeName(moduleName,
            namespace,
            enclosingTypeName,
            irreducibleName);
    }

    /**
     * Parses the {@link TypeName#toString()} encoded form of a{@link TypeName} {@link String} into a {@link TypeName}.
     *
     * @param encodedTypeName the {@link TypeName#toString()} encoded type name
     * @return the {@link TypeName}
     */
    default TypeName getTypeName(final String encodedTypeName) {
        final var enclosingTypeIndex = encodedTypeName.indexOf(':');
        final var enclosingTypeName = enclosingTypeIndex >= 0
            ? Optional.of(getTypeName(encodedTypeName.substring(0, enclosingTypeIndex)))
            : Optional.<TypeName>empty();

        String remaining = encodedTypeName.substring(enclosingTypeIndex + 1);

        final var moduleNameIndex = remaining.indexOf('/');
        final var moduleName = moduleNameIndex >= 0
            ? getModuleName(remaining.substring(0, moduleNameIndex))
            : Optional.<ModuleName>empty();

        remaining = remaining.substring(moduleNameIndex + 1);

        final var namespaceIndex = remaining.lastIndexOf('.');
        final var namespace = namespaceIndex >= 0
            ? getNamespace(remaining.substring(0, namespaceIndex))
            : Optional.<Namespace>empty();

        remaining = remaining.substring(namespaceIndex + 1);

        final var name = getIrreducibleName(remaining);

        return getTypeName(moduleName, namespace, enclosingTypeName, name);
    }
}
