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

import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A {@link Singular} {@link Trait} that captures a JDK {@link Type}.
 *
 * @param type the {@link Type}
 * @author brian.oliver
 * @since Oct-2024
 */
@Singular
public record JDKType(Type type)
    implements Trait {

    /**
     * Constructs the {@link JDKType}.
     *
     * @param type the {@link Field}
     */
    public JDKType {
        Objects.requireNonNull(type, "The Type must not be null");
    }
}
