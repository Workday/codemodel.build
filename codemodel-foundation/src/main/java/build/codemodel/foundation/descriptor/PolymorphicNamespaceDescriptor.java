package build.codemodel.foundation.descriptor;

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
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * A concrete <a href="https://en.wikipedia.org/wiki/Polymorphism_(computer_science)">Polymorphic</a>
 * {@link NamespaceDescriptor} that may be used to represent different types of namespaces.
 *
 * @author brian.oliver
 * @see NamespaceDescriptor
 * @since Jun-2025
 */
public class PolymorphicNamespaceDescriptor
    extends AbstractNamespaceDescriptor {

    /**
     * Constructs an {@link PolymorphicNamespaceDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param namespace  the {@link ModuleName}
     */
    private PolymorphicNamespaceDescriptor(final CodeModel codeModel,
                                           final Namespace namespace) {

        super(codeModel, namespace);
    }

    /**
     * {@link Unmarshal} a {@link PolymorphicNamespaceDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param namespace  the {@link Namespace}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public PolymorphicNamespaceDescriptor(@Bound final CodeModel codeModel,
                                          final Marshaller marshaller,
                                          final Namespace namespace,
                                          final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, namespace, traits);
    }

    /**
     * {@link Marshal} an {@link PolymorphicNamespaceDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param namespace  the {@link Out} {@link Namespace}
     * @param traits     the {@link Out} {@link Stream} of {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Namespace> namespace,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, namespace, traits);
    }

    /**
     * Creates an {@link PolymorphicNamespaceDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param namespace  the {@link Namespace}
     * @return a new {@link PolymorphicNamespaceDescriptor}
     */
    public static PolymorphicNamespaceDescriptor of(final CodeModel codeModel,
                                                    final Namespace namespace) {

        return new PolymorphicNamespaceDescriptor(codeModel, namespace);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(PolymorphicNamespaceDescriptor.class, MethodHandles.lookup());
    }
}
