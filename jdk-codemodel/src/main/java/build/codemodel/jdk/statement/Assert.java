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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@code assert} statement: {@code assert condition [: message]}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Assert
    extends AbstractStatement {

    /**
     * The condition expression.
     */
    private final Expression condition;

    /**
     * The optional detail message expression.
     */
    private final Optional<Expression> message;

    private Assert(final Expression condition, final Optional<Expression> message) {
        super(Objects.requireNonNull(condition, "condition must not be null").codeModel());
        this.condition = condition;
        this.message = message == null ? Optional.empty() : message;
    }

    @Unmarshal
    public Assert(@Bound final CodeModel codeModel,
                  final Marshaller marshaller,
                  final Stream<Marshalled<Trait>> traits,
                  final Marshalled<Expression> condition,
                  final Optional<Marshalled<Expression>> message) {
        super(codeModel, marshaller, traits);
        this.condition = marshaller.unmarshal(condition);
        this.message = message == null ? Optional.empty() : message.map(marshaller::unmarshal);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> condition,
                           final Out<Optional<Marshalled<Expression>>> message) {
        super.destructor(marshaller, traits);
        condition.set(marshaller.marshal(this.condition));
        message.set(this.message.map(marshaller::marshal));
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
     * Obtains the optional detail message expression.
     *
     * @return an {@link Optional} detail message {@link Expression}
     */
    public Optional<Expression> message() {
        return this.message;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Assert other
            && Objects.equals(this.condition, other.condition)
            && Objects.equals(this.message, other.message)
            && super.equals(other);
    }

    /**
     * Creates an {@link Assert} statement.
     *
     * @param condition the condition {@link Expression}
     * @param message   the optional detail message {@link Expression}
     * @return a new {@link Assert}
     */
    public static Assert of(final Expression condition, final Optional<Expression> message) {
        return new Assert(condition, message);
    }

    static {
        Marshalling.register(Assert.class, MethodHandles.lookup());
    }
}
