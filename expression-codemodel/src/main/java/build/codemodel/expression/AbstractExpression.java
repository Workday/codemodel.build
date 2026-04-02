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

import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An abstract {@link Expression}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public abstract class AbstractExpression
    extends AbstractTraitable
    implements Expression {

    /**
     * Constructs a {@link AbstractExpression}.
     *
     * @param codeModel the {@link CodeModel}
     */
    protected AbstractExpression(final CodeModel codeModel) {
        super(codeModel);
    }

    /**
     * {@link Unmarshal} an {@link AbstractExpression}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractExpression(final CodeModel codeModel,
                                 final Marshaller marshaller,
                                 final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);
    }

    @Override
    public Optional<TypeUsage> type() {
        return getTrait(ExpressionType.class).map(ExpressionType::typeUsage);
    }
}
