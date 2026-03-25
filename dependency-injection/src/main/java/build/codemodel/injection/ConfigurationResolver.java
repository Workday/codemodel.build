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

import build.base.configuration.Configuration;
import build.base.configuration.Option;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.TypeUsages;

import java.util.Optional;

/**
 * A {@link Resolver} of {@link Configuration} {@link Option}s.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
public class ConfigurationResolver
    implements Resolver<Object> {

    /**
     * The underlying {@link Configuration} from which to resolve {@link Option}s.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link ConfigurationResolver}.
     *
     * @param configuration the {@link Configuration}
     */
    private ConfigurationResolver(final Configuration configuration) {
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<? extends Binding<Object>> resolve(final Dependency dependency) {

        if (dependency.typeUsage() instanceof NamedTypeUsage namedTypeUsage) {
            try {
                // determine the type required by the Dependency
                final var requiredClass = TypeUsages.getThreadContextClass(namedTypeUsage)
                    .orElseThrow(() -> new ClassNotFoundException("Could not resolve class for " + namedTypeUsage));

                // when it's a Configuration, use the Configuration
                if (requiredClass.isInstance(this.configuration)) {
                    return Optional.of(new ValueBinding<Object>() {
                        @Override
                        public Object value() {
                            return ConfigurationResolver.this.configuration;
                        }

                        @Override
                        public Dependency dependency() {
                            return dependency;
                        }
                    });
                }

                // when it's an Option, use the Configuration to look it up
                if (Option.class.isAssignableFrom(requiredClass)) {
                    return this.configuration.getOptional((Class<? extends Option>) requiredClass)
                        .map(option -> new ValueBinding<Object>() {
                            @Override
                            public Object value() {
                                return option;
                            }

                            @Override
                            public Dependency dependency() {
                                return dependency;
                            }
                        });
                }
            }
            catch (final ClassNotFoundException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a {@link ConfigurationResolver}.
     *
     * @param configuration the {@link Configuration}
     */
    public static ConfigurationResolver of(final Configuration configuration) {
        return new ConfigurationResolver(configuration);
    }
}
