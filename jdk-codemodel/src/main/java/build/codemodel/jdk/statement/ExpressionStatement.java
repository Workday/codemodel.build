package build.codemodel.jdk.statement;

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
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.imperative.AbstractStatement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A statement that wraps an {@link Expression} (e.g. a method call or assignment used as a statement).
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class ExpressionStatement
    extends AbstractStatement {

    /**
     * The wrapped expression.
     */
    private final Expression expression;

    private ExpressionStatement(final Expression expression) {
        super(Objects.requireNonNull(expression, "expression must not be null").codeModel());
        this.expression = expression;
    }

    @Unmarshal
    public ExpressionStatement(@Bound final CodeModel codeModel,
                               final Marshaller marshaller,
                               final Stream<Marshalled<Trait>> traits,
                               final Marshalled<Expression> expression) {
        super(codeModel, marshaller, traits);
        this.expression = marshaller.unmarshal(expression);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> expression) {
        super.destructor(marshaller, traits);
        expression.set(marshaller.marshal(this.expression));
    }

    /**
     * Obtains the wrapped expression.
     *
     * @return the {@link Expression}
     */
    public Expression expression() {
        return this.expression;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof ExpressionStatement other
            && Objects.equals(this.expression, other.expression)
            && super.equals(other);
    }

    /**
     * Creates an {@link ExpressionStatement}.
     *
     * @param expression the {@link Expression} to wrap
     * @return a new {@link ExpressionStatement}
     */
    public static ExpressionStatement of(final Expression expression) {
        return new ExpressionStatement(expression);
    }

    static {
        Marshalling.register(ExpressionStatement.class, MethodHandles.lookup());
    }
}
