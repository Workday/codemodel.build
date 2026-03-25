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

import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;

/**
 * A {@link Dependency} defined by an {@link InjectionPoint}.
 *
 * @author brian.oliver
 * @see IndependentDependency
 * @since Jan-2025
 */
public final class InjectionPointDependency
    extends AbstractDependency {

    /**
     * The {@link InjectionPoint} defining the {@link Dependency}.
     */
    private final InjectionPoint injectionPoint;

    /**
     * The {@link Dependency} defined by the {@link InjectionPoint}.
     */
    private final Dependency dependency;

    /**
     * Constructs a {@link InjectionPointDependency}}.
     *
     * @param injectionPoint the {@link InjectionPoint}
     * @param dependency     the {@link Dependency}
     */
    private InjectionPointDependency(final InjectionPoint injectionPoint,
                                     final Dependency dependency) {

        this.injectionPoint = Objects.requireNonNull(injectionPoint, "The InjectionPoint must not be null");
        this.dependency = Objects.requireNonNull(dependency, "The Dependency must not be null");
    }

    /**
     * Obtains the {@link InjectionPoint} for which the {@link Dependency} was established.
     *
     * @return the {@link InjectionPoint}
     */
    public InjectionPoint injectionPoint() {
        return this.injectionPoint;
    }

    @Override
    public TypeUsage typeUsage() {
        return this.dependency.typeUsage();
    }

    @Override
    public String signature() {
        return this.dependency.signature();
    }

    /**
     * Creates a {@link InjectionPointDependency}.
     *
     * @param injectionPoint the {@link InjectionPoint}
     * @param dependency     the {@link Dependency}
     * @return a new {@link InjectionPointDependency}
     */
    public static InjectionPointDependency of(final InjectionPoint injectionPoint,
                                              final Dependency dependency) {

        return new InjectionPointDependency(injectionPoint, dependency);
    }
}
