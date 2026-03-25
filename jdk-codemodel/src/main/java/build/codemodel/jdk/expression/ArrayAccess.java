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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.expression.AbstractExpression;
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An array access expression: {@code array[index]}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class ArrayAccess
    extends AbstractExpression {

    /**
     * The array expression being accessed.
     */
    private final Expression array;

    /**
     * The index expression.
     */
    private final Expression index;

    /**
     * The resolved type of the array, if available.
     */
    private final Optional<TypeUsage> arrayType;

    private ArrayAccess(final Expression array,
                        final Expression index,
                        final Optional<TypeUsage> arrayType) {
        super(Objects.requireNonNull(array, "array must not be null").codeModel());
        this.array = array;
        this.index = Objects.requireNonNull(index, "index must not be null");
        this.arrayType = arrayType == null ? Optional.empty() : arrayType;
    }

    @Unmarshal
    public ArrayAccess(@Bound final CodeModel codeModel,
                       final Marshaller marshaller,
                       final Stream<Marshalled<Trait>> traits,
                       final Marshalled<Expression> array,
                       final Marshalled<Expression> index,
                       final Optional<Marshalled<TypeUsage>> arrayType) {
        super(codeModel, marshaller, traits);
        this.array = marshaller.unmarshal(array);
        this.index = marshaller.unmarshal(index);
        this.arrayType = arrayType == null ? Optional.empty() : arrayType.map(marshaller::unmarshal);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> array,
                           final Out<Marshalled<Expression>> index,
                           final Out<Optional<Marshalled<TypeUsage>>> arrayType) {
        super.destructor(marshaller, traits);
        array.set(marshaller.marshal(this.array));
        index.set(marshaller.marshal(this.index));
        arrayType.set(this.arrayType.map(marshaller::marshal));
    }

    /**
     * Obtains the array expression being accessed.
     *
     * @return the array {@link Expression}
     */
    public Expression array() {
        return this.array;
    }

    /**
     * Obtains the index expression.
     *
     * @return the index {@link Expression}
     */
    public Expression index() {
        return this.index;
    }

    /**
     * Obtains the resolved type of the array, if available.
     *
     * @return an {@link Optional} {@link TypeUsage} for the array
     */
    public Optional<TypeUsage> arrayType() {
        return this.arrayType;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof ArrayAccess other
            && Objects.equals(this.array, other.array)
            && Objects.equals(this.index, other.index)
            && Objects.equals(this.arrayType, other.arrayType)
            && super.equals(other);
    }

    /**
     * Creates an {@link ArrayAccess} expression.
     *
     * @param array     the array expression being accessed
     * @param index     the index expression
     * @param arrayType the resolved type of the array, if available
     * @return a new {@link ArrayAccess}
     */
    public static ArrayAccess of(final Expression array,
                                 final Expression index,
                                 final Optional<TypeUsage> arrayType) {
        return new ArrayAccess(array, index, arrayType);
    }

    static {
        Marshalling.register(ArrayAccess.class, MethodHandles.lookup());
    }
}
