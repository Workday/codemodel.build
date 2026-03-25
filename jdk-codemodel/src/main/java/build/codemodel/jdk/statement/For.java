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
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.imperative.AbstractStatement;
import build.codemodel.imperative.Statement;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A traditional {@code for} loop: {@code for(init; cond; update) body}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class For
    extends AbstractStatement {

    /**
     * The initializer statements.
     */
    private final ArrayList<Statement> initializers;

    /**
     * The optional loop condition expression.
     */
    private final Optional<Expression> condition;

    /**
     * The update expressions executed after each iteration.
     */
    private final ArrayList<Expression> updates;

    /**
     * The loop body statement.
     */
    private final Statement body;

    private For(final CodeModel codeModel,
                final Stream<Statement> initializers,
                final Optional<Expression> condition,
                final Stream<Expression> updates,
                final Statement body) {
        super(codeModel);
        this.initializers = initializers == null
            ? new ArrayList<>()
            : initializers.collect(Collectors.toCollection(ArrayList::new));
        this.condition = condition == null ? Optional.empty() : condition;
        this.updates = updates == null
            ? new ArrayList<>()
            : updates.collect(Collectors.toCollection(ArrayList::new));
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    @Unmarshal
    public For(@Bound final CodeModel codeModel,
               final Marshaller marshaller,
               final Stream<Marshalled<Trait>> traits,
               final Stream<Marshalled<Statement>> initializers,
               final Optional<Marshalled<Expression>> condition,
               final Stream<Marshalled<Expression>> updates,
               final Marshalled<Statement> body) {
        super(codeModel, marshaller, traits);
        this.initializers = initializers == null
            ? new ArrayList<>()
            : initializers.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
        this.condition = condition == null ? Optional.empty() : condition.map(marshaller::unmarshal);
        this.updates = updates == null
            ? new ArrayList<>()
            : updates.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
        this.body = marshaller.unmarshal(body);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<Statement>>> initializers,
                           final Out<Optional<Marshalled<Expression>>> condition,
                           final Out<Stream<Marshalled<Expression>>> updates,
                           final Out<Marshalled<Statement>> body) {
        super.destructor(marshaller, traits);
        initializers.set(this.initializers.stream().map(marshaller::marshal));
        condition.set(this.condition.map(marshaller::marshal));
        updates.set(this.updates.stream().map(marshaller::marshal));
        body.set(marshaller.marshal(this.body));
    }

    /**
     * Obtains the initializer statements.
     *
     * @return a {@link Stream} of initializer {@link Statement}s
     */
    public Stream<Statement> initializers() {
        return this.initializers.stream();
    }

    /**
     * Obtains the optional loop condition expression.
     *
     * @return an {@link Optional} condition {@link Expression}
     */
    public Optional<Expression> condition() {
        return this.condition;
    }

    /**
     * Obtains the update expressions executed after each iteration.
     *
     * @return a {@link Stream} of update {@link Expression}s
     */
    public Stream<Expression> updates() {
        return this.updates.stream();
    }

    /**
     * Obtains the loop body statement.
     *
     * @return the body {@link Statement}
     */
    public Statement body() {
        return this.body;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof For other
            && Objects.equals(this.initializers, other.initializers)
            && Objects.equals(this.condition, other.condition)
            && Objects.equals(this.updates, other.updates)
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    /**
     * Creates a {@link For} statement.
     *
     * @param codeModel   the {@link CodeModel}
     * @param initializers the initializer {@link Statement}s
     * @param condition    the optional loop condition {@link Expression}
     * @param updates      the update {@link Expression}s
     * @param body         the loop body {@link Statement}
     * @return a new {@link For}
     */
    public static For of(final CodeModel codeModel,
                         final Stream<Statement> initializers,
                         final Optional<Expression> condition,
                         final Stream<Expression> updates,
                         final Statement body) {
        return new For(codeModel, initializers, condition, updates, body);
    }

    static {
        Marshalling.register(For.class, MethodHandles.lookup());
    }
}
