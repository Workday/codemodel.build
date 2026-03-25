package build.codemodel.injection;

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

import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import jakarta.inject.Qualifier;

/**
 * Defines a {@link TypeUsage} for <i>Dependency Injection</i> with an {@link Injector}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public interface Dependency {

    /**
     * Obtains the {@link TypeUsage} for the {@link Dependency}.
     *
     * @return the {@link TypeUsage}
     */
    TypeUsage typeUsage();

    /**
     * Obtains the unique signature for the {@link Dependency}, consisting of the {@link TypeName} and any
     * {@link AnnotationTypeUsage}s that have been annotated with the {@link Qualifier} meta-annotation.
     *
     * @return the <i>Signature</i> for the {@link Dependency}
     */
    String signature();
}
