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
/**
 * Defines interfaces used by the {@code jdk-annotation-processor} enabling discovery of types to
 * reverse-engineer into a <i>Code Model</i>.
 *
 * @author brian.oliver
 * @since Apr-2024
 */
module build.codemodel.jdk.annotation.discovery {
    requires com.google.auto.service;

    exports build.codemodel.annotation.discovery;

    provides build.codemodel.annotation.discovery.AnnotationDiscovery
        with build.codemodel.annotation.discovery.DiscoverableAnnotationDiscovery;
}
