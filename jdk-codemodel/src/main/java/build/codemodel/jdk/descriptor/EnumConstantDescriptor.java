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

import java.util.Objects;

/**
 * A {@link Trait} representing an enum constant on an enum type descriptor.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class EnumConstantDescriptor implements Trait {

    /**
     * The {@link IrreducibleName} of the enum constant.
     */
    private final IrreducibleName name;
    private final int order;

    private EnumConstantDescriptor(final IrreducibleName name, final int order) {
        this.name = Objects.requireNonNull(name, "The name must not be null");
        this.order = order;
    }

    /**
     * Obtains the {@link IrreducibleName} of the enum constant.
     *
     * @return the {@link IrreducibleName} of the enum constant
     */
    public IrreducibleName name() {
        return this.name;
    }

    /**
     * Obtains the zero-based declaration order of this enum constant within its type.
     *
     * @return the declaration order
     */
    public int order() {
        return this.order;
    }

    /**
     * Creates an {@link EnumConstantDescriptor}.
     *
     * @param name  the {@link IrreducibleName} of the enum constant
     * @param order the zero-based declaration order
     * @return a new {@link EnumConstantDescriptor}
     */
    public static EnumConstantDescriptor of(final IrreducibleName name, final int order) {
        return new EnumConstantDescriptor(name, order);
    }
}
