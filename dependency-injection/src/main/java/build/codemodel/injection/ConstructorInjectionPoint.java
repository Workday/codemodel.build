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

import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.jdk.descriptor.ConstructorType;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import jakarta.inject.Qualifier;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An {@link InjectionPoint} for a {@link build.codemodel.objectoriented.descriptor.ConstructorDescriptor}.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public class ConstructorInjectionPoint
    implements InjectionPoint {

    /**
     * The {@link JDKTypeDescriptor} in which the {@link ConstructorInjectionPoint} is defined.
     */
    private final JDKTypeDescriptor typeDescriptor;

    /**
     * The {@link ConstructorDescriptor} defining the {@link Method} that may be injected.
     */
    private final ConstructorDescriptor constructorDescriptor;

    /**
     * The {@link Dependency}s defined by the {@link ConstructorDescriptor} for injection.
     */
    private final ArrayList<Dependency> dependencies;

    /**
     * Constructs a {@link ConstructorInjectionPoint}.
     *
     * @param typeDescriptor               the {@link JDKTypeDescriptor}
     * @param constructorDescriptor        the {@link ConstructorDescriptor}
     * @param qualifierAnnotationExtractor a {@link Function} to extract {@link Qualifier} {@link AnnotationTypeUsage}s
     */
    private ConstructorInjectionPoint(final JDKTypeDescriptor typeDescriptor,
                                      final ConstructorDescriptor constructorDescriptor,
                                      final Function<? super Traitable, Stream<AnnotationTypeUsage>> qualifierAnnotationExtractor) {

        this.typeDescriptor = Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null");
        this.constructorDescriptor = Objects.requireNonNull(constructorDescriptor,
            "The ConstructorDescriptor must not be null");
        Objects.requireNonNull(qualifierAnnotationExtractor, "The Qualifier Annotation Extractor must not be null");

        final var formalParams = constructorDescriptor.formalParameters()
            .toArray(FormalParameterDescriptor[]::new);

        this.dependencies = IntStream.range(0, formalParams.length)
            .mapToObj(i -> InjectionPointDependency.of(
                this,
                IndependentDependency.of(formalParams[i].type(), qualifierAnnotationExtractor)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public JDKTypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public Stream<Dependency> dependencies() {
        return this.dependencies.stream();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T inject(final Object target, final Object[] actualParameters) {

        final var constructor = this.constructorDescriptor.getTrait(ConstructorType.class)
            .map(ConstructorType::constructor)
            .orElseThrow(
                () -> new IllegalArgumentException("MethodType is not defined for " + this.constructorDescriptor));

        if (constructor.trySetAccessible()) {
            try {
                return (T) constructor.newInstance(actualParameters);
            }
            catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new InjectionFailedException(this, e);
            }
        }
        else {
            throw new InjectionFailedException(this, "Can't inject into " + constructor + " as it's inaccessible");
        }
    }

    /**
     * Creates a {@link ConstructorInjectionPoint}.
     *
     * @param typeDescriptor               the {@link JDKTypeDescriptor}
     * @param constructorDescriptor        the {@link ConstructorDescriptor}
     * @param qualifierAnnotationExtractor a {@link Function} to extract {@link Qualifier} {@link AnnotationTypeUsage}s
     * @return a new {@link ConstructorInjectionPoint}
     */
    public static ConstructorInjectionPoint of(final JDKTypeDescriptor typeDescriptor,
                                               final ConstructorDescriptor constructorDescriptor,
                                               final Function<? super Traitable, Stream<AnnotationTypeUsage>> qualifierAnnotationExtractor) {

        return new ConstructorInjectionPoint(typeDescriptor, constructorDescriptor, qualifierAnnotationExtractor);
    }
}
