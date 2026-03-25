package build.codemodel.foundation.usage.pattern;

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

import build.base.foundation.stream.Streamable;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

/**
 * A {@link TypeUsageMatch} for a matched {@link TypeUsage} that has one or more {@link AnnotationTypeUsage}s.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public interface AnnotatedTypeUsageMatch
    extends TypeUsageMatch<TypeUsage> {

    /**
     * The {@link AnnotationTypeUsageMatch}s that match the {@link AnnotationTypeUsage}s.
     *
     * @return a {@link Streamable} of {@link AnnotationTypeUsageMatch}s
     */
    Streamable<AnnotationTypeUsageMatch> matches();
}
