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
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing a {@code uses} directive in a {@code module-info.java}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public final class UsesDescriptor
    extends AbstractTraitable
    implements Trait, Traitable {

    private final TypeUsage serviceType;

    private UsesDescriptor(final CodeModel codeModel, final TypeUsage serviceType) {
        super(codeModel);
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    /**
     * {@link Unmarshal} a {@link UsesDescriptor}.
     */
    @Unmarshal
    public UsesDescriptor(@Bound final CodeModel codeModel,
                          final Marshaller marshaller,
                          final Stream<Marshalled<Trait>> traits,
                          final TypeUsage serviceType) {

        super(codeModel, marshaller, traits);
        this.serviceType = serviceType;
    }

    /**
     * {@link Marshal} a {@link UsesDescriptor}.
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<TypeUsage> serviceType) {

        super.destructor(marshaller, traits);
        serviceType.set(this.serviceType);
    }

    public TypeUsage serviceType() {
        return serviceType;
    }

    @Override
    protected Stream<? extends Composite> compositeChildren() {
        return Stream.of(serviceType);
    }

    public static UsesDescriptor of(final CodeModel codeModel, final TypeUsage serviceType) {
        return new UsesDescriptor(codeModel, serviceType);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof UsesDescriptor other
            && Objects.equals(this.serviceType, other.serviceType)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serviceType, super.hashCode());
    }

    static {
        Marshalling.register(UsesDescriptor.class, MethodHandles.lookup());
    }
}
