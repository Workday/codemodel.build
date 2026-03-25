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

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.Namespace;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An {@code abstract} {@link NamespaceDescriptor} implementation.
 *
 * @author brian.oliver
 * @since Aug-2025
 */
public abstract class AbstractNamespaceDescriptor
    extends AbstractTraitable
    implements NamespaceDescriptor {

    /**
     * The {@link Namespace}.
     */
    private final Namespace namespace;

    /**
     * Constructs an {@link AbstractNamespaceDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param namespace  the {@link Namespace}
     */
    protected AbstractNamespaceDescriptor(final CodeModel codeModel,
                                          final Namespace namespace) {

        super(codeModel);

        this.namespace = Objects.requireNonNull(namespace, "The Namespace must not be null");
    }

    /**
     * {@link Unmarshal} an {@link AbstractNamespaceDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param namespace  the {@link Namespace}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractNamespaceDescriptor(final CodeModel codeModel,
                                          final Marshaller marshaller,
                                          final Namespace namespace,
                                          final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);

        this.namespace = Objects.requireNonNull(namespace, "The Namespace must not be null");
    }

    /**
     * {@link Marshal} an {@link AbstractNamespaceDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param moduleName the {@link Out} {@link Namespace}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Namespace> moduleName,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, traits);
        moduleName.set(this.namespace);
    }

    @Override
    public Namespace namespace() {
        return this.namespace;
    }

    @Override
    public String toString() {
        return this.namespace.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractNamespaceDescriptor other)) {
            return false;
        }
        return Objects.equals(this.namespace, other.namespace)
            && super.equals(other);

    }
}
