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
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Trait} capturing the parameterized nature of a {@link TypeDescriptor}.
 *
 * @author brian.oliver
 * @since May-2024
 */
public final class ParameterizedTypeDescriptor
    extends AbstractTraitable
    implements Trait, Dependent, Traitable {

    /**
     * The <i>type variables</i> defined by the <i>type</i>.
     */
    private final ArrayList<TypeVariableUsage> typeVariables;

    /**
     * Constructs a {@link ParameterizedTypeDescriptor}.
     *
     * @param codeModel    the {@link CodeModel}
     * @param typeVariables the {@link TypeVariableUsage}s
     */
    private ParameterizedTypeDescriptor(final CodeModel codeModel,
                                        final Stream<TypeVariableUsage> typeVariables) {

        super(codeModel);

        this.typeVariables = typeVariables == null
            ? new ArrayList<>()
            : typeVariables.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Unmarshal} a {@link ParameterizedTypeDescriptor}.
     *
     * @param codeModel    the {@link CodeModel}
     * @param marshaller    the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits        the {@link Marshalled} {@link Trait}s
     * @param typeVariables the {@link Marshalled} {@link TypeVariableUsage}s
     */
    @Unmarshal
    public ParameterizedTypeDescriptor(@Bound final CodeModel codeModel,
                                       final Marshaller marshaller,
                                       final Stream<Marshalled<Trait>> traits,
                                       final Stream<Marshalled<TypeVariableUsage>> typeVariables) {

        super(codeModel, marshaller, traits);

        this.typeVariables = typeVariables == null
            ? new ArrayList<>()
            : typeVariables.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} a {@link ParameterizedTypeDescriptor}.
     *
     * @param marshaller    the {@link Marshaller}
     * @param traits        the {@link Marshalled} {@link Trait}s
     * @param typeVariables the {@link Marshalled} {@link TypeVariableUsage}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<TypeVariableUsage>>> typeVariables) {

        super.destructor(marshaller, traits);

        typeVariables.set(this.typeVariables.stream()
            .filter(typeVariable -> marshaller.isMarshallable(typeVariable.getClass()))
            .map(marshaller::marshal));
    }

    /**
     * Obtains the <i>type variables</i> defined by the parameterized type.
     *
     * @return the {@link Stream} of {@link TypeVariableUsage}s
     */
    public Stream<TypeVariableUsage> typeVariables() {
        return this.typeVariables.stream();
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return typeVariables().map(TypeUsage.class::cast);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof ParameterizedTypeDescriptor other
            && Objects.equals(this.typeVariables, other.typeVariables)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.typeVariables, super.hashCode());
    }

    /**
     * Creates a {@link ParameterizedTypeDescriptor}.
     *
     * @param codeModel    the {@link CodeModel}
     * @param typeVariables the {@link TypeVariableUsage}s
     */
    public static ParameterizedTypeDescriptor of(final CodeModel codeModel,
                                                 final Stream<TypeVariableUsage> typeVariables) {

        return new ParameterizedTypeDescriptor(codeModel, typeVariables == null ? Stream.empty() : typeVariables);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(ParameterizedTypeDescriptor.class, MethodHandles.lookup());
    }
}
