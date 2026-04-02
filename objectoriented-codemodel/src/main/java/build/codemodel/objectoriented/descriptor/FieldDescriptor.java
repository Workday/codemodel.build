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
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Provides immutable information concerning the definition of a <i>field</i> of a <i>type</i>.
 *
 * @author brian.oliver
 * @see TypeDescriptor
 * @since Mar-2024
 */
public final class FieldDescriptor
    extends AbstractTraitable
    implements Trait, Dependent, Traitable {

    /**
     * The {@link IrreducibleName}.
     */
    private final IrreducibleName name;

    /**
     * The {@link TypeUsage} defining the type of the <i>field</i>.
     */
    private final TypeUsage type;

    /**
     * Constructs a {@link FieldDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link IrreducibleName}
     * @param type       the {@link TypeUsage} defining the field type
     */
    private FieldDescriptor(final CodeModel codeModel,
                            final IrreducibleName name,
                            final TypeUsage type) {

        super(codeModel);

        this.name = Objects.requireNonNull(name, "The IrreducibleName must not be null");
        this.type = Objects.requireNonNull(type, "The Type TypeUsage must not be null");
    }

    /**
     * {@link Unmarshal} a {@link FieldDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link IrreducibleName}
     * @param type       the {@link Marshalled} {@link TypeUsage} defining the field type
     */
    @Unmarshal
    public FieldDescriptor(@Bound final CodeModel codeModel,
                           final Marshaller marshaller,
                           final Stream<Marshalled<Trait>> traits,
                           final IrreducibleName name,
                           final Marshalled<TypeUsage> type) {

        super(codeModel, marshaller, traits);

        this.name = name;
        this.type = marshaller.unmarshal(type);
    }

    /**
     * {@link Marshal} a {@link FieldDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link IrreducibleName}
     * @param type       the {@link Marshalled} {@link TypeUsage} defining the field type
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<IrreducibleName> name,
                           final Out<Marshalled<TypeUsage>> type) {

        super.destructor(marshaller, traits);

        name.set(this.name);
        type.set(marshaller.marshal(this.type));
    }

    /**
     * Obtains the {@link IrreducibleName} for the <i>field</i>.
     *
     * @return the {@link IrreducibleName}
     */
    public IrreducibleName fieldName() {
        return this.name;
    }

    /**
     * Obtains the <i>type</i> {@link TypeUsage} of the <i>field</i>.
     *
     * @return the return {@link TypeUsage}
     */
    public TypeUsage type() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type + " " + fieldName();
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
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof FieldDescriptor other
            && Objects.equals(this.name, other.name)
            && Objects.equals(this.type, other.type)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.type, super.hashCode());
    }

    /**
     * Creates a {@link FieldDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link IrreducibleName}
     * @param type       the {@link TypeUsage} defining the field type
     */
    public static FieldDescriptor of(final CodeModel codeModel,
                                     final IrreducibleName name,
                                     final TypeUsage type) {

        return new FieldDescriptor(codeModel, name, type);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(FieldDescriptor.class, MethodHandles.lookup());
    }
}
