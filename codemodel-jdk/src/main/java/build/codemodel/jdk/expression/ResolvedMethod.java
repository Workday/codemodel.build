package build.codemodel.jdk.expression;

/*-
 * #%L
 * JDK Code Model
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

import build.base.foundation.iterator.Iterators;
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.CallableDescriptor;
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Trait} on a {@link MethodInvocation} (or {@link MethodReference}) that identifies the
 * method javac resolved the invocation to: the declaring type, method name, and formal parameter
 * types.
 *
 * <p>{@link #descriptor()} looks the {@link MethodDescriptor} up live against {@link #codeModel()}
 * on every call, the same way a {@link NamedTypeUsage} resolves its {@link TypeName} live, rather
 * than pinning to whatever {@link MethodDescriptor} object existed at parse time. This means the
 * resolution keeps tracking the declaring type across a
 * {@link build.codemodel.jdk.JDKCodeModel#rescan} of that type instead of going stale when the
 * type is evicted and re-created.
 *
 * <p>Exactly one {@link ResolvedMethod} may be present on a {@link MethodInvocation}
 * ({@link Singular}). If the declaring type is not present in the
 * {@link build.codemodel.foundation.CodeModel}, no trait is attached.
 *
 * @param codeModel      the {@link CodeModel} to resolve {@link #descriptor()} against
 * @param declaringType  the {@link TypeName} of the type declaring the resolved method
 * @param methodName     the simple name of the resolved method
 * @param parameterTypes the resolved method's formal parameter types, in declaration order
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@Singular
public record ResolvedMethod(CodeModel codeModel,
                             TypeName declaringType,
                             String methodName,
                             List<TypeUsage> parameterTypes) implements Composite, Trait {

    public ResolvedMethod {
        Objects.requireNonNull(codeModel, "codeModel must not be null");
        Objects.requireNonNull(declaringType, "declaringType must not be null");
        Objects.requireNonNull(methodName, "methodName must not be null");
        parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes must not be null"));
    }

    /**
     * Resolves the {@link MethodDescriptor} this invocation currently refers to.
     *
     * @return the resolved {@link MethodDescriptor}, or {@link Optional#empty()} if the declaring
     * type or the matching method is no longer present in {@link #codeModel()}
     */
    public Optional<MethodDescriptor> descriptor() {
        return codeModel.getTypeDescriptor(declaringType)
            .flatMap(typeDescriptor -> typeDescriptor.traits(MethodDescriptor.class)
                .filter(md -> md.methodName().name().toString().equals(methodName))
                .filter(md -> md.getFormalParameterCount() == parameterTypes.size())
                .filter(this::parametersMatch)
                .findFirst());
    }

    private boolean parametersMatch(final CallableDescriptor callableDescriptor) {
        final var formals = callableDescriptor.formalParameters().toList();
        for (int i = 0; i < formals.size(); i++) {
            if (!typeUsageNamesMatch(formals.get(i).type(), parameterTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean typeUsageNamesMatch(final TypeUsage a, final TypeUsage b) {
        if (a instanceof NamedTypeUsage na && b instanceof NamedTypeUsage nb) {
            return na.typeName().canonicalName().equals(nb.typeName().canonicalName());
        }
        return a.toString().equals(b.toString());
    }

    @Override
    public <T> Iterator<T> iterator(final Class<T> type) {
        return descriptor()
            .filter(type::isInstance)
            .map(type::cast)
            .map(Iterators::of)
            .orElseGet(Iterators::empty);
    }
}
