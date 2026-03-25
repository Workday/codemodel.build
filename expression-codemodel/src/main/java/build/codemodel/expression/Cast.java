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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Defines a <a href="https://en.wikipedia.org/wiki/Type_conversion">Cast</a> {@link Expression}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Cast
    extends AbstractExpression {

    /**
     * The {@link TypeUsage} specifying the target <i>Type</i>.
     */
    private final TypeUsage targetType;

    /**
     * The {@link Expression} to cast.
     */
    private final Expression expression;

    /**
     * Constructs a {@link Cast}.
     *
     * @param targetType the {@link TypeUsage} specifying the target <i>Type</i>
     * @param expression the {@link Expression} to cast
     */
    private Cast(final TypeUsage targetType,
                 final Expression expression) {

        super(Objects.requireNonNull(targetType, "The target TypeUsage must not be null").codeModel());
        this.targetType = targetType;
        this.expression = Objects.requireNonNull(expression, "The Expression must not be null");
    }

    /**
     * Un{@link Marshal} a {@link Cast}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param targetType the {@link Marshalled} {@link TypeUsage} specifying the target <i>Type</i>
     * @param expression the {@link Marshalled} {@link Expression} to cast
     */
    @Unmarshal
    public Cast(@Bound final CodeModel codeModel,
                final Marshaller marshaller,
                final Stream<Marshalled<Trait>> traits,
                final Marshalled<TypeUsage> targetType,
                final Marshalled<Expression> expression) {

        super(codeModel, marshaller, traits);

        this.targetType = marshaller.unmarshal(targetType);
        this.expression = marshaller.unmarshal(expression);
    }

    /**
     * {@link Marshal} a {@link Cast}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param targetType the {@link Out} {@link Marshalled} TypeUsage}
     * @param expression the {@link Out} {@link Marshalled} {@link Expression} to cast
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<TypeUsage>> targetType,
                           final Out<Marshalled<Expression>> expression) {

        super.destructor(marshaller, traits);

        targetType.set(marshaller.marshal(this.targetType));
        expression.set(marshaller.marshal(this.expression));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof Cast other
            && this.targetType.equals(other.targetType)
            && this.expression.equals(other.expression)
            && super.equals(other);
    }

    @Override
    public Optional<TypeUsage> type() {
        return Optional.of(this.targetType);
    }

    /**
     * Obtains the target <i>Type</i> {@link TypeUsage}.
     *
     * @return the target {@link TypeUsage}
     */
    public TypeUsage targetType() {
        return this.targetType;
    }

    /**
     * Obtains the {@link Expression} to cast.
     *
     * @return the {@link Expression} to cast
     */
    public Expression expression() {
        return this.expression;
    }

    /**
     * Creates a {@link Cast}.
     *
     * @param targetType the {@link TypeUsage} specifying the target <i>Type</i>
     * @param expression the {@link Expression} to cast
     */
    public static Cast of(final TypeUsage targetType,
                          final Expression expression) {
        return new Cast(targetType, expression);
    }

    static {
        Marshalling.register(Cast.class, MethodHandles.lookup());
    }
}
