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
 * An <a href="https://en.wikipedia.org/wiki/Return_statement">Return</a> {@link Statement}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Return
    extends AbstractStatement {

    /**
     * The {@link Expression} to return.
     */
    private Expression expression;

    /**
     * Constructs an {@link Return} {@link Statement}.
     *
     * @param expression the {@link Expression}
     */
    private Return(final Expression expression) {

        super(Objects.requireNonNull(expression, "The VariableUsage must not be null").codeModel());
        this.expression = expression;
    }

    /**
     * {@link Unmarshal} a {@link Return}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param expression the {@link Marshalled} {@link Expression}
     */
    @Unmarshal
    public Return(@Bound final CodeModel codeModel,
                  @Bound final Marshaller marshaller,
                  final Stream<Marshalled<Trait>> traits,
                  final Marshalled<Expression> expression) {

        super(codeModel, marshaller, traits);

        this.expression = marshaller.unmarshal(expression);
    }

    /**
     * {@link Marshal} a {@link Return}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param expression the {@link Out} {@link Marshalled} {@link Expression}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> expression) {

        super.destructor(marshaller, traits);

        expression.set(marshaller.marshal(this.expression));
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
     * Creates an {@link Return} {@link Statement}.
     *
     * @param expression the {@link Expression}
     */
    public static Return of(final Expression expression) {
        return new Return(expression);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof Return other
            && Objects.equals(this.expression, other.expression)
            && super.equals(other);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(Return.class, MethodHandles.lookup());
    }
}
