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

import build.base.configuration.Default;
import build.base.configuration.Option;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.TypeUsages;
import build.codemodel.jdk.descriptor.ConstructorType;
import build.codemodel.jdk.descriptor.FieldType;
import build.codemodel.jdk.descriptor.MethodType;
import build.codemodel.jdk.descriptor.Static;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * A {@link Resolver} that instantiates {@link Option} subclasses via their {@link Default}-annotated
 * factory (no-arg constructor, static method, or static field) when no other resolver provides them.
 *
 * <p>This is the fallback resolver for {@link Option} types: if the primary {@link ConfigurationResolver}
 * returns empty (because the {@link build.base.configuration.Configuration} bag doesn't contain that option),
 * this resolver creates a default instance using the {@link Default} annotation.
 *
 * @author reed.vonredwitz
 * @see Default
 * @see ConfigurationResolver
 */
public class DefaultOptionResolver
    implements Resolver<Object> {

    /**
     * The canonical name of the {@link Default} annotation, used to match {@link AnnotationTypeUsage} traits.
     */
    private static final String DEFAULT_ANNOTATION_NAME = Default.class.getCanonicalName();

    /**
     * The {@link InjectionFramework} used to obtain {@link build.codemodel.jdk.descriptor.JDKTypeDescriptor}s.
     */
    private final InjectionFramework framework;

    /**
     * Constructs a {@link DefaultOptionResolver}.
     *
     * @param framework the {@link InjectionFramework}
     */
    private DefaultOptionResolver(final InjectionFramework framework) {
        this.framework = framework;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<? extends Binding<Object>> resolve(final Dependency dependency) {

        if (!(dependency.typeUsage() instanceof NamedTypeUsage namedTypeUsage)) {
            return Optional.empty();
        }

        try {
            final var requiredClass = TypeUsages.getThreadContextClass(namedTypeUsage)
                .orElseThrow(() -> new ClassNotFoundException("Could not resolve class for " + namedTypeUsage));

            if (!Option.class.isAssignableFrom(requiredClass) || requiredClass.equals(Option.class)) {
                return Optional.empty();
            }

            final var instance = instantiateDefault((Class<? extends Option>) requiredClass);
            return instance.map(value -> new ValueBinding<Object>() {
                @Override
                public Object value() {
                    return value;
                }

                @Override
                public Dependency dependency() {
                    return dependency;
                }
            });
        }
        catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to instantiate an {@link Option} subclass via its {@link Default}-annotated factory.
     *
     * <p>Priority order:
     * <ol>
     *   <li>No-arg constructor annotated with {@link Default}</li>
     *   <li>Public static method annotated with {@link Default} returning the type</li>
     *   <li>Public static field annotated with {@link Default} of the same type</li>
     * </ol>
     *
     * @param optionClass the {@link Option} subclass
     * @return the instantiated option, or {@link Optional#empty()} if no {@link Default} factory found
     */
    private Optional<Object> instantiateDefault(final Class<? extends Option> optionClass) {

        final var typeDescriptor = this.framework.codeModel()
            .getJDKTypeDescriptor(optionClass)
            .orElse(null);

        if (typeDescriptor == null) {
            return Optional.empty();
        }

        // 1. no-arg constructor annotated with @Default
        final var defaultConstructor = typeDescriptor.declaredConstructors()
            .filter(cd -> cd.formalParameters().findAny().isEmpty())
            .filter(cd -> cd.traits(AnnotationTypeUsage.class)
                .anyMatch(a -> a.typeName().canonicalName().equals(DEFAULT_ANNOTATION_NAME)))
            .findFirst()
            .flatMap(cd -> cd.getTrait(ConstructorType.class))
            .map(ConstructorType::constructor)
            .orElse(null);

        if (defaultConstructor != null) {
            try {
                defaultConstructor.trySetAccessible();
                return Optional.of(defaultConstructor.newInstance());
            }
            catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new InjectionException("@Default constructor failed for " + optionClass, e);
            }
        }

        // 2. static method annotated with @Default
        final var defaultMethod = typeDescriptor.declaredMethods()
            .filter(md -> md.getTrait(Static.class).isPresent())
            .filter(md -> md.formalParameters().findAny().isEmpty())
            .filter(md -> md.traits(AnnotationTypeUsage.class)
                .anyMatch(a -> a.typeName().canonicalName().equals(DEFAULT_ANNOTATION_NAME)))
            .findFirst()
            .flatMap(md -> md.getTrait(MethodType.class))
            .map(MethodType::method)
            .filter(m -> optionClass.isAssignableFrom(m.getReturnType()))
            .orElse(null);

        if (defaultMethod != null) {
            try {
                defaultMethod.trySetAccessible();
                return Optional.of(defaultMethod.invoke(null));
            }
            catch (final IllegalAccessException | InvocationTargetException e) {
                throw new InjectionException("@Default method " + defaultMethod + " failed", e);
            }
        }

        // 3. static field annotated with @Default
        // NOTE: field annotations are stamped onto the field's TypeUsage by JDKCodeModel, not the FieldDescriptor
        final var defaultField = typeDescriptor.declaredFields()
            .filter(fd -> fd.getTrait(Static.class).isPresent())
            .filter(fd -> fd.type().traits(AnnotationTypeUsage.class)
                .anyMatch(a -> a.typeName().canonicalName().equals(DEFAULT_ANNOTATION_NAME)))
            .findFirst()
            .flatMap(fd -> fd.getTrait(FieldType.class))
            .map(FieldType::field)
            .filter(f -> optionClass.isAssignableFrom(f.getType()))
            .orElse(null);

        if (defaultField != null) {
            try {
                defaultField.trySetAccessible();
                return Optional.of(defaultField.get(null));
            }
            catch (final IllegalAccessException e) {
                throw new InjectionException("@Default field " + defaultField + " failed", e);
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a {@link DefaultOptionResolver}.
     *
     * @param framework the {@link InjectionFramework}
     * @return a new {@link DefaultOptionResolver}
     */
    public static DefaultOptionResolver of(final InjectionFramework framework) {
        return new DefaultOptionResolver(framework);
    }
}
