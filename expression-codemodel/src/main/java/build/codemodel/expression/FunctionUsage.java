package build.codemodel.expression;

/*-
 * #%L
 * Expression Code Model
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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.expression.naming.FunctionName;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Function_application">Function Application</a>.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class FunctionUsage
    extends AbstractExpression {

    /**
     * The {@link FunctionName}.
     */
    private final FunctionName functionName;

    /**
     * The actual-parameters for the {@link FunctionUsage}.
     */
    private final ArrayList<Expression> arguments;

    /**
     * Constructs a {@link FunctionUsage}.
     *
     * @param codeModel   the {@link CodeModel}
     * @param functionName the {@link FunctionName} for the function
     * @param arguments    the arguments for the function
     */
    private FunctionUsage(final CodeModel codeModel,
                          final FunctionName functionName,
                          final Stream<Expression> arguments) {

        super(codeModel);
        this.functionName = Objects.requireNonNull(functionName, "The FunctionName must not be null");
        this.arguments = arguments == null
            ? new ArrayList<>()
            : arguments.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Un{@link Marshal} a {@link FunctionUsage}.
     *
     * @param codeModel   the {@link CodeModel}
     * @param marshaller   the {@link Marshaller}
     * @param traits       the {@link Stream} of {@link Trait}s
     * @param functionName the {@link FunctionName}
     * @param arguments    the {@link Stream} of {@link Marshal}ed {@link Expression}s
     */
    @Unmarshal
    public FunctionUsage(@Bound final CodeModel codeModel,
                         final Marshaller marshaller,
                         final Stream<Marshalled<Trait>> traits,
                         final FunctionName functionName,
                         final Stream<Marshalled<Expression>> arguments) {
        
        super(codeModel, marshaller, traits);
        
        this.functionName = functionName;
        this.arguments = arguments.map(marshalled -> marshaller.unmarshal(marshalled)).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} a {@link FunctionUsage}.
     *
     * @param marshaller   the {@link Marshaller}
     * @param traits       the {@link Out} parameter for {@link Stream} of {@link Trait}s
     * @param functionName the {@link Out} parameter for {@link FunctionName}
     * @param arguments    the {@link Out} parameter for {@link Stream} of {@link Expression}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<FunctionName> functionName,
                           final Out<Stream<Marshalled<Expression>>> arguments) {
        
        super.destructor(marshaller, traits);

        functionName.set(this.functionName);
        arguments.set(this.arguments.stream().map(argument -> marshaller.marshal(argument)));
    }

    @Override
    public Optional<TypeUsage> type() {
        // the TypeUsage for the FunctionUsage is inferred from the definition of the Function
        return Optional.empty();
    }

    /**
     * Obtains the {@link FunctionName}.
     *
     * @return the {@link FunctionName}
     */
    public FunctionName functionName() {
        return this.functionName;
    }

    /**
     * Obtains the actual-parameter {@link Expression} arguments.
     *
     * @return the actual-parameter {@link Expression} arguments
     */
    public Stream<Expression> arguments() {
        return this.arguments.stream();
    }

    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof FunctionUsage other
                && Objects.equals(this.functionName, other.functionName)
                && this.arguments.equals(other.arguments)
                && super.equals(object);
    }
    /**
     * Creates a {@link FunctionUsage}.
     *
     * @param codeModel   the {@link CodeModel}
     * @param functionName the {@link FunctionName} for the function
     * @param arguments    the arguments for the function
     */
    public static FunctionUsage of(final CodeModel codeModel,
                                   final FunctionName functionName,
                                   final Stream<Expression> arguments) {

        return new FunctionUsage(codeModel, functionName, arguments);
    }

    /**
     * Creates a {@link FunctionUsage}.
     *
     * @param codeModel   the {@link CodeModel}
     * @param functionName the {@link FunctionName} for the function
     * @param arguments    the arguments for the function
     */
    public static FunctionUsage of(final CodeModel codeModel,
                                   final FunctionName functionName,
                                   final Expression... arguments) {

        return new FunctionUsage(codeModel, functionName, arguments == null ? null : Stream.of(arguments));
    }

    static {
        Marshalling.register(FunctionUsage.class, MethodHandles.lookup());
    }
}
