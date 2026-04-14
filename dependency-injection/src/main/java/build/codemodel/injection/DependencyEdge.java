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
 * An edge in the Track 2 binding graph. Describes a resolved dependency relationship: the
 * {@link InjectionPoint} that required it and the {@link Dependency} that was resolved.
 *
 * <p>Passed to {@link BindingGraphContributor#contributeDependency} each time a dependency is
 * resolved at runtime. In Track 1 the {@code injectionPoint} is {@code null}; Track 2 threads the
 * originating injection point through the resolution path.
 *
 * @param injectionPoint the {@link InjectionPoint} that introduced the dependency, or {@code null}
 *                       in Track 1 (before injection-point threading is wired in)
 * @param dependency     the {@link Dependency} that was resolved
 * @author reed.vonredwitz
 * @see BindingGraphContributor
 * @see BindingNode
 * @since Apr-2026
 */
public record DependencyEdge(InjectionPoint injectionPoint,
                             Dependency dependency) {
}
