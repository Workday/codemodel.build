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
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;

/**
 * A {@link Trait} representing a record component on a record type descriptor.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class RecordComponentDescriptor implements Trait {

    /**
     * The {@link IrreducibleName} of the record component.
     */
    private final IrreducibleName name;

    /**
     * The declared {@link TypeUsage} of the record component.
     */
    private final TypeUsage type;

    private RecordComponentDescriptor(final IrreducibleName name, final TypeUsage type) {
        this.name = Objects.requireNonNull(name, "The name must not be null");
        this.type = Objects.requireNonNull(type, "The type must not be null");
    }

    /**
     * Obtains the {@link IrreducibleName} of the record component.
     *
     * @return the {@link IrreducibleName} of the record component
     */
    public IrreducibleName name() {
        return this.name;
    }

    /**
     * Obtains the declared {@link TypeUsage} of the record component.
     *
     * @return the {@link TypeUsage} of the record component
     */
    public TypeUsage type() {
        return this.type;
    }

    /**
     * Creates a {@link RecordComponentDescriptor}.
     *
     * @param name the {@link IrreducibleName} of the record component
     * @param type the declared {@link TypeUsage} of the record component
     * @return a new {@link RecordComponentDescriptor}
     */
    public static RecordComponentDescriptor of(final IrreducibleName name, final TypeUsage type) {
        return new RecordComponentDescriptor(name, type);
    }
}
