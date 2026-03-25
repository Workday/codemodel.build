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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents the usage of a <a href="https://en.wikipedia.org/wiki/Variable_(computer_science)">Variable</a>.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class VariableUsage
    extends AbstractExpression {

    /**
     * The {@link VariableName} for the {@link VariableUsage}.
     */
    private final VariableName name;

    /**
     * The {@link Optional}ly defined {@link TypeUsage}.
     */
    private final Optional<TypeUsage> type;

    /**
     * Constructs a {@link VariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link VariableName}
     * @param type       the {@link Optional} {@link TypeUsage} for the variable
     */
    private VariableUsage(final CodeModel codeModel,
                          final VariableName name,
                          final Optional<TypeUsage> type) {

        super(codeModel);

        this.name = Objects.requireNonNull(name, "The VariableName must not be null");
        this.type = type == null ? Optional.empty() : type;
    }

    /**
     * Un{@link Marshal} a {@link VariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param name       the {@link VariableName}
     * @param type       the {@link Optional} {@link Marshalled} {@link TypeUsage}
     */
    @Unmarshal
    public VariableUsage(@Bound final CodeModel codeModel,
                         final Marshaller marshaller,
                         final Stream<Marshalled<Trait>> traits,
                         final VariableName name,
                         final Optional<Marshalled<TypeUsage>> type) {

        super(codeModel, marshaller, traits);

        this.name = name;
        this.type = type == null
            ? Optional.empty()
            : type.map(marshaller::unmarshal);
        ;
    }

    /**
     * {@link Marshal} a {@link VariableUsage}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param name       the {@link Out} {@link VariableName}
     * @param type       the {@link Out} {@link Optional} {@link Marshalled} {@link TypeUsage}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<VariableName> name,
                           final Out<Optional<Marshalled<TypeUsage>>> type) {

        super.destructor(marshaller, traits);

        name.set(this.name);
        type.set(this.type.map(marshaller::marshal));
    }

    public String name() {
        return this.name.canonicalName();
    }

    @Override
    public Optional<TypeUsage> type() {
        return this.type;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final VariableUsage variableUsage)) {
            return false;
        }
        return Objects.equals(this.name, variableUsage.name) && Objects.equals(this.type, variableUsage.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.type);
    }

    /**
     * Creates a {@link VariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link VariableName}
     * @param type       the {@link Optional} {@link TypeUsage} for the variable
     */
    public static VariableUsage of(final CodeModel codeModel,
                                   final VariableName name,
                                   final Optional<TypeUsage> type) {

        return new VariableUsage(codeModel, name, type);
    }

    /**
     * Creates a {@link VariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link VariableName}
     * @param type       the {{@link TypeUsage} for the variable
     */
    public static VariableUsage of(final CodeModel codeModel,
                                   final VariableName name,
                                   final TypeUsage type) {

        return new VariableUsage(codeModel, name, Optional.ofNullable(type));
    }

    /**
     * Create a {@link VariableUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param name       the {@link VariableName}
     */
    public static VariableUsage of(final CodeModel codeModel,
                                   final VariableName name) {

        return new VariableUsage(codeModel, name, Optional.empty());
    }

    static {
        Marshalling.register(VariableUsage.class, MethodHandles.lookup());
    }
}
