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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An object creation expression: {@code new Type(args)}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class NewObject
    extends AbstractExpression {

    /**
     * The type expression identifying the class to instantiate.
     */
    private final Expression typeExpression;

    /**
     * The constructor argument expressions.
     */
    private final ArrayList<Expression> args;

    private NewObject(final CodeModel codeModel,
                      final Expression typeExpression,
                      final Stream<Expression> args) {
        super(codeModel);
        this.typeExpression = Objects.requireNonNull(typeExpression, "typeExpression must not be null");
        this.args = args == null
            ? new ArrayList<>()
            : args.collect(Collectors.toCollection(ArrayList::new));
    }

    @Unmarshal
    public NewObject(@Bound final CodeModel codeModel,
                     final Marshaller marshaller,
                     final Stream<Marshalled<Trait>> traits,
                     final Marshalled<Expression> typeExpression,
                     final Stream<Marshalled<Expression>> args) {
        super(codeModel, marshaller, traits);
        this.typeExpression = marshaller.unmarshal(typeExpression);
        this.args = args == null
            ? new ArrayList<>()
            : args.map(marshaller::unmarshal).collect(Collectors.toCollection(ArrayList::new));
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> typeExpression,
                           final Out<Stream<Marshalled<Expression>>> args) {
        super.destructor(marshaller, traits);
        typeExpression.set(marshaller.marshal(this.typeExpression));
        args.set(this.args.stream().map(marshaller::marshal));
    }

    /**
     * Obtains the type expression identifying the class to instantiate.
     *
     * @return the type {@link Expression}
     */
    public Expression typeExpression() {
        return this.typeExpression;
    }

    /**
     * Obtains the constructor argument expressions.
     *
     * @return a {@link Stream} of argument {@link Expression}s
     */
    public Stream<Expression> args() {
        return this.args.stream();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof NewObject other
            && Objects.equals(this.typeExpression, other.typeExpression)
            && Objects.equals(this.args, other.args)
            && super.equals(other);
    }

    /**
     * Creates a {@link NewObject} expression.
     *
     * @param codeModel     the {@link CodeModel}
     * @param typeExpression the type {@link Expression}
     * @param args           the constructor argument {@link Expression}s
     * @return a new {@link NewObject}
     */
    public static NewObject of(final CodeModel codeModel,
                               final Expression typeExpression,
                               final Stream<Expression> args) {
        return new NewObject(codeModel, typeExpression, args);
    }

    static {
        Marshalling.register(NewObject.class, MethodHandles.lookup());
    }
}
