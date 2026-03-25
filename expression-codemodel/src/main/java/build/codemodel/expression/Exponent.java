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

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

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

/**
 * Defines a <a href="https://en.wikipedia.org/wiki/Exponentiation">Exponent</a> between two {@link Expression}s.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class Exponent
    extends AbstractBinaryArithmeticExpression {

    /**
     * Constructs a {@link Exponent}.
     *
     * @param base  the base {@link Expression}
     * @param exponent the exponent {@link Expression}
     */
    private Exponent(final Expression base,
                     final Expression exponent) {

        super(base, exponent);
    }

    /**
     * Un{@link Marshal} an {@link Exponent}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param base       the base {@link Marshalled} {@link Expression}
     * @param exponent   the exponent {@link Marshalled} {@link Expression}
     */
    @Unmarshal
    public Exponent(@Bound final CodeModel codeModel,
                    final Marshaller marshaller,
                    final Stream<Marshalled<Trait>> traits,
                    final Optional<Marshalled<TypeUsage>> typeUsage,
                    final Marshalled<Expression> base,
                    final Marshalled<Expression> exponent) {
        
        super(codeModel, marshaller, traits, typeUsage, base, exponent);
    }

    /**
     * {@link Marshal} an {@link Exponent}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Out} {@link Optional} {@link Marshalled} {@link TypeUsage}
     * @param base       the base {@link Marshalled} {@link Expression}
     * @param exponent   the exponent {@link Marshalled} {@link Expression}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<Marshalled<TypeUsage>>> typeUsage,
                           final Out<Marshalled<Expression>> base,
                           final Out<Marshalled<Expression>> exponent) {

        super.destructor(marshaller, traits, typeUsage, base, exponent);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof Exponent other
            && super.equals(other);
    }

    /**
     * Creates a {@link Exponent} of two {@link Expression}s.
     *
     * @param base  the base {@link Expression}
     * @param exponent the exponent {@link Expression}
     * @return a new {@link Exponent}
     */
    public static Exponent of(final Expression base,
                              final Expression exponent) {

        return new Exponent(base, exponent);
    }
    
    static {
        Marshalling.register(Exponent.class, MethodHandles.lookup());
    }
}
