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

import java.util.Objects;

/**
 * A {@link Trait} attaching the default value of an annotation member to a method descriptor.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class AnnotationMemberDefaultValue implements Trait {

    /**
     * The default value of the annotation member.
     */
    private final Object value;

    /**
     * Constructs an {@link AnnotationMemberDefaultValue}.
     *
     * @param value the default value of the annotation member
     */
    public AnnotationMemberDefaultValue(final Object value) {
        this.value = Objects.requireNonNull(value, "The value must not be null");
    }

    /**
     * Obtains the default value of the annotation member.
     *
     * @return the default value
     */
    public Object value() {
        return this.value;
    }
}
