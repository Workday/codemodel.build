package build.codemodel.imperative;

/*-
 * #%L
 * Imperative Code Model
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

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/While_loop">While</a> Loop {@link Statement}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class While
    extends AbstractStatement {

    /**
     * The condition to be evaluated.
     */
    private final Expression condition;

    /**
     * The {@link Statement} to be executed when the {@link #condition} is {@code true}.
     */
    private final Statement statement;

    /**
     * Constructs an {@link While} {@link Statement}.
     *
     * @param condition the condition {@link Expression}
     * @param statement the  {@link Statement}
     */
    private While(final Expression condition,
                  final Statement statement) {

        super(Objects.requireNonNull(condition, "The 'condition' Expression must not be null").codeModel());
        this.condition = condition;
        this.statement = Objects.requireNonNull(statement, "The Statement must not be null");
    }

    /**
     * {@link Unmarshal} a {@link While}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param condition  the {@link Marshalled} condition {@link Expression}
     * @param statement  the {@link Marshalled} {@link Statement}
     */
    @Unmarshal
    public While(@Bound final CodeModel codeModel,
                 @Bound final Marshaller marshaller,
                 final Stream<Marshalled<Trait>> traits,
                 final Marshalled<Expression> condition,
                 final Marshalled<Statement> statement) {

        super(codeModel, marshaller, traits);

        this.condition = marshaller.unmarshal(condition);
        this.statement = marshaller.unmarshal(statement);
    }

    /**
     * {@link Marshal} a {@link While}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param condition  the {@link Out} {@link Marshalled}  condition {@link Expression}
     * @param statement  the {@link Out} {@link Marshalled} {@link Statement}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> condition,
                           final Out<Marshalled<Statement>> statement) {

        super.destructor(marshaller, traits);

        condition.set(marshaller.marshal(this.condition));
        statement.set(marshaller.marshal(this.statement));
    }

    /**
     * Obtains the condition {@link Expression}.
     *
     * @return the condition {@link Expression}
     */
    public Expression condition() {
        return this.condition;
    }

    /**
     * Obtains the {@link Statement}.
     *
     * @return the {@link Statement}
     */
    public Statement statement() {
        return this.statement;
    }

    /**
     * Creates an {@link While} {@link Statement}.
     *
     * @param condition the condition {@link Expression}
     * @param statement the {@link Statement}
     */
    public static While of(final Expression condition,
                           final Statement statement) {

        return new While(condition, statement);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof While other
            && Objects.equals(this.condition, other.condition)
            && Objects.equals(this.statement, other.statement)
            && super.equals(other);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(While.class, MethodHandles.lookup());
    }
}
