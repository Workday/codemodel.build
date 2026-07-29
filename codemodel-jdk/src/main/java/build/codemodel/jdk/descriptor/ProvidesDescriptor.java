package build.codemodel.jdk.descriptor;

/*-
 * #%L
 * JDK Code Model
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
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing a {@code provides} directive in a {@code module-info.java}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public final class ProvidesDescriptor
    extends AbstractTraitable
    implements Trait, Traitable {

    private final TypeUsage serviceType;
    private final List<TypeUsage> implementationTypes;

    private ProvidesDescriptor(final CodeModel codeModel,
                               final TypeUsage serviceType,
                               final List<TypeUsage> implementationTypes) {

        super(codeModel);
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.implementationTypes = List.copyOf(implementationTypes);
    }

    /**
     * {@link Unmarshal} a {@link ProvidesDescriptor}.
     */
    @Unmarshal
    public ProvidesDescriptor(@Bound final CodeModel codeModel,
                              final Marshaller marshaller,
                              final Stream<Marshalled<Trait>> traits,
                              final TypeUsage serviceType,
                              final List<TypeUsage> implementationTypes) {

        super(codeModel, marshaller, traits);
        this.serviceType = serviceType;
        this.implementationTypes = List.copyOf(implementationTypes);
    }

    /**
     * {@link Marshal} a {@link ProvidesDescriptor}.
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<TypeUsage> serviceType,
                           final Out<List<TypeUsage>> implementationTypes) {

        super.destructor(marshaller, traits);
        serviceType.set(this.serviceType);
        implementationTypes.set(this.implementationTypes);
    }

    public TypeUsage serviceType() {
        return serviceType;
    }

    public List<TypeUsage> implementationTypes() {
        return implementationTypes;
    }

    @Override
    protected Stream<? extends Composite> compositeChildren() {
        return Stream.concat(Stream.of(serviceType), implementationTypes.stream());
    }

    public static ProvidesDescriptor of(final CodeModel codeModel,
                                        final TypeUsage serviceType,
                                        final Stream<TypeUsage> implementationTypes) {
        return new ProvidesDescriptor(codeModel, serviceType, implementationTypes.toList());
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof ProvidesDescriptor other
            && Objects.equals(this.serviceType, other.serviceType)
            && Objects.equals(this.implementationTypes, other.implementationTypes)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serviceType, this.implementationTypes, super.hashCode());
    }

    static {
        Marshalling.register(ProvidesDescriptor.class, MethodHandles.lookup());
    }
}
