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
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.hierarchical.descriptor.AbstractParentTypeDescriptor;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing that a {@link TypeDescriptor} {@code extends} or {@code inherits} from another
 * {@link TypeDescriptor}.
 *
 * @author brian.oliver
 * @since May-2024
 */
public final class ExtendsTypeDescriptor
    extends AbstractParentTypeDescriptor {

    /**
     * Constructs an {@link ExtendsTypeDescriptor}.
     *
     * @param parentTypeUsage the {@link NamedTypeUsage} for the type being extended
     */
    private ExtendsTypeDescriptor(final NamedTypeUsage parentTypeUsage) {

        super(parentTypeUsage);
    }

    /**
     * {@link Unmarshal} an {@link ExtendsTypeDescriptor}.
     *
     * @param codeModel      the {@link CodeModel}
     * @param parentTypeUsage the {@link Marshalled} {@link NamedTypeUsage} for the <i>parent</i> type
     * @param marshaller      the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits          the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public ExtendsTypeDescriptor(@Bound final CodeModel codeModel,
                                 @Bound final Marshaller marshaller,
                                 final Marshalled<NamedTypeUsage> parentTypeUsage,
                                 final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, parentTypeUsage, traits);
    }

    /**
     * {@link Marshal} an {@link ExtendsTypeDescriptor}.
     *
     * @param marshaller      the {@link Marshaller}
     * @param parentTypeUsage the {@link Out}d {@link NamedTypeUsage} for the <i>parent</i> type
     * @param traits          the {@link Out}d {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(@Bound final Marshaller marshaller,
                           final Out<Marshalled<NamedTypeUsage>> parentTypeUsage,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, parentTypeUsage, traits);
    }

    /**
     * Creates an {@link ExtendsTypeDescriptor}.
     *
     * @param superType the {@link NamedTypeUsage} for the type being extended
     */
    public static ExtendsTypeDescriptor of(final NamedTypeUsage superType) {
        return new ExtendsTypeDescriptor(superType);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(ExtendsTypeDescriptor.class, MethodHandles.lookup());
    }
}
