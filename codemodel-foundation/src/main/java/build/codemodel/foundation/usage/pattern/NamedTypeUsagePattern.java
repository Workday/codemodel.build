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

import build.base.foundation.predicate.Predicates;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A {@link TypeUsagePattern} for matching {@link NamedTypeUsage}s whereby the {@link TypeName}
 * satisfies a {@link Predicate}.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public class NamedTypeUsagePattern
    implements TypeUsagePattern<NamedTypeUsage, NamedTypeUsageMatch> {

    /**
     * The {@link NamedTypeUsage} {@link Predicate} to match with a {@link NamedTypeUsage}.
     */
    private final Predicate<? super NamedTypeUsage> predicate;

    /**
     * Constructs a new {@link NamedTypeUsagePattern} for the given {@link TypeName}.
     *
     * @param predicate the {@link TypeName} {@link Predicate}
     */
    protected NamedTypeUsagePattern(final Predicate<? super NamedTypeUsage> predicate) {
        this.predicate = predicate == null
            ? Predicates.always()
            : predicate;
    }

    @Override
    public NamedTypeUsageMatch failure() {
        return Optional::empty;
    }

    @Override
    public NamedTypeUsageMatch match(final TypeUsage typeUsage) {

        return typeUsage instanceof NamedTypeUsage namedTypeUsage
            && this.predicate.test(namedTypeUsage)
            ? () -> Optional.of(namedTypeUsage)
            : failure();
    }

    /**
     * Creates a new {@link NamedTypeUsagePattern} for the given {@link TypeName}.
     *
     * @param typeName the {@link TypeName}
     * @return a new {@link NamedTypeUsagePattern}
     */
    public static NamedTypeUsagePattern of(final TypeName typeName) {
        return typeName == null
            ? any()
            : of(namedTypeUsage -> namedTypeUsage.typeName().equals(typeName));
    }

    /**
     * Creates a new {@link NamedTypeUsagePattern} for any {@link NamedTypeUsage} {@link TypeName}.
     *
     * @return a new {@link NamedTypeUsagePattern}
     */
    public static NamedTypeUsagePattern any() {
        return new NamedTypeUsagePattern(Predicates.always());
    }

    /**
     * Creates a new {@link NamedTypeUsagePattern} based on the specified {@link NamedTypeUsage} {@link Predicate}.
     *
     * @param predicate the {@link NamedTypeUsage} {@link Predicate}
     * @return a new {@link NamedTypeUsagePattern}
     */
    public static NamedTypeUsagePattern of(final Predicate<? super NamedTypeUsage> predicate) {
        return predicate == null
            ? any()
            : new NamedTypeUsagePattern(predicate);
    }
}
