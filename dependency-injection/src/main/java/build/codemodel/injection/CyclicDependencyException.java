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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when a cyclic dependency has been detected between {@link Class}es.
 *
 * @author brian.oliver
 * @since Jun-2018
 */
@SuppressWarnings("java:S2166")
public class CyclicDependencyException
    extends InjectionException {

    /**
     * The {@link Class} from which the cycle was detected.
     */
    private final Dependency detectedInDependency;

    /**
     * The {@link Class} that causes the cycle.
     */
    private final Dependency causedByDependency;

    /**
     * The full cycle path produced by static graph analysis, or {@code null} when detected at runtime.
     */
    private final List<Dependency> cyclePath;

    /**
     * Constructs a {@link CyclicDependencyException} based on a {@link Dependency} that has a {@link Dependency} on
     * itself.
     *
     * @param dependency the {@link Dependency}
     */
    public CyclicDependencyException(final Dependency dependency) {
        this(dependency, dependency);
    }

    /**
     * Constructs a {@link CyclicDependencyException}.
     *
     * @param detectedInDependency the {@link Dependency} on which the cycle was detected
     * @param causedByDependency   the {@link Dependency} that causes the cycle
     */
    public CyclicDependencyException(final Dependency detectedInDependency, final Dependency causedByDependency) {
        this.detectedInDependency = detectedInDependency;
        this.causedByDependency = causedByDependency;
        this.cyclePath = null;
    }

    /**
     * Constructs a {@link CyclicDependencyException} with the full cycle path discovered by static graph
     * analysis (e.g. from {@link Context#validate()}). The path starts and ends with the same
     * {@link Dependency} (e.g. {@code [A, B, C, A]}).
     *
     * @param cyclePath the full cycle path including the repeated start/end node
     */
    public CyclicDependencyException(final List<Dependency> cyclePath) {
        this.cyclePath = List.copyOf(cyclePath);
        // for backwards-compat fields: use first/last unique nodes in path
        this.detectedInDependency = cyclePath.isEmpty() ? null : cyclePath.getFirst();
        this.causedByDependency = cyclePath.size() > 1 ? cyclePath.get(cyclePath.size() - 2) : this.detectedInDependency;
    }

    @Override
    public String getMessage() {
        if (this.cyclePath != null) {
            return "Cyclic dependency detected: "
                + this.cyclePath.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" \u2192 "));
        }
        return "Detected a Cyclic Dependency. " + this.detectedInDependency + " defines a "
            + (this.detectedInDependency == this.causedByDependency
            ? "dependency on itself."
            : "(transitive) dependency on " + this.causedByDependency + ", which defines a dependency on "
            + this.detectedInDependency);
    }
}
