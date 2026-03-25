package build.codemodel.imperative;

/*-
 * #%L
 * Imperative Code Model
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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A <a href="https://en.wikipedia.org/wiki/Block_(programming)">Block</a> of {@link Statement}s.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Block
    extends AbstractStatement {

    /**
     * The {@link Statement}s in the {@link Block}.
     */
    private ArrayList<Statement> statements;

    /**
     * Constructs a {@link Block}.
     *
     * @param codeModel the {@link CodeModel}
     * @param statements the {@link Statement}s
     */
    private Block(final CodeModel codeModel,
                  final Stream<Statement> statements) {

        super(codeModel);

        this.statements = statements == null
            ? new ArrayList<>()
            : statements.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Unmarshal} a {@link Block}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling {@link Marshalled} objects
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param statements the {@link Marshalled} {@link Statement}s
     */
    @Unmarshal
    public Block(@Bound final CodeModel codeModel,
                 @Bound final Marshaller marshaller,
                 final Stream<Marshalled<Trait>> traits,
                 final Stream<Marshalled<Statement>> statements) {

        super(codeModel, marshaller, traits);

        this.statements = statements == null
            ? new ArrayList<>()
            : statements.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} a {@link Block}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param statements the {@link Out} {@link Marshalled} {@link Statement}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<Statement>>> statements) {

        super.destructor(marshaller, traits);

        statements.set(this.statements.stream()
            .filter(statement -> marshaller.isMarshallable(statement.getClass()))
            .map(marshaller::marshal));
    }

    /**
     * Obtains the {@link Statement}s in the {@link Block}.
     *
     * @return the {@link Statement}s
     */
    public Stream<Statement> statements() {
        return this.statements.stream();
    }

    /**
     * Creates an empty {@link Block} for the specified {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel}
     * @return a new empty {@link Block}
     */
    public static Block empty(final CodeModel codeModel) {
        return new Block(codeModel, Stream.empty());
    }

    /**
     * Creates a {@link Block} with the provided {@link Statement}s.
     *
     * @param statements the remaining {@link Statement}s
     * @return a new {@link Block}
     */
    public static Block of(final Statement... statements) {

        if (statements == null || statements.length == 0) {
            throw new IllegalArgumentException("At least one Statement must be provided");
        }

        return new Block(statements[0].codeModel(), Stream.of(statements));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof Block other
            && Objects.equals(this.statements, other.statements)
            && super.equals(other);
    }

    static {
        // register this type to be usable for marshalling
        Marshalling.register(Block.class, MethodHandles.lookup());
    }
}
