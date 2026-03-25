package build.codemodel.foundation.usage;

/*-
 * #%L
 * Code Model Foundation
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
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides type information concerning the <i>usage</i> of a <i>unknown</i> type, where the actual type name
 * can't be inferred.
 *
 * @author brian.oliver
 * @since Feb-2024
 */

public class UnknownTypeUsage
    extends AbstractNamedTypeUsage {

    /**
     * Constructs an {@link UnknownTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     */
    private UnknownTypeUsage(final CodeModel codeModel) {
        super(codeModel,
            codeModel.getNameProvider().getTypeName(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                codeModel.getNameProvider().getIrreducibleName("null")));
    }

    /**
     * {@link Unmarshal} an {@link UnknownTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param typeName   the {@link TypeName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public UnknownTypeUsage(@Bound final CodeModel codeModel,
                            final Marshaller marshaller,
                            final TypeName typeName,
                            final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, typeName, traits);
    }

    /**
     * {@link Marshal} an {@link UnknownTypeUsage}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link TypeName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<TypeName> typeName,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, typeName, traits);
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return traits(Dependent.class)
            .flatMap(Dependent::dependencies);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof UnknownTypeUsage;
    }

    /**
     * Creates an {@link UnknownTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @return an {@link UnknownTypeUsage}
     */
    public static UnknownTypeUsage create(final CodeModel codeModel) {
        return new UnknownTypeUsage(codeModel);
    }

    static {
        Marshalling.register(UnknownTypeUsage.class, MethodHandles.lookup());
    }
}
