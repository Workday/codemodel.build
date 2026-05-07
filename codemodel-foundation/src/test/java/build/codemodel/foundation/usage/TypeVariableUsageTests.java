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
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var numberName = naming.getTypeName(Optional.empty(), "java.lang.Number");
        final var numberUsage = SpecificTypeUsage.of(codeModel, numberName);
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(),
            Optional.of(Lazy.of(numberUsage)));

        assertThat(usage.toString()).isEqualTo("T extends java.lang.Number");
    }

    @Test
    void toStringShouldUseSuperForLowerBound() {
        // T super Integer: lower bound uses "super"
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var integerName = naming.getTypeName(Optional.empty(), "java.lang.Integer");
        final var integerUsage = SpecificTypeUsage.of(codeModel, integerName);
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.of(Lazy.of(integerUsage)),
            Optional.empty());

        assertThat(usage.toString()).isEqualTo("T super java.lang.Integer");
    }

    @Test
    void toStringShouldShowJustNameWhenUnbounded() {
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.empty());

        assertThat(usage.toString()).isEqualTo("T");
    }
}
