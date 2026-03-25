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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides type information concerning the definition of a formal
 * <a href="https://en.wikipedia.org/wiki/Parameter_(computer_programming)">actual parameter</a> for an <i>operation</i>.
 *
 * @author brian.oliver
 * @see CallableDescriptor
 * @see TypeDescriptor
 * @see TypeUsage
 * @since Jan-2024
 */
public final class FormalParameterDescriptor
    extends AbstractTraitable
    implements Traitable {

    /**
     * The {@link Optional} name of the <i>formal parameter</i>.
     */
    private final Optional<IrreducibleName> name;

    /**
     * The {@link TypeUsage} defining the type of the <i>formal parameter</i>.
     */
    private final TypeUsage type;

    /**
     * Constructs a {@link FormalParameterDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link Optional} {@link IrreducibleName}
     * @param type       the {@link TypeUsage} for the type of formal parameter
     */
    private FormalParameterDescriptor(final CodeModel codeModel,
                                      final Optional<IrreducibleName> name,
                                      final TypeUsage type) {

        super(codeModel);

        this.name = name == null ? Optional.empty() : name;
        this.type = Objects.requireNonNull(type, "The Type TypeUsage must not be null");
    }

    /**
     * {@link Unmarshal} an {@link AbstractTraitable}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link Optional} {@link IrreducibleName}
     * @param type       the {@link Marshalled} {@link TypeUsage} for the type of formal parameter
     */
    @Unmarshal
    public FormalParameterDescriptor(@Bound final CodeModel codeModel,
                                     final Marshaller marshaller,
                                     final Stream<Marshalled<Trait>> traits,
                                     final Optional<IrreducibleName> name,
                                     final Marshalled<TypeUsage> type) {

        super(codeModel, marshaller, traits);

        this.name = name;
        this.type = marshaller.unmarshal(type);
    }

    /**
     * {@link Marshal} an {@link AbstractTraitable}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param name       the {@link Out} {@link Optional} {@link IrreducibleName}
     * @param type       the {@link Out} {@link Marshalled} {@link TypeUsage} for the type of formal parameter
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<IrreducibleName>> name,
                           final Out<Marshalled<TypeUsage>> type) {

        super.destructor(marshaller, traits);

        name.set(this.name);
        type.set(marshaller.marshal(this.type));
    }

    /**
     * Obtains the {@link Optional} name of the parameter.  When information concerning the name of a
     * formal parameter is not available at runtime, {@link Optional#empty()} is returned.
     *
     * @return the {@link Optional} name of the parameter
     */
    public Optional<IrreducibleName> name() {
        return this.name;
    }

    /**
     * Obtains the {@link TypeUsage} specifying the type of the parameter.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage type() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type
            + (this.name.isPresent()
            ? (" " + this.name.map(IrreducibleName::toString).orElse(""))
            : "");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FormalParameterDescriptor other)) {
            return false;
        }
        return Objects.equals(this.name, other.name)
            && Objects.equals(this.type, other.type)
            && super.equals(other);
    }

    /**
     * Creates a {@link FormalParameterDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link Optional} {@link IrreducibleName}
     * @param type       the {@link TypeUsage} for the type of formal parameter
     */
    public static FormalParameterDescriptor of(final CodeModel codeModel,
                                               final Optional<IrreducibleName> name,
                                               final TypeUsage type) {

        return new FormalParameterDescriptor(codeModel, name, type);
    }

    static {
        Marshalling.register(FormalParameterDescriptor.class, MethodHandles.lookup());
    }
}
