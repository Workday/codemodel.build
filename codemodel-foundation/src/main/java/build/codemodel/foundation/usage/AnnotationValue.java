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

import build.base.foundation.Capture;
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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The value of an <i>Annotation</i> as part of an {@link AnnotationTypeUsage}.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public class AnnotationValue
    extends AbstractTraitable
    implements Traitable {

    /**
     * The {@link IrreducibleName} of the {@link AnnotationValue}.
     */
    private final IrreducibleName name;

    /**
     * The polymorphic {@link Object} value of the {@link AnnotationValue}.
     */
    private final Object value;

    /**
     * Constructs an {@link AnnotationValue}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link IrreducibleName}
     * @param value      the polymorphic {@link Object} value
     */
    private AnnotationValue(final CodeModel codeModel,
                            final IrreducibleName name,
                            final Object value) {

        super(codeModel);

        this.name = Objects.requireNonNull(name, "The AnnotationValue name must not be null");
        this.value = Objects.requireNonNull(value, "The AnnotationValue value must not be null");
    }

    /**
     * {@link Unmarshal} an {@link AnnotationValue}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link IrreducibleName}
     * @param value      the {@link Object} value
     */
    @Unmarshal
    public AnnotationValue(@Bound final CodeModel codeModel,
                           final Marshaller marshaller,
                           final Stream<Marshalled<Trait>> traits,
                           final IrreducibleName name,
                           final Object value) {
        super(codeModel, marshaller, traits);

        this.name = name;
        this.value = value;
    }

    /**
     * {@link Marshal} an {@link AnnotationValue}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     * @param name       the {@link Capture}d {@link IrreducibleName}
     * @param value      the {@link Capture}d {@link Object} value
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<IrreducibleName> name,
                           final Out<Object> value) {

        super.destructor(marshaller, traits);

        name.set(this.name);
        value.set(this.value);
    }

    /**
     * The {@link IrreducibleName} for the value
     *
     * @return the {@link IrreducibleName}
     */
    public IrreducibleName name() {
        return this.name;
    }

    /**
     * The value.
     *
     * @param <T> the type of the value
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T value() {
        return (T) this.value;
    }

    /**
     * Attempts to obtain the value as the specified {@link Class}.
     *
     * @param <T> the type of the value
     * @return the {@link Optional} value, or {@link Optional#empty()} if not an instance of the specified {@link Class}
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> as(final Class<T> requiredClass) {

        return requiredClass != null && requiredClass.isInstance(this.value)
            ? Optional.of((T) this.value)
            : Optional.empty();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof AnnotationValue that
            && Objects.equals(this.name, that.name())
            && Objects.equals(this.value, that.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    /**
     * Creates a {@link AnnotationValue}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link IrreducibleName}
     * @param value      the polymorphic {@link Object} value
     * @return a new {@link AnnotationValue}
     */
    public static AnnotationValue of(final CodeModel codeModel,
                                     final IrreducibleName name,
                                     final Object value) {

        return new AnnotationValue(codeModel, name, value);
    }

    /**
     * Creates a {@link AnnotationValue}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link String}
     * @param value      the polymorphic {@link Object} value
     * @return a new {@link AnnotationValue}
     */
    public static AnnotationValue of(final CodeModel codeModel,
                                     final String name,
                                     final Object value) {

        Objects.requireNonNull(codeModel, "The CodeModel must not be null");

        final var valueName = codeModel.getNameProvider().getIrreducibleName(name);
        return new AnnotationValue(codeModel, valueName, value);
    }

    static {
        Marshalling.register(AnnotationValue.class, MethodHandles.lookup());
    }
}
