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
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A bitwise or shift binary expression: {@code &}, {@code |}, {@code ^},
 * {@code <<}, {@code >>}, {@code >>>}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class BitwiseBinary
    extends AbstractExpression {

    /**
     * The operator kind string (e.g. {@code "AND"}, {@code "LEFT_SHIFT"}).
     */
    private final String operator;

    /**
     * The left-hand-side operand.
     */
    private final Expression left;

    /**
     * The right-hand-side operand.
     */
    private final Expression right;

    private BitwiseBinary(final CodeModel codeModel,
                          final String operator,
                          final Expression left,
                          final Expression right) {
        super(codeModel);
        this.operator = Objects.requireNonNull(operator, "operator must not be null");
        this.left = Objects.requireNonNull(left, "left must not be null");
        this.right = Objects.requireNonNull(right, "right must not be null");
    }

    @Unmarshal
    public BitwiseBinary(@Bound final CodeModel codeModel,
                         final Marshaller marshaller,
                         final Stream<Marshalled<Trait>> traits,
                         final String operator,
                         final Marshalled<Expression> left,
                         final Marshalled<Expression> right) {
        super(codeModel, marshaller, traits);
        this.operator = operator;
        this.left = marshaller.unmarshal(left);
        this.right = marshaller.unmarshal(right);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<String> operator,
                           final Out<Marshalled<Expression>> left,
                           final Out<Marshalled<Expression>> right) {
        super.destructor(marshaller, traits);
        operator.set(this.operator);
        left.set(marshaller.marshal(this.left));
        right.set(marshaller.marshal(this.right));
    }

    /**
     * Obtains the operator kind string.
     *
     * @return the operator kind string
     */
    public String operator() {
        return this.operator;
    }

    /**
     * Obtains the left-hand-side operand.
     *
     * @return the left-hand-side {@link Expression}
     */
    public Expression left() {
        return this.left;
    }

    /**
     * Obtains the right-hand-side operand.
     *
     * @return the right-hand-side {@link Expression}
     */
    public Expression right() {
        return this.right;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof BitwiseBinary other
            && Objects.equals(this.operator, other.operator)
            && Objects.equals(this.left, other.left)
            && Objects.equals(this.right, other.right)
            && super.equals(other);
    }

    /**
     * Creates a {@link BitwiseBinary} expression.
     *
     * @param codeModel the {@link CodeModel}
     * @param operator   the operator kind string
     * @param left       the left-hand-side {@link Expression}
     * @param right      the right-hand-side {@link Expression}
     * @return a new {@link BitwiseBinary}
     */
    public static BitwiseBinary of(final CodeModel codeModel,
                                   final String operator,
                                   final Expression left,
                                   final Expression right) {
        return new BitwiseBinary(codeModel, operator, left, right);
    }

    static {
        Marshalling.register(BitwiseBinary.class, MethodHandles.lookup());
    }
}
