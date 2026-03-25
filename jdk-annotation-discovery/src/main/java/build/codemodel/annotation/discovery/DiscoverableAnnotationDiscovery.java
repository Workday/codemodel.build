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

import com.google.auto.service.AutoService;

/**
 * An {@link AnnotationDiscovery} for the {@link Discoverable}.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
@AutoService(AnnotationDiscovery.class)
public class DiscoverableAnnotationDiscovery
    implements AnnotationDiscovery {

    /**
     * Constructs a {@link DiscoverableAnnotationDiscovery}.
     */
    public DiscoverableAnnotationDiscovery() {
        // required for ServiceLoaders
    }

    @Override
    public Stream<? extends Class<? extends Annotation>> getDiscoverableAnnotationTypes() {
        return Stream.of(Discoverable.class);
    }
}
