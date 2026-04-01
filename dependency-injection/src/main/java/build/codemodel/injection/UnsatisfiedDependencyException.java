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
 * Thrown when the value for a {@link Dependency} can not be resolved for injection.
 *
 * @author brian.oliver
 * @since Jun-2018
 */
public class UnsatisfiedDependencyException
    extends InjectionException {

    /**
     * The {@link Dependency} that was not satisfied.
     */
    private final Dependency dependency;

    /**
     * Constructs an {@link UnsatisfiedDependencyException}.
     *
     * @param dependency the {@link Dependency} that could not be satisfied
     * @param cause      the causing {@link Throwable}
     */
    public UnsatisfiedDependencyException(final Dependency dependency,
                                          final Throwable cause) {

        super(String.format("Unable to resolve %s", dependency), cause);

        this.dependency = dependency;
    }

    /**
     * Constructs an {@link UnsatisfiedDependencyException}.
     *
     * @param dependency the {@link Dependency} that could not be satisfied
     */
    public UnsatisfiedDependencyException(final Dependency dependency) {
        super(String.format("Unable to resolve %s", dependency));

        this.dependency = dependency;
    }

    /**
     * Constructs an {@link UnsatisfiedDependencyException}.
     *
     * @param dependency the {@link Dependency} that could not be satisfied
     * @param message    the message
     */
    public UnsatisfiedDependencyException(final Dependency dependency,
                                          final String message) {

        super(String.format("Unable to resolve %s: %s", dependency, message));
        this.dependency = dependency;
    }

    /**
     * Constructs an {@link UnsatisfiedDependencyException}.
     *
     * @param dependency the {@link Dependency} that could not be satisfied
     * @param message    the message
     */
    public UnsatisfiedDependencyException(final Dependency dependency,
                                          final String message,
                                          final Throwable cause) {

        super(String.format("Unable to resolve %s: %s", dependency, message), cause);
        this.dependency = dependency;
    }

    /**
     * Obtains the {@link Dependency} that was not satisfied.
     *
     * @return the {@link Dependency}
     */
    public Dependency dependency() {
        return this.dependency;
    }
}
