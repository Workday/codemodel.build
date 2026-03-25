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

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Defines a {@link ModuleName} required by a {@link ModuleDescriptor}, thus defining a dependency of the
 * {@link ModuleDescriptor} on the required {@link ModuleName}.
 *
 * @author brian.oliver
 * @see ModuleName
 * @see ModuleDescriptor
 * @since Jun-2025
 */
@NonSingular
public class RequiresModuleDescriptor
    extends AbstractTraitable
    implements Trait {

    /**
     * The required {@link ModuleName} for the {@link ModuleDescriptor}.
     */
    private final ModuleName requires;

    /**
     * Creates a new {@link RequiresModuleDescriptor} with the specified {@link CodeModel} with the required
     * {@link ModuleName}.
     *
     * @param codeModel the {@link CodeModel}
     * @param requires   the required {@link ModuleName}
     */
    private RequiresModuleDescriptor(final CodeModel codeModel,
                                     final ModuleName requires) {

        super(codeModel);
        this.requires = Objects.requireNonNull(requires, "The required ModuleName must not be null");
    }

    /**
     * {@link Unmarshal} a {@link RequiresModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param requires   the required {@link ModuleName}
     */
    @Unmarshal
    public RequiresModuleDescriptor(@Bound final CodeModel codeModel,
                                    final Marshaller marshaller,
                                    final Stream<Marshalled<Trait>> traits,
                                    final ModuleName requires) {

        super(codeModel, marshaller, traits);
        this.requires = Objects.requireNonNull(requires, "The required ModuleName must not be null");
    }

    /**
     * {@link Marshal} a {@link RequiresModuleDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param requires   the {@link Out} required {@link ModuleName}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<ModuleName> requires) {

        super.destructor(marshaller, traits);
        requires.set(this.requires);
    }

    /**
     * Obtains the required {@link ModuleName} for the {@link ModuleDescriptor}.
     *
     * @return the required {@link ModuleName}
     */
    public ModuleName requiresModuleName() {
        return this.requires;
    }

    /**
     * Obtains the {@link Optional}ly defined {@link ModuleDescriptor} for the required {@link ModuleName}.
     *
     * @return the {@link Optional} {@link ModuleDescriptor} if it exists, otherwise {@link Optional#empty()}
     */
    public Optional<ModuleDescriptor> requires() {
        return codeModel().getModuleDescriptor(this.requires);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final RequiresModuleDescriptor other)) {
            return false;
        }
        return Objects.equals(this.requires, other.requires)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.requires);
    }

    /**
     * Creates a {@link RequiresModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param requires   the required {@link ModuleName}
     * @return a new {@link RequiresModuleDescriptor}
     */
    public static RequiresModuleDescriptor of(final CodeModel codeModel,
                                              final ModuleName requires) {

        return new RequiresModuleDescriptor(codeModel, requires);
    }

    static {
        Marshalling.register(RequiresModuleDescriptor.class, MethodHandles.lookup());
    }
}
