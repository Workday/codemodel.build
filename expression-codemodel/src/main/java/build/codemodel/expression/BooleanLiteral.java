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
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

/**
 * Describes {@link Boolean}-based {@link Literal}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class BooleanLiteral
    extends Literal<Boolean>
    implements LogicalExpression {

    /**
     * Constructs a {@link BooleanLiteral}.
     *
     * @param codeModel the {@link CodeModel}
     * @param value      the non-{@code null}able value
     */
    private BooleanLiteral(final CodeModel codeModel,
                           final boolean value) {

        super(codeModel, value,
            SpecificTypeUsage.of(codeModel, codeModel.getNameProvider().getTypeName(Boolean.class)));
    }

    /**
     * Unmarshalling constructor for {@link BooleanLiteral}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param value      the non-{@code null}able value
     * @param type       the {@link Optional} {@link TypeUsage} for the {@link BooleanLiteral}
     */
    @Unmarshal
    public BooleanLiteral(@Bound final CodeModel codeModel,
                          final Marshaller marshaller,
                          final Stream<Marshalled<Trait>> traits,
                          final Boolean value,
                          final Optional<TypeUsage> type) {

        super(codeModel, marshaller, traits, value, type);
    }

    /**
     * {@link Marshal} a {@link BooleanLiteral}.
     *
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param value      the non-{@code null}able value
     * @param type       the {@link Optional} {@link TypeUsage} for the {@link BooleanLiteral}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Boolean> value,
                           final Out<Optional<TypeUsage>> type) {

        super.destructor(marshaller, traits, value, type);
    }

    /**
     * Creates a {@link BooleanLiteral}.
     *
     * @param codeModel the {@link CodeModel}
     * @param value      the {@code null}able value
     */
    public static BooleanLiteral of(final CodeModel codeModel,
                                    final boolean value) {

        return new BooleanLiteral(codeModel, value);
    }

    static {
        Marshalling.register(BooleanLiteral.class, MethodHandles.lookup());
    }
}
