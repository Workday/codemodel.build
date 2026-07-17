package build.codemodel.jdk.expression;

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
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A parameter of a lambda expression, with its resolved {@link TypeUsage} and parameter name.
 *
 * <p>For explicitly-annotated parameters the type is always present (resolving to
 * {@link build.codemodel.foundation.usage.UnknownTypeUsage} on failure). For implicitly-typed
 * parameters the type is present when javac's inferred type is reachable, and empty when it
 * is not — distinguishing "no annotation and inference unavailable" from "annotation present
 * but unresolvable".
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public final class LambdaParameter
    extends AbstractTraitable
    implements Traitable {

    /**
     * The {@link Optional} resolved {@link TypeUsage} of the parameter.
     */
    private final Optional<TypeUsage> type;

    /**
     * The parameter name.
     */
    private final String name;

    /**
     * Constructs a {@link LambdaParameter}.
     *
     * @param codeModel the {@link CodeModel}
     * @param type      the {@link Optional} resolved {@link TypeUsage} of the parameter
     * @param name      the parameter name
     */
    public LambdaParameter(final CodeModel codeModel,
                           final Optional<TypeUsage> type,
                           final String name) {

        super(codeModel);

        this.type = type == null ? Optional.empty() : type;
        this.name = Objects.requireNonNull(name, "The name must not be null");
    }

    /**
     * {@link Unmarshal} a {@link LambdaParameter}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param type       the {@link Optional} {@link Marshalled} {@link TypeUsage} of the parameter
     * @param name       the parameter name
     */
    @Unmarshal
    public LambdaParameter(@Bound final CodeModel codeModel,
                           final Marshaller marshaller,
                           final Stream<Marshalled<Trait>> traits,
                           final Optional<Marshalled<TypeUsage>> type,
                           final String name) {

        super(codeModel, marshaller, traits);

        this.type = type == null ? Optional.empty() : type.map(marshaller::unmarshal);
        this.name = name;
    }

    /**
     * {@link Marshal} a {@link LambdaParameter}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param type       the {@link Optional} {@link Marshalled} {@link TypeUsage} of the parameter
     * @param name       the parameter name
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<Marshalled<TypeUsage>>> type,
                           final Out<String> name) {

        super.destructor(marshaller, traits);

        type.set(this.type.map(marshaller::marshal));
        name.set(this.name);
    }

    /**
     * Obtains the {@link Optional} resolved {@link TypeUsage} of the parameter.
     *
     * @return the {@link Optional} {@link TypeUsage}
     */
    public Optional<TypeUsage> type() {
        return this.type;
    }

    /**
     * Obtains the parameter name.
     *
     * @return the parameter name
     */
    public String name() {
        return this.name;
    }

    @Override
    public Stream<? extends Composite> compositeChildren() {
        return this.type.stream();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof LambdaParameter other
            && Objects.equals(this.type, other.type)
            && Objects.equals(this.name, other.name)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.name, super.hashCode());
    }

    static {
        Marshalling.register(LambdaParameter.class, MethodHandles.lookup());
    }
}
