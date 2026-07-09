package build.codemodel.jdk;

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.InstanceOf;
import build.codemodel.jdk.statement.SwitchStatement;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@code switch} case label conversion, covering constant labels, type-pattern labels
 * with binding variables, {@code when} guards, and the {@code default} case.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class SwitchPatternCaptureTests {

    private static SwitchStatement switchStatementIn(final String methodBody) {
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
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        return (SwitchStatement) body.statements()
            .filter(s -> s instanceof SwitchStatement)
            .findFirst().orElseThrow();
    }

    @Test
    void shouldConvertConstantCaseLabels() {
        final var sw = switchStatementIn("""
                    switch (input) {
                        case "a":
                            return "matched-a";
                        default:
                            return "none";
                    }
            """);

        final var cases = sw.cases().toList();
        assertThat(cases).hasSize(2);

        final var constantCase = cases.get(0);
        assertThat(constantCase.labels().toList()).hasSize(1);
        assertThat(constantCase.guard()).isEmpty();

        final var defaultCase = cases.get(1);
        assertThat(defaultCase.labels().toList()).isEmpty();
    }

    @Test
    void shouldConvertTypePatternLabelToInstanceOfWithBindingVariable() {
        final var sw = switchStatementIn("""
                    switch (input) {
                        case Integer i -> {
                            return "int:" + i;
                        }
                        default -> {
                            return "none";
                        }
                    }
            """);

        final var patternCase = sw.cases().findFirst().orElseThrow();
        final var labels = patternCase.labels().toList();
        assertThat(labels).hasSize(1);

        final var label = (InstanceOf) labels.get(0);
        assertThat(label.checkedType().toString()).contains("Integer");
        assertThat(label.bindingVariable()).contains("i");
        assertThat(label.expression()).isSameAs(sw.selector());
    }

    @Test
    void shouldConvertGuardedPatternLabel() {
        final var sw = switchStatementIn("""
                    switch (input) {
                        case Integer i when i > 0 -> {
                            return "positive";
                        }
                        default -> {
                            return "none";
                        }
                    }
            """);

        final var patternCase = sw.cases().findFirst().orElseThrow();
        assertThat(patternCase.guard()).isPresent();

        final var label = (InstanceOf) patternCase.labels().findFirst().orElseThrow();
        assertThat(label.bindingVariable()).contains("i");
    }

    @Test
    void shouldNotDropCaseWhenLabelIsAPattern() {
        // Regression test: JCCase#getExpressions() filters to CONSTANTCASELABEL only, so a
        // naive implementation using getExpressions() silently produces an empty labels() for a
        // pattern case, indistinguishable from `default`. Assert the pattern case is not empty.
        final var sw = switchStatementIn("""
                    switch (input) {
                        case String s -> {
                            return s;
                        }
                        default -> {
                            return "none";
                        }
                    }
            """);

        final var cases = sw.cases().toList();
        assertThat(cases).hasSize(2);
        assertThat(cases.get(0).labels().toList()).isNotEmpty();
        assertThat(cases.get(1).labels().toList()).isEmpty();
    }
}
