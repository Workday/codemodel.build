package build.codemodel.jdk.descriptor;

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
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.hierarchical.descriptor.AbstractParentTypeDescriptor;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing that a {@code sealed} {@link TypeDescriptor} {@code permits} another
 * {@link TypeDescriptor} to extend or implement it.
 *
 * @author reed.vonredwitz
 * @see Sealed
 * @since Jul-2026
 */
@NonSingular
public final class PermitsTypeDescriptor
    extends AbstractParentTypeDescriptor {

    /**
     * Constructs a {@link PermitsTypeDescriptor}.
     *
     * @param permittedTypeUsage the {@link NamedTypeUsage} for the permitted type
     */
    private PermitsTypeDescriptor(final NamedTypeUsage permittedTypeUsage) {

        super(permittedTypeUsage);
    }

    /**
     * {@link Unmarshal} a {@link PermitsTypeDescriptor}.
     *
     * @param codeModel          the {@link CodeModel}
     * @param permittedTypeUsage the {@link Marshalled} {@link NamedTypeUsage} for the permitted type
     * @param marshaller         the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits             the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public PermitsTypeDescriptor(@Bound final CodeModel codeModel,
                                 @Bound final Marshaller marshaller,
                                 final Marshalled<NamedTypeUsage> permittedTypeUsage,
                                 final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, permittedTypeUsage, traits);
    }

    /**
     * {@link Marshal} a {@link PermitsTypeDescriptor}.
     *
     * @param marshaller         the {@link Marshaller}
     * @param permittedTypeUsage the {@link Out}d {@link NamedTypeUsage} for the permitted type
     * @param traits             the {@link Out}d {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(@Bound final Marshaller marshaller,
                           final Out<Marshalled<NamedTypeUsage>> permittedTypeUsage,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, permittedTypeUsage, traits);
    }

    /**
     * Creates a {@link PermitsTypeDescriptor}.
     *
     * @param permittedType the {@link NamedTypeUsage} for the type permitted to extend/implement the sealed type
     */
    public static PermitsTypeDescriptor of(final NamedTypeUsage permittedType) {
        return new PermitsTypeDescriptor(permittedType);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(PermitsTypeDescriptor.class, MethodHandles.lookup());
    }
}
