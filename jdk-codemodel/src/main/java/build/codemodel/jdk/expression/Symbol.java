package build.codemodel.jdk.expression;

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

import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

/**
 * A {@link Trait} on an {@link Identifier} that captures the result of javac symbol resolution:
 * what the identifier refers to and, for variable-kind symbols, its declared type.
 *
 * <p>Exactly one {@link Symbol} may be present on an {@link Identifier} ({@link Singular}).
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@Singular
public sealed interface Symbol extends Trait
    permits Symbol.LocalVariable, Symbol.Parameter, Symbol.Field,
            Symbol.TypeReference, Symbol.ThisReference, Symbol.SuperReference {

    /**
     * A local variable reference, e.g. {@code x} in {@code int x = 1; return x;}.
     *
     * @param declaredType the declared type of the variable
     */
    record LocalVariable(TypeUsage declaredType) implements Symbol {}

    /**
     * A method or constructor parameter reference.
     *
     * @param declaredType the declared type of the parameter
     */
    record Parameter(TypeUsage declaredType) implements Symbol {}

    /**
     * A field reference, e.g. {@code field} in {@code this.field} or a bare {@code field}.
     *
     * @param declaredType the declared type of the field
     */
    record Field(TypeUsage declaredType) implements Symbol {}

    /**
     * A type name reference, e.g. {@code String} in {@code String.valueOf(...)}.
     *
     * @param type the type being referenced
     */
    record TypeReference(TypeUsage type) implements Symbol {}

    /**
     * A {@code this} reference.
     *
     * @param declaredType the type of the enclosing instance
     */
    record ThisReference(TypeUsage declaredType) implements Symbol {}

    /**
     * A {@code super} reference.
     *
     * @param declaredType the type of the direct superclass
     */
    record SuperReference(TypeUsage declaredType) implements Symbol {}
}
