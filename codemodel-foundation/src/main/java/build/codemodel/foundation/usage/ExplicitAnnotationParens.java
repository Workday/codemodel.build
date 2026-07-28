package build.codemodel.foundation.usage;

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

import build.base.marshalling.Marshalling;
import build.codemodel.foundation.descriptor.Trait;

/**
 * A {@link Trait} on an {@code AnnotationTypeUsage} indicating that the annotation was written
 * with explicit parentheses in source (e.g. {@code @Foo()}), as distinct from the marker form
 * (e.g. {@code @Foo}). Both forms resolve to the same, empty {@code AnnotationMirror} element
 * values, so this is the only place that distinction survives.
 *
 * <p>Only ever attached when the {@code AnnotationTypeUsage} was populated from source (i.e. a
 * {@code com.sun.source.util.Trees} instance was available); reflection-based population has no
 * way to recover this, since {@code .class} files do not retain it.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
public enum ExplicitAnnotationParens
    implements Trait {

    EXPLICIT_ANNOTATION_PARENS;

    static {
        Marshalling.registerEnum(ExplicitAnnotationParens.class);
    }
}
