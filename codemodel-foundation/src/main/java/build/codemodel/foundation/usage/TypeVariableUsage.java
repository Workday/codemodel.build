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
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides type information concerning the <i>usage</i> of a generic <i>type variable</i>, for example the
 * <i>generic type</i> for an attribute, the <i>generic type</i> returned by a method or the <i>generic type</i> for a
 * parameter.
 *
 * @author brian.oliver
 * @see TypeDescriptor
 * @since Feb-2024
 */

public class TypeVariableUsage
    extends AbstractNamedTypeUsage {

    /**
     * The {@link Lazy} {@link TypeUsage} of the <i>lower-bound</i>.
     */
    private final Optional<Lazy<TypeUsage>> lowerBound;

    /**
     * The {@link Lazy} {@link TypeUsage} of the <i>upper-bound</i>.
     */
    private final Optional<Lazy<TypeUsage>> upperBound;

    /**
     * Constructs a {@link TypeVariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @param lowerBound the {@link Optional} {@link Lazy} {@link TypeUsage} of the <i>lower-bound</i>
     * @param upperBound the {@link Optional} {@link Lazy} {@link TypeUsage} of the <i>upper-bound</i>
     */
    protected TypeVariableUsage(final CodeModel codeModel,
                                final TypeName typeName,
                                final Optional<Lazy<TypeUsage>> lowerBound,
                                final Optional<Lazy<TypeUsage>> upperBound) {

        super(codeModel, typeName);
        this.lowerBound = Objects.requireNonNull(lowerBound, "The lower-bound TypeUsage must not be null");
        this.upperBound = Objects.requireNonNull(upperBound, "The upper-bound TypeUsage must not be null");
    }

    /**
     * {@link Unmarshal} a {@link TypeVariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param typeName   the {@link TypeName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param lowerBound the {@link Optional} {@link TypeUsage} of the <i>lower-bound</i>
     * @param upperBound the {@link Optional} {@link TypeUsage} of the <i>upper-bound</i>
     */
    @Unmarshal
    public TypeVariableUsage(@Bound final CodeModel codeModel,
                             final Marshaller marshaller,
                             final TypeName typeName,
                             final Stream<Marshalled<Trait>> traits,
                             final Optional<TypeUsage> lowerBound,
                             final Optional<TypeUsage> upperBound) {

        super(codeModel, marshaller, typeName, traits);

        this.lowerBound = lowerBound.map(Lazy::of);
        this.upperBound = upperBound.map(Lazy::of);
    }

    /**
     * {@link Marshal} an {@link TypeVariableUsage}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link TypeName}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param lowerBound the {@link Optional} {@link TypeUsage} of the <i>lower-bound</i>
     * @param upperBound the {@link Optional} {@link TypeUsage} of the <i>upper-bound</i>
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<TypeName> typeName,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<TypeUsage>> lowerBound,
                           final Out<Optional<TypeUsage>> upperBound) {

        super.destructor(marshaller, typeName, traits);

        lowerBound.set(this.lowerBound.map(Lazy::get));
        upperBound.set(this.upperBound.map(Lazy::get));
    }

    /**
     * Obtains the {@link Optional} <i>lower-bound</i> of the type.
     *
     * @return the {@link Optional} {@link TypeUsage} representing the <i>lower-bound</i>
     */
    public Optional<TypeUsage> lowerBound() {
        return this.lowerBound.map(Lazy::get);
    }

    /**
     * Obtains the {@link Optional} <i>upper-bound</i> of the type.
     *
     * @return the {@link Optional} {@link TypeUsage} representing the <i>upper-bound</i>
     */
    public Optional<TypeUsage> upperBound() {
        return this.upperBound.map(Lazy::get);
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return Streams.concat(
            lowerBound().stream(),
            upperBound().stream(),
            traits(Dependent.class)
                .flatMap(Dependent::dependencies));
    }

    @Override
    public int hashCode() {
        return super.typeName().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof TypeVariableUsage other
            && Objects.equals(lowerBound(), other.lowerBound())
            && Objects.equals(upperBound(), other.upperBound())
            && super.equals(other);
    }

    @Override
    public String toString() {
        return typeName()
            + lowerBound().map(lowerBound -> " extends " + lowerBound).orElse("")
            + upperBound().map(upperBound -> " super " + upperBound).orElse("")
            + Traitable.toString(this);
    }

    /**
     * Creates a {@link TypeVariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @param lowerBound the {@link Optional} {@link Lazy} {@link TypeUsage} of the <i>lower-bound</i>
     * @param upperBound the {@link Optional} {@link Lazy} {@link TypeUsage} of the <i>upper-bound</i>
     * @return a {@link TypeVariableUsage}
     */
    public static TypeVariableUsage of(final CodeModel codeModel,
                                       final TypeName typeName,
                                       final Optional<Lazy<TypeUsage>> lowerBound,
                                       final Optional<Lazy<TypeUsage>> upperBound) {

        return new TypeVariableUsage(codeModel, typeName, lowerBound, upperBound);
    }

    static {
        Marshalling.register(TypeVariableUsage.class, MethodHandles.lookup());
    }
}
