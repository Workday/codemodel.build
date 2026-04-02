package build.codemodel.injection;

/*-
 * #%L
 * Dependency Injection
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

import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.jdk.descriptor.FieldType;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import jakarta.inject.Qualifier;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An {@link InjectionPoint} for a {@link FieldDescriptor}.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public class FieldInjectionPoint
    implements InjectionPoint {

    /**
     * The {@link JDKTypeDescriptor} in which the {@link FieldInjectionPoint} is defined.
     */
    private final JDKTypeDescriptor typeDescriptor;

    /**
     * The {@link FieldDescriptor} defining the field that may be injected.
     */
    private final FieldDescriptor fieldDescriptor;

    /**
     * The {@link Dependency} defined by the {@link FieldDescriptor}.
     */
    private final Dependency dependency;

    /**
     * Constructs a {@link FieldInjectionPoint}.
     *
     * @param typeDescriptor               the {@link JDKTypeDescriptor}
     * @param fieldDescriptor              the {@link FieldDescriptor}
     * @param qualifierAnnotationExtractor a {@link Function} to extract {@link Qualifier} {@link AnnotationTypeUsage}s
     */
    private FieldInjectionPoint(final JDKTypeDescriptor typeDescriptor,
                                final FieldDescriptor fieldDescriptor,
                                final Function<? super Traitable, Stream<AnnotationTypeUsage>> qualifierAnnotationExtractor) {

        this.typeDescriptor = Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null");
        this.fieldDescriptor = Objects.requireNonNull(fieldDescriptor, "The FieldDescriptor must not be null");
        Objects.requireNonNull(qualifierAnnotationExtractor, "The Qualifier Annotation Extractor must not be null");

        // create the InjectionPointDependency based on the FieldDescriptor
        this.dependency = InjectionPointDependency.of(
            this,
            IndependentDependency.of(this.fieldDescriptor.type(), qualifierAnnotationExtractor));
    }

    @Override
    public JDKTypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public Stream<Dependency> dependencies() {
        return Stream.of(this.dependency);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T inject(final Object target, final Object[] actualParameters) {

        final var field = this.fieldDescriptor.getTrait(FieldType.class)
            .map(FieldType::field)
            .orElseThrow(() -> new IllegalArgumentException("FieldType is not defined for " + this.fieldDescriptor));

        if (field.trySetAccessible()) {
            // ensure there's a single parameter
            if (actualParameters.length != 1) {
                throw new InjectionFailedException(this,
                    "Expected one parameter but was provided with " + Arrays.toString(actualParameters));
            }

            // attempt to perform injection
            try {
                field.set(target, actualParameters[0]);

                return (T) field.get(target);
            }
            catch (final IllegalAccessException | IllegalArgumentException e) {
                throw new InjectionFailedException(this, e);
            }
        }
        else {
            throw new InjectionFailedException(this, "Can't inject into " + field + " as it's inaccessible");
        }
    }

    /**
     * Obtains the {@link FieldDescriptor} for which the {@link FieldInjectionPoint} was created.
     *
     * @return the {@link FieldDescriptor}
     */
    public FieldDescriptor fieldDescriptor() {
        return this.fieldDescriptor;
    }

    /**
     * Creates a {@link FieldInjectionPoint}.
     *
     * @param typeDescriptor               the {@link JDKTypeDescriptor}
     * @param fieldDescriptor              the {@link FieldDescriptor}
     * @param qualifierAnnotationExtractor a {@link Function} to extract {@link Qualifier} {@link AnnotationTypeUsage}s
     * @return a new {@link FieldInjectionPoint}
     */
    public static FieldInjectionPoint of(final JDKTypeDescriptor typeDescriptor,
                                         final FieldDescriptor fieldDescriptor,
                                         final Function<? super Traitable, Stream<AnnotationTypeUsage>> qualifierAnnotationExtractor) {

        return new FieldInjectionPoint(typeDescriptor, fieldDescriptor, qualifierAnnotationExtractor);
    }
}
