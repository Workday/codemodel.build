package build.codemodel.foundation.usage.pattern;

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

import build.base.foundation.stream.Streamable;
import build.codemodel.foundation.usage.GenericTypeUsage;

import java.util.Optional;

/**
 * A {@link TypeUsageMatch} for a matched {@link GenericTypeUsage}.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public interface GenericTypeUsageMatch
    extends TypeUsageMatch<GenericTypeUsage> {

    /**
     * Obtains the {@link TypeUsageMatch}es for the {@link GenericTypeUsage} parameters.
     *
     * @return the {@link Streamable} of {@link TypeUsageMatch}s for the parameters
     */
    Streamable<? extends TypeUsageMatch<?>> parameters();

    /**
     * Obtains the {@link TypeUsageMatch} for the matched parameter at the specified index
     * of the matched {@link GenericTypeUsage}.
     *
     * @param index the index of the parameter to obtain (0-based)
     * @return the {@link Optional} containing the {@link TypeUsageMatch} for the parameter if it exists,
     * or an empty {@link Optional} if the index is out of bounds
     */
    Optional<? extends TypeUsageMatch<?>> parameter(int index);
}
