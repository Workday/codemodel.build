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
 * A ternary conditional expression: {@code condition ? thenExpr : elseExpr}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Ternary
    extends AbstractExpression {

    /**
     * The condition expression.
     */
    private final Expression condition;

    /**
     * The expression evaluated when the condition is {@code true}.
     */
    private final Expression thenExpr;

    /**
     * The expression evaluated when the condition is {@code false}.
     */
    private final Expression elseExpr;

    private Ternary(final Expression condition,
                    final Expression thenExpr,
                    final Expression elseExpr) {
        super(Objects.requireNonNull(condition, "condition must not be null").codeModel());
        this.condition = condition;
        this.thenExpr = Objects.requireNonNull(thenExpr, "thenExpr must not be null");
        this.elseExpr = Objects.requireNonNull(elseExpr, "elseExpr must not be null");
    }

    @Unmarshal
    public Ternary(@Bound final CodeModel codeModel,
                   final Marshaller marshaller,
                   final Stream<Marshalled<Trait>> traits,
                   final Marshalled<Expression> condition,
                   final Marshalled<Expression> thenExpr,
                   final Marshalled<Expression> elseExpr) {
        super(codeModel, marshaller, traits);
        this.condition = marshaller.unmarshal(condition);
        this.thenExpr = marshaller.unmarshal(thenExpr);
        this.elseExpr = marshaller.unmarshal(elseExpr);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> condition,
                           final Out<Marshalled<Expression>> thenExpr,
                           final Out<Marshalled<Expression>> elseExpr) {
        super.destructor(marshaller, traits);
        condition.set(marshaller.marshal(this.condition));
        thenExpr.set(marshaller.marshal(this.thenExpr));
        elseExpr.set(marshaller.marshal(this.elseExpr));
    }

    /**
     * Obtains the condition expression.
     *
     * @return the condition {@link Expression}
     */
    public Expression condition() {
        return this.condition;
    }

    /**
     * Obtains the expression evaluated when the condition is {@code true}.
     *
     * @return the then {@link Expression}
     */
    public Expression thenExpr() {
        return this.thenExpr;
    }

    /**
     * Obtains the expression evaluated when the condition is {@code false}.
     *
     * @return the else {@link Expression}
     */
    public Expression elseExpr() {
        return this.elseExpr;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Ternary other
            && Objects.equals(this.condition, other.condition)
            && Objects.equals(this.thenExpr, other.thenExpr)
            && Objects.equals(this.elseExpr, other.elseExpr)
            && super.equals(other);
    }

    /**
     * Creates a {@link Ternary} expression.
     *
     * @param condition the condition {@link Expression}
     * @param thenExpr  the {@link Expression} evaluated when the condition is {@code true}
     * @param elseExpr  the {@link Expression} evaluated when the condition is {@code false}
     * @return a new {@link Ternary}
     */
    public static Ternary of(final Expression condition,
                             final Expression thenExpr,
                             final Expression elseExpr) {
        return new Ternary(condition, thenExpr, elseExpr);
    }

    static {
        Marshalling.register(Ternary.class, MethodHandles.lookup());
    }
}
