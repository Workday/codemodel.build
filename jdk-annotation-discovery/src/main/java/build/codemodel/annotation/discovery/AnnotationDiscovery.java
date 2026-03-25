package build.codemodel.annotation.discovery;

/*-
 * #%L
 * JDK Annotation Discovery
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

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

/**
 * Specifies {@link java.lang.annotation.Annotation}s that may be used to discover types to be
 * reverse-engineered into a <i>Code Model</i>.
 * <p>
 * {@link AnnotationDiscovery} implementations are typically located using the {@link java.util.ServiceLoader}.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
public interface AnnotationDiscovery {

    /**
     * Obtains the {@link Class}es of {@link Annotation} types that indicate {@link Class}es reverse-engineer.
     *
     * @return the {@link Stream} of {@link Annotation}
     */
    Stream<? extends Class<? extends Annotation>> getDiscoverableAnnotationTypes();
}
