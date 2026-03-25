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
import build.codemodel.imperative.Statement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@code do-while} loop: {@code do body while (condition)}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class DoWhile
    extends AbstractStatement {

    /**
     * The loop body statement.
     */
    private final Statement body;

    /**
     * The loop condition expression.
     */
    private final Expression condition;

    private DoWhile(final Statement body, final Expression condition) {
        super(Objects.requireNonNull(body, "body must not be null").codeModel());
        this.body = body;
        this.condition = Objects.requireNonNull(condition, "condition must not be null");
    }

    @Unmarshal
    public DoWhile(@Bound final CodeModel codeModel,
                   final Marshaller marshaller,
                   final Stream<Marshalled<Trait>> traits,
                   final Marshalled<Statement> body,
                   final Marshalled<Expression> condition) {
        super(codeModel, marshaller, traits);
        this.body = marshaller.unmarshal(body);
        this.condition = marshaller.unmarshal(condition);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Statement>> body,
                           final Out<Marshalled<Expression>> condition) {
        super.destructor(marshaller, traits);
        body.set(marshaller.marshal(this.body));
        condition.set(marshaller.marshal(this.condition));
    }

    /**
     * Obtains the loop body statement.
     *
     * @return the body {@link Statement}
     */
    public Statement body() {
        return this.body;
    }

    /**
     * Obtains the loop condition expression.
     *
     * @return the condition {@link Expression}
     */
    public Expression condition() {
        return this.condition;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof DoWhile other
            && Objects.equals(this.body, other.body)
            && Objects.equals(this.condition, other.condition)
            && super.equals(other);
    }

    /**
     * Creates a {@link DoWhile} statement.
     *
     * @param body      the loop body {@link Statement}
     * @param condition the loop condition {@link Expression}
     * @return a new {@link DoWhile}
     */
    public static DoWhile of(final Statement body, final Expression condition) {
        return new DoWhile(body, condition);
    }

    static {
        Marshalling.register(DoWhile.class, MethodHandles.lookup());
    }
}
