package build.codemodel.jdk;

/*-
 * #%L
 * JDK Code Model
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

import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.TypeName;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

/**
 * A set of importable JDK-based {@link TypeName}s.
 *
 * @author brian.oliver
 * @since Jun-2024
 */
public class ImportedTypeNames {

    /**
     * The imported {@link TypeName}s by {@link IrreducibleName}.
     */
    final private LinkedHashMap<IrreducibleName, TypeName> importedTypeNames;

    /**
     * Constructs an empty {@link ImportedTypeNames}.
     */
    public ImportedTypeNames() {
        this.importedTypeNames = new LinkedHashMap<>();
    }

    /**
     * Attempts to include the specified {@link TypeName}.
     * <p>
     * Should the {@link IrreducibleName} of the {@link TypeName} already exist for a different {@link TypeName}, the
     * {@link TypeName} is not included and {@code false} is returned, consequently meaning that the specified
     * {@link TypeName} must be used in a fully-qualified-manner.
     *
     * @param typeName the {@link TypeName}
     * @return {@code true} if the {@link TypeName} was included, {@code false} otherwise
     */
    public boolean include(final TypeName typeName) {
        if (typeName == null) {
            return false;
        }

        final var existing = this.importedTypeNames.get(typeName.name());

        if (existing == null) {
            this.importedTypeNames.put(typeName.name(), typeName);
            return true;
        }

        return existing == typeName;
    }

    /**
     * Obtains the {@link Stream} of imported {@link TypeName}s.
     *
     * @return the imported {@link TypeName}s
     */
    public Stream<TypeName> stream() {
        return this.importedTypeNames.values().stream().sorted();
    }
}
