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
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.TypeUsage;
import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * An abstract {@link BindingBuilder}.
 *
 * @param <T> the type of {@link Object}
 * @author brian.oliver
 * @since Oct-2024
 */
public abstract class AbstractBindingBuilder<T>
    implements BindingBuilder<T> {

    /**
     * The {@link InjectionFramework}.
     */
    protected final InjectionFramework injectionFramework;

    /**
     * The {@link TypeUsage}.
     */
    protected final TypeUsage typeUsage;

    /**
     * Constructs the {@link AbstractBindingBuilder}.
     *
     * @param injectionFramework the {@link InjectionFramework}
     * @param typeUsage          the {@link TypeUsage}
     */
    protected AbstractBindingBuilder(final InjectionFramework injectionFramework,
                                     final TypeUsage typeUsage) {

        this.injectionFramework = Objects.requireNonNull(injectionFramework, "The Injection must not be null");
        this.typeUsage = Objects.requireNonNull(typeUsage, "The TypeUsage must not be null");
    }

    /**
     * Obtains the currently configured {@link TypeUsage} for the {@link BindingBuilder}.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage typeUsage() {
        return this.typeUsage;
    }

    @Override
    public BindingBuilder<T> as(final String name) {
        if (name != null && !name.isEmpty()) {

            final var codeModel = this.typeUsage.codeModel();
            final var namingProvider = codeModel.getNameProvider();
            final var namedTypeName = namingProvider.getTypeName(Named.class);

            final var named = AnnotationTypeUsage.of(
                codeModel,
                namedTypeName,
                AnnotationValue.of(codeModel, "value", name));

            // TODO: ensure the Name annotation isn't already added to the Trait

            this.typeUsage.addTrait(named);
        }

        return this;
    }

    @Override
    public BindingBuilder<T> with(final Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(annotationClass, "The Annotation Class must not be null");

        final var codeModel = typeUsage.codeModel();
        final var namingProvider = codeModel.getNameProvider();
        final var annotationTypeName = namingProvider.getTypeName(annotationClass);

        return with(AnnotationTypeUsage.of(codeModel, annotationTypeName));
    }

    @Override
    public BindingBuilder<T> with(final Annotation annotation) {
        Objects.requireNonNull(annotation, "The Annotation must not be null");

        return with(this.injectionFramework.codeModel().getAnnotation(annotation));
    }

    @Override
    public BindingBuilder<T> with(final AnnotationTypeUsage annotationTypeUsage) {
        Objects.requireNonNull(annotationTypeUsage, "The AnnotationTypeUsage must not be null");

        this.typeUsage.addTrait(annotationTypeUsage);

        return this;
    }
}
