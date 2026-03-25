package build.codemodel.foundation;

/*-
 * #%L
 * Code Model Foundation
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

import build.codemodel.foundation.usage.TypeUsage;

import java.util.stream.Stream;

/**
 * Provide a mechanism to obtain the {@link TypeUsage} on which something depends.
 *
 * @author brian.oliver
 * @since Apr-2024
 */
public interface Dependent {

    /**
     * Obtains the {@link TypeUsage}s on which this {@link Dependent} <i>directly</i> depends, and no other.
     *
     * @return a {@link Stream} of {@link TypeUsage}
     */
    Stream<TypeUsage> dependencies();
}
