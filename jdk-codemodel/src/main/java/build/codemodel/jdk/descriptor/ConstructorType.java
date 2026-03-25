package build.codemodel.jdk.descriptor;

/*-
 * #%L
 * JDK Code Model
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

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * A {@link Trait} that captures a {@link Constructor}.
 *
 * @param constructor the {@link Constructor}
 * @author brian.oliver
 * @since Oct-2024
 */
public record ConstructorType(Constructor<?> constructor)
    implements Trait {

    /**
     * Constructs the {@link ConstructorType}.
     *
     * @param constructor the {@link Constructor}
     */
    public ConstructorType {
        Objects.requireNonNull(constructor, "The Constructor must not be null");
    }

}
