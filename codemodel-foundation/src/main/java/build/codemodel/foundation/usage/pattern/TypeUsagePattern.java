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

import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a pattern for matching a {@link TypeUsage}.
 *
 * @param <T> the type of {@link TypeUsage} that this pattern matches
 * @param <M> the type of {@link TypeUsageMatch} that this pattern produces
 * @author brian.oliver
 * @since Jul-2025
 */
public interface TypeUsagePattern<T extends TypeUsage, M extends TypeUsageMatch<T>> {

    /**
     * Obtains a {@link TypeUsageMatch} that represents an unsuccessful match.
     *
     * @return a {@link TypeUsageMatch} that indicates failure to match
     */
    M failure();

    /**
     * Attempts to match the specified {@link TypeUsage} against this {@link TypeUsagePattern}.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link TypeUsageMatch} representing the result of the match.
     */
    M match(TypeUsage typeUsage);

    /**
     * Obtains a {@link TypeUsagePattern} that always matches any {@link TypeUsage}.
     *
     * @return a {@link TypeUsagePattern} that always matches
     */
    static TypeUsagePattern<TypeUsage, TypeUsageMatch<TypeUsage>> any() {
        return new TypeUsagePattern<>() {

            @Override
            public TypeUsageMatch<TypeUsage> failure() {
                return Optional::empty;
            }

            @Override
            public TypeUsageMatch<TypeUsage> match(final TypeUsage typeUsage) {
                return () -> Optional.of(typeUsage);
            }
        };
    }

    /**
     * Obtains a {@link TypeUsagePattern} that matches no {@link TypeUsage}.
     *
     * @return a {@link TypeUsagePattern} that never matches any {@link TypeUsage}
     */
    static TypeUsagePattern<TypeUsage, TypeUsageMatch<TypeUsage>> none() {
        return new TypeUsagePattern<>() {
            @Override
            public TypeUsageMatch<TypeUsage> failure() {
                return Optional::empty;
            }

            @Override
            public TypeUsageMatch<TypeUsage> match(final TypeUsage typeUsage) {
                return failure();
            }
        };
    }

    /**
     * Obtains a {@link TypeUsagePattern} that only matches a specific type of {@link TypeUsage}.
     *
     * @param <T>            the type of {@link TypeUsage} to match
     * @param typeUsageClass the {@link Class} of {@link TypeUsage} to match
     * @return a {@link TypeUsagePattern} that always matches
     */
    static <T extends TypeUsage> TypeUsagePattern<T, TypeUsageMatch<T>> isInstanceOf(final Class<T> typeUsageClass) {
        Objects.requireNonNull(typeUsageClass, "The Class<TypeUsage> must not be null");

        return new TypeUsagePattern<>() {
            @Override
            public TypeUsageMatch<T> failure() {
                return Optional::empty;
            }

            @Override
            public TypeUsageMatch<T> match(final TypeUsage typeUsage) {
                if (typeUsageClass.isInstance(typeUsage)) {
                    return new TypeUsageMatch<>() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public T orElseThrow() {
                            return (T) typeUsage;
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public Optional<T> typeUsage() {
                            return Optional.of((T) typeUsage);
                        }
                    };
                }
                return failure();
            }
        };
    }
}
