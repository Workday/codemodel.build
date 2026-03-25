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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A single case in a {@code switch} statement or expression.
 * An empty {@code labels()} stream represents the {@code default} case.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class SwitchCase
    extends AbstractStatement {

    /**
     * The case label expressions; empty for the {@code default} case.
     */
    private final ArrayList<Expression> labels;

    /**
     * The statements in the case body.
     */
    private final ArrayList<Statement> statements;

    private SwitchCase(final CodeModel codeModel,
                       final Stream<Expression> labels,
                       final Stream<Statement> statements) {
        super(codeModel);
        this.labels = labels == null
            ? new ArrayList<>()
            : labels.collect(Collectors.toCollection(ArrayList::new));
        this.statements = statements == null
            ? new ArrayList<>()
            : statements.collect(Collectors.toCollection(ArrayList::new));
    }

    @Unmarshal
    public SwitchCase(@Bound final CodeModel codeModel,
                      final Marshaller marshaller,
                      final Stream<Marshalled<Trait>> traits,
                      final Stream<Marshalled<Expression>> labels,
                      final Stream<Marshalled<Statement>> statements) {
        super(codeModel, marshaller, traits);
        this.labels = labels == null
            ? new ArrayList<>()
            : labels.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
        this.statements = statements == null
            ? new ArrayList<>()
            : statements.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<Marshalled<Expression>>> labels,
                           final Out<Stream<Marshalled<Statement>>> statements) {
        super.destructor(marshaller, traits);
        labels.set(this.labels.stream().map(marshaller::marshal));
        statements.set(this.statements.stream().map(marshaller::marshal));
    }

    /**
     * Obtains the case label expressions.
     * An empty stream indicates this is the {@code default} case.
     *
     * @return a {@link Stream} of label {@link Expression}s
     */
    public Stream<Expression> labels() {
        return this.labels.stream();
    }

    /**
     * Obtains the statements in the case body.
     *
     * @return a {@link Stream} of body {@link Statement}s
     */
    public Stream<Statement> statements() {
        return this.statements.stream();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof SwitchCase other
            && Objects.equals(this.labels, other.labels)
            && Objects.equals(this.statements, other.statements)
            && super.equals(other);
    }

    /**
     * Creates a {@link SwitchCase}.
     * Pass {@code null} or an empty stream for {@code labels} to create the {@code default} case.
     *
     * @param codeModel the {@link CodeModel}
     * @param labels     the case label {@link Expression}s, or {@code null} for the {@code default} case
     * @param statements the body {@link Statement}s
     * @return a new {@link SwitchCase}
     */
    public static SwitchCase of(final CodeModel codeModel,
                                final Stream<Expression> labels,
                                final Stream<Statement> statements) {
        return new SwitchCase(Objects.requireNonNull(codeModel, "codeModel must not be null"),
                              labels, statements);
    }

    static {
        Marshalling.register(SwitchCase.class, MethodHandles.lookup());
    }
}
