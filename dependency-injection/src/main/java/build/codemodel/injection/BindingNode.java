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

import build.codemodel.jdk.descriptor.JDKTypeDescriptor;

import java.lang.annotation.Annotation;

/**
 * A vertex in the Track 2 binding graph. Describes a single registered {@link Binding}: the type
 * it produces, the scope it operates under, the {@link Module} that declared it (if any), and its
 * {@link BindingKind}.
 *
 * <p>Instances are constructed by {@link InjectionContext} and passed to
 * {@link BindingGraphContributor#contributeBinding}. Fields that cannot be populated in Track 1
 * (e.g. {@code declaringModule} when no module tracking is in place) are {@code null}.
 *
 * @param typeDescriptor  the {@link JDKTypeDescriptor} for the concrete type, or {@code null} for
 *                        value/supplier bindings where no class is directly tracked
 * @param scope           the scope annotation that governs this binding (e.g. {@code Singleton.class}),
 *                        or {@code null} for prototype-scoped bindings
 * @param declaringModule the {@link Module} subclass that registered this binding, or {@code null}
 *                        when the binding was not registered through a module (Track 1 always passes
 *                        {@code null}; Track 2 fills this in)
 * @param kind            how this binding produces its value
 * @author reed.vonredwitz
 * @see BindingGraphContributor
 * @see DependencyEdge
 * @since Apr-2026
 */
public record BindingNode(JDKTypeDescriptor typeDescriptor,
                          Class<? extends Annotation> scope,
                          Class<? extends Module> declaringModule,
                          BindingKind kind) {
}
