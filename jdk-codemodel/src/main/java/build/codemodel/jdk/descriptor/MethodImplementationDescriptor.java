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
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import java.util.Objects;

/**
 * A {@link Trait} representing a {@link java.lang.reflect.Method}.
 *
 * @author brian.oliver
 * @since May-2024
 */
public class MethodImplementationDescriptor
    implements Trait {

    private final MethodDescriptor methodDescriptor;

    /**
     * Constructs a {@link MethodImplementationDescriptor} for the specified {@link MethodDescriptor}.
     *
     * @param methodDescriptor the {@link MethodDescriptor}
     */
    public MethodImplementationDescriptor(final MethodDescriptor methodDescriptor) {
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "The MethodDescriptor must not be null");
    }

    /**
     * Obtains the {@link MethodDescriptor}.
     *
     * @return the {@link MethodDescriptor}
     */
    public MethodDescriptor methodDescriptor() {
        return this.methodDescriptor;
    }
}
