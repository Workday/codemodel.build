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
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;

/**
 * A {@link TypeUsagePattern} for matching {@link Optional}-based {@link GenericTypeUsage}s.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public class OptionalTypeUsagePattern
    implements TypeUsagePattern<GenericTypeUsage, OptionalTypeUsageMatch> {

    /**
     * A {@link GenericTypeUsagePattern} for matching the {@link Optional}-based {@link GenericTypeUsage}.
     */
    private final GenericTypeUsagePattern genericTypeUsagePattern;

    /**
     * Constructs a new {@link OptionalTypeUsagePattern} using with the {@link TypeUsagePattern} to match the
     * {@link Optional}-based {@link GenericTypeUsage} parameter.
     *
     * @param parameterPattern the {@link TypeUsagePattern} for the parameter of the {@link Optional}-based {@link GenericTypeUsage}
     */
    private OptionalTypeUsagePattern(final TypeUsagePattern<?, ?> parameterPattern) {

        this.genericTypeUsagePattern = GenericTypeUsagePattern.of(codeModel -> NamedTypeUsagePattern.of(codeModel
                .getNameProvider()
                .getTypeName(Optional.class)),
            parameterPattern);
    }

    @Override
    public OptionalTypeUsageMatch failure() {
        return new OptionalTypeUsageMatch() {
            @Override
            public Optional<? extends TypeUsageMatch<?>> parameter() {
                return Optional.empty();
            }

            @Override
            public Optional<GenericTypeUsage> typeUsage() {
                return Optional.empty();
            }
        };
    }

    @Override
    public OptionalTypeUsageMatch match(final TypeUsage typeUsage) {
        final var match = this.genericTypeUsagePattern.match(typeUsage);

        return match.isPresent()
            ? new OptionalTypeUsageMatch() {
            @Override
            public Optional<? extends TypeUsageMatch<?>> parameter() {
                return match.parameter(0);
            }

            @Override
            public Optional<GenericTypeUsage> typeUsage() {
                return match.typeUsage();
            }
        }
            : this.failure();
    }

    /**
     * Creates a new {@link OptionalTypeUsagePattern} using with the {@link TypeUsagePattern} to match the
     * {@link Optional}-based {@link GenericTypeUsage} parameter.
     *
     * @param parameterPattern the {@link TypeUsagePattern} for the parameter of the {@link Optional}-based {@link GenericTypeUsage}
     * @return a new {@link OptionalTypeUsagePattern}
     */
    public static OptionalTypeUsagePattern of(final TypeUsagePattern<?, ?> parameterPattern) {

        return new OptionalTypeUsagePattern(parameterPattern == null
            ? TypeUsagePattern.any()
            : parameterPattern);
    }

    /**
     * Creates a new {@link OptionalTypeUsagePattern} using the specified {@link TypeName} for a
     * {@link NamedTypeUsagePattern} to match the {@link Optional}-based {@link GenericTypeUsage} parameter.
     *
     * @param typeName the {@link TypeName}
     * @return a new {@link OptionalTypeUsagePattern}
     */
    public static OptionalTypeUsagePattern of(final TypeName typeName) {

        return new OptionalTypeUsagePattern(typeName == null
            ? TypeUsagePattern.any()
            : NamedTypeUsagePattern.of(typeName));
    }

    /**
     * Creates a new {@link OptionalTypeUsagePattern} to match any {@link Optional}-based {@link GenericTypeUsage}.
     *
     * @return a new {@link OptionalTypeUsagePattern}
     */
    public static OptionalTypeUsagePattern any() {
        return new OptionalTypeUsagePattern(TypeUsagePattern.any());
    }
}
