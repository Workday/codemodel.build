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

import build.base.foundation.iterator.Iterators;
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

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
public sealed interface Symbol extends Trait, Composite
    permits Symbol.LocalVariable, Symbol.Parameter, Symbol.Field,
    Symbol.TypeReference, Symbol.ThisReference, Symbol.SuperReference {

    /**
     * The {@link TypeUsage} carried by this symbol, exposed for mereological traversal.
     *
     * @return the type this symbol refers to or declares
     */
    TypeUsage declaredType();

    @Override
    default <T> Iterator<T> iterator(final Class<T> kind) {
        final var usage = declaredType();
        return kind.isInstance(usage)
            ? Iterators.of(kind.cast(usage))
            : Iterators.empty();
    }

    /**
     * A local variable reference, e.g. {@code x} in {@code int x = 1; return x;}.
     *
     * @param declaredType the declared type of the variable
     * @param declaration  the {@link Optional} {@link LocalVariableDeclaration} statement that
     *                     declared this variable, when it could be resolved back to the
     *                     declaring statement within the same body conversion
     */
    record LocalVariable(TypeUsage declaredType, Optional<LocalVariableDeclaration> declaration) implements Symbol {

        /**
         * Constructs a {@link LocalVariable} with no resolved declaring statement.
         *
         * @param declaredType the declared type of the variable
         */
        public LocalVariable(final TypeUsage declaredType) {
            this(declaredType, Optional.empty());
        }
    }

    /**
     * A method or constructor parameter reference.
     *
     * @param descriptor the resolved {@link FormalParameterDescriptor} declaring this parameter
     */
    record Parameter(FormalParameterDescriptor descriptor) implements Symbol {
        @Override
        public TypeUsage declaredType() {
            return descriptor.type();
        }
    }

    /**
     * A field reference, e.g. {@code field} in {@code this.field} or a bare {@code field}.
     *
     * <p>{@link #descriptor()} looks the {@link FieldDescriptor} up live against
     * {@link #codeModel()} on every call, the same way a {@link build.codemodel.foundation.usage.NamedTypeUsage}
     * resolves its {@link TypeName} live, rather than pinning to whatever {@link FieldDescriptor}
     * object existed at parse time. This keeps the resolution tracking the declaring type across a
     * {@link build.codemodel.jdk.JDKCodeModel#rescan} of that type instead of going stale when the
     * type is evicted and re-created.
     *
     * @param declaredType  the declared type of the field, resolved independently of {@link #descriptor()}
     * @param codeModel     the {@link CodeModel} to resolve {@link #descriptor()} against
     * @param declaringType the {@link TypeName} of the type declaring the resolved field
     * @param fieldName     the simple name of the resolved field
     */
    record Field(TypeUsage declaredType, CodeModel codeModel, TypeName declaringType,
                String fieldName) implements Symbol {

        public Field {
            Objects.requireNonNull(declaredType, "declaredType must not be null");
            Objects.requireNonNull(codeModel, "codeModel must not be null");
            Objects.requireNonNull(declaringType, "declaringType must not be null");
            Objects.requireNonNull(fieldName, "fieldName must not be null");
        }

        /**
         * Resolves the {@link FieldDescriptor} this reference currently refers to.
         *
         * @return the resolved {@link FieldDescriptor}, or {@link Optional#empty()} if the
         *     declaring type or the matching field is no longer present in {@link #codeModel()}
         */
        public Optional<FieldDescriptor> descriptor() {
            return codeModel.getTypeDescriptor(declaringType)
                .flatMap(typeDescriptor -> typeDescriptor.traits(FieldDescriptor.class)
                    .filter(fd -> fd.fieldName().toString().equals(fieldName))
                    .findFirst());
        }
    }

    /**
     * A type name reference, e.g. {@code String} in {@code String.valueOf(...)}.
     *
     * @param declaredType the type being referenced
     */
    record TypeReference(TypeUsage declaredType) implements Symbol {
    }

    /**
     * A {@code this} reference.
     *
     * @param declaredType the type of the enclosing instance
     */
    record ThisReference(TypeUsage declaredType) implements Symbol {
    }

    /**
     * A {@code super} reference.
     *
     * @param declaredType the type of the direct superclass
     */
    record SuperReference(TypeUsage declaredType) implements Symbol {
    }
}
