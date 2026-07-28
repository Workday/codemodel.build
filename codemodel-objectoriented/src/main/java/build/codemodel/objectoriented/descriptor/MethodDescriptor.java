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

import build.base.foundation.stream.Streams;
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
import build.codemodel.foundation.descriptor.CallableDescriptor;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.objectoriented.naming.MethodName;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides type information concerning the definition of a <i>method</i> <i>operation</i> for a <i>type</i>.
 *
 * @author brian.oliver
 * @see CallableDescriptor
 * @see TypeDescriptor
 * @since Jan-2024
 */
@NonSingular
public final class MethodDescriptor
    extends AbstractTraitable
    implements CallableDescriptor {

    /**
     * The {@link TypeDescriptor} in which the {@link MethodDescriptor} is defined.
     */
    private final TypeDescriptor typeDescriptor;

    /**
     * The {@link MethodName}.
     */
    private final MethodName methodName;

    /**
     * The {@link TypeUsage} defining the return type of the <i>method</i>.
     */
    private final TypeUsage returnType;

    /**
     * The {@link FormalParameterDescriptor} for the formal parameters.
     */
    private final ArrayList<FormalParameterDescriptor> formalParameters;

    /**
     * Constructs a {@link MethodDescriptor}.
     *
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param methodName       the <i>method</i> name
     * @param returnType       the {@link TypeUsage} of the return type
     * @param formalParameters the {@link FormalParameterDescriptor}s for the formal parameters
     */
    private MethodDescriptor(final TypeDescriptor typeDescriptor,
                             final MethodName methodName,
                             final TypeUsage returnType,
                             final Stream<FormalParameterDescriptor> formalParameters) {

        super(Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null").codeModel());

        this.typeDescriptor = typeDescriptor;
        this.methodName = Objects.requireNonNull(methodName, "The MethodName must not be null");
        this.returnType = Objects.requireNonNull(returnType, "The TypeUsage for the return type must not be null");
        this.formalParameters = formalParameters == null
            ? new ArrayList<>()
            : formalParameters.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Unmarshal} a {@link MethodDescriptor}.
     *
     * @param codeModel        the {@link CodeModel}
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param marshaller       the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param methodName       the {@link MethodName}
     * @param returnType       the {@link Marshalled} {@link TypeUsage} of the return type
     * @param formalParameters the {@link Marshalled} {@link FormalParameterDescriptor}s for the formal parameters
     * @param traits           the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public MethodDescriptor(@Bound final CodeModel codeModel,
                            @Bound final TypeDescriptor typeDescriptor,
                            final Marshaller marshaller,
                            final MethodName methodName,
                            final Marshalled<TypeUsage> returnType,
                            final Stream<Marshalled<FormalParameterDescriptor>> formalParameters,
                            final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);

        this.typeDescriptor = typeDescriptor;
        this.methodName = methodName;
        this.returnType = marshaller.unmarshal(returnType);
        this.formalParameters = formalParameters == null
            ? new ArrayList<>()
            : formalParameters
            .map(marshaller::unmarshal)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} a {@link MethodDescriptor}.
     *
     * @param marshaller       the {@link Marshaller}
     * @param traits           the {@link Marshalled} {@link Trait}s
     * @param methodName       the {@link MethodName}
     * @param returnType       the {@link Marshalled} {@link TypeUsage} of the return type
     * @param formalParameters the {@link Marshalled} {@link FormalParameterDescriptor}s for the formal parameters
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<MethodName> methodName,
                           final Out<Marshalled<TypeUsage>> returnType,
                           final Out<Stream<Marshalled<FormalParameterDescriptor>>> formalParameters) {

        super.destructor(marshaller, traits);

        methodName.set(this.methodName);
        returnType.set(marshaller.marshal(this.returnType));
        formalParameters.set(this.formalParameters.stream()
            .filter(formalParameter -> marshaller.isMarshallable(formalParameter.getClass()))
            .map(marshaller::marshal));
    }

    /**
     * The {@link MethodName} for the <i>Method</i>, a synonym for {@link #callableName()}.
     *
     * @return the {@link MethodName}
     */
    public MethodName methodName() {
        return this.methodName;
    }

    @Override
    public TypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public MethodName callableName() {
        return methodName();
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
     * Obtains a human-readable Java-like signature of the {@link MethodDescriptor}, e.g.
     * {@code "void doWork(java.lang.String)"}.
     *
     * <p>This is for display purposes only; it is not guaranteed to be unique within the {@link CodeModel}
     * (private methods of the same name and parameter types in different declaring classes render identically).
     * For a key that uniquely identifies the method for override-detection purposes, see {@link #overrideKey()}.
     *
     * @return a human-readable signature
     */
    public String signature() {
        return renderTypeName(returnType()) + " " + methodName().name() + "("
            + formalParameters()
            .map(p -> renderTypeName(p.type()))
            .collect(Collectors.joining(", "))
            + ")";
    }

    /**
     * Obtains a key that uniquely identifies the {@link MethodDescriptor} with in the {@link CodeModel}, for use
     * by consumers (such as {@code InjectionFramework}) that need to detect when one method overrides another
     * while walking a type hierarchy. This is <strong>not</strong> a human-readable signature — for that, see
     * {@link #signature()}. The declaring type's namespace is folded in so that {@link AccessModifier#PRIVATE}
     * methods (which are not polymorphic) never collide across declaring classes.
     *
     * @return a key uniquely identifying the {@link MethodDescriptor} with in the {@link CodeModel}
     */
    public String overrideKey() {

        final StringBuilder builder = new StringBuilder();

        final var accessModifier = getTrait(AccessModifier.class)
            .orElse(null);

        // include the namespace when the method no defined AccessModifier
        if (accessModifier == null) {
            methodName().namespace()
                .ifPresent(namespace -> builder.append(namespace).append(" "));
        }

        // include the declaring class name when the method is private
        if (accessModifier == AccessModifier.PRIVATE) {
            methodName().namespace()
                .ifPresent(namespace -> builder.append(namespace).append("."));
        }

        // include the return type
        builder.append(renderTypeName(returnType()));
        builder.append(' ');

        // include the method name
        builder.append(methodName().name());

        // include the method formal parameter types
        builder.append('(');
        builder.append(formalParameters()
            .map(p -> renderTypeName(p.type()))
            .collect(Collectors.joining(", ")));
        builder.append(')');

        return builder.toString();
    }

    /**
     * The canonical names JDK primitive {@link TypeUsage}s are (mis)represented with internally, since
     * primitives are modeled with a synthetic {@code java.lang} namespace despite not actually residing there.
     */
    private static final Set<String> PRIMITIVE_CANONICAL_NAMES = Set.of(
        "java.lang.void", "java.lang.boolean", "java.lang.byte", "java.lang.short",
        "java.lang.int", "java.lang.long", "java.lang.char", "java.lang.float", "java.lang.double");

    /**
     * Renders a {@link TypeUsage}'s name for inclusion in a {@link #signature()}, stripping the synthetic
     * {@code java.lang} namespace primitives are internally represented with.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the rendered type name
     */
    private static String renderTypeName(final TypeUsage typeUsage) {
        final var canonical = typeUsage.canonicalName();
        return PRIMITIVE_CANONICAL_NAMES.contains(canonical)
            ? canonical.substring("java.lang.".length())
            : canonical;
    }

    @Override
    public Stream<? extends Composite> compositeChildren() {
        return Streams.concat(Stream.of(returnType()), formalParameters(), throwables());
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof MethodDescriptor other
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

    @Override
    public String toString() {
        return signature();
    }

    /**
     * Creates a {@link MethodDescriptor}.
     *
     * @param typeDescriptor   the {@link TypeDescriptor}
     * @param methodName       the <i>method</i> name
     * @param returnType       the {@link TypeUsage} of the return type
     * @param formalParameters the {@link FormalParameterDescriptor}s for the formal parameters
     */
    public static MethodDescriptor of(final TypeDescriptor typeDescriptor,
                                      final MethodName methodName,
                                      final TypeUsage returnType,
                                      final Stream<FormalParameterDescriptor> formalParameters) {

        return new MethodDescriptor(typeDescriptor, methodName, returnType, formalParameters);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(MethodDescriptor.class, MethodHandles.lookup());
    }
}
