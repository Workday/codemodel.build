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
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.imperative.AbstractStatement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A local class/interface/enum/record declaration statement (e.g. {@code class Foo { ... }}
 * written inside a method body). The declared type itself is registered as its own
 * {@link build.codemodel.jdk.descriptor.JDKTypeDescriptor} in the enclosing {@link CodeModel};
 * this statement only marks the point in the enclosing block where that declaration appears,
 * referencing it by {@link TypeName}.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
public final class LocalTypeDeclaration
    extends AbstractStatement {

    /**
     * The {@link TypeName} of the declared local type.
     */
    private final TypeName typeName;

    private LocalTypeDeclaration(final CodeModel codeModel,
                                 final TypeName typeName) {
        super(codeModel);
        this.typeName = Objects.requireNonNull(typeName, "typeName must not be null");
    }

    @Unmarshal
    public LocalTypeDeclaration(@Bound final CodeModel codeModel,
                                final Marshaller marshaller,
                                final Stream<Marshalled<Trait>> traits,
                                final TypeName typeName) {
        super(codeModel, marshaller, traits);
        this.typeName = typeName;
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<TypeName> typeName) {
        super.destructor(marshaller, traits);
        typeName.set(this.typeName);
    }

    /**
     * Obtains the {@link TypeName} of the declared local type.
     *
     * @return the {@link TypeName}
     */
    public TypeName typeName() {
        return this.typeName;
    }

    @Override
    public Stream<? extends Composite> compositeChildren() {
        return Stream.empty();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof LocalTypeDeclaration other
            && Objects.equals(this.typeName, other.typeName)
            && super.equals(other);
    }

    /**
     * Creates a {@link LocalTypeDeclaration} statement.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName  the {@link TypeName} of the declared local type
     * @return a new {@link LocalTypeDeclaration}
     */
    public static LocalTypeDeclaration of(final CodeModel codeModel,
                                          final TypeName typeName) {
        return new LocalTypeDeclaration(Objects.requireNonNull(codeModel, "codeModel must not be null"),
            typeName);
    }

    static {
        Marshalling.register(LocalTypeDeclaration.class, MethodHandles.lookup());
    }
}
