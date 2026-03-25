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
import java.util.stream.Stream;

/**
 * Provides type information concerning the <i>usage</i> of a specific type in a non-parameterized manner.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public class SpecificTypeUsage
    extends AbstractNamedTypeUsage {

    /**
     * Constructs a {@link SpecificTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     */
    private SpecificTypeUsage(final CodeModel codeModel,
                              final TypeName typeName) {

        super(codeModel, typeName);
    }

    /**
     * {@link Unmarshal} a {@link SpecificTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param typeName   the {@link TypeName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public SpecificTypeUsage(@Bound final CodeModel codeModel,
                             final Marshaller marshaller,
                             final TypeName typeName,
                             final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, typeName, traits);
    }

    /**
     * {@link Marshal} an {@link AbstractNamedTypeUsage}.
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
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof SpecificTypeUsage other
            && super.equals(other);
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        // the direct dependencies are those of the annotations
        return traits(Dependent.class)
            .flatMap(Dependent::dependencies);
    }

    /**
     * Creates a {@link SpecificTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @return a {@link SpecificTypeUsage}
     */
    public static SpecificTypeUsage of(final CodeModel codeModel,
                                       final TypeName typeName) {

        return new SpecificTypeUsage(codeModel, typeName);
    }

    static {
        Marshalling.register(SpecificTypeUsage.class, MethodHandles.lookup());
    }
}
