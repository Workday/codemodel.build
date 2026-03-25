package build.codemodel.jdk.statement;

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
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.imperative.AbstractStatement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@code continue} statement: {@code continue [label]}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Continue
    extends AbstractStatement {

    /**
     * The optional target label.
     */
    private final Optional<String> label;

    private Continue(final CodeModel codeModel, final Optional<String> label) {
        super(codeModel);
        this.label = label == null ? Optional.empty() : label;
    }

    @Unmarshal
    public Continue(@Bound final CodeModel codeModel,
                    final Marshaller marshaller,
                    final Stream<Marshalled<Trait>> traits,
                    final Optional<String> label) {
        super(codeModel, marshaller, traits);
        this.label = label == null ? Optional.empty() : label;
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Optional<String>> label) {
        super.destructor(marshaller, traits);
        label.set(this.label);
    }

    /**
     * Obtains the optional target label.
     *
     * @return an {@link Optional} label name
     */
    public Optional<String> label() {
        return this.label;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Continue other
            && Objects.equals(this.label, other.label)
            && super.equals(other);
    }

    /**
     * Creates a {@link Continue} statement.
     *
     * @param codeModel the {@link CodeModel}
     * @param label      the optional target label name
     * @return a new {@link Continue}
     */
    public static Continue of(final CodeModel codeModel, final Optional<String> label) {
        return new Continue(Objects.requireNonNull(codeModel, "codeModel must not be null"), label);
    }

    static {
        Marshalling.register(Continue.class, MethodHandles.lookup());
    }
}
