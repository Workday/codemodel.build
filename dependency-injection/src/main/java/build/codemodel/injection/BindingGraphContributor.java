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

/**
 * Observes binding registration and dependency resolution events in an {@link InjectionContext}.
 * The Track 1 default is {@link #NOOP}. Track 2 provides a real implementation that builds and
 * maintains a {@code BindingGraphTrait} on the {@link InjectionFramework}'s {@code JDKTypeDescriptor}.
 *
 * <p>Register a contributor via {@link InjectionFramework#setBindingGraphContributor}. Every
 * context created by that framework will call through to it; switching from the NOOP to a real
 * implementation requires no changes to {@link InjectionContext}.
 *
 * @author reed.vonredwitz
 * @see BindingNode
 * @see DependencyEdge
 * @see InjectionFramework#setBindingGraphContributor
 * @since Apr-2026
 */
public interface BindingGraphContributor {

    /**
     * Called each time a {@link Binding} is registered in an {@link InjectionContext} (including
     * override registrations via {@code toOverriding}).
     *
     * @param node a description of the registered binding
     */
    void contributeBinding(BindingNode node);

    /**
     * Called each time a {@link Dependency} is resolved during injection. The {@code from} node is
     * the binding whose injection point required the dependency; {@code to} is the binding that
     * satisfied it.
     *
     * <p>In Track 1, {@link DependencyEdge#injectionPoint()} is {@code null} and the {@code from}
     * / {@code to} nodes may be {@code null} for auto-resolved or multibinding dependencies that
     * are not registered as explicit class bindings.
     *
     * @param from the binding that required the dependency, or {@code null} if not determinable
     * @param to   the binding that satisfied the dependency, or {@code null} if not determinable
     * @param edge the edge, carrying the originating injection point and the resolved dependency
     */
    void contributeDependency(BindingNode from, BindingNode to, DependencyEdge edge);

    /**
     * No-op implementation used until Track 2 installs a real contributor.
     */
    BindingGraphContributor NOOP = new BindingGraphContributor() {
        @Override
        public void contributeBinding(final BindingNode node) {
        }

        @Override
        public void contributeDependency(final BindingNode from, final BindingNode to, final DependencyEdge edge) {
        }
    };
}
