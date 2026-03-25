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
import jakarta.inject.Named;
import jakarta.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * A fluent builder for {@link Binding}s.
 *
 * @param <T> the type of {@link Object} that is injectable
 * @author brian.oliver
 * @since Oct-2024
 */
public interface BindingBuilder<T> {

    /**
     * Creates a {@link Binding} that uses the specified value for injection.
     *
     * @param value the value
     * @return the newly created {@link Binding}
     */
    Binding<T> to(T value);

    /**
     * Creates a {@link Binding} that uses an instance of the specified concrete {@link Class} to
     * construct values for injection.
     *
     * @param bindingValueClass the {@link Class} of value to bind
     * @return the newly created {@link Binding}
     */
    Binding<T> to(Class<? extends T> bindingValueClass);

    /**
     * Creates a {@link Binding} that uses the specified {@link Supplier} for acquiring values for injection.
     *
     * @param supplier the {@link Supplier}
     * @return a newly created {@link Binding}
     */
    Binding<T> to(Supplier<T> supplier);

    /**
     * Configures the {@link BindingBuilder} to create {@link Binding}s injectable with {@link Dependency}s
     * that require named values, the name being specified using the {@link Named} annotation.
     *
     * @param name the name
     * @return this {@link BindingBuilder} to permit fluent-style method invocation
     */
    BindingBuilder<T> as(String name);

    /**
     * Configures the {@link BindingBuilder} to create {@link Binding}s requiring the specified Marker {@link Qualifier}
     * {@link Annotation}.
     *
     * @param annotationClass the {@link Class} of {@link Annotation}
     * @return this {@link BindingBuilder} to permit fluent-style method invocation
     */
    BindingBuilder<T> with(Class<? extends Annotation> annotationClass);

    /**
     * Configures the {@link BindingBuilder} to create {@link Binding}s requiring the specified {@link Qualifier}
     * {@link Annotation}.
     *
     * @param annotation the {@link Annotation}
     * @return this {@link BindingBuilder} to permit fluent-style method invocation
     */
    BindingBuilder<T> with(Annotation annotation);

    /**
     * Configures the {@link BindingBuilder} to create {@link Binding}s requiring the specified
     * {@link AnnotationTypeUsage}.
     *
     * @param qualifier the {@link AnnotationTypeUsage}
     * @return this {@link BindingBuilder} to permit fluent-style method invocation
     */
    BindingBuilder<T> with(AnnotationTypeUsage qualifier);
}
