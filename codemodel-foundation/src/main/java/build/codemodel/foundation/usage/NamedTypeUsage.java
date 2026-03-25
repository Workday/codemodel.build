package build.codemodel.foundation.usage;

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

import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;

/**
 * Provides type information concerning the <i>usage</i> of a <i>named type</i>, for example the type for an attribute, the
 * type returned by a method or the type for a parameter, either generic or otherwise, or annotation.
 *
 * @author brian.oliver
 * @see TypeDescriptor
 * @since Jan-2024
 */
public interface NamedTypeUsage
    extends TypeUsage {

    /**
     * Obtains the {@link TypeName} for the type being used.
     *
     * @return the {@link TypeName}
     */
    TypeName typeName();
}
