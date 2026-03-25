package build.codemodel.expression.parsing;

import build.codemodel.expression.*;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.CachingNameProvider;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateParserTests {

    static private TemplateParser parser;
    static private CodeModel codeModel;

    /**
     * Initializes the {@link TemplateParser} and {@link CodeModel} for testing template expression parsing.
     */
    @BeforeAll
    static void init() {
        final var javaNameProvider = new CachingNameProvider(new NonCachingNameProvider());
        codeModel = new ConceptualCodeModel(javaNameProvider);

        parser = new TemplateParser(codeModel);

        parser.defineAtom("\\[\\[", _ -> StringLiteral.of(codeModel, "["));
        parser.defineAtom("\\[[^]]+\\]", token -> VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of(token.trimFirstAndLast()))));
        parser.defineAtom("[^\\[]*", token -> StringLiteral.of(codeModel, token.value()));
    }

    /**
     * Ensures that various template expressions are parsed correctly.
     */
    @ParameterizedTest
    @MethodSource("templateAndParsedProvider")
    void shouldCorrectlyParseTemplateExpressions(String expression, List<Expression> expected) {
        final var result = parser.parse(expression);
        assertNotNull(result);

        for (var i = 0; i < expected.size(); i++) {
            final var expectedExpression = expected.get(i);
            final var actualExpression = result.expressions().get(i);
            assertEquals(expectedExpression, actualExpression);
        }
    }

    static Stream<Arguments> templateAndParsedProvider() {
        final var javaNameProvider = new CachingNameProvider(new NonCachingNameProvider());
        final var codeModel = new ConceptualCodeModel(javaNameProvider);
        return Stream.of(
                Arguments.arguments("some text", List.of(StringLiteral.of(codeModel, "some text"))),
                Arguments.arguments("[[]", List.of(StringLiteral.of(codeModel, "[]"))),
                Arguments.arguments(":[[", List.of(StringLiteral.of(codeModel, ":["))),
                Arguments.arguments("[[\\[[\\]]+", List.of(StringLiteral.of(codeModel, "[\\[\\]]+"))),
                Arguments.arguments("[[ [variable] ]", List.of(
                        StringLiteral.of(codeModel, "[ "),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("variable"))),
                        StringLiteral.of(codeModel, " ]"))),
                Arguments.arguments("\"", List.of(StringLiteral.of(codeModel, "\""))),
                Arguments.arguments("\"[[[variable]]\"", List.of(
                        StringLiteral.of(codeModel, "\"["),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("variable"))),
                        StringLiteral.of(codeModel, "]\""))),
                Arguments.arguments("'[[[variable]]'", List.of(
                        StringLiteral.of(codeModel, "'["),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("variable"))),
                        StringLiteral.of(codeModel, "]'"))),
                Arguments.arguments("[template]:[expression]", List.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("template"))),
                        StringLiteral.of(codeModel, ":"),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("expression"))))),
                Arguments.arguments("[[template]:[expression]", List.of(
                        StringLiteral.of(codeModel, "[template]:"),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("expression"))))),
                Arguments.arguments("[[[template]:[expression]", List.of(
                        StringLiteral.of(codeModel, "["),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("template"))),
                        StringLiteral.of(codeModel, ":"),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("expression"))))),
                Arguments.arguments("[instance] ([class])", List.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instance"))),
                        StringLiteral.of(codeModel, " ("),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("class"))),
                        StringLiteral.of(codeModel, ")"))),
                Arguments.arguments("[prev]\n[curr]", List.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("prev"))),
                        StringLiteral.of(codeModel, "\n"),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("curr")))))
        );
    }
}
