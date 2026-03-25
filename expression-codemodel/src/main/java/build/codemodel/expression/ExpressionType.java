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
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * A {@link Trait} that carries the resolved {@link TypeUsage} for an {@link Expression} —
 * the type of value the expression produces when evaluated.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class ExpressionType implements Trait {

    /**
     * The resolved type of the expression.
     */
    private final TypeUsage typeUsage;

    private ExpressionType(final TypeUsage typeUsage) {
        this.typeUsage = Objects.requireNonNull(typeUsage, "typeUsage must not be null");
    }

    @Unmarshal
    public ExpressionType(final Marshaller marshaller,
                          final Marshalled<TypeUsage> typeUsage) {
        this.typeUsage = marshaller.unmarshal(typeUsage);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Marshalled<TypeUsage>> typeUsage) {
        typeUsage.set(marshaller.marshal(this.typeUsage));
    }

    /**
     * Obtains the resolved {@link TypeUsage} of the expression.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage typeUsage() {
        return this.typeUsage;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof ExpressionType other
            && Objects.equals(this.typeUsage, other.typeUsage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.typeUsage);
    }

    /**
     * Creates an {@link ExpressionType} trait for the given {@link TypeUsage}.
     *
     * @param typeUsage the resolved {@link TypeUsage}
     * @return a new {@link ExpressionType}
     */
    public static ExpressionType of(final TypeUsage typeUsage) {
        return new ExpressionType(typeUsage);
    }

    static {
        Marshalling.register(ExpressionType.class, MethodHandles.lookup());
    }
}
