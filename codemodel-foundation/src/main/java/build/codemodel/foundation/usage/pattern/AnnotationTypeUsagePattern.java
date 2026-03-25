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

import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link TypeUsagePattern} for matching {@link AnnotationTypeUsage}s with a specific {@link TypeName}.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public class AnnotationTypeUsagePattern
    implements TypeUsagePattern<AnnotationTypeUsage, AnnotationTypeUsageMatch> {

    /**
     * The {@link TypeName} to match with a {@link AnnotationTypeUsage}.
     */
    private final TypeName typeName;

    /**
     * Constructs a new {@link AnnotationTypeUsagePattern} for the given {@link TypeName}.
     *
     * @param typeName the {@link TypeName}
     */
    protected AnnotationTypeUsagePattern(final TypeName typeName) {
        this.typeName = Objects.requireNonNull(typeName, "The TypeName must not be null");
    }

    @Override
    public AnnotationTypeUsageMatch failure() {
        return Optional::empty;
    }

    @Override
    public AnnotationTypeUsageMatch match(final TypeUsage typeUsage) {

        return typeUsage instanceof AnnotationTypeUsage annotationTypeUsage
            && annotationTypeUsage.typeName().equals(this.typeName)
            ? () -> Optional.of(annotationTypeUsage)
            : failure();
    }

    /**
     * Creates a new {@link AnnotationTypeUsagePattern} for the given {@link TypeName}.
     *
     * @param typeName the {@link TypeName}
     * @return a new {@link AnnotationTypeUsagePattern}
     */
    public static AnnotationTypeUsagePattern of(final TypeName typeName) {
        return new AnnotationTypeUsagePattern(typeName);
    }
}
