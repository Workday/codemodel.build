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

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@code catch} clause: {@code catch (ExType param) body}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class CatchClause
    extends AbstractStatement {

    /**
     * The source-form type names of the caught exception(s).
     * Has more than one element for multi-catch clauses (e.g. {@code catch (IOException | RuntimeException e)}).
     */
    private final List<String> exceptionTypeNames;

    /**
     * The name of the exception parameter.
     */
    private final String paramName;

    /**
     * The catch body block.
     */
    private final Block body;

    private CatchClause(final CodeModel codeModel,
                        final List<String> exceptionTypeNames,
                        final String paramName,
                        final Block body) {
        super(codeModel);
        this.exceptionTypeNames = Objects.requireNonNull(exceptionTypeNames, "exceptionTypeNames must not be null");
        this.paramName = Objects.requireNonNull(paramName, "paramName must not be null");
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    @Unmarshal
    public CatchClause(@Bound final CodeModel codeModel,
                       final Marshaller marshaller,
                       final Stream<Marshalled<Trait>> traits,
                       final Stream<String> exceptionTypeNames,
                       final String paramName,
                       final Marshalled<Block> body) {
        super(codeModel, marshaller, traits);
        this.exceptionTypeNames = exceptionTypeNames == null ? List.of() : exceptionTypeNames.toList();
        this.paramName = paramName;
        this.body = marshaller.unmarshal(body);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<String>> exceptionTypeNames,
                           final Out<String> paramName,
                           final Out<Marshalled<Block>> body) {
        super.destructor(marshaller, traits);
        exceptionTypeNames.set(this.exceptionTypeNames.stream());
        paramName.set(this.paramName);
        body.set(marshaller.marshal(this.body));
    }

    /**
     * Obtains the source-form type names of the caught exception(s).
     * Returns more than one element for multi-catch clauses.
     *
     * @return a {@link Stream} of exception type names
     */
    public Stream<String> exceptionTypeNames() {
        return this.exceptionTypeNames.stream();
    }

    /**
     * Obtains the name of the exception parameter.
     *
     * @return the parameter name
     */
    public String paramName() {
        return this.paramName;
    }

    /**
     * Obtains the catch body block.
     *
     * @return the body {@link Block}
     */
    public Block body() {
        return this.body;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof CatchClause other
            && Objects.equals(this.exceptionTypeNames, other.exceptionTypeNames)
            && Objects.equals(this.paramName, other.paramName)
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    /**
     * Creates a {@link CatchClause}.
     *
     * @param codeModel          the {@link CodeModel}
     * @param exceptionTypeNames the source-form type names of the caught exception(s)
     * @param paramName          the name of the exception parameter
     * @param body               the catch body {@link Block}
     * @return a new {@link CatchClause}
     */
    public static CatchClause of(final CodeModel codeModel,
                                 final List<String> exceptionTypeNames,
                                 final String paramName,
                                 final Block body) {
        return new CatchClause(codeModel, exceptionTypeNames, paramName, body);
    }

    static {
        Marshalling.register(CatchClause.class, MethodHandles.lookup());
    }
}
