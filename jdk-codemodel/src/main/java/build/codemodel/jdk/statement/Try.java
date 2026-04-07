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
import build.codemodel.imperative.Block;
import build.codemodel.imperative.Statement;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@code try} statement: {@code try { } catch(E e) { } finally { }}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Try
    extends AbstractStatement {

    /**
     * The try-with-resources declarations, in order.
     */
    private final ArrayList<Statement> resources;

    /**
     * The try block body.
     */
    private final Block body;

    /**
     * The catch clauses, in order.
     */
    private final ArrayList<CatchClause> catches;

    /**
     * The optional {@code finally} block.
     */
    private final Optional<Block> finallyBlock;

    private Try(final CodeModel codeModel,
                final Stream<Statement> resources,
                final Block body,
                final Stream<CatchClause> catches,
                final Optional<Block> finallyBlock) {
        super(codeModel);
        this.resources = resources == null
            ? new ArrayList<>()
            : resources.collect(Collectors.toCollection(ArrayList::new));
        this.body = Objects.requireNonNull(body, "body must not be null");
        this.catches = catches == null
            ? new ArrayList<>()
            : catches.collect(Collectors.toCollection(ArrayList::new));
        this.finallyBlock = finallyBlock == null ? Optional.empty() : finallyBlock;
    }

    @Unmarshal
    public Try(@Bound final CodeModel codeModel,
               final Marshaller marshaller,
               final Stream<Marshalled<Trait>> traits,
               final Stream<Marshalled<Statement>> resources,
               final Marshalled<Block> body,
               final Stream<Marshalled<CatchClause>> catches,
               final Optional<Marshalled<Block>> finallyBlock) {
        super(codeModel, marshaller, traits);
        this.resources = resources == null
            ? new ArrayList<>()
            : resources.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
        this.body = marshaller.unmarshal(body);
        this.catches = catches == null
            ? new ArrayList<>()
            : catches.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
        this.finallyBlock = finallyBlock == null ? Optional.empty() : finallyBlock.map(marshaller::unmarshal);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<Statement>>> resources,
                           final Out<Marshalled<Block>> body,
                           final Out<Stream<Marshalled<CatchClause>>> catches,
                           final Out<Optional<Marshalled<Block>>> finallyBlock) {
        super.destructor(marshaller, traits);
        resources.set(this.resources.stream().map(marshaller::marshal));
        body.set(marshaller.marshal(this.body));
        catches.set(this.catches.stream().map(marshaller::marshal));
        finallyBlock.set(this.finallyBlock.map(marshaller::marshal));
    }

    /**
     * Obtains the try-with-resources declarations.
     *
     * @return a {@link Stream} of resource {@link Statement}s
     */
    public Stream<Statement> resources() {
        return this.resources.stream();
    }

    /**
     * Obtains the try block body.
     *
     * @return the body {@link Block}
     */
    public Block body() {
        return this.body;
    }

    /**
     * Obtains the catch clauses.
     *
     * @return a {@link Stream} of {@link CatchClause}s
     */
    public Stream<CatchClause> catches() {
        return this.catches.stream();
    }

    /**
     * Obtains the optional {@code finally} block.
     *
     * @return an {@link Optional} {@code finally} {@link Block}
     */
    public Optional<Block> finallyBlock() {
        return this.finallyBlock;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Try other
            && Objects.equals(this.resources, other.resources)
            && Objects.equals(this.body, other.body)
            && Objects.equals(this.catches, other.catches)
            && Objects.equals(this.finallyBlock, other.finallyBlock)
            && super.equals(other);
    }

    /**
     * Creates a {@link Try} statement.
     *
     * @param codeModel   the {@link CodeModel}
     * @param resources    the try-with-resources {@link Statement}s
     * @param body         the try body {@link Block}
     * @param catches      the {@link CatchClause}s
     * @param finallyBlock the optional {@code finally} {@link Block}
     * @return a new {@link Try}
     */
    public static Try of(final CodeModel codeModel,
                         final Stream<Statement> resources,
                         final Block body,
                         final Stream<CatchClause> catches,
                         final Optional<Block> finallyBlock) {
        return new Try(codeModel, resources, body, catches, finallyBlock);
    }

    /**
     * Creates a {@link Try} statement with no resources.
     *
     * @param codeModel   the {@link CodeModel}
     * @param body         the try body {@link Block}
     * @param catches      the {@link CatchClause}s
     * @param finallyBlock the optional {@code finally} {@link Block}
     * @return a new {@link Try}
     */
    public static Try of(final CodeModel codeModel,
                         final Block body,
                         final Stream<CatchClause> catches,
                         final Optional<Block> finallyBlock) {
        return new Try(codeModel, null, body, catches, finallyBlock);
    }

    static {
        Marshalling.register(Try.class, MethodHandles.lookup());
    }
}
