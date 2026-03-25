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
import build.codemodel.imperative.Statement;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A labeled statement: {@code label: statement}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Labeled
    extends AbstractStatement {

    /**
     * The label name.
     */
    private final String label;

    /**
     * The labeled statement.
     */
    private final Statement statement;

    private Labeled(final CodeModel codeModel,
                    final String label,
                    final Statement statement) {
        super(codeModel);
        this.label = Objects.requireNonNull(label, "label must not be null");
        this.statement = Objects.requireNonNull(statement, "statement must not be null");
    }

    @Unmarshal
    public Labeled(@Bound final CodeModel codeModel,
                   final Marshaller marshaller,
                   final Stream<Marshalled<Trait>> traits,
                   final String label,
                   final Marshalled<Statement> statement) {
        super(codeModel, marshaller, traits);
        this.label = label;
        this.statement = marshaller.unmarshal(statement);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<String> label,
                           final Out<Marshalled<Statement>> statement) {
        super.destructor(marshaller, traits);
        label.set(this.label);
        statement.set(marshaller.marshal(this.statement));
    }

    /**
     * Obtains the label name.
     *
     * @return the label name
     */
    public String label() {
        return this.label;
    }

    /**
     * Obtains the labeled statement.
     *
     * @return the {@link Statement}
     */
    public Statement statement() {
        return this.statement;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Labeled other
            && Objects.equals(this.label, other.label)
            && Objects.equals(this.statement, other.statement)
            && super.equals(other);
    }

    /**
     * Creates a {@link Labeled} statement.
     *
     * @param codeModel the {@link CodeModel}
     * @param label      the label name
     * @param statement  the labeled {@link Statement}
     * @return a new {@link Labeled}
     */
    public static Labeled of(final CodeModel codeModel,
                             final String label,
                             final Statement statement) {
        return new Labeled(Objects.requireNonNull(codeModel, "codeModel must not be null"),
                           label, statement);
    }

    static {
        Marshalling.register(Labeled.class, MethodHandles.lookup());
    }
}
