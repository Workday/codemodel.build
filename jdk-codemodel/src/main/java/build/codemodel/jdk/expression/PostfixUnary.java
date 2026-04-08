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
 * A postfix unary expression: {@code x++}, {@code x--}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class PostfixUnary
    extends AbstractExpression {

    /**
     * The operator.
     */
    private final PostfixOperator operator;

    /**
     * The operand expression.
     */
    private final Expression operand;

    private PostfixUnary(final CodeModel codeModel,
                         final PostfixOperator operator,
                         final Expression operand) {
        super(codeModel);
        this.operator = Objects.requireNonNull(operator, "operator must not be null");
        this.operand = Objects.requireNonNull(operand, "operand must not be null");
    }

    @Unmarshal
    public PostfixUnary(@Bound final CodeModel codeModel,
                        final Marshaller marshaller,
                        final Stream<Marshalled<Trait>> traits,
                        final String operator,
                        final Marshalled<Expression> operand) {
        super(codeModel, marshaller, traits);
        this.operator = PostfixOperator.valueOf(operator);
        this.operand = marshaller.unmarshal(operand);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<String> operator,
                           final Out<Marshalled<Expression>> operand) {
        super.destructor(marshaller, traits);
        operator.set(this.operator.name());
        operand.set(marshaller.marshal(this.operand));
    }

    /**
     * Obtains the operator.
     *
     * @return the {@link PostfixOperator}
     */
    public PostfixOperator operator() {
        return this.operator;
    }

    /**
     * Obtains the operand expression.
     *
     * @return the operand {@link Expression}
     */
    public Expression operand() {
        return this.operand;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof PostfixUnary other
            && this.operator == other.operator
            && Objects.equals(this.operand, other.operand)
            && super.equals(other);
    }

    /**
     * Creates a {@link PostfixUnary} expression.
     *
     * @param codeModel the {@link CodeModel}
     * @param operator  the {@link PostfixOperator}
     * @param operand   the operand {@link Expression}
     * @return a new {@link PostfixUnary}
     */
    public static PostfixUnary of(final CodeModel codeModel,
                                  final PostfixOperator operator,
                                  final Expression operand) {
        return new PostfixUnary(codeModel, operator, operand);
    }

    static {
        Marshalling.register(PostfixUnary.class, MethodHandles.lookup());
    }
}
