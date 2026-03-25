package build.codemodel.expression;

/*-
 * #%L
 * Expression Code Model
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
import build.codemodel.expression.naming.FunctionName;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.CallableDescriptor;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides type information concerning the definition of <i>Function</i>.
 *
 * @author brian.oliver
 * @see CallableDescriptor
 * @see TypeDescriptor
 * @since Jan-2024
 */
public final class FunctionDescriptor
    extends AbstractTraitable
    implements CallableDescriptor {

    /**
     * The {@link TypeDescriptor} in which the {@link FunctionDescriptor} is defined.
     */
    private final TypeDescriptor typeDescriptor;

    /**
     * The {@link FunctionName}.
     */
    private final FunctionName functionName;

    /**
     * The {@link TypeUsage} defining the return type of the <i>method</i>.
     */
    private final TypeUsage returnType;

    /**
     * The {@link FormalParameterDescriptor} for the formal parameters.
     */
    private final ArrayList<FormalParameterDescriptor> formalParameters;

    /**
     * Constructs a {@link FunctionDescriptor}.
     *
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param functionName     the <i>Function</i> name
     * @param returnType       the {@link TypeUsage} of the return type
     * @param formalParameters the {@link FormalParameterDescriptor}s for the formal parameters
     */
    private FunctionDescriptor(final TypeDescriptor typeDescriptor,
                               final FunctionName functionName,
                               final TypeUsage returnType,
                               final Stream<FormalParameterDescriptor> formalParameters) {

        super(Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null").codeModel());

        this.typeDescriptor = typeDescriptor;
        this.functionName = Objects.requireNonNull(functionName, "The MethodName must not be null");
        this.returnType = Objects.requireNonNull(returnType, "The TypeUsage for the return type must not be null");
        this.formalParameters = formalParameters == null
            ? new ArrayList<>()
            : formalParameters.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Un{@link Marshal} a {@link FunctionDescriptor}.
     *
     * @param codeModel       the {@link CodeModel}
     * @param marshaller       the {@link Marshaller}
     * @param traits           the {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param typeDescriptor   the {@link Marshalled} {@link TypeDescriptor}
     * @param functionName     the {@link FunctionName}
     * @param returnType       the {@link Marshalled} {@link TypeUsage} of the return type
     * @param formalParameters the {@link Stream} of {@link Marshalled} {@link FormalParameterDescriptor}s
     */
    @Unmarshal
    public FunctionDescriptor(@Bound final CodeModel codeModel,
                              final Marshaller marshaller,
                              final Stream<Marshalled<Trait>> traits,
                              final Marshalled<TypeDescriptor> typeDescriptor,
                              final FunctionName functionName,
                              final Marshalled<TypeUsage> returnType,
                              final Stream<Marshalled<FormalParameterDescriptor>> formalParameters) {

        super(codeModel, marshaller, traits);

        this.typeDescriptor = marshaller.unmarshal(typeDescriptor);
        this.functionName = functionName;
        this.returnType = marshaller.unmarshal(returnType);
        this.formalParameters = formalParameters
            .map(marshaller::unmarshal)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} a {@link FunctionDescriptor}.
     *
     * @param marshaller       the {@link Marshaller}
     * @param traits           the {@link Out} {@link Stream} of {@link Marshalled} {@link Trait}s
     * @param typeDescriptor   the {@link Out} {@link Marshalled} {@link TypeDescriptor}
     * @param functionName     the {@link Out} {@link FunctionName}
     * @param returnType       the {@link Out} {@link TypeUsage} of the return type
     * @param formalParameters the {@link Out} {@link Stream} of {@link Marshalled} {@link FormalParameterDescriptor}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<TypeDescriptor>> typeDescriptor,
                           final Out<FunctionName> functionName,
                           final Out<Marshalled<TypeUsage>> returnType,
                           final Out<Stream<Marshalled<FormalParameterDescriptor>>> formalParameters) {

        super.destructor(marshaller, traits);
        typeDescriptor.set(marshaller.marshal(this.typeDescriptor));
        functionName.set(this.functionName);
        returnType.set(marshaller.marshal(this.returnType));
        formalParameters.set(this.formalParameters.stream()
            .map(marshaller::marshal));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof FunctionDescriptor other
            && this.typeDescriptor.equals(other.typeDescriptor)
            && this.functionName.equals(other.functionName)
            && this.returnType.equals(other.returnType)
            && this.formalParameters.equals(other.formalParameters)
            && super.equals(other);
    }

    /**
     * The {@link FunctionName} for the <i>Function</i>, a synonym for {@link #callableName()}.
     *
     * @return the {@link FunctionName}
     */
    public FunctionName functionName() {
        return this.functionName;
    }

    @Override
    public TypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public FunctionName callableName() {
        return functionName();
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

    /**
     * Creates a {@link FunctionDescriptor}.
     *
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param functionName     the <i>Function</i> name
     * @param returnType       the {@link TypeUsage} of the return type
     * @param formalParameters the {@link FormalParameterDescriptor}s for the formal parameters
     */
    public static FunctionDescriptor of(final TypeDescriptor typeDescriptor,
                                        final FunctionName functionName,
                                        final TypeUsage returnType,
                                        final Stream<FormalParameterDescriptor> formalParameters) {

        return new FunctionDescriptor(typeDescriptor, functionName, returnType, formalParameters);
    }

    static {
        Marshalling.register(FunctionDescriptor.class, MethodHandles.lookup());
    }
}
