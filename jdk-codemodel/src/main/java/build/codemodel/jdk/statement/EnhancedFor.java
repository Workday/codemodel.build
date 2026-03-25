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
     * The source-form type name of the loop variable.
     */
    private final String typeName;

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
     */
    private final Statement body;

    private EnhancedFor(final CodeModel codeModel,
                        final boolean isFinal,
                        final String typeName,
                        final String variable,
                        final Expression iterable,
                        final Statement body) {
        super(codeModel);
        this.isFinal = isFinal;
        this.typeName = Objects.requireNonNull(typeName, "typeName must not be null");
        this.variable = Objects.requireNonNull(variable, "variable must not be null");
        this.iterable = Objects.requireNonNull(iterable, "iterable must not be null");
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    @Unmarshal
    public EnhancedFor(@Bound final CodeModel codeModel,
                       final Marshaller marshaller,
                       final Stream<Marshalled<Trait>> traits,
                       final Boolean isFinal,
                       final String typeName,
                       final String variable,
                       final Marshalled<Expression> iterable,
                       final Marshalled<Statement> body) {
        super(codeModel, marshaller, traits);
        this.isFinal = isFinal != null && isFinal;
        this.typeName = typeName;
        this.variable = variable;
        this.iterable = marshaller.unmarshal(iterable);
        this.body = marshaller.unmarshal(body);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Boolean> isFinal,
                           final Out<String> typeName,
                           final Out<String> variable,
                           final Out<Marshalled<Expression>> iterable,
                           final Out<Marshalled<Statement>> body) {
        super.destructor(marshaller, traits);
        isFinal.set(this.isFinal);
        typeName.set(this.typeName);
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
     * Obtains the source-form type name of the loop variable.
     *
     * @return the type name
     */
    public String typeName() {
        return this.typeName;
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
    public boolean equals(final Object object) {
        return object instanceof EnhancedFor other
            && this.isFinal == other.isFinal
            && Objects.equals(this.typeName, other.typeName)
            && Objects.equals(this.variable, other.variable)
            && Objects.equals(this.iterable, other.iterable)
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    /**
     * Creates an {@link EnhancedFor} statement.
     *
     * @param codeModel the {@link CodeModel}
     * @param isFinal    whether the loop variable is {@code final}
     * @param typeName   the source-form type name of the loop variable
     * @param variable   the name of the loop variable
     * @param iterable   the iterable {@link Expression}
     * @param body       the loop body {@link Statement}
     * @return a new {@link EnhancedFor}
     */
    public static EnhancedFor of(final CodeModel codeModel,
                                 final boolean isFinal,
                                 final String typeName,
                                 final String variable,
                                 final Expression iterable,
                                 final Statement body) {
        return new EnhancedFor(codeModel, isFinal, typeName, variable, iterable, body);
    }

    static {
        Marshalling.register(EnhancedFor.class, MethodHandles.lookup());
    }
}
