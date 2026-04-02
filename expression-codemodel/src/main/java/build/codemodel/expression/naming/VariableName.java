package build.codemodel.expression.naming;

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
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Name;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.TypeName;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Name} of a <a href="https://en.wikipedia.org/wiki/Variable_(computer_science)">Variable</a> defined
 * in a {@link CodeModel}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public final class VariableName
    implements Name {

    /**
     * The {@link Optional} {@link ModuleName} in which the {@link VariableName} is defined.
     */
    private Optional<ModuleName> moduleName;

    /**
     * The {@link Optional} {@link Namespace} defining the {@link VariableName}.
     */
    private Optional<Namespace> namespace;

    /**
     * The {@link Optional} {@link TypeName} in which this {@link VariableName} is defined.
     */
    private Optional<TypeName> typeName;

    /**
     * The {@link IrreducibleName} of the {@link VariableName}.
     */
    private final IrreducibleName irreducibleName;

    /**
     * The internal {@link String} representation of the {@link VariableName}
     */
    private final String string;

    /**
     * Constructs a {@link VariableName}.
     *
     * @param moduleName      the {@link Optional} {@link ModuleName}
     * @param namespace       the {@link Optional} {@link Namespace}
     * @param typeName        the {@link Optional}  {@link TypeName}
     * @param irreducibleName the {@link IrreducibleName} for the {@link TypeName}
     */
    private VariableName(final Optional<ModuleName> moduleName,
                         final Optional<Namespace> namespace,
                         final Optional<TypeName> typeName,
                         final IrreducibleName irreducibleName) {

        this.moduleName = moduleName == null ? Optional.empty() : moduleName;
        this.namespace = namespace == null ? Optional.empty() : namespace;
        this.typeName = typeName == null ? Optional.empty() : typeName;
        this.irreducibleName =
            Objects.requireNonNull(irreducibleName, "The name of the method must not be null");

        this.string =
            (this.typeName.isPresent()
                ? this.typeName
                .map(TypeName::toString)
                .map(name -> name + ".")
                .orElse("")
                : this.moduleName
                    .map(name -> name.toString() + "/")
                    .orElse("")
                    + this.namespace()
                    .map(Namespace::toString)
                    .map(name -> name + ".")
                    .orElse(""))
                + this.irreducibleName;
    }

    /**
     * Unmarshalling constructor for {@link VariableName}.
     *
     * @param codeModel      the {@link CodeModel}
     * @param marshaller      the {@link Marshaller}
     * @param moduleName      the {@link Optional} {@link ModuleName}
     * @param namespace       the {@link Optional} {@link Namespace}
     * @param typeName        the {@link Optional} {@link TypeName}
     * @param irreducibleName the {@link IrreducibleName} for the {@link VariableName}
     * @param string          the {@link String} representation of the {@link VariableName}
     */
    @Unmarshal
    public VariableName(@Bound final CodeModel codeModel,
                        final Marshaller marshaller,
                        final Optional<ModuleName> moduleName,
                        final Optional<Namespace> namespace,
                        final Optional<TypeName> typeName,
                        final IrreducibleName irreducibleName,
                        final String string) {

        this.moduleName = moduleName;
        this.namespace = namespace;
        this.typeName = typeName;
        this.irreducibleName = irreducibleName; 
        this.string = string;
    }

    /**
     * {@link Marshal} a {@link VariableName}.
     *
     * @param marshaller      the {@link Marshaller}
     * @param moduleName      the {@link Out} parameter for {@link Optional} {@link ModuleName}
     * @param namespace       the {@link Out} parameter for {@link Optional} {@link Namespace}
     * @param typeName        the {@link Out} parameter for {@link Optional} {@link TypeName}
     * @param irreducibleName the {@link Out} parameter for {@link IrreducibleName}
     * @param string          the {@link Out} parameter for {@link String}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Optional<ModuleName>> moduleName,
                           final Out<Optional<Namespace>> namespace,
                           final Out<Optional<TypeName>> typeName,
                           final Out<IrreducibleName> irreducibleName,
                           final Out<String> string) {
        
        moduleName.set(this.moduleName);
        namespace.set(this.namespace);
        typeName.set(this.typeName);
        irreducibleName.set(this.irreducibleName);
        string.set(this.string);
    }

    /**
     * The {@link Optional} {@link ModuleName} in which the {@link VariableName} is defined.
     *
     * @return the {@link Optional} {@link ModuleName}
     */
    public Optional<ModuleName> moduleName() {
        return this.moduleName;
    }

    /**
     * The {@link Optional} {@link Namespace} in which the {@link VariableName} is defined.
     *
     * @return the {@link Optional} {@link Namespace}
     */
    public Optional<Namespace> namespace() {
        return this.namespace;
    }

    /**
     * Obtains the {@link Optional} {@link TypeName} in which this {@link VariableName} is defined.
     *
     * @return the {@link Optional} {@link VariableName}
     */
    public Optional<TypeName> typeName() {
        return this.typeName;
    }

    /**
     * The {@link IrreducibleName} name of the {@link VariableName}.
     *
     * @return the {@link IrreducibleName}
     */
    public IrreducibleName name() {
        return this.irreducibleName;
    }

    /**
     * Obtains the <i>canonical-name</i>.
     *
     * @return the canonical name
     */
    public String canonicalName() {
        return namespace()
            .map(p -> p + ".")
            .orElse("")
            + typeName()
            .map(e -> e.name() + ".")
            .orElse("")
            + name().toString();
    }

    @Override
    public boolean matches(final String regularExpression) {
        return this.string.matches(regularExpression);
    }

    @Override
    public int length() {
        return this.string.length();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof VariableName that
            && Objects.equals(this.string, that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.string);
    }

    @Override
    public String toString() {
        return this.string;
    }

    /**
     * Creates a {@link VariableName}.
     *
     * @param moduleName      the {@link Optional} {@link ModuleName}
     * @param namespace       the {@link Optional} {@link Namespace}
     * @param typeName        the {@link Optional} enclosing {@link TypeName}
     * @param irreducibleName the {@link IrreducibleName} for the {@link VariableName}
     * @return a {@link VariableName}
     */
    public static VariableName of(final Optional<ModuleName> moduleName,
                                  final Optional<Namespace> namespace,
                                  final Optional<TypeName> typeName,
                                  final IrreducibleName irreducibleName) {

        return new VariableName(moduleName, namespace, typeName, irreducibleName);
    }

    /**
     * Creates a {@link VariableName}.
     *
     * @param irreducibleName the {@link IrreducibleName} for the {@link VariableName}
     * @return a {@link VariableName}
     */
    public static VariableName of(final IrreducibleName irreducibleName) {
        return VariableName.of(Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                irreducibleName);
    }

    static {
        Marshalling.register(VariableName.class, MethodHandles.lookup());
    }
}
