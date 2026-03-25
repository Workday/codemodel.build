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
import build.codemodel.foundation.usage.pattern.GenericTypeUsagePattern;
import jakarta.inject.Provider;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Resolver} of JSR-330 {@link Provider}-based specifications of {@link Dependency}s.
 * <p>
 * NOTE: This {@link Resolver} must be added to {@link Context}s that wish to be used in a JSR-330 compliant manner
 * with {@link Provider} annotations.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public class ProviderResolver
    implements Resolver<Provider<Object>> {

    /**
     * The {@link InjectionFramework} to use for resolving {@link InjectionPoint}s.
     */
    private final InjectionFramework injectionFramework;

    /**
     * The {@link Context} to use to resolve {@link Provider}s.
     */
    private final Context context;

    /**
     * The {@link GenericTypeUsagePattern} to match a Provider&gt;T&lt; {@link Dependency} {@link TypeUsage}.
     */
    private final GenericTypeUsagePattern provideTypeUsagePattern;

    /**
     * Constructs a {@link ProviderResolver} for a specified {@link Context}
     *
     * @param injectionFramework the {@link InjectionFramework}
     * @param context            the {@link Context}
     */
    public ProviderResolver(final InjectionFramework injectionFramework,
                            final Context context) {

        this.injectionFramework = Objects.requireNonNull(injectionFramework, "The InjectionFramework must not be null");
        this.context = Objects.requireNonNull(context, "The Context must not be null");
        this.provideTypeUsagePattern = GenericTypeUsagePattern.of(this.injectionFramework.codeModel()
            .getNameProvider()
            .getTypeName(Provider.class));
    }

    @Override
    public Optional<? extends Binding<Provider<Object>>> resolve(final Dependency dependency) {

        final var self = this;

        // attempt to match the Dependency typeUsage against a Provider<T> type
        final var match = this.provideTypeUsagePattern.match(dependency.typeUsage());

        return match
            .map(providerTypeUsage -> {
                final var requiredTypeUsage = providerTypeUsage
                    .parameters()
                    .findFirst()
                    .orElseThrow(() -> new UnsatisfiedDependencyException(dependency));

                // create a Dependency for the required type with the annotations of the Provided type
                // (thus "promoting them" for the Dependency from Provided<T> to T)
                final var requiredDependency = IndependentDependency.of(
                    requiredTypeUsage,
                    _ -> this.injectionFramework.getQualifierAnnotationTypes(providerTypeUsage));

                return new ValueBinding<Provider<Object>>() {
                    @Override
                    public Dependency dependency() {
                        return requiredDependency;
                    }

                    @Override
                    public Provider<Object> value() {
                        // use the Context to create the T TypeUsage
                        return () -> self.context.create(requiredDependency);
                    }
                };
            });
    }
}
