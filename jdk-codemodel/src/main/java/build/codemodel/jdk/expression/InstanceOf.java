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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@code instanceof} expression: {@code expr instanceof Type}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class InstanceOf
    extends AbstractExpression {

    /**
     * The expression being tested.
     */
    private final Expression expression;

    /**
     * The type expression on the right-hand side of {@code instanceof}.
     */
    private final Expression typeExpression;

    /**
     * The optional pattern-binding variable name (Java 16+ pattern matching).
     */
    private final Optional<String> bindingVariable;

    private InstanceOf(final Expression expression,
                       final Expression typeExpression,
                       final Optional<String> bindingVariable) {
        super(Objects.requireNonNull(expression, "expression must not be null").codeModel());
        this.expression = expression;
        this.typeExpression = Objects.requireNonNull(typeExpression, "typeExpression must not be null");
        this.bindingVariable = bindingVariable == null ? Optional.empty() : bindingVariable;
    }

    @Unmarshal
    public InstanceOf(@Bound final CodeModel codeModel,
                      final Marshaller marshaller,
                      final Stream<Marshalled<Trait>> traits,
                      final Marshalled<Expression> expression,
                      final Marshalled<Expression> typeExpression,
                      final Optional<String> bindingVariable) {
        super(codeModel, marshaller, traits);
        this.expression = marshaller.unmarshal(expression);
        this.typeExpression = marshaller.unmarshal(typeExpression);
        this.bindingVariable = bindingVariable == null ? Optional.empty() : bindingVariable;
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> expression,
                           final Out<Marshalled<Expression>> typeExpression,
                           final Out<Optional<String>> bindingVariable) {
        super.destructor(marshaller, traits);
        expression.set(marshaller.marshal(this.expression));
        typeExpression.set(marshaller.marshal(this.typeExpression));
        bindingVariable.set(this.bindingVariable);
    }

    /**
     * Obtains the expression being tested.
     *
     * @return the tested {@link Expression}
     */
    public Expression expression() {
        return this.expression;
    }

    /**
     * Obtains the type expression on the right-hand side of {@code instanceof}.
     *
     * @return the type {@link Expression}
     */
    public Expression typeExpression() {
        return this.typeExpression;
    }

    /**
     * Obtains the optional pattern-binding variable name (Java 16+ pattern matching).
     *
     * @return an {@link Optional} binding variable name, or {@link Optional#empty()} for classic {@code instanceof}
     */
    public Optional<String> bindingVariable() {
        return this.bindingVariable;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof InstanceOf other
            && Objects.equals(this.expression, other.expression)
            && Objects.equals(this.typeExpression, other.typeExpression)
            && Objects.equals(this.bindingVariable, other.bindingVariable)
            && super.equals(other);
    }

    /**
     * Creates an {@link InstanceOf} expression.
     *
     * @param expression      the expression being tested
     * @param typeExpression  the type expression on the right-hand side
     * @param bindingVariable the optional pattern-binding variable name
     * @return a new {@link InstanceOf}
     */
    public static InstanceOf of(final Expression expression,
                                final Expression typeExpression,
                                final Optional<String> bindingVariable) {
        return new InstanceOf(expression, typeExpression, bindingVariable);
    }

    /**
     * Creates a classic (non-pattern) {@link InstanceOf} expression.
     *
     * @param expression     the expression being tested
     * @param typeExpression the type expression on the right-hand side
     * @return a new {@link InstanceOf}
     */
    public static InstanceOf of(final Expression expression, final Expression typeExpression) {
        return new InstanceOf(expression, typeExpression, Optional.empty());
    }

    static {
        Marshalling.register(InstanceOf.class, MethodHandles.lookup());
    }
}
