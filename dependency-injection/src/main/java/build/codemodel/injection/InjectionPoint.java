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
import build.codemodel.foundation.usage.TypeUsage;

import java.util.stream.Stream;

/**
 * Describes a point with in a {@link JDKTypeDescriptor} in which {@link Dependency}s may be injected.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public interface InjectionPoint {

    /**
     * The {@link JDKTypeDescriptor} in which the {@link InjectionPoint} is defined
     *
     * @return the {@link JDKTypeDescriptor}
     */
    JDKTypeDescriptor typeDescriptor();

    /**
     * The {@link Dependency}s for the parameters required to be injected at the {@link InjectionPoint},
     * in the order in which they are required.
     *
     * @return the {@link TypeUsage}s
     */
    Stream<Dependency> dependencies();

    /**
     * Inject into the {@link InjectionPoint} with the necessary zero or more resolved actual parameter
     * {@link #dependencies()}.
     *
     * @param target           the optional target {@link Object} into which injection must occur
     * @param actualParameters the resolved {@link TypeUsage} values to be injected
     * @return the result of the injection
     * @throws InjectionFailedException when injection fails
     */
    <T> T inject(Object target, Object[] actualParameters);
}
