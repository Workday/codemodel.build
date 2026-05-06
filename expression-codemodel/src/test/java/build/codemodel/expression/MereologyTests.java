package build.codemodel.expression;

/*-
 * #%L
 * Expression Code Model
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

import build.codemodel.expression.naming.FunctionName;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link Expression} subclasses correctly expose their structural children
 * via {@link build.base.mereology.Composite#parts()} and
 * {@link build.base.mereology.Composite#composition()}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class MereologyTests {

    private ConceptualCodeModel codeModel;
    private NonCachingNameProvider nameProvider;

    @BeforeEach
    void init() {
        nameProvider = new NonCachingNameProvider();
        codeModel = new ConceptualCodeModel(nameProvider);
    }

    private NumericLiteral num(final int value) {
        return NumericLiteral.of(codeModel, value);
    }

    private BooleanLiteral bool(final boolean value) {
        return BooleanLiteral.of(codeModel, value);
    }

    private VariableUsage variable(final String name) {
        return VariableUsage.of(codeModel, VariableName.of(
            ModuleName.of("java.lang", nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of(name)));
    }

    // -------------------------------------------------------------------------
    // Literals — no parts
    // -------------------------------------------------------------------------

    @Test
    void numericLiteralPartsIsEmpty() {
        assertTrue(num(1).parts().toList().isEmpty());
    }

    @Test
    void booleanLiteralPartsIsEmpty() {
        assertTrue(bool(true).parts().toList().isEmpty());
    }

    @Test
    void stringLiteralPartsIsEmpty() {
        assertTrue(StringLiteral.of(codeModel, "hello").parts().toList().isEmpty());
    }

    @Test
    void variableUsagePartsIsEmpty() {
        assertTrue(variable("x").parts().toList().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Binary arithmetic
    // -------------------------------------------------------------------------

    @Test
    void additionPartsContainsLeftAndRight() {
        final var left = num(1);
        final var right = num(2);
        final var expr = Addition.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    @Test
    void subtractionPartsContainsLeftAndRight() {
        final var left = num(5);
        final var right = num(3);
        final var expr = Subtraction.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    @Test
    void multiplicationPartsContainsLeftAndRight() {
        final var left = num(3);
        final var right = num(4);
        final var expr = Multiplication.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    @Test
    void divisionPartsContainsLeftAndRight() {
        final var left = num(10);
        final var right = num(2);
        final var expr = Division.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    // -------------------------------------------------------------------------
    // Unary arithmetic
    // -------------------------------------------------------------------------

    @Test
    void negativePartsContainsOperand() {
        final var operand = num(5);
        final var expr = Negative.of(operand);
        final var parts = expr.parts().toList();
        assertEquals(1, parts.size());
        assertTrue(parts.contains(operand));
    }

    // -------------------------------------------------------------------------
    // Binary logical
    // -------------------------------------------------------------------------

    @Test
    void conjunctionPartsContainsLeftAndRight() {
        final var left = bool(true);
        final var right = bool(false);
        final var expr = Conjunction.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    @Test
    void disjunctionPartsContainsLeftAndRight() {
        final var left = bool(true);
        final var right = bool(false);
        final var expr = Disjunction.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    // -------------------------------------------------------------------------
    // Unary logical
    // -------------------------------------------------------------------------

    @Test
    void negationPartsContainsOperand() {
        final var operand = bool(true);
        final var expr = Negation.of(operand);
        final var parts = expr.parts().toList();
        assertEquals(1, parts.size());
        assertTrue(parts.contains(operand));
    }

    // -------------------------------------------------------------------------
    // Comparison
    // -------------------------------------------------------------------------

    @Test
    void equalToPartsContainsLeftAndRight() {
        final var left = num(1);
        final var right = num(1);
        final var expr = EqualTo.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    @Test
    void lessThanPartsContainsLeftAndRight() {
        final var left = num(1);
        final var right = num(2);
        final var expr = LessThan.of(left, right);
        final var parts = expr.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(left));
        assertTrue(parts.contains(right));
    }

    // -------------------------------------------------------------------------
    // Cast
    // -------------------------------------------------------------------------

    @Test
    void castPartsContainsTargetTypeAndExpression() {
        final var targetType = SpecificTypeUsage.of(codeModel,
            nameProvider.getTypeName(Long.class));
        final var expr = num(42);
        final var cast = Cast.of(targetType, expr);
        final var parts = cast.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(targetType));
        assertTrue(parts.contains(expr));
    }

    // -------------------------------------------------------------------------
    // FunctionUsage
    // -------------------------------------------------------------------------

    @Test
    void functionUsagePartsContainsArguments() {
        final var arg1 = num(1);
        final var arg2 = num(2);
        final var expr = FunctionUsage.of(codeModel,
            FunctionName.of(
                ModuleName.of("java.lang", nameProvider),
                Optional.empty(),
                Optional.empty(),
                IrreducibleName.of("add")),
            arg1, arg2);
        final var parts = expr.parts().toList();
        assertTrue(parts.contains(arg1));
        assertTrue(parts.contains(arg2));
    }

    // -------------------------------------------------------------------------
    // Composition — transitive traversal
    // -------------------------------------------------------------------------

    @Test
    void nestedAdditionCompositionReachesAllLeaves() {
        final var a = num(1);
        final var b = num(2);
        final var c = num(3);
        final var inner = Addition.of(a, b);
        final var outer = Addition.of(inner, c);
        final var composition = outer.composition().toList();
        assertTrue(composition.contains(inner));
        assertTrue(composition.contains(a));
        assertTrue(composition.contains(b));
        assertTrue(composition.contains(c));
    }
}
