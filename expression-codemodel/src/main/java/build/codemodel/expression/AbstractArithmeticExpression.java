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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An abstract {@link ArithmeticExpression}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public abstract class AbstractArithmeticExpression
    extends AbstractExpression
    implements ArithmeticExpression {

    /**
     * The {@link TypeUsage} for the {@link ArithmeticExpression}.
     */
    private final Optional<TypeUsage> typeUsage;

    /**
     * Constructs a {@link AbstractArithmeticExpression}.
     *
     * @param codeModel the {@link CodeModel}
     */
    protected AbstractArithmeticExpression(final CodeModel codeModel,
                                           final Optional<TypeUsage> typeUsage) {

        super(codeModel);
        this.typeUsage = typeUsage == null ? Optional.empty() : typeUsage;
    }

    /**
     * {@link Unmarshal} an {@link AbstractArithmeticExpression}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     */
    protected AbstractArithmeticExpression(final CodeModel codeModel,
                                           final Marshaller marshaller,
                                           final Stream<Marshalled<Trait>> traits,
                                           final Optional<Marshalled<TypeUsage>> typeUsage) {

        super(codeModel, marshaller, traits);

        this.typeUsage = typeUsage == null
            ? Optional.empty()
            : typeUsage.map(marshaller::unmarshal);
    }

    /**
     * {@link Marshal} an {@link AbstractArithmeticExpression}.
     *
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param typeUsage  the {@link Optional} {@link Marshalled} {@link TypeUsage}
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Stream<Marshalled<Trait>>> traits,
                              final Out<Optional<Marshalled<TypeUsage>>> typeUsage) {

        super.destructor(marshaller, traits);

        typeUsage.set(this.typeUsage.map(marshaller::marshal));
    }

    @Override
    public Optional<TypeUsage> type() {
        return this.typeUsage;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof AbstractArithmeticExpression other
            && typeUsage.equals(other.typeUsage)
            && super.equals(other);
    }
}
