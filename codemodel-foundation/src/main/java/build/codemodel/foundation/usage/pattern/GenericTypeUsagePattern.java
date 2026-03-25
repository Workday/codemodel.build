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
import build.base.foundation.stream.Streams;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link TypeUsagePattern} for matching {@link GenericTypeUsage}s.
 *
 * @author brian.oliver
 * @since Jul-2025
 */
public class GenericTypeUsagePattern
    implements TypeUsagePattern<GenericTypeUsage, GenericTypeUsageMatch> {

    /**
     * A {@link Function} to produce a {@link NamedTypeUsagePattern} used to match the
     * {@link GenericTypeUsage} {@link NamedTypeUsage} in a {@link CodeModel}.
     */
    private final Function<CodeModel, NamedTypeUsagePattern> namedTypeUsagePatternFunction;

    /**
     * The {@link TypeUsagePattern}s for the parameters of the {@link GenericTypeUsage}.
     */
    private final ArrayList<? extends TypeUsagePattern<?, ?>> parameterPatterns;

    /**
     * Constructs a new {@link GenericTypeUsagePattern} using the specified {@link Function} to
     * obtain a {@link NamedTypeUsagePattern} together with the {@link TypeUsagePattern}s to match the
     * {@link GenericTypeUsage} parameters.
     *
     * @param namedTypeUsagePatternFunction the {@link Function} to obtain a {@link NamedTypeUsagePattern}
     *                                      for matching the {@link GenericTypeUsage} {@link NamedTypeUsage}
     *                                      in a {@link CodeModel}
     * @param parameterPatterns             the {@link TypeUsagePattern}s for the parameters of the {@link GenericTypeUsage}
     */
    protected GenericTypeUsagePattern(final Function<CodeModel, NamedTypeUsagePattern> namedTypeUsagePatternFunction,
                                      final Stream<? extends TypeUsagePattern<?, ?>> parameterPatterns) {

        this.namedTypeUsagePatternFunction = Objects
            .requireNonNull(namedTypeUsagePatternFunction,
                "The Function<CodeModel, TypeUsagePattern> must not be null");

        this.parameterPatterns = parameterPatterns == null
            ? new ArrayList<>()
            : parameterPatterns.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public GenericTypeUsageMatch failure() {
        return new GenericTypeUsageMatch() {
            @Override
            public Optional<GenericTypeUsage> typeUsage() {
                return Optional.empty();
            }

            @Override
            public Streamable<TypeUsageMatch<?>> parameters() {
                return Streamable.empty();
            }

            @Override
            public Optional<? extends TypeUsageMatch<?>> parameter(final int index) {
                return Optional.empty();
            }
        };
    }

    @Override
    public GenericTypeUsageMatch match(final TypeUsage typeUsage) {
        if (!(typeUsage instanceof GenericTypeUsage genericTypeUsage)) {
            return failure();
        }

        // attempt to match the TypeUsage of the GenericTypeUsage
        final var namedTypeUsageMatch = this.namedTypeUsagePatternFunction
            .apply(typeUsage.codeModel())
            .match(genericTypeUsage);

        if (namedTypeUsageMatch.isEmpty()) {
            return failure();
        }

        final var parameters = genericTypeUsage.parameters()
            .toList();

        // ensure the number of parameters matches the number of patterns
        if (!this.parameterPatterns.isEmpty() && parameters.size() != this.parameterPatterns.size()) {
            return failure();
        }

        // match the parameters against the parameter patterns
        final var parameterMatches = Streamable.of(Streams.zip(parameters.stream(), this.parameterPatterns.stream())
            .map(pair -> pair.second().match(pair.first())));

        // ensure all parameter matches are successful
        if (parameterMatches.stream()
            .anyMatch(TypeUsageMatch::isEmpty)) {

            return failure();
        }

        return new GenericTypeUsageMatch() {
            @Override
            public Optional<GenericTypeUsage> typeUsage() {
                return Optional.of(genericTypeUsage);
            }

            @Override
            public Streamable<? extends TypeUsageMatch<?>> parameters() {
                return parameterMatches;
            }

            @Override
            public Optional<? extends TypeUsageMatch<?>> parameter(final int index) {
                return index < 0 || index >= parameterMatches.count()
                    ? Optional.empty()
                    : parameterMatches.stream()
                        .skip(index)
                        .findFirst();
            }
        };
    }

    /**
     * Creates a new {@link GenericTypeUsagePattern} for the given {@link TypeName}
     * and optionally specified parameter {@link TypeUsagePattern}s.
     *
     * @param typeName          the {@link TypeName}
     * @param parameterPatterns the optionally specified parameter {@link TypeUsagePattern}s
     * @return a new {@link GenericTypeUsagePattern}
     */
    public static GenericTypeUsagePattern of(final TypeName typeName,
                                             final TypeUsagePattern<?, ?>... parameterPatterns) {

        return new GenericTypeUsagePattern(
            _ -> NamedTypeUsagePattern.of(typeName),
            Streams.of(parameterPatterns));
    }

    /**
     * Creates a new {@link GenericTypeUsagePattern} based on a {@link NamedTypeUsagePattern}
     * and optionally specified parameter {@link TypeUsagePattern}s.
     *
     * @param pattern           the {@link NamedTypeUsagePattern}
     * @param parameterPatterns the optionally specified parameter {@link TypeUsagePattern}s
     * @return a new {@link GenericTypeUsagePattern}
     */
    public static GenericTypeUsagePattern of(final NamedTypeUsagePattern pattern,
                                             final TypeUsagePattern<?, ?>... parameterPatterns) {

        return new GenericTypeUsagePattern(_ -> pattern, Streams.of(parameterPatterns));
    }

    /**
     * Creates a new {@link GenericTypeUsagePattern }using the specified {@link Function} to
     * obtain a {@link NamedTypeUsagePattern} together with the {@link TypeUsagePattern}s to match the
     * {@link GenericTypeUsage} parameters.
     *
     * @param namedTypeUsagePatternFunction the {@link Function} to obtain a {@link NamedTypeUsagePattern}
     *                                      for matching the {@link GenericTypeUsage} {@link NamedTypeUsage}
     *                                      in a {@link CodeModel}
     * @param parameterPatterns             the optionally specified parameter {@link TypeUsagePattern}s
     * @return a new {@link GenericTypeUsagePattern}
     */
    public static GenericTypeUsagePattern of(final Function<CodeModel, NamedTypeUsagePattern> namedTypeUsagePatternFunction,
                                             final TypeUsagePattern<?, ?>... parameterPatterns) {

        return new GenericTypeUsagePattern(namedTypeUsagePatternFunction, Streams.of(parameterPatterns));
    }
}
