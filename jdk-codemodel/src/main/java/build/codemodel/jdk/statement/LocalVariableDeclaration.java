package build.codemodel.jdk.statement;

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
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.imperative.AbstractStatement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A local variable declaration: {@code [final] Type name [= init]}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class LocalVariableDeclaration
    extends AbstractStatement {

    /**
     * Whether the variable is declared {@code final}.
     */
    private final boolean isFinal;

    /**
     * The resolved type of the variable.
     */
    private final TypeUsage type;

    /**
     * The variable name.
     */
    private final String name;

    /**
     * The optional initializer expression.
     */
    private final Optional<Expression> initializer;

    private LocalVariableDeclaration(final CodeModel codeModel,
                                     final boolean isFinal,
                                     final TypeUsage type,
                                     final String name,
                                     final Optional<Expression> initializer) {
        super(codeModel);
        this.isFinal = isFinal;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.initializer = initializer == null ? Optional.empty() : initializer;
    }

    @Unmarshal
    public LocalVariableDeclaration(@Bound final CodeModel codeModel,
                                    final Marshaller marshaller,
                                    final Stream<Marshalled<Trait>> traits,
                                    final Boolean isFinal,
                                    final Marshalled<TypeUsage> type,
                                    final String name,
                                    final Optional<Marshalled<Expression>> initializer) {
        super(codeModel, marshaller, traits);
        this.isFinal = isFinal != null && isFinal;
        this.type = marshaller.unmarshal(type);
        this.name = name;
        this.initializer = initializer == null ? Optional.empty() : initializer.map(marshaller::unmarshal);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Boolean> isFinal,
                           final Out<Marshalled<TypeUsage>> type,
                           final Out<String> name,
                           final Out<Optional<Marshalled<Expression>>> initializer) {
        super.destructor(marshaller, traits);
        isFinal.set(this.isFinal);
        type.set(marshaller.marshal(this.type));
        name.set(this.name);
        initializer.set(this.initializer.map(marshaller::marshal));
    }

    /**
     * Returns {@code true} if the variable is declared {@code final}.
     *
     * @return {@code true} if the variable is {@code final}
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    /**
     * Obtains the resolved type of the variable.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage type() {
        return this.type;
    }

    /**
     * Obtains the variable name.
     *
     * @return the variable name
     */
    public String name() {
        return this.name;
    }

    /**
     * Obtains the optional initializer expression.
     *
     * @return an {@link Optional} initializer {@link Expression}
     */
    public Optional<Expression> initializer() {
        return this.initializer;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof LocalVariableDeclaration other
            && this.isFinal == other.isFinal
            && Objects.equals(this.type, other.type)
            && Objects.equals(this.name, other.name)
            && Objects.equals(this.initializer, other.initializer)
            && super.equals(other);
    }

    /**
     * Creates a {@link LocalVariableDeclaration} statement.
     *
     * @param codeModel   the {@link CodeModel}
     * @param isFinal     whether the variable is {@code final}
     * @param type        the resolved {@link TypeUsage}
     * @param name        the variable name
     * @param initializer the optional initializer {@link Expression}
     * @return a new {@link LocalVariableDeclaration}
     */
    public static LocalVariableDeclaration of(final CodeModel codeModel,
                                              final boolean isFinal,
                                              final TypeUsage type,
                                              final String name,
                                              final Optional<Expression> initializer) {
        return new LocalVariableDeclaration(codeModel, isFinal, type, name, initializer);
    }

    static {
        Marshalling.register(LocalVariableDeclaration.class, MethodHandles.lookup());
    }
}
