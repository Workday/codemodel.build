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

import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;

/**
 * A {@link Trait} represents the abstract-ness classification.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public enum Classification
    implements Trait {

    /**
     * Indicates that a {@link Traitable} is an <a href="https://en.wikipedia.org/wiki/Abstract_type">Abstract</a>
     * type that can not be instantiated or must be further defined.
     */
    ABSTRACT,

    /**
     * Indicates that a {@link Traitable} is a <a href="https://en.wikipedia.org/wiki/Abstract_type">Concrete</a>
     * type that may be instantiated or used directly.
     */
    CONCRETE,

    /**
     * Indicates that a {@link Traitable} is a {@link #CONCRETE} that may not be further extended
     */
    FINAL;

    /**
     * Determines if a {@link Classification} is {@code abstract} in nature.
     *
     * @return {@code true} when {@code abstract} in nature, {@code false} otherwise
     */
    public boolean isAbstract() {
        return this == ABSTRACT;
    }
}
