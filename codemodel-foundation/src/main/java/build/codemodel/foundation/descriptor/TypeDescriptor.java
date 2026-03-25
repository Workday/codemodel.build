package build.codemodel.foundation.descriptor;

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

import java.util.stream.Stream;

import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.TypeUsage;

/**
 * Provides type information concerning the <i>definition</i> of a type.
 *
 * @author brian.oliver
 * @see TypeUsage
 * @see TypeName
 * @since Jan-2024
 */
public interface TypeDescriptor
    extends Dependent, Traitable {

    /**
     * The {@link TypeName} for the <i>Type</i>.
     *
     * @return the {@link TypeName}
     */
    TypeName typeName();

    @Override
    default Stream<TypeUsage> dependencies() {
        return traits(Dependent.class)
            .flatMap(Dependent::dependencies)
            .distinct();
    }
}
