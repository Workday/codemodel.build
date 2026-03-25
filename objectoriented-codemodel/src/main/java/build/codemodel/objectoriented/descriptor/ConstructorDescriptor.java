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
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.CallableDescriptor;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.objectoriented.naming.MethodName;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides type information concerning the definition of a
 * <a href="https://en.wikipedia.org/wiki/Constructor_(object-oriented_programming)">Constructor</a> for
 * a {@link TypeDescriptor}.
 *
 * @author brian.oliver
 * @see CallableDescriptor
 * @see TypeDescriptor
 * @see MethodDescriptor
 * @since Oct-2024
 */
public final class ConstructorDescriptor
    extends AbstractTraitable
    implements CallableDescriptor {

    /**
     * The {@link TypeDescriptor} defining the <i>Constructor</i>.
     */
    private final TypeDescriptor typeDescriptor;

    /**
     * The {@link MethodName} for the <i>Constructor</i> (based on the {@link TypeDescriptor#typeName()}.
     */
    private final MethodName methodName;

    /**
     * The {@link TypeUsage} defining the return type of the <i>Constructor</i>.
     */
    private final TypeUsage returnType;

    /**
     * The {@link FormalParameterDescriptor} for the formal parameters.
     */
    private final ArrayList<FormalParameterDescriptor> formalParameters;

    /**
     * Constructs a {@link ConstructorDescriptor}.
     *
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param formalParameters the {@link FormalParameterDescriptor}s for the formal parameters
     */
    private ConstructorDescriptor(final TypeDescriptor typeDescriptor,
                                  final Stream<FormalParameterDescriptor> formalParameters) {

        super(Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null").codeModel());

        this.typeDescriptor = typeDescriptor;

        final var typeName = typeDescriptor.typeName();

        this.methodName = MethodName
            .of(typeName.moduleName(), typeName.namespace(), Optional.of(typeName), typeName.name());

        this.returnType = SpecificTypeUsage.of(this.typeDescriptor.codeModel(), typeName);

        this.formalParameters = formalParameters == null
            ? new ArrayList<>()
            : formalParameters.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Unmarshal} a {@link ConstructorDescriptor}.
     *
     * @param codeModel       the {@link CodeModel}
     * @param marshaller       the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits           the {@link Marshalled} {@link Trait}s
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param methodName       the {@link MethodName}
     * @param returnType       the {@link Marshalled} {@link TypeUsage} defining the return type
     * @param formalParameters the {@link Marshalled} {@link FormalParameterDescriptor}s for the formal parameters
     */
    @Unmarshal
    public ConstructorDescriptor(@Bound final CodeModel codeModel,
                                 final Marshaller marshaller,
                                 final Stream<Marshalled<Trait>> traits,
                                 final Marshalled<TypeDescriptor> typeDescriptor,
                                 final MethodName methodName,
                                 final Marshalled<TypeUsage> returnType,
                                 final Stream<Marshalled<FormalParameterDescriptor>> formalParameters) {

        super(codeModel, marshaller, traits);

        this.typeDescriptor = marshaller.unmarshal(typeDescriptor);
        this.methodName = methodName;
        this.returnType = marshaller.unmarshal(returnType);
        this.formalParameters = formalParameters == null
            ? new ArrayList<>()
            : formalParameters.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} a {@link ConstructorDescriptor}.
     *
     * @param marshaller       the {@link Marshaller}
     * @param traits           the {@link Marshalled} {@link Trait}s
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param methodName       the {@link MethodName}
     * @param returnType       the {@link Marshalled} {@link TypeUsage} defining the return type
     * @param formalParameters the {@link Marshalled} {@link FormalParameterDescriptor}s for the formal parameters
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<TypeDescriptor>> typeDescriptor,
                           final Out<MethodName> methodName,
                           final Out<Marshalled<TypeUsage>> returnType,
                           final Out<Stream<Marshalled<FormalParameterDescriptor>>> formalParameters) {

        super.destructor(marshaller, traits);

        typeDescriptor.set(marshaller.marshal(this.typeDescriptor));
        methodName.set(this.methodName);
        returnType.set(marshaller.marshal(this.returnType));
        formalParameters.set(this.formalParameters.stream()
            .filter(formalParameter -> marshaller.isMarshallable(formalParameter.getClass()))
            .map(marshaller::marshal));
    }

    @Override
    public TypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public MethodName callableName() {
        return this.methodName;
    }

    @Override
    public TypeUsage returnType() {
        return this.returnType;
    }

    @Override
    public int getFormalParameterCount() {
        return this.formalParameters.size();
    }

    @Override
    public FormalParameterDescriptor getFormalParameter(final int index)
        throws IndexOutOfBoundsException {

        return this.formalParameters.get(index);
    }

    @Override
    public Stream<FormalParameterDescriptor> formalParameters() {
        return this.formalParameters.stream();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof ConstructorDescriptor other
            && Objects.equals(this.typeDescriptor, other.typeDescriptor)
            && Objects.equals(this.methodName, other.methodName)
            && Objects.equals(this.returnType, other.returnType)
            && Objects.equals(this.formalParameters, other.formalParameters)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.typeDescriptor, this.methodName, this.returnType,
            this.formalParameters, super.hashCode());
    }

    /**
     * Creates a {@link ConstructorDescriptor}.
     *
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param formalParameters the {@link FormalParameterDescriptor}s for the formal parameters
     */
    public static ConstructorDescriptor of(final TypeDescriptor typeDescriptor,
                                           final Stream<FormalParameterDescriptor> formalParameters) {

        return new ConstructorDescriptor(typeDescriptor, formalParameters);
    }

    /**
     * Creates a {@link ConstructorDescriptor} (with no parameters).
     *
     * @param typeDescriptor the {@link TypeDescriptor}
     */
    public static ConstructorDescriptor of(final TypeDescriptor typeDescriptor) {

        return new ConstructorDescriptor(typeDescriptor, Stream.empty());
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(ConstructorDescriptor.class, MethodHandles.lookup());
    }
}
