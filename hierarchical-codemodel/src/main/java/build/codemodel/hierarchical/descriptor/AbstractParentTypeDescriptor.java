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

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An {@code abstract} {@link ParentTypeDescriptor}.
 *
 * @author brian.oliver
 * @since Feb-2025
 */
public abstract class AbstractParentTypeDescriptor
    extends AbstractTraitable
    implements ParentTypeDescriptor {

    /**
     * The {@link NamedTypeUsage} defining the <i>parent</i> type.
     */
    private final NamedTypeUsage parentTypeUsage;

    /**
     * Constructs an {@link AbstractParentTypeDescriptor}.
     *
     * @param parentTypeUsage the {@link NamedTypeUsage} for the <i>parent</i> type
     */
    protected AbstractParentTypeDescriptor(final NamedTypeUsage parentTypeUsage) {
        super(Objects.requireNonNull(parentTypeUsage, "The Parent TypeUsage must not be null").codeModel());

        this.parentTypeUsage = parentTypeUsage;
    }

    /**
     * {@link Unmarshal} an {@link AbstractParentTypeDescriptor}.
     *
     * @param codeModel      the {@link CodeModel}
     * @param marshaller      the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param parentTypeUsage the {@link Marshalled} {@link NamedTypeUsage} for the <i>parent</i> type
     * @param traits          the {@link Marshalled} {@link Trait}s
     */
    protected AbstractParentTypeDescriptor(final CodeModel codeModel,
                                           final Marshaller marshaller,
                                           final Marshalled<NamedTypeUsage> parentTypeUsage,
                                           final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);

        this.parentTypeUsage = marshaller.unmarshal(parentTypeUsage);
    }

    /**
     * {@link Marshal} an {@link AbstractParentTypeDescriptor}.
     *
     * @param marshaller      the {@link Marshaller}
     * @param parentTypeUsage the {@link Out}d {@link Marshalled} {@link NamedTypeUsage} for the <i>parent</i> type
     * @param traits          the {@link Out}d {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Marshalled<NamedTypeUsage>> parentTypeUsage,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, traits);

        parentTypeUsage.set(marshaller.marshal(this.parentTypeUsage));
    }

    @Override
    public NamedTypeUsage parentTypeUsage() {
        return this.parentTypeUsage;
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return Stream.of(parentTypeUsage());
    }
}
