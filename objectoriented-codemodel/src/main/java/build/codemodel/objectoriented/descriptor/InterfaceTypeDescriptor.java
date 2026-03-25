package build.codemodel.objectoriented.descriptor;

/*-
 * #%L
 * Object-Oriented Code Model
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

import build.base.foundation.Capture;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.hierarchical.descriptor.AbstractHierarchicalTypeDescriptor;
import build.codemodel.hierarchical.descriptor.HierarchicalTypeDescriptor;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * A {@link TypeDescriptor} for representing an <a href="https://en.wikipedia.org/wiki/Interface_(object-oriented_programming)">Interface</a>
 * type in the <a href="https://en.wikipedia.org/wiki/Object-oriented_programming">Object-Oriented</a> Paradigm.
 *
 * @author brian.oliver
 * @see HierarchicalTypeDescriptor
 * @see ClassTypeDescriptor
 * @since Feb-2025
 */
public class InterfaceTypeDescriptor
    extends AbstractHierarchicalTypeDescriptor {

    /**
     * Constructs an {@link InterfaceTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     */
    protected InterfaceTypeDescriptor(final CodeModel codeModel,
                                      final TypeName typeName) {

        super(codeModel, typeName);
    }

    /**
     * {@link Unmarshal} a {@link InterfaceTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public InterfaceTypeDescriptor(@Bound final CodeModel codeModel,
                                   final Marshaller marshaller,
                                   final TypeName typeName,
                                   final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, typeName, traits);
    }

    /**
     * {@link Marshal} an {@link InterfaceTypeDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link Capture}d {@link TypeName}
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<TypeName> typeName,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, typeName, traits);
    }

    /**
     * Creates an {@link InterfaceTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @return a new {@link InterfaceTypeDescriptor}
     */
    public static InterfaceTypeDescriptor of(final CodeModel codeModel,
                                             final TypeName typeName) {

        return new InterfaceTypeDescriptor(codeModel, typeName);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(InterfaceTypeDescriptor.class, MethodHandles.lookup());
    }
}
