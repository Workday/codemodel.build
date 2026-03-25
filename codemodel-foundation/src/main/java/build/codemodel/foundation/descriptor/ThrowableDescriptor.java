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
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Describes a <a href="https://en.wikipedia.org/wiki/Exception_handling_(programming)">Throwable</a> Exception that
 * may be thrown during the execution of a <i>Callable</i>.
 *
 * @author brian.oliver
 * @see CallableDescriptor
 * @since Sep-2024
 */
public class ThrowableDescriptor
    extends AbstractTraitable
    implements Trait, Dependent {

    /**
     * The <i>Throwable</i> {@link TypeUsage}.
     */
    private final TypeUsage throwableType;

    /**
     * Constructs a {@link ThrowableDescriptor}.
     *
     * @param throwableType the {@link TypeUsage} for the <i>Throwable</i> <i>Type</i>
     */
    private ThrowableDescriptor(final TypeUsage throwableType) {
        super(Objects.requireNonNull(throwableType, "The throwable TypeUsage must not be null").codeModel());
        this.throwableType = throwableType;
    }

    /**
     * {@link Unmarshal} an {@link AbstractTraitable}.
     *
     * @param codeModel    the {@link CodeModel}
     * @param marshaller    the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits        the {@link Marshalled} {@link Trait}s
     * @param throwableType the {@link Marshalled} {@link TypeUsage} for the <i>Throwable</i> <i>Type</i>
     */
    @Unmarshal
    public ThrowableDescriptor(@Bound final CodeModel codeModel,
                               final Marshaller marshaller,
                               final Stream<Marshalled<Trait>> traits,
                               final Marshalled<TypeUsage> throwableType) {

        super(codeModel, marshaller, traits);

        this.throwableType = marshaller.unmarshal(throwableType);
    }

    /**
     * {@link Marshal} an {@link AbstractTraitable}.
     *
     * @param marshaller    the {@link Marshaller}
     * @param traits        the {@link Out} {@link Marshalled} {@link Trait}s
     * @param throwableType the {@link Out} {@link Marshalled} {@link TypeUsage} for the <i>Throwable</i> <i>Type</i>
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<TypeUsage>> throwableType) {

        super.destructor(marshaller, traits);

        throwableType.set(marshaller.marshal(this.throwableType));
    }

    /**
     * Obtains the {@link TypeUsage} for the <i>Throwable</i>.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage throwable() {
        return throwableType;
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return Stream.of(throwable());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThrowableDescriptor other)) {
            return false;
        }
        return Objects.equals(throwableType, other.throwableType)
            && super.equals(other);
    }

    /**
     * Creates a {@link ThrowableDescriptor}.
     *
     * @param throwableType the {@link TypeUsage} for the <i>Throwable</i> <i>Type</i>
     * @return a new {@link ThrowableDescriptor}
     */
    public static ThrowableDescriptor of(final TypeUsage throwableType) {
        return new ThrowableDescriptor(throwableType);
    }

    static {
        Marshalling.register(ThrowableDescriptor.class, MethodHandles.lookup());
    }
}
