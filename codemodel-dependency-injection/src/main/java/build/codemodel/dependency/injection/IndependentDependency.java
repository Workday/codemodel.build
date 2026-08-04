package build.codemodel.dependency.injection;

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
import build.codemodel.foundation.usage.TypeUsage;
import jakarta.inject.Qualifier;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link Dependency} independently defined outside the scope of an {@link InjectionPoint}.
 *
 * @author brian.oliver
 * @see InjectionPointDependency
 * @since Jan-2025
 */
public final class IndependentDependency
    extends AbstractDependency {

    /**
     * The {@link TypeUsage} defining the {@link Dependency}.
     */
    private final TypeUsage typeUsage;

    /**
     * The unique signature for the {@link IndependentDependency}, consisting of the {@link TypeUsage}'s
     * {@link TypeUsage#canonicalName()} (recursively including any generic parameters) and any
     * {@link AnnotationTypeUsage}s that have been annotated with the {@link Qualifier} meta-annotation.
     */
    private final String signature;

    /**
     * Constructs a {@link IndependentDependency} given a {@link TypeUsage}.
     *
     * @param typeUsage                    the {@link TypeUsage}
     * @param qualifierAnnotationExtractor a {@link Function} to extract {@link Qualifier} {@link AnnotationTypeUsage}s
     */
    private IndependentDependency(final TypeUsage typeUsage,
                                  final Function<? super Traitable, Stream<AnnotationTypeUsage>> qualifierAnnotationExtractor) {

        this.typeUsage = Objects.requireNonNull(typeUsage, "The TypeUsage must not be null");
        Objects.requireNonNull(qualifierAnnotationExtractor, "The Qualifier Annotation Extractor must not be null");

        // the signature includes the type usage's full canonical name (recursively rendering any generic
        // parameters, e.g. "java.util.List<com.example.Person>", so differently-parameterized usages of the
        // same raw type - such as List<Person> and List<Car> - never collide) and ordered qualifier
        // annotations; throws DuplicateQualifierException if the TypeUsage carries more than one qualifier
        // annotation of the same type (e.g. two differently-valued @Named annotations from repeated
        // BindingBuilder#as(String) calls), as that would make the qualifier ambiguous
        this.signature = Dependency.signatureOf(typeUsage, qualifierAnnotationExtractor.apply(typeUsage));
    }

    /**
     * Obtains the {@link TypeUsage} for the {@link IndependentDependency}.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage typeUsage() {
        return this.typeUsage;
    }

    @Override
    public String signature() {
        return this.signature;
    }

    /**
     * Creates a {@link IndependentDependency} for the specified {@link TypeUsage}.
     *
     * @param typeUsage                    the {@link TypeUsage}
     * @param qualifierAnnotationExtractor a {@link Function} to extract {@link Qualifier} {@link AnnotationTypeUsage}s
     * @return a new {@link IndependentDependency}
     */
    public static IndependentDependency of(final TypeUsage typeUsage,
                                           final Function<? super Traitable, Stream<AnnotationTypeUsage>> qualifierAnnotationExtractor) {

        return new IndependentDependency(typeUsage, qualifierAnnotationExtractor);
    }
}
