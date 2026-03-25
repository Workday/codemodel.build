package build.codemodel.foundation.usage;

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

import build.base.foundation.Lazy;
import build.base.foundation.stream.Streams;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Provides type information concerning the <i>usage</i> of a specific type in an array.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public class ArrayTypeUsage
    extends AbstractTypeUsage {

    /**
     * The {@link Lazy} {@link TypeUsage} for the component type of the array.
     */
    private final Lazy<TypeUsage> component;

    /**
     * Constructs an {@link ArrayTypeUsage} for the specified component {@link TypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param component  the {@link TypeUsage}
     */
    private ArrayTypeUsage(final CodeModel codeModel,
                           final Lazy<TypeUsage> component) {
        super(codeModel);
        this.component = Objects.requireNonNull(component, "The Component TypeUsage must not be null");
    }

    /**
     * {@link Unmarshal} an {@link ArrayTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param component  the {@link Marshalled} {@link TypeUsage} for the array component type
     */
    @Unmarshal
    public ArrayTypeUsage(@Bound final CodeModel codeModel,
                          final Marshaller marshaller,
                          final Stream<Marshalled<Trait>> traits,
                          final Marshalled<TypeUsage> component) {

        super(codeModel, marshaller, traits);

        this.component = Lazy.of(marshaller.unmarshal(component));
    }

    /**
     * {@link Marshal} an {@link ArrayTypeUsage}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param component  the {@link Marshalled} {@link TypeUsage} for the array component type
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<TypeUsage>> component) {

        super.destructor(marshaller, traits);

        component.set(marshaller.marshal(this.component.get()));
    }

    /**
     * Obtains the {@link TypeUsage} for the array component type.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage type() {
        return this.component.get();
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return Streams.concat(
            Stream.of(type()),
            traits(Dependent.class)
                .flatMap(Dependent::dependencies));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ArrayTypeUsage other
            && Objects.equals(type(), other.type())
            && super.equals(object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.component);
    }

    @Override
    public String toString() {
        return this.component.get() + "[]" + Traitable.toString(this);
    }

    /**
     * Creates an {@link ArrayTypeUsage} for the specified component {@link TypeUsage}.
     *
     * @param component the {@link Lazy} {@link TypeUsage}
     * @return an {@link ArrayTypeUsage}
     */
    public static ArrayTypeUsage of(final CodeModel codeModel,
                                    final Lazy<TypeUsage> component) {

        return new ArrayTypeUsage(codeModel, component);
    }

    static {
        Marshalling.register(ArrayTypeUsage.class, MethodHandles.lookup());
    }
}
