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

import build.base.foundation.Lazy;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeVariableUsage}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class TypeVariableUsageTests {

    private final NonCachingNameProvider naming = new NonCachingNameProvider();
    private final ConceptualCodeModel codeModel = new ConceptualCodeModel(naming);

    @Test
    void toStringShouldUseExtendsForUpperBound() {
        // T extends Number: upper bound uses "extends"
        final var tName = naming.getEmptyModuleTypeName("T");
        final var numberName = naming.getEmptyModuleTypeName("java.lang.Number");
        final var numberUsage = SpecificTypeUsage.of(codeModel, numberName);
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(),
            Optional.of(Lazy.of(numberUsage)));

        assertThat(usage.toString()).isEqualTo("T extends java.lang.Number");
    }

    @Test
    void toStringShouldUseSuperForLowerBound() {
        // T super Integer: lower bound uses "super"
        final var tName = naming.getEmptyModuleTypeName("T");
        final var integerName = naming.getEmptyModuleTypeName("java.lang.Integer");
        final var integerUsage = SpecificTypeUsage.of(codeModel, integerName);
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.of(Lazy.of(integerUsage)),
            Optional.empty());

        assertThat(usage.toString()).isEqualTo("T super java.lang.Integer");
    }

    @Test
    void toStringShouldShowJustNameWhenUnbounded() {
        final var tName = naming.getEmptyModuleTypeName("T");
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.empty());

        assertThat(usage.toString()).isEqualTo("T");
    }

    @Test
    void equalsShouldNotStackOverflowOnMutuallyRecursiveBounds() {
        // Comparable<T extends Comparable<T>> — upper bound references a GenericTypeUsage
        // whose parameter is the same TypeVariableUsage, creating a cycle in equals().
        final var tName = naming.getEmptyModuleTypeName("T");
        final var comparableName = naming.getEmptyModuleTypeName("java.lang.Comparable");

        final var tUsage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.empty());
        final var genericBound = GenericTypeUsage.of(codeModel, comparableName, tUsage);
        final var tWithBound = TypeVariableUsage.of(codeModel, tName, Optional.empty(),
            Optional.of(Lazy.of(genericBound)));

        assertThat(tWithBound.equals(tWithBound)).isTrue();

        final var tWithBound2 = TypeVariableUsage.of(codeModel, tName, Optional.empty(),
            Optional.of(Lazy.of(GenericTypeUsage.of(codeModel, comparableName, tUsage))));

        assertThat(tWithBound.equals(tWithBound2)).isTrue();
    }
}
