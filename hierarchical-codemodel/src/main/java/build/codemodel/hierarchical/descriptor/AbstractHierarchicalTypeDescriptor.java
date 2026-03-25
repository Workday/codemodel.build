package build.codemodel.hierarchical.descriptor;

/*-
 * #%L
 * Hierarchical Code Model
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
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTypeDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TraitAware;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.hierarchical.AbstractHierarchicalCodeModel;

import java.util.stream.Stream;

/**
 * An {@code abstract} {@link HierarchicalTypeDescriptor}.
 *
 * @author brian.oliver
 * @see HierarchicalTypeDescriptor
 * @since Feb-2025
 */
public abstract class AbstractHierarchicalTypeDescriptor
    extends AbstractTypeDescriptor
    implements HierarchicalTypeDescriptor, TraitAware {

    /**
     * Constructs an {@link AbstractHierarchicalTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     */
    protected AbstractHierarchicalTypeDescriptor(final CodeModel codeModel,
                                                 final TypeName typeName) {

        super(codeModel, typeName);
    }

    /**
     * {@link Unmarshal} a {@link AbstractHierarchicalTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractHierarchicalTypeDescriptor(final CodeModel codeModel,
                                                 final Marshaller marshaller,
                                                 final TypeName typeName,
                                                 final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, typeName, traits);
    }

    /**
     * {@link Marshal} an {@link AbstractHierarchicalTypeDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link Capture}d {@link TypeName}
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<TypeName> typeName,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, typeName, traits);
    }

    @Override
    public void onAddedTrait(final Trait trait) {

        // notify the HierarchicalCodeModel of an added ParentTypeDescriptor
        if (trait instanceof ParentTypeDescriptor parentTypeDescriptor
            && codeModel() instanceof AbstractHierarchicalCodeModel hierarchicalCodeModel) {

            hierarchicalCodeModel.onCreatedParentTypeDescriptor(parentTypeDescriptor.parentTypeName(), this);
        }
    }

    @Override
    public void onRemovedTrait(final Trait trait) {

        // notify the HierarchicalCodeModel of a removed ParentTypeDescriptor
        if (trait instanceof ParentTypeDescriptor parentTypeDescriptor
            && codeModel() instanceof AbstractHierarchicalCodeModel hierarchicalCodeModel) {

            hierarchicalCodeModel.onRemovedParentTypeDescriptor(parentTypeDescriptor.parentTypeName(), this);
        }
    }

    @Override
    public Stream<HierarchicalTypeDescriptor> parents() {
        return codeModel() instanceof AbstractHierarchicalCodeModel hierarchicalCodeModel
            ? hierarchicalCodeModel.parents(this)
            : HierarchicalTypeDescriptor.super.parents();
    }

    @Override
    public Stream<HierarchicalTypeDescriptor> children() {
        return codeModel() instanceof AbstractHierarchicalCodeModel hierarchicalCodeModel
            ? hierarchicalCodeModel.children(this)
            : HierarchicalTypeDescriptor.super.children();
    }
}
