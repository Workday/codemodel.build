package build.codemodel.foundation.usage;

/*-
 * #%L
 * Code Model Foundation
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

import build.base.foundation.Capture;
import build.base.foundation.stream.Streams;
import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.TypeName;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides type information concerning the <i>usage</i> of an <i>Annotation</i>.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public class AnnotationTypeUsage
    extends AbstractNamedTypeUsage
    implements Trait, Dependent, Comparable<AnnotationTypeUsage> {

    /**
     * The {@link AnnotationValue}s.
     */
    private final ArrayList<AnnotationValue> values;

    /**
     * Constructs an {@link AnnotationTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @param values     the {@link AnnotationValue}s
     */
    private AnnotationTypeUsage(final CodeModel codeModel,
                                final TypeName typeName,
                                final Stream<AnnotationValue> values) {

        super(codeModel, typeName);

        this.values = values == null
            ? new ArrayList<>()
            : values.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Unmarshal} an {@link AnnotationTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param typeName   the {@link TypeName}
     * @param values     the {@link Marshalled} {@link AnnotationValue}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    @Unmarshal
    public AnnotationTypeUsage(@Bound final CodeModel codeModel,
                               final Marshaller marshaller,
                               final TypeName typeName,
                               final Stream<Marshalled<AnnotationValue>> values,
                               final Stream<Marshalled<Trait>> traits) {
        super(codeModel, marshaller, typeName, traits);
        this.values = values
            .map(marshaller::unmarshal)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * {@link Marshal} an {@link AnnotationTypeUsage}.
     *
     * @param marshaller the {@link Marshaller}
     * @param typeName   the {@link Capture}d {@link TypeName}
     * @param values     the {@link Capture}d {@link Marshalled} {@link AnnotationValue}s
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     */
    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<TypeName> typeName,
                           final Out<Stream<Marshalled<AnnotationValue>>> values,
                           final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, typeName, traits);

        values.set(this.values.stream()
            .map(marshaller::marshal));
    }

    /**
     * Obtains the {@link Stream} {@link AnnotationValue}s.
     *
     * @return the {@link Stream} {@link AnnotationValue}s
     */
    public Stream<AnnotationValue> values() {
        return this.values.stream();
    }

    @Override
    public Stream<TypeUsage> dependencies() {
        return traits(Dependent.class)
            .flatMap(Dependent::dependencies);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof AnnotationTypeUsage that
            && Objects.equals(typeName(), that.typeName())
            && Streams.equals(this.values.stream(), that.values());
    }

    @Override
    public String toString() {
        return "@" + super.toString()
            + values()
            .map(Object::toString)
            .collect(Collectors.joining(",", "(", ")"))
            + Traitable.toString(this);
    }

    @Override
    public int compareTo(final AnnotationTypeUsage other) {
        return this.typeName().compareTo(other.typeName());
    }

    /**
     * Creates an {@link AnnotationTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @param values     the {@link AnnotationValue}s
     * @return a new {@link AnnotationTypeUsage}
     */
    public static AnnotationTypeUsage of(final CodeModel codeModel,
                                         final TypeName typeName,
                                         final Stream<AnnotationValue> values) {

        return new AnnotationTypeUsage(codeModel, typeName, values);
    }

    /**
     * Creates an {@link AnnotationTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param typeName   the {@link TypeName}
     * @param values     the {@link AnnotationValue}s
     * @return a new {@link AnnotationTypeUsage}
     */
    public static AnnotationTypeUsage of(final CodeModel codeModel,
                                         final TypeName typeName,
                                         final AnnotationValue... values) {

        return new AnnotationTypeUsage(codeModel, typeName, Streams.of(values));
    }

    static {
        Marshalling.register(AnnotationTypeUsage.class, MethodHandles.lookup());
    }
}
