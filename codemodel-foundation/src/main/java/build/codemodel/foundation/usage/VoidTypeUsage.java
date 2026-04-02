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
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides type information concerning the <i>usage</i> of the {@code void} type, for example the type returned by a
 * method.
 *
 * @author brian.oliver
 * @see TypeDescriptor
 * @since Jan-2024
 */
public class VoidTypeUsage
    extends AbstractNamedTypeUsage
    implements Dependent {

    /**
     * Constructs a {@link VoidTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     */
    private VoidTypeUsage(final CodeModel codeModel) {
        super(codeModel,
            codeModel.getNameProvider().getTypeName(
                codeModel.getNameProvider().getModuleName("java.base"),
                codeModel.getNameProvider().getNamespace("java.lang"),
                Optional.empty(),
                codeModel.getNameProvider().getIrreducibleName("void")));
    }

    /**
     * {@link Unmarshal} an {@link VoidTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param typeName   the {@link TypeName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public VoidTypeUsage(@Bound final CodeModel codeModel,
                         final Marshaller marshaller,
                         final TypeName typeName,
                         final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, typeName, traits);
    }

    /**
     * {@link Marshal} an {@link VoidTypeUsage}.
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
        if (this == object) {
            return true;
        }
        return object instanceof VoidTypeUsage;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Creates a {@link VoidTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @return a {@link VoidTypeUsage}
     */
    public static VoidTypeUsage create(final CodeModel codeModel) {
        return new VoidTypeUsage(codeModel);
    }

    static {
        Marshalling.register(VoidTypeUsage.class, MethodHandles.lookup());
    }
}
