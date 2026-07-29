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

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing a single import declaration from the source file that declared a type.
 * Traits of this kind are ordered as they appear in source.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
@NonSingular
public final class ImportDeclaration
    extends AbstractTraitable
    implements Trait, Traitable {

    private final String qualifiedName;
    private final boolean isStatic;
    private final boolean isOnDemand;
    private final int order;

    private ImportDeclaration(final CodeModel codeModel, final String qualifiedName, final boolean isStatic,
                              final boolean isOnDemand, final int order) {

        super(codeModel);
        this.qualifiedName = Objects.requireNonNull(qualifiedName, "qualifiedName");
        this.isStatic = isStatic;
        this.isOnDemand = isOnDemand;
        this.order = order;
    }

    /**
     * {@link Unmarshal} an {@link ImportDeclaration}.
     */
    @Unmarshal
    public ImportDeclaration(@Bound final CodeModel codeModel,
                             final Marshaller marshaller,
                             final Stream<Marshalled<Trait>> traits,
                             final String qualifiedName,
                             final boolean isStatic,
                             final boolean isOnDemand,
                             final int order) {

        super(codeModel, marshaller, traits);
        this.qualifiedName = qualifiedName;
        this.isStatic = isStatic;
        this.isOnDemand = isOnDemand;
        this.order = order;
    }

    /**
     * {@link Marshal} an {@link ImportDeclaration}.
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<String> qualifiedName,
                           final Out<Boolean> isStatic,
                           final Out<Boolean> isOnDemand,
                           final Out<Integer> order) {

        super.destructor(marshaller, traits);
        qualifiedName.set(this.qualifiedName);
        isStatic.set(this.isStatic);
        isOnDemand.set(this.isOnDemand);
        order.set(this.order);
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isOnDemand() {
        return isOnDemand;
    }

    public int order() {
        return order;
    }

    public static ImportDeclaration of(final CodeModel codeModel, final String qualifiedName, final int order) {
        return new ImportDeclaration(codeModel, qualifiedName, false, false, order);
    }

    public static ImportDeclaration ofStatic(final CodeModel codeModel, final String qualifiedName, final int order) {
        return new ImportDeclaration(codeModel, qualifiedName, true, false, order);
    }

    public static ImportDeclaration ofOnDemand(final CodeModel codeModel, final String qualifiedName, final int order) {
        return new ImportDeclaration(codeModel, qualifiedName, false, true, order);
    }

    public static ImportDeclaration ofStaticOnDemand(final CodeModel codeModel, final String qualifiedName,
                                                     final int order) {
        return new ImportDeclaration(codeModel, qualifiedName, true, true, order);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof ImportDeclaration other
            && this.isStatic == other.isStatic
            && this.isOnDemand == other.isOnDemand
            && this.order == other.order
            && Objects.equals(this.qualifiedName, other.qualifiedName)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.qualifiedName, this.isStatic, this.isOnDemand, this.order, super.hashCode());
    }

    static {
        Marshalling.register(ImportDeclaration.class, MethodHandles.lookup());
    }
}
