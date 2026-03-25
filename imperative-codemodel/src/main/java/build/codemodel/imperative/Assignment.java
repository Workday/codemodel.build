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
import build.codemodel.expression.VariableUsage;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An <a href="https://en.wikipedia.org/wiki/Assignment_(computer_science)">Assignment</a>  {@link Statement}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Assignment
    extends AbstractStatement {

    /**
     * The <i>Variable</i> being assigned.
     */
    private VariableUsage variable;

    /**
     * The {@link Expression} being assigned.
     */
    private Expression expression;

    /**
     * Constructs an {@link Assignment} {@link Statement}.
     *
     * @param variable   the {@link VariableUsage}
     * @param expression the {@link Expression}
     */
    private Assignment(final VariableUsage variable,
                       final Expression expression) {

        super(Objects.requireNonNull(variable, "The VariableUsage must not be null").codeModel());
        this.variable = variable;
        this.expression = Objects.requireNonNull(expression, "The Expression must not be null");
    }

    /**
     * {@link Unmarshal} an {@link Assignment}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param variable   the {@link Marshalled} {@link VariableUsage}
     * @param expression the {@link Marshalled} {@link Expression}
     */
    @Unmarshal
    public Assignment(@Bound final CodeModel codeModel,
                      @Bound final Marshaller marshaller,
                      final Stream<Marshalled<Trait>> traits,
                      final Marshalled<VariableUsage> variable,
                      final Marshalled<Expression> expression) {

        super(codeModel, marshaller, traits);

        this.variable = marshaller.unmarshal(variable);
        this.expression = marshaller.unmarshal(expression);
    }

    /**
     * {@link Marshal} an {@link Assignment}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param variable   the {@link Out} {@link Marshalled} {@link VariableUsage}
     * @param expression the {@link Out} {@link Marshalled} {@link Expression}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<VariableUsage>> variable,
                           final Out<Marshalled<Expression>> expression) {

        super.destructor(marshaller, traits);

        variable.set(marshaller.marshal(this.variable));
        expression.set(marshaller.marshal(this.expression));
    }

    /**
     * Obtains the <i>Variable</i> being assigned.
     *
     * @return the {@link VariableUsage}
     */
    public VariableUsage variable() {
        return this.variable;
    }

    /**
     * Obtains the {@link Expression} being assigned.
     *
     * @return the {@link Expression}
     */
    public Expression expression() {
        return this.expression;
    }

    /**
     * Creates an {@link Assignment} {@link Statement}.
     *
     * @param variable   the {@link VariableUsage}
     * @param expression the {@link Expression}
     */
    public static Assignment of(final VariableUsage variable,
                                final Expression expression) {

        return new Assignment(variable, expression);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof Assignment other
            && Objects.equals(this.variable, other.variable)
            && Objects.equals(this.expression, other.expression)
            && super.equals(other);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(Assignment.class, MethodHandles.lookup());
    }
}
