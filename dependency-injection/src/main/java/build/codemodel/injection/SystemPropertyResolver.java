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

import build.base.foundation.Strings;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.TypeUsages;
import jakarta.inject.Inject;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Resolver} of values for {@link SystemProperty} annotated {@link Inject}ion points,
 * that uses {@link System#getProperties()} to resolve values.
 *
 * @author brian.oliver
 * @see SystemProperty
 * @since Nov-2017
 */
public class SystemPropertyResolver
    implements Resolver<Object> {

    /**
     * The {@link JDKCodeModel} with which to resolve the {@link SystemProperty} annotations.
     */
    private final JDKCodeModel codeModel;

    /**
     * The {@link TypeName} of the {@link SystemProperty} annotation.
     */
    private final TypeName systemPropertyTypeName;

    /**
     * The {@link TypeName} of the {@link SystemProperty.Default} annotation.
     */
    private final TypeName systemPropertyDefaultTypeName;

    /**
     * Constructs a new {@link SystemPropertyResolver} with the specified {@link JDKCodeModel}.
     *
     * @param codeModel the {@link JDKCodeModel}
     */
    private SystemPropertyResolver(final JDKCodeModel codeModel) {
        this.codeModel = Objects.requireNonNull(codeModel, "The CodeModel must not be null");

        this.systemPropertyTypeName = this.codeModel.getNameProvider()
            .getTypeName(SystemProperty.class);

        this.systemPropertyDefaultTypeName = this.codeModel.getNameProvider()
            .getTypeName(SystemProperty.Default.class);
    }

    @Override
    public Optional<? extends Binding<Object>> resolve(final Dependency dependency) {

        return dependency.typeUsage()
            .traits(AnnotationTypeUsage.class)
            .filter(annotationTypeUsage -> annotationTypeUsage
                .typeName().equals(this.systemPropertyTypeName))
            .findFirst()
            .map(systemPropertyUsage -> {

                final var propertyName = systemPropertyUsage.values()
                    .findFirst()
                    .orElseThrow()
                    .value()
                    .toString();

                // attempt to obtain the default value from the SystemProperty.Default annotation
                final var defaultValue = dependency.typeUsage()
                    .traits(AnnotationTypeUsage.class)
                    .filter(annotationTypeUsage -> annotationTypeUsage
                        .typeName().equals(this.systemPropertyDefaultTypeName))
                    .findFirst()
                    .flatMap(defaultUsage -> defaultUsage.values()
                        .findFirst()
                        .map(value -> value.value().toString()))
                    .orElse(null);

                final var propertyValue = System.getProperty(propertyName, defaultValue);

                return propertyValue == null
                    ? null
                    : TypeUsages.getThreadContextClass(dependency.typeUsage())
                        .map(requiredClass ->
                            new ValueBinding<Object>() {
                                @Override
                                public Dependency dependency() {
                                    return dependency;
                                }

                                @Override
                                public Object value() {
                                    return Strings.convert(propertyValue, requiredClass);
                                }
                            })
                        .orElse(null);
            });
    }

    /**
     * Creates a new {@link SystemPropertyResolver} for the specified {@link JDKCodeModel}.
     *
     * @param codeModel the {@link JDKCodeModel}
     * @return a new {@link SystemPropertyResolver}
     */
    public static SystemPropertyResolver of(final JDKCodeModel codeModel) {
        return new SystemPropertyResolver(codeModel);
    }
}
