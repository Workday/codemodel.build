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
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.imperative.Block;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Trait} on a type descriptor representing a static or instance initializer block.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
@NonSingular
public final class InitializerBlockDescriptor
    extends AbstractTraitable
    implements Trait, Traitable {

    private final boolean isStatic;
    private final Block body;

    public InitializerBlockDescriptor(final CodeModel codeModel, final boolean isStatic, final Block body) {
        super(codeModel);
        this.isStatic = isStatic;
        this.body = Objects.requireNonNull(body, "body");
    }

    @Unmarshal
    public InitializerBlockDescriptor(@Bound final CodeModel codeModel,
                                      final Marshaller marshaller,
                                      final Stream<Marshalled<Trait>> traits,
                                      final boolean isStatic,
                                      final Block body) {

        super(codeModel, marshaller, traits);

        this.isStatic = isStatic;
        this.body = body;
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Boolean> isStatic,
                           final Out<Block> body) {

        super.destructor(marshaller, traits);

        isStatic.set(this.isStatic);
        body.set(this.body);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Block body() {
        return body;
    }

    @Override
    protected Stream<? extends Composite> compositeChildren() {
        return Stream.of(body);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof InitializerBlockDescriptor other
            && this.isStatic == other.isStatic
            && Objects.equals(this.body, other.body)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isStatic, this.body, super.hashCode());
    }

    static {
        Marshalling.register(InitializerBlockDescriptor.class, MethodHandles.lookup());
    }
}
