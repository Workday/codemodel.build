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

import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.MethodType;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Resolver} of values produced by {@link Provides}-annotated methods on a provider object.
 *
 * <p>At construction, the provider object's type descriptor hierarchy is scanned via the
 * {@link InjectionFramework} for non-static, non-abstract, non-void, no-arg methods annotated with
 * {@link Provides}.  Each such method's return type is registered as a resolvable type.
 * At resolve time, the matching method is invoked and its return value is wrapped in a {@link ValueBinding}.
 *
 * <p>Each call to {@link #resolve} invokes the {@link Provides} method afresh, giving factory-per-resolution
 * semantics (analogous to an unscoped binding in Dagger/Guice).  If singleton semantics are required, the
 * provider object should memoize the value itself.
 *
 * @author reed.vonredwitz
 * @see Provides
 */
public class ProvidesResolver
    implements Resolver<Object> {

    /**
     * The provider object whose {@link Provides} methods are invoked on resolution.
     */
    private final Object providerObject;

    /**
     * Map from return-type canonical name to the corresponding {@link Provides} {@link MethodDescriptor}.
     */
    private final Map<String, MethodDescriptor> methodsByTypeName;

    /**
     * Constructs a {@link ProvidesResolver}.
     *
     * @param providerObject the provider object
     * @param framework      the {@link InjectionFramework} used to scan for {@link Provides} methods
     */
    private ProvidesResolver(final Object providerObject, final InjectionFramework framework) {
        this.providerObject = Objects.requireNonNull(providerObject, "The provider object must not be null");
        Objects.requireNonNull(framework, "The InjectionFramework must not be null");

        this.methodsByTypeName = new LinkedHashMap<>();

        final var codeModel = framework.codeModel();

        codeModel.getJDKTypeDescriptor(providerObject.getClass())
            .ifPresent(typeDescriptor ->
                codeModel.getTraitsInHierarchy(typeDescriptor, MethodDescriptor.class)
                    .filter(framework::isProvides)
                    .filter(md -> md.formalParameters().findAny().isEmpty())
                    .filter(md -> md.returnType() instanceof NamedTypeUsage ntu
                        && !ntu.typeName().canonicalName().equals("void"))
                    .forEach(md -> {
                        final var canonicalName = ((NamedTypeUsage) md.returnType()).typeName().canonicalName();
                        this.methodsByTypeName.putIfAbsent(canonicalName, md);
                    }));
    }

    @Override
    public Optional<? extends Binding<Object>> resolve(final Dependency dependency) {

        if (!(dependency.typeUsage() instanceof NamedTypeUsage namedTypeUsage)) {
            return Optional.empty();
        }

        final var methodDescriptor = this.methodsByTypeName.get(namedTypeUsage.typeName().canonicalName());

        if (methodDescriptor == null) {
            return Optional.empty();
        }

        final var method = methodDescriptor.getTrait(MethodType.class)
            .map(MethodType::method)
            .orElseThrow(() -> new InjectionException("No MethodType trait for @Provides method " + methodDescriptor));

        method.trySetAccessible();

        try {
            final var value = method.invoke(this.providerObject);
            return Optional.of(new ValueBinding<Object>() {
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
        catch (final IllegalAccessException | InvocationTargetException e) {
            throw new InjectionException("@Provides method " + method + " failed", e);
        }
    }

    /**
     * Creates a {@link ProvidesResolver} for the specified provider object.
     *
     * @param providerObject the provider object
     * @param framework      the {@link InjectionFramework}
     * @return a new {@link ProvidesResolver}
     */
    public static ProvidesResolver of(final Object providerObject, final InjectionFramework framework) {
        return new ProvidesResolver(providerObject, framework);
    }
}
