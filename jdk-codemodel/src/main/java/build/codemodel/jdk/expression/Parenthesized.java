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
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A parenthesized expression: {@code (expr)}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Parenthesized
    extends AbstractExpression {

    /**
     * The inner expression wrapped in parentheses.
     */
    private final Expression inner;

    private Parenthesized(final Expression inner) {
        super(Objects.requireNonNull(inner, "inner must not be null").codeModel());
        this.inner = inner;
    }

    @Unmarshal
    public Parenthesized(@Bound final CodeModel codeModel,
                         final Marshaller marshaller,
                         final Stream<Marshalled<Trait>> traits,
                         final Marshalled<Expression> inner) {
        super(codeModel, marshaller, traits);
        this.inner = marshaller.unmarshal(inner);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> inner) {
        super.destructor(marshaller, traits);
        inner.set(marshaller.marshal(this.inner));
    }

    @Override
    public Optional<TypeUsage> type() {
        return inner.type();
    }

    /**
     * Obtains the inner expression wrapped in parentheses.
     *
     * @return the inner {@link Expression}
     */
    public Expression inner() {
        return this.inner;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Parenthesized other
            && Objects.equals(this.inner, other.inner)
            && super.equals(other);
    }

    /**
     * Creates a {@link Parenthesized} expression.
     *
     * @param inner the inner {@link Expression}
     * @return a new {@link Parenthesized}
     */
    public static Parenthesized of(final Expression inner) {
        return new Parenthesized(inner);
    }

    static {
        Marshalling.register(Parenthesized.class, MethodHandles.lookup());
    }
}
