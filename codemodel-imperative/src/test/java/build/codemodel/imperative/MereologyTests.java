package build.codemodel.imperative;

/*-
 * #%L
 * Imperative Code Model
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

import build.codemodel.expression.BooleanLiteral;
import build.codemodel.expression.NumericLiteral;
import build.codemodel.expression.VariableUsage;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link Statement} subclasses correctly expose their structural children
 * via {@link build.base.mereology.Composite#parts()} and
 * {@link build.base.mereology.Composite#composition()}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class MereologyTests {

    private CodeModel codeModel;

    @BeforeEach
    void init() {
        codeModel = new ConceptualCodeModel(new NonCachingNameProvider());
    }

    private VariableUsage variable(final String name) {
        return VariableUsage.of(codeModel, VariableName.of(
            ModuleName.of("java.lang", codeModel.getNameProvider()),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of(name)));
    }

    private Assignment assignment(final String name, final int value) {
        return Assignment.of(variable(name), NumericLiteral.of(codeModel, value));
    }

    // -------------------------------------------------------------------------
    // Assignment
    // -------------------------------------------------------------------------

    @Test
    void assignmentPartsContainsVariableAndExpression() {
        final var stmt = assignment("x", 1);
        final var parts = stmt.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(stmt.variable()));
        assertTrue(parts.contains(stmt.expression()));
    }

    // -------------------------------------------------------------------------
    // Return
    // -------------------------------------------------------------------------

    @Test
    void returnWithExpressionPartsContainsExpression() {
        final var expr = NumericLiteral.of(codeModel, 42);
        final var stmt = Return.of(expr);
        final var parts = stmt.parts().toList();
        assertEquals(1, parts.size());
        assertTrue(parts.contains(expr));
    }

    @Test
    void bareReturnPartsIsEmpty() {
        final var stmt = Return.of(codeModel);
        assertTrue(stmt.parts().toList().isEmpty());
    }

    // -------------------------------------------------------------------------
    // If
    // -------------------------------------------------------------------------

    @Test
    void ifWithElsePartsContainsConditionThenElse() {
        final var condition = BooleanLiteral.of(codeModel, true);
        final var thenStmt = assignment("x", 1);
        final var elseStmt = assignment("x", 0);
        final var stmt = If.of(condition, thenStmt, Optional.of(elseStmt));
        final var parts = stmt.parts().toList();
        assertEquals(3, parts.size());
        assertTrue(parts.contains(condition));
        assertTrue(parts.contains(thenStmt));
        assertTrue(parts.contains(elseStmt));
    }

    @Test
    void ifWithoutElsePartsContainsConditionAndThen() {
        final var condition = BooleanLiteral.of(codeModel, true);
        final var thenStmt = assignment("x", 1);
        final var stmt = If.of(condition, thenStmt);
        final var parts = stmt.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(condition));
        assertTrue(parts.contains(thenStmt));
    }

    // -------------------------------------------------------------------------
    // While
    // -------------------------------------------------------------------------

    @Test
    void whilePartsContainsConditionAndBody() {
        final var condition = BooleanLiteral.of(codeModel, true);
        final var body = assignment("i", 0);
        final var stmt = While.of(condition, body);
        final var parts = stmt.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(condition));
        assertTrue(parts.contains(body));
    }

    // -------------------------------------------------------------------------
    // Block
    // -------------------------------------------------------------------------

    @Test
    void blockPartsContainsItsStatements() {
        final var a = assignment("x", 1);
        final var b = assignment("y", 2);
        final var block = Block.of(a, b);
        final var parts = block.parts().toList();
        assertEquals(2, parts.size());
        assertTrue(parts.contains(a));
        assertTrue(parts.contains(b));
    }

    @Test
    void blockCompositionIsTransitive() {
        final var inner = assignment("z", 3);
        final var innerBlock = Block.of(inner);
        final var outer = Block.of(innerBlock);
        final var composition = outer.composition().toList();
        assertTrue(composition.contains(innerBlock));
        assertTrue(composition.contains(inner));
    }

    // -------------------------------------------------------------------------
    // Nested If — composition walks into children
    // -------------------------------------------------------------------------

    @Test
    void nestedIfCompositionReachesDeepAssignment() {
        final var deep = assignment("result", 99);
        final var inner = If.of(BooleanLiteral.of(codeModel, false), deep);
        final var outer = If.of(BooleanLiteral.of(codeModel, true), inner);
        final var composition = outer.composition().toList();
        assertTrue(composition.contains(inner));
        assertTrue(composition.contains(deep));
    }
}
