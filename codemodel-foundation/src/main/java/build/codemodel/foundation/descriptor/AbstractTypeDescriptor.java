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
import build.codemodel.foundation.naming.TypeName;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An {@code abstract} {@link TypeDescriptor} implementation.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public abstract class AbstractTypeDescriptor
    extends AbstractTraitable
    implements TypeDescriptor {

    /**
     * The {@link TypeName}.
     */
    private final TypeName typeName;

    /**
     * Constructs an {@link AbstractTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     */
    protected AbstractTypeDescriptor(final CodeModel codeModel,
                                     final TypeName typeName) {

        super(codeModel);

        this.typeName = Objects.requireNonNull(typeName, "The TypeName must not be null");
    }

    /**
     * {@link Unmarshal} an {@link AbstractTypeDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractTypeDescriptor(final CodeModel codeModel,
                                     final Marshaller marshaller,
                                     final TypeName typeName,
                                     final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);

        this.typeName = Objects.requireNonNull(typeName, "The TypeName must not be null");
    }

    /**
     * {@link Marshal} an {@link AbstractTypeDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link Out} {@link TypeName}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<TypeName> typeName,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, traits);
        typeName.set(this.typeName);
    }

    @Override
    public TypeName typeName() {
        return this.typeName;
    }

    @Override
    public String toString() {
        return typeName().toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractTypeDescriptor other)) {
            return false;
        }
        return Objects.equals(this.typeName, other.typeName)
            && super.equals(other);

    }
}
