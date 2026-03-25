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

import build.codemodel.foundation.usage.AnnotationTypeUsage;
import jakarta.inject.Inject;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A {@link Resolver} to resolve to a specific non-{@code null} {@link Object} if and only if a requested
 * {@link Dependency} satisfies an {@link AnnotationTypeUsage} {@link Predicate}.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public class QualifiedResolver<T>
    implements Resolver<T> {

    /**
     * The {@link AnnotationTypeUsage} {@link Predicate} to be satisfied for the {@link Dependency} to be resolved
     * to the {@link Object}.
     */
    private final Predicate<? super AnnotationTypeUsage> predicate;

    /**
     * The non-{@code null} {@link Object} to inject.
     */
    private final T object;

    /**
     * Constructs a {@link QualifiedResolver}.
     *
     * @param predicate the {@link AnnotationTypeUsage} {@link Predicate} to be satisfied
     * @param object    the non-{@code null} {@link Object} to inject
     */
    private QualifiedResolver(final Predicate<? super AnnotationTypeUsage> predicate,
                              final T object) {

        this.predicate = Objects.requireNonNull(predicate, "Then AnnotationTypeUsage Predicate must not be null");
        this.object = Objects.requireNonNull(object, "The Object to inject must not be null");
    }

    @Override
    public Optional<? extends Binding<T>> resolve(final Dependency dependency) {

        return dependency.typeUsage()
            .traits(AnnotationTypeUsage.class)
            .anyMatch(this.predicate)
            ? Optional.of(new ValueBinding<T>() {
            @Override
            public T value() {
                return QualifiedResolver.this.object;
            }

            @Override
            public Dependency dependency() {
                return dependency;
            }
        })
            : Optional.empty();
    }

    /**
     * Creates a new {@link QualifiedResolver} that resolves {@link Inject}able {@link Dependency}s to the provided
     * non-{@code null} {@link Object} when the {@link Dependency#typeUsage()} satisfies specified
     * {@link AnnotationTypeUsage} {@link Predicate}.
     *
     * @param <T>       the type of the {@link Object} to inject
     * @param predicate the {@link AnnotationTypeUsage} {@link Predicate} to be satisfied
     * @param object    the non-{@code null} {@link Object} to inject
     * @return a new {@link QualifiedResolver}
     */
    public static <T> QualifiedResolver<T> of(final Predicate<? super AnnotationTypeUsage> predicate,
                                              final T object) {

        return new QualifiedResolver<>(predicate, object);
    }

    /**
     * Creates a new {@link QualifiedResolver} that resolves {@link Inject}able {@link Dependency}s to the provided
     * non-{@code null} {@link Object} when the {@link Dependency#typeUsage()} is annotated with the specified
     * {@link Annotation} class.
     *
     * @param <T>             the type of the {@link Object} to inject
     * @param annotationClass the {@link Class} of the {@link Annotation} to match
     * @param object          the non-{@code null} {@link Object} to inject
     * @return a new {@link QualifiedResolver}
     */
    public static <T> QualifiedResolver<T> of(final Class<? extends Annotation> annotationClass,
                                              final T object) {

        return of(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .equals(annotationTypeUsage.codeModel().getNameProvider().getTypeName(annotationClass)),
            object);
    }
}
