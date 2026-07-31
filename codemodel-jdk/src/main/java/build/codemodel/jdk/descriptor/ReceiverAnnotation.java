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
import build.codemodel.foundation.usage.AnnotationTypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Trait}, attached to a {@code MethodDescriptor} or {@code ConstructorDescriptor},
 * representing a single annotation written directly on a method's or constructor's receiver
 * parameter (e.g. {@code @Anno} in {@code void m(@Anno MyClass this)}). One instance is added
 * per annotation present on the receiver.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
@NonSingular
public final class ReceiverAnnotation
    extends AbstractTraitable
    implements Trait, Traitable {

    /**
     * The {@link AnnotationTypeUsage} written on the receiver parameter.
     */
    private final AnnotationTypeUsage annotation;

    private ReceiverAnnotation(final CodeModel codeModel,
                               final AnnotationTypeUsage annotation) {
        super(codeModel);
        this.annotation = Objects.requireNonNull(annotation, "The annotation must not be null");
    }

    /**
     * {@link Unmarshal} a {@link ReceiverAnnotation}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     * @param annotation the {@link Marshalled} {@link AnnotationTypeUsage}
     */
    @Unmarshal
    public ReceiverAnnotation(@Bound final CodeModel codeModel,
                              final Marshaller marshaller,
                              final Stream<Marshalled<Trait>> traits,
                              final Marshalled<AnnotationTypeUsage> annotation) {
        super(codeModel, marshaller, traits);
        this.annotation = marshaller.unmarshal(annotation);
    }

    /**
     * {@link Marshal} a {@link ReceiverAnnotation}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Out} {@link Marshalled} {@link Trait}s
     * @param annotation the {@link Out} {@link Marshalled} {@link AnnotationTypeUsage}
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<AnnotationTypeUsage>> annotation) {
        super.destructor(marshaller, traits);
        annotation.set(marshaller.marshal(this.annotation));
    }

    /**
     * Obtains the {@link AnnotationTypeUsage} written on the receiver parameter.
     *
     * @return the {@link AnnotationTypeUsage}
     */
    public AnnotationTypeUsage annotation() {
        return this.annotation;
    }

    @Override
    public Stream<? extends Composite> compositeChildren() {
        return Stream.of(this.annotation);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ReceiverAnnotation other
            && Objects.equals(this.annotation, other.annotation)
            && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.annotation, super.hashCode());
    }

    /**
     * Creates a {@link ReceiverAnnotation}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param annotation the {@link AnnotationTypeUsage} written on the receiver parameter
     * @return a new {@link ReceiverAnnotation}
     */
    public static ReceiverAnnotation of(final CodeModel codeModel,
                                        final AnnotationTypeUsage annotation) {
        return new ReceiverAnnotation(codeModel, annotation);
    }

    static {
        Marshalling.register(ReceiverAnnotation.class, MethodHandles.lookup());
    }
}
