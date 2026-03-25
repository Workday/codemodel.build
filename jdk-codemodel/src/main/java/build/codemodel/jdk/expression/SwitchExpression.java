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
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.jdk.statement.SwitchCase;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@code switch} expression: {@code switch (selector) { cases }}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class SwitchExpression
    extends AbstractExpression {

    /**
     * The selector expression.
     */
    private final Expression selector;

    /**
     * The switch cases.
     */
    private final ArrayList<SwitchCase> cases;

    private SwitchExpression(final Expression selector, final Stream<SwitchCase> cases) {
        super(Objects.requireNonNull(selector, "selector must not be null").codeModel());
        this.selector = selector;
        this.cases = cases == null
            ? new ArrayList<>()
            : cases.collect(Collectors.toCollection(ArrayList::new));
    }

    @Unmarshal
    public SwitchExpression(@Bound final CodeModel codeModel,
                            final Marshaller marshaller,
                            final Stream<Marshalled<Trait>> traits,
                            final Marshalled<Expression> selector,
                            final Stream<Marshalled<SwitchCase>> cases) {
        super(codeModel, marshaller, traits);
        this.selector = marshaller.unmarshal(selector);
        this.cases = cases == null
            ? new ArrayList<>()
            : cases.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> selector,
                           final Out<Stream<Marshalled<SwitchCase>>> cases) {
        super.destructor(marshaller, traits);
        selector.set(marshaller.marshal(this.selector));
        cases.set(this.cases.stream().map(marshaller::marshal));
    }

    /**
     * Obtains the selector expression.
     *
     * @return the selector {@link Expression}
     */
    public Expression selector() {
        return this.selector;
    }

    /**
     * Obtains the switch cases.
     *
     * @return a {@link Stream} of {@link SwitchCase}s
     */
    public Stream<SwitchCase> cases() {
        return this.cases.stream();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof SwitchExpression other
            && Objects.equals(this.selector, other.selector)
            && Objects.equals(this.cases, other.cases)
            && super.equals(other);
    }

    /**
     * Creates a {@link SwitchExpression}.
     *
     * @param selector the selector {@link Expression}
     * @param cases    the {@link SwitchCase}s
     * @return a new {@link SwitchExpression}
     */
    public static SwitchExpression of(final Expression selector, final Stream<SwitchCase> cases) {
        return new SwitchExpression(selector, cases);
    }

    static {
        Marshalling.register(SwitchExpression.class, MethodHandles.lookup());
    }
}
