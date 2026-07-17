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
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.IrreducibleName;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing an enum constant on an enum type descriptor.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class EnumConstantDescriptor
    extends AbstractTraitable
    implements Trait, Traitable {

    /**
     * The {@link IrreducibleName} of the enum constant.
     */
    private final IrreducibleName name;

    /**
     * The zero-based declaration order of this enum constant within its type.
     */
    private final int order;

    private EnumConstantDescriptor(final CodeModel codeModel,
                                   final IrreducibleName name,
                                   final int order) {

        super(codeModel);

        this.name = Objects.requireNonNull(name, "The name must not be null");
        this.order = order;
    }

    /**
     * {@link Unmarshal} an {@link EnumConstantDescriptor}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link IrreducibleName} of the enum constant
     * @param order      the zero-based declaration order
     */
    @Unmarshal
    public EnumConstantDescriptor(@Bound final CodeModel codeModel,
                                  final Marshaller marshaller,
                                  final Stream<Marshalled<Trait>> traits,
                                  final IrreducibleName name,
                                  final int order) {

        super(codeModel, marshaller, traits);

        this.name = name;
        this.order = order;
    }

    /**
     * {@link Marshal} an {@link EnumConstantDescriptor}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link IrreducibleName} of the enum constant
     * @param order      the zero-based declaration order
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<IrreducibleName> name,
                           final Out<Integer> order) {

        super.destructor(marshaller, traits);

        name.set(this.name);
        order.set(this.order);
    }

    /**
     * Obtains the {@link IrreducibleName} of the enum constant.
     *
     * @return the {@link IrreducibleName} of the enum constant
     */
    public IrreducibleName name() {
        return this.name;
    }

    /**
     * Obtains the zero-based declaration order of this enum constant within its type.
     *
     * @return the declaration order
     */
    public int order() {
        return this.order;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof EnumConstantDescriptor other
            && this.order == other.order
            && Objects.equals(this.name, other.name)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.order, super.hashCode());
    }

    /**
     * Creates an {@link EnumConstantDescriptor}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name      the {@link IrreducibleName} of the enum constant
     * @param order     the zero-based declaration order
     * @return a new {@link EnumConstantDescriptor}
     */
    public static EnumConstantDescriptor of(final CodeModel codeModel,
                                            final IrreducibleName name,
                                            final int order) {

        return new EnumConstantDescriptor(codeModel, name, order);
    }

    static {
        Marshalling.register(EnumConstantDescriptor.class, MethodHandles.lookup());
    }
}
