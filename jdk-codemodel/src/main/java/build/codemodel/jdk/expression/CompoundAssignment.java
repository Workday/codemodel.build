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
 * A compound assignment expression: {@code var OP= value} (e.g. {@code x += 1}).
 * Also used for simple assignment {@code var = value} when the operator is {@code "ASSIGNMENT"}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class CompoundAssignment
    extends AbstractExpression {

    /**
     * The operator kind string (e.g. {@code "PLUS_ASSIGNMENT"}, {@code "ASSIGNMENT"}).
     */
    private final String operator;

    /**
     * The left-hand-side variable expression.
     */
    private final Expression variable;

    /**
     * The right-hand-side value expression.
     */
    private final Expression value;

    private CompoundAssignment(final CodeModel codeModel,
                               final String operator,
                               final Expression variable,
                               final Expression value) {
        super(codeModel);
        this.operator = Objects.requireNonNull(operator, "operator must not be null");
        this.variable = Objects.requireNonNull(variable, "variable must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    @Unmarshal
    public CompoundAssignment(@Bound final CodeModel codeModel,
                              final Marshaller marshaller,
                              final Stream<Marshalled<Trait>> traits,
                              final String operator,
                              final Marshalled<Expression> variable,
                              final Marshalled<Expression> value) {
        super(codeModel, marshaller, traits);
        this.operator = operator;
        this.variable = marshaller.unmarshal(variable);
        this.value = marshaller.unmarshal(value);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<String> operator,
                           final Out<Marshalled<Expression>> variable,
                           final Out<Marshalled<Expression>> value) {
        super.destructor(marshaller, traits);
        operator.set(this.operator);
        variable.set(marshaller.marshal(this.variable));
        value.set(marshaller.marshal(this.value));
    }

    /**
     * Obtains the operator kind string.
     *
     * @return the operator kind string
     */
    public String operator() {
        return this.operator;
    }

    /**
     * Obtains the left-hand-side variable expression.
     *
     * @return the variable {@link Expression}
     */
    public Expression variable() {
        return this.variable;
    }

    /**
     * Obtains the right-hand-side value expression.
     *
     * @return the value {@link Expression}
     */
    public Expression value() {
        return this.value;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof CompoundAssignment other
            && Objects.equals(this.operator, other.operator)
            && Objects.equals(this.variable, other.variable)
            && Objects.equals(this.value, other.value)
            && super.equals(other);
    }

    /**
     * Creates a {@link CompoundAssignment} expression.
     *
     * @param codeModel the {@link CodeModel}
     * @param operator   the operator kind string
     * @param variable   the left-hand-side variable {@link Expression}
     * @param value      the right-hand-side value {@link Expression}
     * @return a new {@link CompoundAssignment}
     */
    public static CompoundAssignment of(final CodeModel codeModel,
                                        final String operator,
                                        final Expression variable,
                                        final Expression value) {
        return new CompoundAssignment(codeModel, operator, variable, value);
    }

    static {
        Marshalling.register(CompoundAssignment.class, MethodHandles.lookup());
    }
}
