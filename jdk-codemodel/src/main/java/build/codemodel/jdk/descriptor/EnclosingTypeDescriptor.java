package build.codemodel.jdk.descriptor;

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

import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;

import java.util.Objects;

/**
 * A {@link Trait} identifying the enclosing type of a nested class or interface.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class EnclosingTypeDescriptor implements Trait {

    /**
     * The {@link TypeName} of the enclosing type.
     */
    private final TypeName enclosingType;

    /**
     * Constructs an {@link EnclosingTypeDescriptor}.
     *
     * @param enclosingType the {@link TypeName} of the enclosing type
     */
    public EnclosingTypeDescriptor(final TypeName enclosingType) {
        this.enclosingType = Objects.requireNonNull(enclosingType, "The enclosingType must not be null");
    }

    /**
     * Obtains the {@link TypeName} of the enclosing type.
     *
     * @return the {@link TypeName} of the enclosing type
     */
    public TypeName enclosingType() {
        return this.enclosingType;
    }
}
