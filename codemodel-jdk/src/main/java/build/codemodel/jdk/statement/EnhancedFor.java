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
import build.base.mereology.Composite;
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.imperative.AbstractStatement;
import build.codemodel.imperative.Statement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An enhanced {@code for} loop: {@code for (Type var : iterable) body}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class EnhancedFor
    extends AbstractStatement {

    /**
     * Whether the loop variable is declared {@code final}.
     */
    private final boolean isFinal;

    /**
     * The resolved type of the loop variable.
     */
    private final TypeUsage type;

    /**
     * The name of the loop variable.
     */
    private final String variable;

    /**
     * The iterable expression.
     */
    private final Expression iterable;

    /**
     * The loop body statement.
     *
     * <p>Not {@code final}: {@link #ofPending} constructs an {@link EnhancedFor} before its body
     * is converted, so that the loop variable's {@code Element} can be registered for
     * {@code Symbol} resolution before body conversion visits identifiers referencing it — the
     * body transitively references this very node, so it cannot be supplied as a constructor
     * argument. {@link #completeBody} sets it exactly once; from any external caller's perspective
     * the node is immutable once returned.
     */
    private Statement body;

    private EnhancedFor(final CodeModel codeModel,
                        final boolean isFinal,
                        final TypeUsage type,
                        final String variable,
                        final Expression iterable,
                        final Statement body) {
        super(codeModel);
        this.isFinal = isFinal;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.variable = Objects.requireNonNull(variable, "variable must not be null");
        this.iterable = Objects.requireNonNull(iterable, "iterable must not be null");
        this.body = body;
    }

    @Unmarshal
    public EnhancedFor(@Bound final CodeModel codeModel,
                       final Marshaller marshaller,
                       final Stream<Marshalled<Trait>> traits,
                       final Boolean isFinal,
                       final Marshalled<TypeUsage> type,
                       final String variable,
                       final Marshalled<Expression> iterable,
                       final Marshalled<Statement> body) {
        super(codeModel, marshaller, traits);
        this.isFinal = isFinal != null && isFinal;
        this.type = marshaller.unmarshal(type);
        this.variable = variable;
        this.iterable = marshaller.unmarshal(iterable);
        this.body = marshaller.unmarshal(body);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Boolean> isFinal,
                           final Out<Marshalled<TypeUsage>> type,
                           final Out<String> variable,
                           final Out<Marshalled<Expression>> iterable,
                           final Out<Marshalled<Statement>> body) {
        super.destructor(marshaller, traits);
        isFinal.set(this.isFinal);
        type.set(marshaller.marshal(this.type));
        variable.set(this.variable);
        iterable.set(marshaller.marshal(this.iterable));
        body.set(marshaller.marshal(this.body));
    }

    /**
     * Returns {@code true} if the loop variable is declared {@code final}.
     *
     * @return {@code true} if the variable is {@code final}
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    /**
     * Obtains the resolved type of the loop variable.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage type() {
        return this.type;
    }

    /**
     * Obtains the name of the loop variable.
     *
     * @return the variable name
     */
    public String variable() {
        return this.variable;
    }

    /**
     * Obtains the iterable expression.
     *
     * @return the iterable {@link Expression}
     */
    public Expression iterable() {
        return this.iterable;
    }

    /**
     * Obtains the loop body statement.
     *
     * @return the body {@link Statement}
     */
    public Statement body() {
        return this.body;
    }

    @Override
    public Stream<? extends Composite> compositeChildren() {
        return Stream.of(type, iterable, body);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof EnhancedFor other
            && this.isFinal == other.isFinal
            && Objects.equals(this.type, other.type)
            && Objects.equals(this.variable, other.variable)
            && Objects.equals(this.iterable, other.iterable)
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    /**
     * Creates an {@link EnhancedFor} statement.
     *
     * @param codeModel the {@link CodeModel}
     * @param isFinal   whether the loop variable is {@code final}
     * @param type      the resolved {@link TypeUsage} of the loop variable
     * @param variable  the name of the loop variable
     * @param iterable  the iterable {@link Expression}
     * @param body      the loop body {@link Statement}
     * @return a new {@link EnhancedFor}
     */
    public static EnhancedFor of(final CodeModel codeModel,
                                 final boolean isFinal,
                                 final TypeUsage type,
                                 final String variable,
                                 final Expression iterable,
                                 final Statement body) {
        return new EnhancedFor(codeModel, isFinal, type, variable, iterable,
            Objects.requireNonNull(body, "body must not be null"));
    }

    /**
     * Creates an {@link EnhancedFor} without its body, for callers that need the node's identity
     * (e.g. to register the loop variable's declaring construct for {@code Symbol} resolution)
     * before the body can be converted. {@link #completeBody} must be called exactly once before
     * any other code observes this node.
     *
     * @param codeModel the {@link CodeModel}
     * @param isFinal   whether the loop variable is {@code final}
     * @param type      the resolved {@link TypeUsage} of the loop variable
     * @param variable  the name of the loop variable
     * @param iterable  the iterable {@link Expression}
     * @return a new bodyless {@link EnhancedFor}, to be finished with {@link #completeBody}
     */
    public static EnhancedFor ofPending(final CodeModel codeModel,
                                        final boolean isFinal,
                                        final TypeUsage type,
                                        final String variable,
                                        final Expression iterable) {
        return new EnhancedFor(codeModel, isFinal, type, variable, iterable, null);
    }

    /**
     * Sets the loop body of an {@link EnhancedFor} created via {@link #ofPending}. May be called
     * exactly once.
     *
     * @param body the loop body {@link Statement}
     * @throws IllegalStateException if the body was already set
     */
    public void completeBody(final Statement body) {
        if (this.body != null) {
            throw new IllegalStateException("EnhancedFor body is already set");
        }
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    static {
        Marshalling.register(EnhancedFor.class, MethodHandles.lookup());
    }
}
