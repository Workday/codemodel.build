package build.codemodel.objectoriented.descriptor;

/*-
 * #%L
 * Object-Oriented Code Model
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

import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;

/**
 * A {@link Trait} to capture the {@code public}, {@code protected} or {@code private} access modifier for an
 * Object-Oriented {@link TypeDescriptor}.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
@Singular
public enum AccessModifier
    implements Trait {

    /**
     * Indicates {@code public} access is permitted.
     */
    PUBLIC,

    /**
     * Indicates only {@code protected} access is permitted.
     */
    PROTECTED,

    /**
     * Indicates only {@code private} access is permitted.
     */
    PRIVATE;
}
