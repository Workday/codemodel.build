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

import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import jakarta.inject.Qualifier;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines a {@link TypeUsage} for <i>Dependency Injection</i> with an {@link Injector}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public interface Dependency {

    /**
     * Obtains the {@link TypeUsage} for the {@link Dependency}.
     *
     * @return the {@link TypeUsage}
     */
    TypeUsage typeUsage();

    /**
     * Obtains the unique signature for the {@link Dependency}, consisting of the {@link TypeName} and any
     * {@link AnnotationTypeUsage}s that have been annotated with the {@link Qualifier} meta-annotation.
     *
     * @return the <i>Signature</i> for the {@link Dependency}
     */
    String signature();

    /**
     * Computes the canonical signature for a {@link TypeUsage} and a set of qualifier {@link AnnotationTypeUsage}s,
     * consisting of the {@link TypeUsage}'s {@link TypeUsage#canonicalName()} (recursively including any generic
     * parameters) followed by the qualifier annotations, sorted and rendered so that the signature is stable
     * regardless of the order in which the qualifiers were declared.
     *
     * <p>Anywhere two things must be recognized as the "same qualified type" (an {@link IndependentDependency}
     * and a {@link Provides} method it may resolve to, for example) must derive their signature from this method,
     * so the two stay comparable.
     *
     * @param typeUsage            the {@link TypeUsage}
     * @param qualifierAnnotations the {@link Qualifier} {@link AnnotationTypeUsage}s
     * @return the canonical signature
     * @throws DuplicateQualifierException if more than one {@link AnnotationTypeUsage} of the same qualifier
     *                                     annotation type is present, making the signature ambiguous
     */
    static String signatureOf(final TypeUsage typeUsage,
                              final Stream<? extends AnnotationTypeUsage> qualifierAnnotations) {

        final var sortedQualifiers = qualifierAnnotations.sorted().toList();

        // reject more than one qualifier annotation of the same type (e.g. two differently-valued @Named
        // annotations), as that would make the qualifier ambiguous
        sortedQualifiers.stream()
            .collect(Collectors.groupingBy(AnnotationTypeUsage::typeName))
            .values()
            .stream()
            .filter(duplicates -> duplicates.size() > 1)
            .findFirst()
            .ifPresent(duplicates -> {
                throw new DuplicateQualifierException(
                    "The TypeUsage [" + typeUsage.canonicalName() + "] defines more than one ["
                        + duplicates.getFirst().typeName() + "] qualifier: " + duplicates);
            });

        return sortedQualifiers.isEmpty()
            ? typeUsage.canonicalName()
            : typeUsage.canonicalName()
              + sortedQualifiers.stream()
            .map(Object::toString)
            .collect(Collectors.joining(" ", " ", ""));
    }
}
