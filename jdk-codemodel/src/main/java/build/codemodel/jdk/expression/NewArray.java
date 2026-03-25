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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An array creation expression: {@code new Type[dims]} or array initializer.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class NewArray
    extends AbstractExpression {

    /**
     * The element type expression.
     */
    private final Expression elementType;

    /**
     * The dimension expressions.
     */
    private final ArrayList<Expression> dimensions;

    private NewArray(final CodeModel codeModel,
                     final Expression elementType,
                     final Stream<Expression> dimensions) {
        super(codeModel);
        this.elementType = Objects.requireNonNull(elementType, "elementType must not be null");
        this.dimensions = dimensions == null
            ? new ArrayList<>()
            : dimensions.collect(Collectors.toCollection(ArrayList::new));
    }

    @Unmarshal
    public NewArray(@Bound final CodeModel codeModel,
                    final Marshaller marshaller,
                    final Stream<Marshalled<Trait>> traits,
                    final Marshalled<Expression> elementType,
                    final Stream<Marshalled<Expression>> dimensions) {
        super(codeModel, marshaller, traits);
        this.elementType = marshaller.unmarshal(elementType);
        this.dimensions = dimensions == null
            ? new ArrayList<>()
            : dimensions.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> elementType,
                           final Out<Stream<Marshalled<Expression>>> dimensions) {
        super.destructor(marshaller, traits);
        elementType.set(marshaller.marshal(this.elementType));
        dimensions.set(this.dimensions.stream().map(marshaller::marshal));
    }

    /**
     * Obtains the element type expression.
     *
     * @return the element type {@link Expression}
     */
    public Expression elementType() {
        return this.elementType;
    }

    /**
     * Obtains the dimension expressions.
     *
     * @return a {@link Stream} of dimension {@link Expression}s
     */
    public Stream<Expression> dimensions() {
        return this.dimensions.stream();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof NewArray other
            && Objects.equals(this.elementType, other.elementType)
            && Objects.equals(this.dimensions, other.dimensions)
            && super.equals(other);
    }

    /**
     * Creates a {@link NewArray} expression.
     *
     * @param codeModel  the {@link CodeModel}
     * @param elementType the element type {@link Expression}
     * @param dimensions  the dimension {@link Expression}s
     * @return a new {@link NewArray}
     */
    public static NewArray of(final CodeModel codeModel,
                              final Expression elementType,
                              final Stream<Expression> dimensions) {
        return new NewArray(codeModel, elementType, dimensions);
    }

    static {
        Marshalling.register(NewArray.class, MethodHandles.lookup());
    }
}
