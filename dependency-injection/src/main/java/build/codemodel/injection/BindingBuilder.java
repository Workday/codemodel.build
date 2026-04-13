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
import java.util.function.Predicate;
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

    /**
     * Replaces an existing {@link Binding} for this type with the specified value, or registers a new
     * {@link Binding} if none exists. Unlike {@link #to(Object)}, this does not throw
     * {@link BindingAlreadyExistsException} when a binding is already registered.
     *
     * @param value the value
     * @return the newly registered {@link Binding}
     */
    default Binding<T> toOverriding(final T value) {
        throw new UnsupportedOperationException("toOverriding is not supported by this BindingBuilder");
    }

    /**
     * Replaces an existing {@link Binding} for this type with an instance of the specified concrete
     * {@link Class}, or registers a new {@link Binding} if none exists.
     *
     * @param implementationClass the concrete {@link Class} to bind
     * @return the newly registered {@link Binding}
     */
    default Binding<T> toOverriding(final Class<? extends T> implementationClass) {
        throw new UnsupportedOperationException("toOverriding is not supported by this BindingBuilder");
    }

    /**
     * Replaces an existing {@link Binding} for this type with the specified {@link Supplier}, or registers
     * a new {@link Binding} if none exists.
     *
     * @param supplier the {@link Supplier}
     * @return the newly registered {@link Binding}
     */
    default Binding<T> toOverriding(final Supplier<T> supplier) {
        throw new UnsupportedOperationException("toOverriding is not supported by this BindingBuilder");
    }

    /**
     * Registers a {@link Binding} of the bound value to each non-{@code java.*} interface in its type
     * hierarchy. Equivalent to calling {@link #asAllInterfaces(Predicate)} with a filter that excludes
     * interfaces whose package name starts with {@code "java."}.
     *
     * <p>Example:
     * <pre>{@code
     * context.bind(myService).asAllInterfaces();
     * // equivalent to registering bind(ServiceInterface.class).to(myService) for every user interface
     * }</pre>
     *
     * @throws UnsupportedOperationException if called on a builder that was not created via
     *                                       {@link Context#bind(Object)}
     */
    default void asAllInterfaces() {
        throw new UnsupportedOperationException("asAllInterfaces is not supported by this BindingBuilder");
    }

    /**
     * Registers a {@link Binding} of the bound value to each interface in its type hierarchy that matches
     * the supplied {@link Predicate}.
     *
     * @param filter a {@link Predicate} applied to each interface {@link Class}; only matching interfaces
     *               are bound
     * @throws UnsupportedOperationException if called on a builder that was not created via
     *                                       {@link Context#bind(Object)}
     */
    default void asAllInterfaces(final Predicate<Class<?>> filter) {
        throw new UnsupportedOperationException("asAllInterfaces is not supported by this BindingBuilder");
    }
}
