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

import build.base.foundation.Capture;
import build.base.foundation.iterator.Iterators;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An abstract {@link Traitable} whose requests are delegated onto {@link CodeModel} created {@link Traitable}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public abstract class AbstractTraitable
    implements Traitable {

    /**
     * The {@link CodeModel} responsible for creating the {@link Traitable} delegate.
     */
    private final CodeModel codeModel;

    /**
     * The lazily initialized {@link Traitable}.
     */
    private volatile Traitable lazyTraitable;

    /**
     * Constructs an {@link AbstractTraitable} for the specified {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel}
     */
    protected AbstractTraitable(final CodeModel codeModel) {
        this.codeModel = Objects.requireNonNull(codeModel, "The CodeModel must not be null");
        this.lazyTraitable = null;
    }

    /**
     * Constructs an {@link AbstractTraitable} for the specified {@link Traitable}.
     *
     * @param traitable the {@link Traitable}
     */
    protected AbstractTraitable(final Traitable traitable) {
        this(traitable instanceof CodeModel self
            ? self
            : Objects.requireNonNull(traitable, "The Traitable must not be null")
                .codeModel());
    }

    /**
     * {@link Unmarshal} an {@link AbstractTraitable}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractTraitable(final CodeModel codeModel,
                                final Marshaller marshaller,
                                final Stream<Marshalled<Trait>> traits) {

        this(codeModel);

        marshaller.bind(this)
            .withAllInterfaces();

        marshaller.bind(this)
            .withConcreteClass();

        traits.forEach(marshalled -> getDelegate().addTrait(marshaller.unmarshal(marshalled)));
    }

    /**
     * {@link Marshal} an {@link AbstractTraitable}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        if (hasTraits()) {
            traits.set(traits()
                .filter(trait -> marshaller.isMarshallable(trait.getClass()))
                .map(marshaller::marshal));
        }
        else {
            traits.set(Stream.empty());
        }
    }

    @Override
    public <T> Iterator<T> iterator(final Class<T> type) {
        return hasTraits()
            ? getDelegate().iterator(type)
            : Iterators.empty();
    }

    @Override
    public CodeModel codeModel() {
        return this.codeModel;
    }

    /**
     * Obtain the {@link Traitable} onto which to delegate {@link Traitable} requests.
     *
     * @return the {@link Traitable}
     */
    private Traitable getDelegate() {

        Traitable local = this.lazyTraitable;

        if (local == null) {
            synchronized (this) {
                local = this.lazyTraitable;
                if (local == null) {
                    local = codeModel().createTraitable(this);
                    this.lazyTraitable = local;
                }
            }
        }

        return local;
    }

    @Override
    public <C extends Traitable, T extends Trait> T createTrait(final Function<C, T> traitSupplier) {
        return getDelegate().createTrait(traitSupplier);
    }

    @Override
    public <T extends Trait> boolean removeTrait(final T trait) {
        return getDelegate().removeTrait(trait);
    }

    @Override
    public <C extends Traitable, T extends Trait> Optional<T> computeIfAbsent(final Class<T> traitClass,
                                                                              final Function<C, T> function) {

        return getDelegate().computeIfAbsent(traitClass, function);
    }

    @Override
    public <C extends Traitable, T extends Trait> Optional<T> computeIfPresent(final Class<T> traitClass,
                                                                               final BiFunction<C, T, T> biFunction) {

        return getDelegate().computeIfPresent(traitClass, biFunction);
    }

    @Override
    public <T> Optional<T> getTrait(final Class<T> requiredClass)
        throws IllegalStateException {

        return hasTraits()
            ? getDelegate().getTrait(requiredClass)
            : Optional.empty();
    }

    @Override
    public <T> T trait(final Class<T> requiredClass)
        throws IllegalStateException {

        return hasTraits()
            ? getDelegate().trait(requiredClass)
            : null;
    }

    @Override
    public <T> Stream<T> traits(final Class<T> requiredClass) {
        return requiredClass != null && hasTraits()
            ? getDelegate().traits(requiredClass)
            : Stream.empty();
    }

    @Override
    public Stream<Object> traits(final Class<?>... classes) {
        if (classes == null || classes.length == 0 || !hasTraits()) {
            return Stream.empty();
        }
        return Stream.of(classes)
            .flatMap(this::traits);
    }

    @Override
    public boolean hasTraits() {
        return this.lazyTraitable != null && this.lazyTraitable.hasTraits();
    }

    @Override
    public boolean hasTrait(final Class<?> requiredClass) {
        return hasTraits() && getDelegate().hasTrait(requiredClass);
    }

    @Override
    public Stream<Trait> traits() {
        return hasTraits()
            ? getDelegate().traits()
            : Stream.empty();
    }

    @Override
    public String toString() {
        return hasTraits()
            ? getDelegate().toString()
            : "";
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof AbstractTraitable other
            && Objects.equals(this.lazyTraitable, other.lazyTraitable);
    }
}
