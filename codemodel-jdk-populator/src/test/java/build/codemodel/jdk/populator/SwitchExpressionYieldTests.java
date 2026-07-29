package build.codemodel.jdk.populator;

/*-
 * #%L
 * JDK Code Model
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

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.expression.NumericLiteral;
import build.codemodel.jdk.expression.SwitchExpression;
import build.codemodel.jdk.statement.Yield;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for {@code yield} statements in block-form cases of a switch expression.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class SwitchExpressionYieldTests {

    private static SwitchExpression switchExpressionIn(final String methodBody) {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String bar(Object input) {
            %s
                }
            }
            """.formatted(methodBody));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(java.util.Optional.empty(), "com.example.Foo");
        return codeModel.getTypeDescriptor(typeName).stream()
            .flatMap(td -> td.composition(SwitchExpression.class))
            .findFirst()
            .orElseThrow();
    }

    @Test
    void yieldStatementInBlockCaseShouldCarryYieldedExpression() {
        final var expr = switchExpressionIn("""
                    int result = switch (input) {
                        case "a" -> 1;
                        default -> {
                            yield 2;
                        }
                    };
                    return String.valueOf(result);
            """);

        final var cases = expr.cases().toList();
        assertThat(cases).hasSize(2);

        final var defaultCase = cases.get(1);
        assertThat(defaultCase.labels().toList()).isEmpty();

        final var statements = defaultCase.statements().toList();
        assertThat(statements).hasSize(1);
        assertThat(statements.getFirst()).isInstanceOf(Yield.class);

        final var yield = (Yield) statements.getFirst();
        assertThat(yield.expression()).isInstanceOf(NumericLiteral.class);
        assertThat(((NumericLiteral) yield.expression()).value()).isEqualTo(2);
    }
}
