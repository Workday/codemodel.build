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
import java.util.stream.Stream;

/**
 * A concrete <a href="https://en.wikipedia.org/wiki/Polymorphism_(computer_science)">Polymorphic</a>
 * {@link ModuleDescriptor} that may be used to represent different types of module information about a named module.
 *
 * @author brian.oliver
 * @see ModuleDescriptor
 * @since Jun-2025
 */
public class PolymorphicModuleDescriptor
    extends AbstractModuleDescriptor {

    /**
     * Constructs an {@link PolymorphicModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param moduleName the {@link ModuleName}
     */
    private PolymorphicModuleDescriptor(final CodeModel codeModel,
                                        final ModuleName moduleName) {

        super(codeModel, moduleName);
    }

    /**
     * {@link Unmarshal} a {@link PolymorphicModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param moduleName the {@link ModuleName}
     * @param traits     the {@link Stream} {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public PolymorphicModuleDescriptor(@Bound final CodeModel codeModel,
                                       final Marshaller marshaller,
                                       final ModuleName moduleName,
                                       final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, moduleName, traits);
    }

    /**
     * {@link Marshal} an {@link PolymorphicModuleDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param moduleName the {@link Out} {@link Marshalled}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<ModuleName> moduleName,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, moduleName, traits);
    }

    /**
     * Creates an {@link PolymorphicModuleDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param moduleName the {@link ModuleName}
     * @return a new {@link PolymorphicModuleDescriptor}
     */
    public static PolymorphicModuleDescriptor of(final CodeModel codeModel,
                                                 final ModuleName moduleName) {

        return new PolymorphicModuleDescriptor(codeModel, moduleName);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(PolymorphicModuleDescriptor.class, MethodHandles.lookup());
    }
}
