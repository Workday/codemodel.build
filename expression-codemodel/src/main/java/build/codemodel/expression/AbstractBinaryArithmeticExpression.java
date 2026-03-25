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
 * An abstract {@link BinaryLogicalExpression}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public abstract class AbstractBinaryArithmeticExpression
    extends AbstractArithmeticExpression
    implements BinaryArithmeticExpression {

    /**
     * The left-hand-side {@link Expression}.
     */
    private final Expression left;

    /**
     * The right-hand-side {@link Expression}.
     */
    private final Expression right;

    /**
     * Constructs an {@link AbstractBinaryArithmeticExpression}.
     *
     * @param left  left-hand-side {@link Expression}
     * @param right right-hand-side {@link Expression}
     */
    protected AbstractBinaryArithmeticExpression(final Expression left,
                                                 final Expression right) {

        super(Objects.requireNonNull(left, "The left-hand-side Expression must not be null")
            .codeModel(), Optional.empty());

        this.left = left;
        this.right = Objects.requireNonNull(right, "The right-hand-side Expression must not be null");
    }

    /**
     * {@link Unmarshal} an {@link AbstractBinaryArithmeticExpression}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param left       the left-hand-side {@link Marshalled} {@link Expression}
     * @param right      the right-hand-side {@link Marshalled} {@link Expression}
     */
    protected AbstractBinaryArithmeticExpression(final CodeModel codeModel,
                                                 final Marshaller marshaller,
                                                 final Stream<Marshalled<Trait>> traits,
                                                 final Optional<Marshalled<TypeUsage>> typeUsage,
                                                 final Marshalled<Expression> left,
                                                 final Marshalled<Expression> right) {

        super(codeModel, marshaller, traits, typeUsage);

        this.left = marshaller.unmarshal(left);
        this.right = marshaller.unmarshal(right);
    }

    /**
     * {@link Marshal} an {@link AbstractBinaryArithmeticExpression}.
     *
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param left       the left-hand-side {@link Marshalled} {@link Expression}
     * @param right      the right-hand-side {@link Marshalled} {@link Expression}
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Stream<Marshalled<Trait>>> traits,
                              final Out<Optional<Marshalled<TypeUsage>>> typeUsage,
                              final Out<Marshalled<Expression>> left,
                              final Out<Marshalled<Expression>> right) {

        super.destructor(marshaller, traits, typeUsage);

        left.set(marshaller.marshal(this.left));
        right.set(marshaller.marshal(this.right));
    }

    @Override
    public Expression left() {
        return this.left;
    }

    @Override
    public Expression right() {
        return this.right;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof AbstractBinaryArithmeticExpression other
            && left.equals(other.left)
            && right.equals(other.right)
            && super.equals(other);
    }
}
