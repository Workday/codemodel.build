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
import build.codemodel.foundation.naming.ModuleName;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An {@code abstract} {@link ModuleDescriptor} implementation.
 *
 * @author brian.oliver
 * @since Jun-2025
 */
public abstract class AbstractModuleDescriptor
    extends AbstractTraitable
    implements ModuleDescriptor {

    /**
     * The {@link ModuleName}.
     */
    private final ModuleName moduleName;

    /**
     * Constructs an {@link AbstractModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param moduleName the {@link ModuleName}
     */
    protected AbstractModuleDescriptor(final CodeModel codeModel,
                                       final ModuleName moduleName) {

        super(codeModel);

        this.moduleName = Objects.requireNonNull(moduleName, "The ModuleName must not be null");
    }

    /**
     * {@link Unmarshal} an {@link AbstractModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param moduleName the {@link ModuleName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractModuleDescriptor(final CodeModel codeModel,
                                       final Marshaller marshaller,
                                       final ModuleName moduleName,
                                       final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);

        this.moduleName = Objects.requireNonNull(moduleName, "The ModuleName must not be null");
    }

    /**
     * {@link Marshal} an {@link AbstractModuleDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param moduleName the {@link Out} {@link ModuleName}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<ModuleName> moduleName,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, traits);
        moduleName.set(this.moduleName);
    }

    @Override
    public ModuleName moduleName() {
        return this.moduleName;
    }

    @Override
    public String toString() {
        return this.moduleName.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractModuleDescriptor other)) {
            return false;
        }
        return Objects.equals(this.moduleName, other.moduleName)
            && super.equals(other);

    }
}
