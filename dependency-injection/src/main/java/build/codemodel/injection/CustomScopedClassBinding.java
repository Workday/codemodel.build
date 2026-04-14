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

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * A {@link ClassBinding} whose instance lifecycle is managed by a custom {@link Scope} (i.e. any
 * scope other than {@link jakarta.inject.Singleton} or prototype). Wraps the {@link ValueBinding}
 * returned by {@link Scope#scope} and exposes the concrete class and scope annotation for
 * introspection by {@link Context#validate()} and {@link BindingGraphContributor}.
 *
 * <p>Used by {@link InjectionContext} when {@link InjectionFramework#findScopeEntry} matches a
 * registered custom scope on the concrete class. Instances are resolved via the delegate, which
 * applies the scope's caching or sharing strategy.
 *
 * @param <T> the type produced by this binding
 * @author reed.vonredwitz
 * @see Scope
 * @see ScopeAnnotation
 * @since Apr-2026
 */
class CustomScopedClassBinding<T>
    extends AbstractBinding<T>
    implements ClassBinding<T>, ValueBinding<T> {

    /**
     * The concrete class to be instantiated by the scope.
     */
    private final Class<? extends T> concreteClass;

    /**
     * The scope annotation that governs this binding's lifecycle.
     */
    private final Class<? extends Annotation> scopeAnnotation;

    /**
     * The scoped {@link ValueBinding} returned by {@link Scope#scope}. Applies the scope's
     * caching or sharing strategy on each {@link #value()} call.
     */
    private final ValueBinding<T> delegate;

    /**
     * Constructs a {@link CustomScopedClassBinding}.
     *
     * @param dependency      the {@link Dependency} this binding satisfies
     * @param concreteClass   the concrete class whose instances the scope manages
     * @param scopeAnnotation the scope annotation that governs the lifecycle
     * @param scopedBinding   the {@link Binding} returned by {@link Scope#scope}; must implement
     *                        {@link ValueBinding}
     * @throws InjectionException if {@code scopedBinding} does not implement {@link ValueBinding}
     */
    @SuppressWarnings("unchecked")
    CustomScopedClassBinding(final Dependency dependency,
                             final Class<? extends T> concreteClass,
                             final Class<? extends Annotation> scopeAnnotation,
                             final Binding<?> scopedBinding) {

        super(dependency);
        this.concreteClass = Objects.requireNonNull(concreteClass, "The concrete class must not be null");
        this.scopeAnnotation = Objects.requireNonNull(scopeAnnotation, "The scope annotation must not be null");

        if (!(scopedBinding instanceof ValueBinding)) {
            throw new InjectionException(
                "Scope.scope() must return a ValueBinding for dependency [" + dependency + "]; "
                    + "got: " + (scopedBinding == null ? "null" : scopedBinding.getClass().getName()));
        }
        this.delegate = (ValueBinding<T>) scopedBinding;
    }

    @Override
    public Class<? extends T> concreteClass() {
        return this.concreteClass;
    }

    /**
     * The scope annotation that governs this binding's instance lifecycle.
     *
     * @return the scope annotation class
     */
    Class<? extends Annotation> scopeAnnotation() {
        return this.scopeAnnotation;
    }

    @Override
    public T value() {
        return this.delegate.value();
    }
}
