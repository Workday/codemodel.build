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

import jakarta.inject.Inject;

/**
 * Resolves and injects values into fields, methods and/or constructors annotated with {@link Inject}.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
@FunctionalInterface
public interface Injector {

    /**
     * Resolves and injects values into the specified injectable object.
     * <p>
     * When injection fails an {@link InjectionException} is thrown.  For specific types of injection failures,
     * an appropriate subclass of {@link InjectionException}, like {@link UnsatisfiedDependencyException}, will
     * be thrown.
     *
     * @param <T>        the type of object being injected
     * @param injectable the object in which values are to be injected
     * @return the injected object allowing for fluent-style method calls
     * @throws InjectionException when injection fails
     */
    <T> T inject(T injectable)
        throws InjectionException;
}
