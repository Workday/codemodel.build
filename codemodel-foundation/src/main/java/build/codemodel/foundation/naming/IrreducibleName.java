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

import build.base.foundation.Strings;

import java.util.Objects;

/**
 * A simple {@link Comparable} {@link Name} that is irreducible into other meaningful parts.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public final class IrreducibleName
    implements Name, Comparable<IrreducibleName> {

    /**
     * The internal representation of the {@link IrreducibleName}.
     */
    private final String string;

    /**
     * An empty {@link IrreducibleName}.
     */
    private final static IrreducibleName EMPTY = new IrreducibleName("");

    /**
     * Constructs a {@link IrreducibleName} given a {@link CharSequence}.
     *
     * @param string the {@link String}
     */
    private IrreducibleName(final String string) {
        this.string = Objects.isNull(string) || string.isEmpty()
            ? ""
            : string;
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

        return object instanceof IrreducibleName that
            && Objects.equals(this.string, that.toString());
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @Override
    public String toString() {
        return this.string;
    }

    @Override
    public int compareTo(final IrreducibleName other) {
        return this.string.compareTo(other.toString());
    }

    /**
     * Creates a {@link IrreducibleName}.
     * <p>
     * <strong>NOTE:</strong> Applications should use the {@link NameProvider#getIrreducibleName(String)} method
     * instead of this method to allow name-based caching to occur.
     *
     * @param string the {@link String}
     * @return a {@link IrreducibleName}
     */
    public static IrreducibleName of(final String string) {
        if (Objects.isNull(string) || Strings.isEmpty(string.trim())) {
            return EMPTY;
        }

        return new IrreducibleName(string);
    }

    /**
     * Obtains an empty {@link IrreducibleName}.
     *
     * @return an empty {@link IrreducibleName}
     */
    public static IrreducibleName empty() {
        return EMPTY;
    }
}
