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
import java.util.Optional;
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
     * The {@link Expression} to return, or empty for a bare {@code return;}.
     */
    private Optional<Expression> expression;

    /**
     * Constructs a {@link Return} {@link Statement}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param expression the optional {@link Expression}
     */
    private Return(final CodeModel codeModel, final Optional<Expression> expression) {

        super(Objects.requireNonNull(codeModel, "codeModel must not be null"));
        this.expression = expression == null ? Optional.empty() : expression;
    }

    /**
     * {@link Unmarshal} a {@link Return}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param expression the optional {@link Marshalled} {@link Expression}
     */
    @Unmarshal
    public Return(@Bound final CodeModel codeModel,
                  @Bound final Marshaller marshaller,
                  final Stream<Marshalled<Trait>> traits,
                  final Optional<Marshalled<Expression>> expression) {

        super(codeModel, marshaller, traits);

        this.expression = expression == null ? Optional.empty() : expression.map(marshaller::unmarshal);
    }

    /**
     * {@link Marshal} a {@link Return}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param expression the {@link Out} optional {@link Marshalled} {@link Expression}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<Marshalled<Expression>>> expression) {

        super.destructor(marshaller, traits);

        expression.set(this.expression.map(marshaller::marshal));
    }

    /**
     * Obtains the {@link Expression} being returned, or empty for a bare {@code return;}.
     *
     * @return an {@link Optional} {@link Expression}
     */
    public Optional<Expression> expression() {
        return this.expression;
    }

    /**
     * Creates a {@link Return} {@link Statement} with a value.
     *
     * @param expression the {@link Expression} to return
     */
    public static Return of(final Expression expression) {
        Objects.requireNonNull(expression, "expression must not be null");
        return new Return(expression.codeModel(), Optional.of(expression));
    }

    /**
     * Creates a bare {@code return;} {@link Statement}.
     *
     * @param codeModel the {@link CodeModel}
     */
    public static Return of(final CodeModel codeModel) {
        return new Return(Objects.requireNonNull(codeModel, "codeModel must not be null"), Optional.empty());
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
