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

import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests for the {@link Expression} type hierarchy.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class ExpressionHierarchyTests {

    @Test
    void unaryArithmeticExpressionShouldBeArithmeticNotLogical() {
        final var codeModel = new ConceptualCodeModel(new NonCachingNameProvider());
        final var operand = NumericLiteral.of(codeModel, 1);
        final var negative = Negative.of(operand);

        assertInstanceOf(UnaryArithmeticExpression.class, negative);
        assertInstanceOf(ArithmeticExpression.class, negative);
        assertFalse(negative instanceof LogicalExpression, "UnaryArithmeticExpression must not be a LogicalExpression");
    }

    @Test
    void binaryArithmeticExpressionShouldBeArithmeticNotLogical() {
        final var codeModel = new ConceptualCodeModel(new NonCachingNameProvider());
        final var left = NumericLiteral.of(codeModel, 1);
        final var right = NumericLiteral.of(codeModel, 2);
        final var addition = Addition.of(left, right);

        assertInstanceOf(BinaryArithmeticExpression.class, addition);
        assertInstanceOf(ArithmeticExpression.class, addition);
        assertFalse(addition instanceof LogicalExpression, "BinaryArithmeticExpression must not be a LogicalExpression");
    }
}
