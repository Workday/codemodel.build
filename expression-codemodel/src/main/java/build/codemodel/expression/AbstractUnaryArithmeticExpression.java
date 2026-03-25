package build.codemodel.expression;

/*-
 * #%L
 * Expression Code Model
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

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An abstract {@link UnaryArithmeticExpression}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public abstract class AbstractUnaryArithmeticExpression
    extends AbstractArithmeticExpression
    implements UnaryArithmeticExpression {

    /**
     * The {@link Expression} for the <i>Operand</i>.
     */
    private final Expression expression;

    /**
     * Constructs an {@link AbstractUnaryArithmeticExpression}.
     *
     * @param expression {@link Expression}
     */
    protected AbstractUnaryArithmeticExpression(final Expression expression) {

        super(Objects.requireNonNull(expression, "The Expression must not be null")
                .codeModel(),
            expression.type());

        this.expression = expression;
    }

    /**
     * {@link Unmarshal} an {@link AbstractUnaryArithmeticExpression}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param expression the {@link Marshalled} {@link Expression}
     */
    protected AbstractUnaryArithmeticExpression(final CodeModel codeModel,
                                                final Marshaller marshaller,
                                                final Stream<Marshalled<Trait>> traits,
                                                final Optional<Marshalled<TypeUsage>> typeUsage,
                                                final Marshalled<Expression> expression) {

        super(codeModel, marshaller, traits, typeUsage);

        this.expression = marshaller.unmarshal(expression);
    }

    /**
     * {@link Marshal} an {@link AbstractUnaryArithmeticExpression}.
     *
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param expression the {@link Marshalled} {@link Expression}
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Stream<Marshalled<Trait>>> traits,
                              final Out<Optional<Marshalled<TypeUsage>>> typeUsage,
                              final Out<Marshalled<Expression>> expression) {

        super.destructor(marshaller, traits, typeUsage);

        expression.set(marshaller.marshal(this.expression));
    }

    @Override
    public Expression expression() {
        return this.expression;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof AbstractUnaryArithmeticExpression other
            && expression.equals(other.expression)
            && super.equals(other);
    }
}
