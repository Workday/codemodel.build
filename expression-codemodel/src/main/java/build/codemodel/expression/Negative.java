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
 * Defines a <a href="https://en.wikipedia.org/wiki/Plus_and_minus_signs#Minus_sign">Negative</a> {@link Expression}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Negative
    extends AbstractUnaryArithmeticExpression {

    /**
     * Constructs a {@link Negative}.
     *
     * @param expression the {@link Expression}
     */
    private Negative(final Expression expression) {

        super(expression);
    }

    /**
     * Un{@link Marshal} a {@link Negative}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param expression the {@link Marshalled} {@link Expression} to negate
     */
    @Unmarshal
    public Negative(@Bound final CodeModel codeModel,
                    final Marshaller marshaller,
                    final Stream<Marshalled<Trait>> traits,
                    final Optional<Marshalled<TypeUsage>> typeUsage,
                    final Marshalled<Expression> expression) {
        
        super(codeModel, marshaller, traits, typeUsage, expression);
    }

    /**
     * {@link Marshal} a {@link Negative}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param expression the {@link Out} {@link Marshalled} {@link Expression}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<Marshalled<TypeUsage>>> typeUsage,
                           final Out<Marshalled<Expression>> expression) {

        super.destructor(marshaller, traits, typeUsage, expression);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof Negative other
            && super.equals(other);
    }

    /**
     * Creates a {@link Negative} of an {@link LogicalExpression}.
     *
     * @param expression the {@link Expression}
     */
    public static Negative of(final Expression expression) {
        return new Negative(expression);
    }
    
    static {
        Marshalling.register(Negative.class, MethodHandles.lookup());
    }
}
