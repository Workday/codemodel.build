package build.codemodel.foundation.usage.pattern;

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

import build.base.foundation.stream.Streamable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link TypeUsagePattern} for matching {@link TypeUsage}s with {@link AnnotationTypeUsage} {@link Trait}s
 * that satisfy each of the {@link AnnotationTypeUsagePattern}s.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public class AnnotatedTypeUsagePattern
    implements TypeUsagePattern<TypeUsage, AnnotatedTypeUsageMatch> {

    /**
     * The {@link AnnotationTypeUsagePattern}s to match against a {@link TypeUsage}.
     */
    private final ArrayList<AnnotationTypeUsagePattern> annotationTypeUsagePatterns;

    /**
     * Constructs an {@link AnnotatedTypeUsagePattern} for {@link TypeUsage}s with the specified
     * {@link AnnotationTypeUsagePattern}s.
     *
     * @param annotationTypeUsagePatterns the {@link AnnotationTypeUsagePattern}s
     */
    private AnnotatedTypeUsagePattern(final Stream<AnnotationTypeUsagePattern> annotationTypeUsagePatterns) {

        this.annotationTypeUsagePatterns = annotationTypeUsagePatterns == null
            ? new ArrayList<>()
            : annotationTypeUsagePatterns.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public AnnotatedTypeUsageMatch failure() {
        return new AnnotatedTypeUsageMatch() {
            @Override
            public Streamable<AnnotationTypeUsageMatch> matches() {
                return Streamable.empty();
            }

            @Override
            public Optional<TypeUsage> typeUsage() {
                return Optional.empty();
            }
        };
    }

    @Override
    public AnnotatedTypeUsageMatch match(final TypeUsage typeUsage) {

        // collect the AnnotationTypeUsages from the TypeUsage
        final var annotationTypeUsages = typeUsage.traits(AnnotationTypeUsage.class)
            .toList();

        // when there are no AnnotationTypeUsages but there are AnnotationTypeUsagePatterns, we fail
        if (annotationTypeUsages.isEmpty() && !this.annotationTypeUsagePatterns.isEmpty()) {
            return failure();
        }

        // when no AnnotationTypeUsagePatterns are specified, we match all TypeUsages
        if (this.annotationTypeUsagePatterns.isEmpty()) {
            return new AnnotatedTypeUsageMatch() {
                @Override
                public Streamable<AnnotationTypeUsageMatch> matches() {
                    return Streamable.empty();
                }

                @Override
                public Optional<TypeUsage> typeUsage() {
                    return Optional.of(typeUsage);
                }
            };
        }

        // collect the AnnotationTypeUsageMatches for each of the AnnotationTypeUsages
        // that match each of the AnnotationTypeUsagePatterns
        final var matches = new HashMap<AnnotationTypeUsagePattern, List<AnnotationTypeUsageMatch>>();

        this.annotationTypeUsagePatterns
            .forEach(annotationTypeUsagePattern -> annotationTypeUsages.stream()
                .map(annotationTypeUsagePattern::match)
                .filter(AnnotationTypeUsageMatch::isPresent)
                .forEach(annotationTypeUsageMatch -> matches
                    .computeIfAbsent(annotationTypeUsagePattern, k -> new ArrayList<>())
                    .add(annotationTypeUsageMatch)));

        if (this.annotationTypeUsagePatterns.size() != matches.size()) {
            return failure();
        }

        // collect the AnnotationTypeUsageMatches
        final var streamable = Streamable.of(matches.values().stream()
            .flatMap(List::stream));

        return new AnnotatedTypeUsageMatch() {
            @Override
            public Optional<TypeUsage> typeUsage() {
                return Optional.of(typeUsage);
            }

            @Override
            public Streamable<AnnotationTypeUsageMatch> matches() {
                return streamable;
            }
        };
    }

    /**
     * Creates an {@link AnnotatedTypeUsagePattern} for {@link TypeUsage}s with the specified
     * {@link AnnotationTypeUsagePattern}s.
     *
     * @param annotationTypeUsagePatterns the {@link AnnotationTypeUsagePattern}s
     * @return a new {@link AnnotatedTypeUsagePattern}
     */
    public static AnnotatedTypeUsagePattern of(final AnnotationTypeUsagePattern... annotationTypeUsagePatterns) {

        return new AnnotatedTypeUsagePattern(annotationTypeUsagePatterns == null
            ? Stream.empty()
            : Stream.of(annotationTypeUsagePatterns));
    }

    /**
     * Creates an {@link AnnotatedTypeUsagePattern} for {@link TypeUsage}s with one or more {@link AnnotationTypeUsage}s.
     *
     * @return a new {@link AnnotatedTypeUsagePattern}
     */
    public static AnnotatedTypeUsagePattern any() {
        return new AnnotatedTypeUsagePattern(Stream.empty());
    }
}
