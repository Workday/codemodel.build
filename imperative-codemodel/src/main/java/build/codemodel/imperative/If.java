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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Conditional_(computer_programming)">Conditional</a>
 * {@link Statement}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class If
    extends AbstractStatement {

    /**
     * The condition to be evaluated.
     */
    private final Expression condition;

    /**
     * The "then" {@link Statement} to be executed when the {@link #condition} is {@code true}.
     */
    private final Statement thenStatement;

    /**
     * The {@link Optional} "else" {@link Statement} to be executed when the {@link #condition} is {@code false}.
     */
    private final Optional<Statement> elseStatement;

    /**
     * Constructs an {@link If} {@link Statement}.
     *
     * @param condition     the condition {@link Expression}
     * @param thenStatement the 'then' {@link Statement}
     * @param elseStatement the {@link Optional} 'else' {@link Statement}
     */
    private If(final Expression condition,
               final Statement thenStatement,
               final Optional<Statement> elseStatement) {

        super(Objects.requireNonNull(condition, "The 'condition' Expression must not be null").codeModel());
        this.condition = condition;
        this.thenStatement = Objects.requireNonNull(thenStatement, "The 'then' Statement must not be null");
        this.elseStatement = elseStatement == null ? Optional.empty() : elseStatement;
    }

    /**
     * {@link Unmarshal} an {@link If}.
     *
     * @param codeModel    the {@link CodeModel}
     * @param marshaller    the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits        the {@link Marshalled} {@link Trait}s
     * @param condition     the {@link Marshalled} condition {@link Expression}
     * @param thenStatement the {@link Marshalled} 'then' {@link Statement}
     * @param elseStatement the {@link Optional} {@link Marshalled} 'else' {@link Statement}
     */
    @Unmarshal
    public If(@Bound final CodeModel codeModel,
              @Bound final Marshaller marshaller,
              final Stream<Marshalled<Trait>> traits,
              final Marshalled<Expression> condition,
              final Marshalled<Statement> thenStatement,
              final Optional<Marshalled<Statement>> elseStatement) {

        super(codeModel, marshaller, traits);

        this.condition = marshaller.unmarshal(condition);
        this.thenStatement = marshaller.unmarshal(thenStatement);
        this.elseStatement = elseStatement == null
            ? Optional.empty()
            : elseStatement.map(marshaller::unmarshal);
    }

    /**
     * {@link Marshal} an {@link If}.
     *
     * @param marshaller    the {@link Marshaller}
     * @param traits        the {@link Out} {@link Marshalled} {@link Trait}s
     * @param condition     the {@link Out} {@link Marshalled} condition {@link Expression}
     * @param thenStatement the {@link Out} {@link Marshalled} 'then' {@link Statement}
     * @param elseStatement the {@link Out} {@link Optional} {@link Marshalled} 'else' {@link Statement}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> condition,
                           final Out<Marshalled<Statement>> thenStatement,
                           final Out<Optional<Marshalled<Statement>>> elseStatement) {

        super.destructor(marshaller, traits);

        condition.set(marshaller.marshal(this.condition));
        thenStatement.set(marshaller.marshal(this.thenStatement));
        elseStatement.set(this.elseStatement.map(marshaller::marshal));
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
     * Obtains the 'then' {@link Statement}.
     *
     * @return the 'then' {@link Statement}
     */
    public Statement thenStatement() {
        return this.thenStatement;
    }

    /**
     * Obtains the {@link Optional} 'else' {@link Statement}.
     *
     * @return the {@link Optional} 'else' {@link Statement}
     */
    public Optional<Statement> elseStatement() {
        return this.elseStatement;
    }

    /**
     * Creates an {@link If} {@link Statement}.
     *
     * @param condition     the condition {@link Expression}
     * @param thenStatement the 'then' {@link Statement}
     * @param elseStatement the {@link Optional} 'else' {@link Statement}
     */
    public static If of(final Expression condition,
                        final Statement thenStatement,
                        final Optional<Statement> elseStatement) {

        return new If(condition, thenStatement, elseStatement);
    }

    /**
     * Creates an {@link If} {@link Statement}.
     *
     * @param condition     the condition {@link Expression}
     * @param thenStatement the 'then' {@link Statement}
     */
    public static If of(final Expression condition,
                        final Statement thenStatement) {

        return new If(condition, thenStatement, Optional.empty());
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof If other
            && Objects.equals(this.condition, other.condition)
            && Objects.equals(this.thenStatement, other.thenStatement)
            && Objects.equals(this.elseStatement, other.elseStatement)
            && super.equals(other);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(If.class, MethodHandles.lookup());
    }
}
