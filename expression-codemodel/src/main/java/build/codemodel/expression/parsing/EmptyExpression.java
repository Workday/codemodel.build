package build.codemodel.expression.parsing;

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
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@link Expression} that represents en empty expression.
 *
 * @author tim.berston
 * @since Feb-2025
 */
public class EmptyExpression
    extends AbstractTraitable
    implements Expression {

    /**
     * Constructs a {@link EmptyExpression}.
     *
     * @param codeModel the {@link CodeModel}
     */
    protected EmptyExpression(final CodeModel codeModel) {
        super(codeModel);
    }

    /**
     * {@link Unmarshal} an {@link EmptyExpression}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public EmptyExpression(@Bound final CodeModel codeModel,
                           @Bound final Marshaller marshaller,
                           final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);
    }

    /**
     * {@link Marshal} an {@link EmptyExpression}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(@Bound final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, traits);
    }

    /**
     * Creates an {@link EmptyExpression}.
     *
     * @param codeModel the {@link CodeModel} in which this {@link EmptyExpression} occurs
     */
    public static EmptyExpression of(final CodeModel codeModel) {
        return new EmptyExpression(codeModel);
    }

    @Override
    public Optional<TypeUsage> type() {
        return Optional.empty();
    }

    static {
        Marshalling.register(EmptyExpression.class, MethodHandles.lookup());
    }
}
