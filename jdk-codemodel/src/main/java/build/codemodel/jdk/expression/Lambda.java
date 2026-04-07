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
import build.codemodel.expression.AbstractExpression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.imperative.Statement;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A lambda expression: {@code (params) -> body}.
 * The body is either an {@link build.codemodel.jdk.statement.ExpressionStatement}
 * (expression lambdas) or a {@link build.codemodel.imperative.Block} (block lambdas).
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class Lambda
    extends AbstractExpression {

    /**
     * The lambda parameters (may be empty for zero-arg or implicitly-typed lambdas).
     */
    private final List<LambdaParameter> parameters;

    /**
     * The lambda body statement.
     */
    private final Statement body;

    private Lambda(final CodeModel codeModel,
                   final List<LambdaParameter> parameters,
                   final Statement body) {
        super(codeModel);
        this.parameters = parameters == null ? List.of() : List.copyOf(parameters);
        this.body = Objects.requireNonNull(body, "body must not be null");
    }

    @Unmarshal
    public Lambda(@Bound final CodeModel codeModel,
                  final Marshaller marshaller,
                  final Stream<Marshalled<Trait>> traits,
                  final Stream<String> paramTypeNames,
                  final Stream<String> paramNames,
                  final Marshalled<Statement> body) {
        super(codeModel, marshaller, traits);
        final var typeNameList = paramTypeNames == null ? List.<String>of() : paramTypeNames.toList();
        final var nameList = paramNames == null ? List.<String>of() : paramNames.toList();
        this.parameters = IntStream.range(0, Math.min(typeNameList.size(), nameList.size()))
            .mapToObj(i -> new LambdaParameter(typeNameList.get(i), nameList.get(i)))
            .toList();
        this.body = marshaller.unmarshal(body);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Stream<String>> paramTypeNames,
                           final Out<Stream<String>> paramNames,
                           final Out<Marshalled<Statement>> body) {
        super.destructor(marshaller, traits);
        paramTypeNames.set(this.parameters.stream().map(LambdaParameter::typeName));
        paramNames.set(this.parameters.stream().map(LambdaParameter::name));
        body.set(marshaller.marshal(this.body));
    }

    /**
     * Obtains the lambda parameters.
     *
     * @return a {@link Stream} of {@link LambdaParameter}s
     */
    public Stream<LambdaParameter> parameters() {
        return this.parameters.stream();
    }

    /**
     * Obtains the lambda body statement.
     *
     * @return the body {@link Statement}
     */
    public Statement body() {
        return this.body;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Lambda other
            && Objects.equals(this.parameters, other.parameters)
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    /**
     * Creates a {@link Lambda} expression.
     *
     * @param codeModel  the {@link CodeModel}
     * @param parameters the lambda parameters
     * @param body       the body {@link Statement}
     * @return a new {@link Lambda}
     */
    public static Lambda of(final CodeModel codeModel,
                            final List<LambdaParameter> parameters,
                            final Statement body) {
        return new Lambda(Objects.requireNonNull(codeModel, "codeModel must not be null"), parameters, body);
    }

    /**
     * Creates a {@link Lambda} expression with no parameters.
     *
     * @param codeModel the {@link CodeModel}
     * @param body       the body {@link Statement}
     * @return a new {@link Lambda}
     */
    public static Lambda of(final CodeModel codeModel, final Statement body) {
        return new Lambda(Objects.requireNonNull(codeModel, "codeModel must not be null"), List.of(), body);
    }

    static {
        Marshalling.register(Lambda.class, MethodHandles.lookup());
    }
}
