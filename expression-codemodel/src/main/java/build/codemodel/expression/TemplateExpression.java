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

import build.base.foundation.stream.Streams;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract {@link Expression}.
 *
 * @author tim.berston
 * @since Feb-2025
 */
public class TemplateExpression
    extends AbstractExpression {

    private final List<Expression> expressions;
    private final TypeUsage type = SpecificTypeUsage.of(this.codeModel(), this.codeModel().getNameProvider().getTypeName(String.class));

    protected TemplateExpression(final CodeModel codeModel, final List<Expression> expressions) {

        super(codeModel);
        
        this.expressions = expressions;
    }
    
    /**
     * Un{@link Marshal} a {@link TemplateExpression}.
     *
     * @param codeModel   the {@link CodeModel}
     * @param marshaller   the {@link Marshaller}
     * @param traits       the {@link Stream} of {@link Trait}s
     * @param expressions  the {@link List} of {@link Expression}s
     */
    @Unmarshal
    public TemplateExpression(@Bound final CodeModel codeModel,
                              final Marshaller marshaller,
                              final Stream<Marshalled<Trait>> traits,
                              final Stream<Marshalled<Expression>> expressions) {
        
        super(codeModel, marshaller, traits);

        this.expressions = expressions
            .map(expr -> marshaller.unmarshal(expr))
            .collect(Collectors.toList());
    }

    /**
     * {@link Marshal} a {@link TemplateExpression}.
     *
     * @param marshaller  the {@link Marshaller}
     * @param traits      the {@link Out} parameter for {@link Stream} of {@link Trait}s
     * @param expressions the {@link Out} parameter for the {@link List} of {@link Expression}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<Expression>>> expressions) {
        
        super.destructor(marshaller, traits);

        expressions.set(this.expressions.stream().map(expr -> marshaller.marshal(expr)));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof TemplateExpression other
            && Streams.equals(expressions.stream(), other.expressions.stream())
            && super.equals(other);
    }

    public List<Expression> expressions() {
        return expressions;
    }

    @Override
    public Optional<TypeUsage> type() {
        return Optional.of(type);
    }

    public static TemplateExpression of(final List<Expression> expressions) {
        return new TemplateExpression(expressions.getFirst().codeModel(), expressions);
    }

    public static TemplateExpression empty(final CodeModel codeModel) {
        return new TemplateExpression(codeModel, new ArrayList<>());
    }
    
    static {
        Marshalling.register(TemplateExpression.class, MethodHandles.lookup());
    }
}
