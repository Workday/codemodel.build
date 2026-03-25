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
 * Thrown when a cyclic dependency has been detected between {@link Class}es.
 *
 * @author brian.oliver
 * @since Jun-2018
 */
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
    }

    @Override
    public String getMessage() {
        return "Detected a Cyclic Dependency. " + this.detectedInDependency + " defines a "
            + (this.detectedInDependency == this.causedByDependency
            ? "dependency on itself."
            : "(transitive) dependency on " + this.causedByDependency + ", which defines a dependency on "
                + this.detectedInDependency);
    }
}
