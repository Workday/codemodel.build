package build.codemodel.dependency.injection;

/*-
 * #%L
 * Dependency Injection
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

import build.codemodel.foundation.usage.TypeUsage;

/**
 * Indicates an attempt to create a {@link Binding} whose {@link TypeUsage} carries more than one
 * {@link build.codemodel.foundation.usage.AnnotationTypeUsage} of the same qualifier annotation type (for
 * example, two different {@code @Named} values), making the {@link Binding}'s qualifier ambiguous.
 *
 * @author brian.oliver
 * @see BindingBuilder#as(String)
 * @see BindingBuilder#with(java.lang.annotation.Annotation)
 * @since Jul-2026
 */
public class DuplicateQualifierException
    extends InjectionException {

    /**
     * Constructs a {@link DuplicateQualifierException}.
     *
     * @param message the message
     */
    public DuplicateQualifierException(final String message) {
        super(message);
    }
}
