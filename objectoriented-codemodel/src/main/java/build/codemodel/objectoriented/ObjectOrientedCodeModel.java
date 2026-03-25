package build.codemodel.objectoriented;

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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.ModuleDescriptor;
import build.codemodel.foundation.descriptor.NamespaceDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.hierarchical.AbstractHierarchicalCodeModel;
import build.codemodel.hierarchical.HierarchicalCodeModel;
import jakarta.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * An <i>Object-Oriented</i> {@link HierarchicalCodeModel}.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public class ObjectOrientedCodeModel
    extends AbstractHierarchicalCodeModel {

    /**
     * Constructs an empty {@link ObjectOrientedCodeModel}.
     *
     * @param nameProvider the {@link NameProvider}
     */
    @Inject
    public ObjectOrientedCodeModel(final NameProvider nameProvider) {
        super(nameProvider);
    }

    /**
     * Constructs an {@link ObjectOrientedCodeModel} using a {@link Marshaller}.
     *
     * @param nameProvider         the {@link NameProvider}
     * @param marshaller           the {@link Marshaller}
     * @param traits               the {@link Traitable}
     * @param typeDescriptors      the {@link Stream} of {@link Marshalled} {@link TypeDescriptor}s
     * @param moduleDescriptors    the {@link Stream} of {@link Marshalled} {@link ModuleDescriptor}s
     * @param namespaceDescriptors the {@link Stream} of {@link Marshalled} {@link NamespaceDescriptor}s
     */
    @Unmarshal
    public ObjectOrientedCodeModel(final @Bound NameProvider nameProvider,
                                    final Marshaller marshaller,
                                    final Stream<Marshalled<Trait>> traits,
                                    final Stream<Marshalled<TypeDescriptor>> typeDescriptors,
                                    final Stream<Marshalled<ModuleDescriptor>> moduleDescriptors,
                                    final Stream<Marshalled<NamespaceDescriptor>> namespaceDescriptors) {

        super(nameProvider, marshaller, traits, typeDescriptors, moduleDescriptors, namespaceDescriptors);
    }

    /**
     * Destructs an {@link ObjectOrientedCodeModel} so it can be {@link Marshal}led.
     *
     * @param marshaller           the {@link Marshaller} to use for marshalling
     * @param traits               the {@link Marshalled} {@link Trait}s of the {@link CodeModel} itself
     * @param typeDescriptors      the {@link Stream} of marshallable {@link TypeDescriptor}s
     * @param moduleDescriptors    the {@link Stream} of marshallable {@link ModuleDescriptor}s
     * @param namespaceDescriptors the {@link Stream} of marshallable {@link NamespaceDescriptor}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<TypeDescriptor>>> typeDescriptors,
                           final Out<Stream<Marshalled<ModuleDescriptor>>> moduleDescriptors,
                           final Out<Stream<Marshalled<NamespaceDescriptor>>> namespaceDescriptors) {

        super.destructor(marshaller, traits, typeDescriptors, moduleDescriptors, namespaceDescriptors);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(ObjectOrientedCodeModel.class, MethodHandles.lookup());
    }
}
