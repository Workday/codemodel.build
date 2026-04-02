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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Defines a <a href="https://en.wikipedia.org/wiki/Multiplication">Multiplication</a> between two {@link Expression}s.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Multiplication
    extends AbstractBinaryArithmeticExpression {

    /**
     * Constructs a {@link Multiplication}.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     */
    private Multiplication(final Expression left,
                           final Expression right) {

        super(left, right);
    }

    /**
     * Un{@link Marshal} a {@link Multiplication}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param left       the left-hand-side {@link Marshalled} {@link Expression}
     * @param right      the right-hand-side {@link Marshalled} {@link Expression}
     */
    @Unmarshal
    public Multiplication(@Bound final CodeModel codeModel,
                          final Marshaller marshaller,
                          final Stream<Marshalled<Trait>> traits,
                          final Optional<Marshalled<TypeUsage>> typeUsage,
                          final Marshalled<Expression> left,
                          final Marshalled<Expression> right) {

        super(codeModel, marshaller, traits, typeUsage, left, right);
    }

    /**
     * {@link Marshal} a {@link Multiplication}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Out} {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param left       the left-hand-side {@link Marshalled} {@link Expression}
     * @param right      the right-hand-side {@link Marshalled} {@link Expression}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<Marshalled<TypeUsage>>> typeUsage,
                           final Out<Marshalled<Expression>> left,
                           final Out<Marshalled<Expression>> right) {

        super.destructor(marshaller, traits, typeUsage, left, right);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof Multiplication other
            && super.equals(other);
    }

    /**
     * Creates a {@link Multiplication} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link Multiplication}
     */
    public static Multiplication of(final Expression left,
                                    final Expression right) {

        return new Multiplication(left, right);
    }
    
    static {
        Marshalling.register(Multiplication.class, MethodHandles.lookup());
    }
}
