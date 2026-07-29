package build.codemodel.jdk.descriptor;

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
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing an {@code exports} directive in a {@code module-info.java}.
 * An empty {@code targetModuleNames} list means an unqualified export.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public final class ExportsDescriptor
    extends AbstractTraitable
    implements Trait, Traitable {

    private final Namespace packageName;
    private final List<ModuleName> targetModuleNames;
    private final Optional<PackageDirectiveModifier> modifier;

    private ExportsDescriptor(final CodeModel codeModel,
                              final Namespace packageName,
                              final List<ModuleName> targetModuleNames,
                              final Optional<PackageDirectiveModifier> modifier) {

        super(codeModel);
        this.packageName = Objects.requireNonNull(packageName, "packageName");
        this.targetModuleNames = List.copyOf(targetModuleNames);
        this.modifier = Objects.requireNonNull(modifier, "modifier must not be null");
    }

    /**
     * {@link Unmarshal} an {@link ExportsDescriptor}.
     */
    @Unmarshal
    public ExportsDescriptor(@Bound final CodeModel codeModel,
                             final Marshaller marshaller,
                             final Stream<Marshalled<Trait>> traits,
                             final Namespace packageName,
                             final List<ModuleName> targetModuleNames,
                             final Optional<PackageDirectiveModifier> modifier) {

        super(codeModel, marshaller, traits);
        this.packageName = packageName;
        this.targetModuleNames = List.copyOf(targetModuleNames);
        this.modifier = modifier;
    }

    /**
     * {@link Marshal} an {@link ExportsDescriptor}.
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Namespace> packageName,
                           final Out<List<ModuleName>> targetModuleNames,
                           final Out<Optional<PackageDirectiveModifier>> modifier) {

        super.destructor(marshaller, traits);
        packageName.set(this.packageName);
        targetModuleNames.set(this.targetModuleNames);
        modifier.set(this.modifier);
    }

    public Namespace packageName() {
        return packageName;
    }

    public List<ModuleName> targetModuleNames() {
        return targetModuleNames;
    }

    public Optional<PackageDirectiveModifier> modifier() {
        return modifier;
    }

    /**
     * Creates an {@link ExportsDescriptor} with no modifier (source-parsed).
     */
    public static ExportsDescriptor of(final CodeModel codeModel,
                                       final Namespace packageName,
                                       final Stream<ModuleName> targetModuleNames) {
        return new ExportsDescriptor(codeModel, packageName, targetModuleNames.toList(), Optional.empty());
    }

    /**
     * Creates an {@link ExportsDescriptor} with a {@link PackageDirectiveModifier} (bytecode-extracted).
     */
    public static ExportsDescriptor of(final CodeModel codeModel,
                                       final Namespace packageName,
                                       final Stream<ModuleName> targetModuleNames,
                                       final PackageDirectiveModifier modifier) {
        return new ExportsDescriptor(codeModel, packageName, targetModuleNames.toList(), Optional.of(modifier));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof ExportsDescriptor other
            && Objects.equals(this.packageName, other.packageName)
            && Objects.equals(this.targetModuleNames, other.targetModuleNames)
            && Objects.equals(this.modifier, other.modifier)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.packageName, this.targetModuleNames, this.modifier, super.hashCode());
    }

    static {
        Marshalling.register(ExportsDescriptor.class, MethodHandles.lookup());
    }
}
