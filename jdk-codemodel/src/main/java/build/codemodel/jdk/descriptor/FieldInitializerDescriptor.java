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

import build.codemodel.expression.Expression;
import build.codemodel.foundation.descriptor.Trait;

import java.util.Objects;

/**
 * A {@link Trait} attaching a captured field initializer (as an {@link Expression}) to a field descriptor.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class FieldInitializerDescriptor
    implements Trait {

    private final Expression initializer;

    /**
     * Constructs a {@link FieldInitializerDescriptor}.
     *
     * @param initializer the captured field initializer expression
     */
    public FieldInitializerDescriptor(final Expression initializer) {
        this.initializer = Objects.requireNonNull(initializer, "initializer must not be null");
    }

    /**
     * Obtains the captured field initializer expression.
     *
     * @return the {@link Expression} representing the field initializer
     */
    public Expression initializer() {
        return this.initializer;
    }
}
