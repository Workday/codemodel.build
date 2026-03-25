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
 * Defines a {@link Number}-based {@link Literal}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class NumericLiteral
    extends Literal<Number>
    implements ArithmeticExpression {

    /**
     * Constructs a {@link NumericLiteral}.
     *
     * @param codeModel the {@link CodeModel}
     * @param value      the non-{@code null}able value
     */
    private NumericLiteral(final CodeModel codeModel,
                           final Number value) {

        super(codeModel, value);
    }

    /**
     * Unmarshalling constructor for {@link NumericLiteral}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param value      the non-{@code null}able value
     * @param type       the {@link Optional} {@link TypeUsage} for the {@link NumericLiteral}
     */
    @Unmarshal
    public NumericLiteral(@Bound final CodeModel codeModel,
                          final Marshaller marshaller,
                          final Stream<Marshalled<Trait>> traits,
                          final Number value,
                          final Optional<TypeUsage> type) {

        super(codeModel, marshaller, traits, value, type);
    }

    /**
     * {@link Marshal} a {@link NumericLiteral}.
     *
     * @param marshaller the {@link Marshaller} for marshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param value      the non-{@code null}able value
     * @param type       the {@link Optional} {@link TypeUsage} for the {@link NumericLiteral}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Number> value,
                           final Out<Optional<TypeUsage>> type) {

        super.destructor(marshaller, traits, value, type);
    }

    /**
     * Creates a {@link NumericLiteral}.
     *
     * @param codeModel the {@link CodeModel}
     * @param value      the {@code null}able value
     */
    public static NumericLiteral of(final CodeModel codeModel,
                                    final Number value) {

        return new NumericLiteral(codeModel, value);
    }

    static {
        Marshalling.register(NumericLiteral.class, MethodHandles.lookup());
    }
}
