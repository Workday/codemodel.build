package build.codemodel.expression.parsing;

import build.codemodel.expression.Addition;
import build.codemodel.expression.AnyInCommon;
import build.codemodel.expression.Conjunction;
import build.codemodel.expression.Disjunction;
import build.codemodel.expression.Division;
import build.codemodel.expression.EqualTo;
import build.codemodel.expression.ExclusiveDisjunction;
import build.codemodel.expression.Exponent;
import build.codemodel.expression.Expression;
import build.codemodel.expression.FunctionUsage;
import build.codemodel.expression.GreaterThan;
import build.codemodel.expression.GreaterThanOrEqualTo;
import build.codemodel.expression.LessThan;
import build.codemodel.expression.LessThanOrEqualTo;
import build.codemodel.expression.Literal;
import build.codemodel.expression.Modulo;
import build.codemodel.expression.Multiplication;
import build.codemodel.expression.Negation;
import build.codemodel.expression.Negative;
import build.codemodel.expression.NoneInCommon;
import build.codemodel.expression.NotEqualTo;
import build.codemodel.expression.NumericLiteral;
import build.codemodel.expression.Subtraction;
import build.codemodel.expression.Then;
import build.codemodel.expression.VariableUsage;
import build.codemodel.expression.naming.FunctionName;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.CachingNameProvider;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ExpressionParser}.
 */
public class ExpressionParserTests {

    private ExpressionParser parser;
    private CodeModel codeModel;

    /**
     * Initializes the {@link ExpressionParser} and {@link CodeModel} for testing.
     */
    @BeforeEach
    void init() {
        final var javaNameProvider = new CachingNameProvider(new NonCachingNameProvider());
        codeModel = new ConceptualCodeModel(javaNameProvider);

        parser = new ExpressionParser(codeModel);

        parser.defineSection("(", ")");
        parser.defineAtom("[0-9]+([.][0-9]+)?", token -> NumericLiteral.of(codeModel, new BigDecimal(token.value())));
        parser.defineAtom("\\[[^]]+\\]", token -> VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of(token.trimFirstAndLast()))));
        parser.defineUnary("-", 1, Negative::of);
        parser.defineUnary("abs", 1, (operand) -> FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("abs")), operand));
        parser.defineBinary("^", 2, Exponent::of);
        parser.defineBinary("le", 2, LessThanOrEqualTo::of);
        parser.defineBinary("<=", 2, LessThanOrEqualTo::of);
        parser.defineBinary("lt", 2, LessThan::of);
        parser.defineBinary("<", 2, LessThan::of);
        parser.defineBinary("ge", 2, GreaterThanOrEqualTo::of);
        parser.defineBinary(">=", 2, GreaterThanOrEqualTo::of);
        parser.defineBinary("gt", 2, GreaterThan::of);
        parser.defineBinary(">", 2, GreaterThan::of);
        parser.defineBinary("ac", 2, AnyInCommon::of);
        parser.defineBinary("nc", 2, NoneInCommon::of);
        parser.defineBinary("*", 3, Multiplication::of);
        parser.defineBinary("/", 3, Division::of);
        parser.defineBinary("%", 3, Modulo::of);
        parser.defineBinary("eq", 3, EqualTo::of);
        parser.defineBinary("==", 3, EqualTo::of);
        parser.defineBinary("ne", 3, NotEqualTo::of);
        parser.defineBinary("!=", 3, NotEqualTo::of);
        parser.defineBinary("-", 4, Subtraction::of);
        parser.defineBinary("+", 4, Addition::of);
        parser.defineBinary("min", 5, (left, right) -> FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("min")), left, right));
        parser.defineBinary("max", 5, (left, right) -> FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("max")), left, right));
        parser.defineUnary("not", 11, Negation::of);
        parser.defineBinary("and", 12, Conjunction::of);
        parser.defineBinary("or", 13, Disjunction::of);
        parser.defineBinary("xor", 14, ExclusiveDisjunction::of);
        parser.defineBinary("then", 15, Then::of);
    }

    /**
     * Ensures that an empty expression is correctly parsed as an {@link EmptyExpression}.
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "      " })
    void shouldParseAnEmptyExpression(String expr)
    {
        final var result = parser.parse(expr);
        assertInstanceOf(EmptyExpression.class, result);
    }

    /**
     * Ensures that a {@link NumericLiteral} is correctly parsed regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1", " 1", "1 " })
    void shouldParseSingleLiteral(String expr) {
        final var expected = NumericLiteral.of(codeModel, new BigDecimal("1"));
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link Addition} is correctly parsed regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2", "1+2", "1    +2 " })
    void shouldParseSimpleAddition(String expr) {
        final var expected = Addition.of(
                NumericLiteral.of(codeModel, new BigDecimal("1")),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link Multiplication} is correctly parsed regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 * 2", "1*2", "1    *2 " })
    void shouldParseSimpleMultiplication(String expr) {
        final var expected = Multiplication.of(
                NumericLiteral.of(codeModel, new BigDecimal("1")),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that an {@link Addition} followed by a {@link Multiplication} is correctly parsed regardless of whitespace.
     * Note tha this test is a simple case to ensure proper handling of precedence.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2 * 3", "1+2*3", "1 +2* 3" })
    void shouldParseAdditionThenMultiplicationWithCorrectPrecedence(String expr) {
        final var expected = Addition.of(
                NumericLiteral.of(codeModel, new BigDecimal("1")),
                Multiplication.of(
                        NumericLiteral.of(codeModel, new BigDecimal("2")),
                        NumericLiteral.of(codeModel, new BigDecimal("3"))
                )
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link Multiplication} followed by an {@link Addition} is correctly parsed regardless of whitespace.
     * Note tha this test is a simple case to ensure proper handling of precedence.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 * 2 + 3", "1*2+3", "1 *2+ 3" })
    void shouldParseMultiplicationThenAdditionWithCorrectPrecedence(String expr) {
        final var expected = Addition.of(
                Multiplication.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1")),
                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                ),
                NumericLiteral.of(codeModel, new BigDecimal("3"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that an {@link Addition} followed by a {@link Subtraction} is correctly parsed regardless of whitespace.
     * Note tha this test is a simple case to ensure proper handling of precedence.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2 - 3", "1+2-3" })
    void shouldParseAdditionAndSubtractionAsEqualPrecedence(String expr) {
        final var expected = Subtraction.of(
                Addition.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1")),
                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                ),
                NumericLiteral.of(codeModel, new BigDecimal("3"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a multipart expression is correctly parsed according to precedence and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2 * 3 - 4", "1+2*3-4" })
    void shouldParseMultiNodeExpressionWithCorrectPrecedence(String expr) {
        final var expected = Subtraction.of(
                Addition.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1")),
                        Multiplication.of(
                                NumericLiteral.of(codeModel, new BigDecimal("2")),
                                NumericLiteral.of(codeModel, new BigDecimal("3"))
                        )
                ),
                NumericLiteral.of(codeModel, new BigDecimal("4"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link Negative} is correctly parsed.
     */
    @Test
    void shouldParseNegativeCorrectly() {
        final var expected = Negative.of(
                NumericLiteral.of(codeModel, new BigDecimal("1"))
        );
        final var result = parser.parse("-1");
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link Negative} is correctly parsed at the beginning of an expression and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "-1 + 2", "-1+2" })
    void shouldParseNegativeBeginningAnExpressionCorrectly(String expr) {
        final var expected = Addition.of(
                Negative.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1"))
                ),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link Negative} is correctly parsed within an expression and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "1 + -2", "1+-2" })
    void shouldParseNegativeWithinAnExpressionCorrectly(String expr) {
        final var expected = Addition.of(
                NumericLiteral.of(codeModel, new BigDecimal("1")),
                Negative.of(
                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                )
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a section is parsed with the correct precedence and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "(1 + 2) * 3", "(1+2)*3" })
    void shouldParseASectionAtTheStartOfAnExpressionWithTheCorrectPrecedence(String expr) {
        final var expected = Multiplication.of(
                Addition.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1")),
                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                ),
                NumericLiteral.of(codeModel, new BigDecimal("3"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a section is parsed with the correct precedence and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "4 * (1 + 2) * 3", "4*(1+2)*3" })
    void shouldParseASectionWithinAnExpressionWithTheCorrectPrecedence(String expr) {
        final var expected = Multiplication.of(
                Multiplication.of(
                        NumericLiteral.of(codeModel, new BigDecimal("4")),
                        Addition.of(
                                NumericLiteral.of(codeModel, new BigDecimal("1")),
                                NumericLiteral.of(codeModel, new BigDecimal("2"))
                        )
                ),
                NumericLiteral.of(codeModel, new BigDecimal("3"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that nested sections are parsed with the correct precedence and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "4 * (1 + (5 - 2)) * 3", "4*(1+(5-2))*3" })
    void shouldParseNestedSectionsWithinAnExpressionCorrectly(String expr) {
        final var expected = Multiplication.of(
                Multiplication.of(
                        NumericLiteral.of(codeModel, new BigDecimal("4")),
                        Addition.of(
                                NumericLiteral.of(codeModel, new BigDecimal("1")),
                                Subtraction.of(
                                        NumericLiteral.of(codeModel, new BigDecimal("5")),
                                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                                )
                        )
                ),
                NumericLiteral.of(codeModel, new BigDecimal("3"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that multiple sections are parsed with the correct precedence and regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "(1 + 2) * (3 - 4)", "(1+2)*(3-4)" })
    void shouldParseMultipleDistinctSectionsWithinAnExpressionWithCorrectPrecedence(String expr) {
        final var expected = Multiplication.of(
                Addition.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1")),
                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                ),
                Subtraction.of(
                        NumericLiteral.of(codeModel, new BigDecimal("3")),
                        NumericLiteral.of(codeModel, new BigDecimal("4"))
                )
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that double {@link Negative} are correctly parsed regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "--1", "-   -1" })
    void shouldParseDoubleNegativeCorrectly(String expr) {
        final var expected = Negative.of(
                Negative.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1"))
                )
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that a {@link NumericLiteral} is correctly parsed from a decimal representation.
     */
    @Test
    void shouldParseDecimalsCorrectly() {
        final var expected = Addition.of(
                NumericLiteral.of(codeModel, new BigDecimal("1.3534")),
                NumericLiteral.of(codeModel, new BigDecimal("2.7111111"))
        );
        final var result = parser.parse("1.3534 + 2.7111111");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link Modulo} is correctly parsed regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "3 % 2", "3%2" })
    void shouldParseModuloCorrectly(String expr) {
        final var expected = Modulo.of(
                NumericLiteral.of(codeModel, new BigDecimal("3")),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link Exponent} is correctly parsed regardless of whitespace.
     */
    @ParameterizedTest
    @ValueSource(strings = { "3 ^ 2", "3^2" })
    void shouldParseExponentCorrectly(String expr) {
        final var expected = Exponent.of(
                NumericLiteral.of(codeModel, new BigDecimal("3")),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link Exponent} is parsed with the correct precedence.
     */
    @Test
    void shouldParseExponentWithTheCorrectPrecedence() {
        final var expected = Multiplication.of(
                NumericLiteral.of(codeModel, new BigDecimal("3")),
                Exponent.of(
                        NumericLiteral.of(codeModel, new BigDecimal("1")),
                        NumericLiteral.of(codeModel, new BigDecimal("2"))
                )
        );
        final var result = parser.parse("3 * 1 ^ 2");
        assertGraph(expected, result);
    }

    /**
     * Ensure that a {@link Subtraction} (as opposed to a {@link Negative}) is correctly parsed after the end of a section.
     */
    @Test
    void shouldParseSubtractionAfterEndSection() {
        final var expected = Subtraction.of(
                NumericLiteral.of(codeModel, new BigDecimal("1")),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse("(1)-2");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link VariableUsage} is correctly parsed.
     */
    @Test
    void shouldParseVariablesCorrectly() {
        final var expected = VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("bob")));
        final var result = parser.parse("[bob]");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link VariableUsage} is correctly parsed within a more complex expression.
     */
    @Test
    void shouldParseVariablesWithinAnExpressionCorrectly() {
        final var expected = Multiplication.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("bob"))),
                NumericLiteral.of(codeModel, new BigDecimal("5"))
        );
        final var result = parser.parse("[bob] * 5");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link FunctionUsage} is correctly parsed for 'min'.
     */
    @Test
    void shouldParseMinFunctionCorrectly() {
        final var expected = FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("min")),
                NumericLiteral.of(codeModel, new BigDecimal("3")),
                NumericLiteral.of(codeModel, new BigDecimal("5"))
        );
        final var result = parser.parse("3 min 5");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link FunctionUsage} is correctly parsed for 'max'.
     */
    @Test
    void shouldParseMaxFunctionCorrectly() {
        final var expected = FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("max")),
                NumericLiteral.of(codeModel, new BigDecimal("3")),
                Multiplication.of(
                        NumericLiteral.of(codeModel, new BigDecimal("5")),
                        NumericLiteral.of(codeModel, new BigDecimal("3"))
                )
        );
        final var result = parser.parse("3 max (5 * 3)");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link FunctionUsage} is correctly parsed for 'abs'.
     */
    @Test
    void shouldParseAbsFunctionCorrectly() {
        final var expected = FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("abs")),
                Negative.of(
                        NumericLiteral.of(codeModel, new BigDecimal("3")))
        );
        final var result = parser.parse("abs -3");
        assertGraph(expected, result);
    }

    /**
     * Ensures that {@link FunctionUsage} is correctly parsed for 'abs' with the correct precedence.
     */
    @Test
    void shouldParseAbsFunctionWithCorrectPrecedence() {
        final var expected = Multiplication.of(
                FunctionUsage.of(codeModel, FunctionName.of(IrreducibleName.of("abs")),
                    Negative.of(
                        NumericLiteral.of(codeModel, new BigDecimal("3")))),
                NumericLiteral.of(codeModel, new BigDecimal("2"))
        );
        final var result = parser.parse("abs -3 * 2");
        assertGraph(expected, result);
    }

    /**
     * Ensures that incomplete expressions throw an {@link ExpressionParserException}.
     */
    @ParameterizedTest
    @ValueSource(strings = { "(1 + 2", "1 +", "-" })
    void shouldThrowOnMalformedExpression(String expr) {
        final var exception = assertThrows(ExpressionParserException.class, () -> parser.parse(expr));
        assertEquals("The expression is malformed", exception.getMessage());
    }

    /**
     * Ensures that expressions containing unknown elements throw an {@link ExpressionParserException}.
     */
    @ParameterizedTest
    @MethodSource("provideStringsForShouldThrowOnUnknownToken")
    void shouldThrowOnUnknownToken(String expr, int location) {
        final var exception = assertThrows(ExpressionParserException.class, () -> parser.parse(expr));
        assertEquals("The expression contains an unknown or unexpected token at Location 1:" + location, exception.getMessage());
    }

    /**
     * Ensures that expressions contains 'ac' are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_1_Issue_263() {
        final var expected = AnyInCommon.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instances")))
        );
        final var result = parser.parse("[this] ac [instances]");
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing 'less than' tokens ('lt' or '<') are parsed correctly.
     */
    @ParameterizedTest
    @ValueSource(strings = { "[this] lt [instances]", "[this] < [instances]" })
    void shouldCorrectlyParseExpression_2_lt_Issue_263(String expr) {
        final var expected = LessThan.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instances")))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing 'less than or equal to' token ('le' or '<=') are parsed correctly.
     */
    @ParameterizedTest
    @ValueSource(strings = { "[this] le [instances]", "[this] <= [instances]" })
    void shouldCorrectlyParseExpression_2_le_Issue_263(String expr) {
        final var expected = LessThanOrEqualTo.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instances")))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing 'greater than' tokens ('gt' or '>') are parsed correctly.
     */
    @ParameterizedTest
    @ValueSource(strings = { "[this] gt [instances]", "[this] > [instances]" })
    void shouldCorrectlyParseExpression_2_gt_Issue_263(String expr) {
        final var expected = GreaterThan.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instances")))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing 'greater than or equal to' token ('ge' or '>=') are parsed correctly.
     */
    @ParameterizedTest
    @ValueSource(strings = { "[this] ge [instances]", "[this] >= [instances]" })
    void shouldCorrectlyParseExpression_2_ge_Issue_263(String expr) {
        final var expected = GreaterThanOrEqualTo.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instances")))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing negation of complete sections are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_3_Issue_263() {
        final var expected = Conjunction.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("bem process"))),
                Negation.of(
                        LessThanOrEqualTo.of(
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("bem process"))),
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("bem processes")))
                        )
                )
        );
        final var result = parser.parse("[bem process] and not ([bem process] le [bem processes])");
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing 'equal to' tokens ('eq' or '==') are parsed correctly.
     */
    @ParameterizedTest
    @ValueSource(strings = { "[this] eq [instance parm]", "[this] == [instance parm]" })
    void shouldCorrectlyParseExpression_4a_Issue_263(String expr) {
        final var expected = EqualTo.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instance parm")))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that expressions containing 'not equal to' tokens ('ne' or '!=') are parsed correctly.
     */
    @ParameterizedTest
    @ValueSource(strings = { "[this] ne [instance parm]", "[this] != [instance parm]" })
    void shouldCorrectlyParseExpression_4b_Issue_263(String expr) {
        final var expected = NotEqualTo.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("this"))),
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("instance parm")))
        );
        final var result = parser.parse(expr);
        assertGraph(expected, result);
    }

    /**
     * Ensures that complex expressions are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_5_Issue_263() {
        final var expected = Disjunction.of(
                Disjunction.of(
                        Disjunction.of(
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("sub interest"))),
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("all parms")))
                        ),
                        Negation.of(
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("is parm possible")))
                        )
                ),
                AnyInCommon.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("work data parm"))),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("parms for method")))
                )
        );
        final var result = parser.parse("[sub interest] or [all parms] or (not [is parm possible]) or ([work data parm] ac [parms for method])");
        assertGraph(expected, result);
    }

    /**
     * Ensures that complex expressions are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_6_Issue_263() {
        final var expected = Disjunction.of(
                Disjunction.of(
                        Conjunction.of(
                                Negation.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method for class")))
                                ),
                                Negation.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method implements for class")))
                                )
                        ),
                        Conjunction.of(
                                Conjunction.of(
                                        Conjunction.of(
                                                Conjunction.of(
                                                        Negation.of(
                                                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method for class")))
                                                        ),
                                                        Negation.of(
                                                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("not check use method")))
                                                        )
                                                ),
                                                Negation.of(
                                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method implements method")))
                                                )
                                        ),
                                        Negation.of(
                                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method implemented by Method")))
                                        )
                                ),
                                Negation.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("mes in use")))
                                )
                        )
                ),
                Conjunction.of(
                        Conjunction.of(
                                Conjunction.of(
                                        Conjunction.of(
                                                Conjunction.of(
                                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method for class"))),
                                                        Negation.of(
                                                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("not check use method")))
                                                        )
                                                ),
                                                Negation.of(
                                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("mes in use")))
                                                )
                                        ),
                                        Negation.of(
                                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("implements methods other than self")))
                                        )
                                ),
                                Negation.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("method referenced by yp")))
                                )
                        ),
                        Negation.of(
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("referenced by override conversion mapping")))
                        )
                )
        );
        final var result = parser.parse("(not [method for class] and not [method implements for class]) or(not [method for class] and (not [not check use method]) and (not [method implements method]) and (not [method implemented by Method]) and (not [mes in use]))or ([method for class] and (not [not check use method]) and (not [mes in use]) and (not [implements methods other than self]) and (not [method referenced by yp])) and not [referenced by override conversion mapping]");
        assertGraph(expected, result);
    }

    /**
     * Ensures that complex expressions are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_7_Issue_263() {
        final var expected = Disjunction.of(
                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("removes not enterable or rto or dapt"))),
                Conjunction.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("removes do not show"))),
                        Negation.of(
                                Disjunction.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("ec not enterable"))),
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("ec as page title")))
                                )
                        )
                )
        );
        final var result = parser.parse("[removes not enterable or rto or dapt] or ([removes do not show] and not ([ec not enterable] or [ec as page title]))");
        assertGraph(expected, result);
    }

    /**
     * Ensures that complex expressions that are longer than 4096 characters are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_8_Issue_263() {
        final var result = parser.parse("[existing supplier] " +
                        "and " +
                        "(( [Accepted Currencies on element] eq [Accepted Currencies persisted] ) or not [Accepted Currencies was submitted]) " +
                        "and " +
                        "(( [Always Separate Payments on element] eq [Always Separate Payments persisted] ) or not [Always Separate Payments was submitted]) " +
                        "and " +
                        "( [Supplier Name on element] eq [Supplier Name persisted] ) " +
                        "and " +
                        "(( [Business Entity External ID on element] eq [Business Entity External ID persisted] ) or not [Business Entity External ID was submitted] ) " +
                        "and " +
                        "(( [Basic Worktag Only on element] eq [Basic Worktag Only persisted] ) or not [Basic Worktag Only was submitted] ) " +
                        "and " +
                        "(( [Certificate Number on element] eq [Certificate Number persisted] ) or not [Certificate Number was submitted] ) " +
                        "and " +
                        "(( [Certifier on element] eq [Certifier persisted] ) or not [Certifier was submitted] ) " +
                        "and " +
                        "(( [Suppliers Customer Account Number on element] eq [Suppliers Customer Account Number persisted] ) or not [Suppliers Customer Account Number was submitted] ) " +
                        "and " +
                        "(( [Disable Change Order on element] eq [Disable Change Order persisted] ) or not [Disable Change Order was submitted] ) " +
                        "and " +
                        "(( [Do Not Pay During Bank Account Updates on element] eq [Do Not Pay During Bank Account Updates persisted] ) or not [Do Not Pay During Bank Account Updates was submitted] ) " +
                        "and " +
                        "(( [Do Not Reimburse Contingent Worker Expense Reports on element] eq [Do Not Reimburse Contingent Worker Expense Reports persisted] ) or not [Do Not Reimburse Contingent Worker Expense Reports was submitted] ) " +
                        "and " +
                        "(( [DUNS Number on element] eq [DUNS Number persisted] ) or not [DUNS Number was submitted] ) " +
                        "and " +
                        "(( [Unique Entity Identifier on element] eq [Unique Entity Identifier persisted] ) or not [Unique Entity Identifier was submitted] ) " +
                        "and " +
                        "(( [FATCA on element] eq [FATCA persisted] ) or not [FATCA was submitted] ) " +
                        "and " +
                        "(( [Exclude Freight for Discount Calculation on element] eq [Exclude Freight for Discount Calculation persisted] ) or not [Exclude Freight for Discount Calculation was submitted] ) " +
                        "and " +
                        "(( [Exclude Other for Discount Calculation on element] eq [Exclude Other for Discount Calculation persisted] ) or not [Exclude Other for Discount Calculation was submitted] ) " +
                        "and " +
                        "(( [Exclude Tax for Discount Calculation on element] eq [Exclude Tax for Discount Calculation persisted] ) or not [Exclude Tax for Discount Calculation was submitted] ) " +
                        "and " +
                        "(( [Supplier ID on element] eq [Supplier ID persisted] ) or not [Supplier ID was submitted] ) " +
                        "and " +
                        "(( [Invoice Any Supplier on element] eq [Invoice Any Supplier persisted] ) or not [Invoice Any Supplier was submitted] ) " +
                        "and " +
                        "(( [MBE/WMBE Business on element] eq [MBE/WMBE Business persisted] ) or not [MBE/WMBE Business was submitted] ) " +
                        "and " +
                        "(( [Supplier Payment Memo on element] eq [Supplier Payment Memo persisted] ) or not [Supplier Payment Memo was submitted] ) " +
                        "and " +
                        "(( [ID on element] eq [ID persisted] ) or not [ID was submitted] ) " +
                        "and " +
                        "(( [Report 1099 MISC with Parent on element] eq [Report 1099 MISC with Parent persisted] ) or not [Report 1099 MISC with Parent was submitted] ) " +
                        "and " +
                        "(( [Use Invoice Memo on element] eq [Use Invoice Memo persisted] ) or not [Use Invoice Memo was submitted] ) " +
                        "and " +
                        "(( [Use Supplier Connection Memo on element] eq [Use Supplier Connection Memo persisted] ) or not [Use Supplier Connection Memo was submitted] ) " +
                        "and " +
                        "(( [Use Supplier Reference on element] eq [Use Supplier Reference persisted] ) or not [Use Supplier Reference was submitted] ) " +
                        "and " +
                        "(( [Tax Document Date on element] eq [Tax Document Date persisted] ) or not [Tax Document Date was submitted] ) " +
                        "and " +
                        "(( [Certificate of Insurance Date on element] eq [Certificate of Insurance Date persisted] ) or not [Certificate of Insurance Date was submitted] ) " +
                        "and " +
                        "(( [Certification Expiration Date on element] eq [Certification Expiration Date persisted] ) or not [Certification Expiration Date was submitted] ) " +
                        "and " +
                        "(( [Companies and Company Hierarchies on element] eq [Companies and Company Hierarchies persisted] ) or not [Companies and Company Hierarchies was submitted] ) " +
                        "and " +
                        "(( [Approval Status on element] eq [Approval Status persisted] ) or not [Approval Status was submitted] ) " +
                        "and " +
                        "(( [Tax Authority Form Type on element] eq [Tax Authority Form Type persisted] ) or not [Tax Authority Form Type was submitted] ) " +
                        "and " +
                        "(( [Supplier Category on element] eq [Supplier Category persisted] ) or not [Supplier Category was submitted] ) " +
                        "and " +
                        "(( [Supplier Group on element] eq [Supplier Group persisted] ) or not [Supplier Group was submitted] ) " +
                        "and " +
                        "(( [Default Tax Code on element] eq [Default Tax Code persisted] ) or not [Default Tax Code was submitted] ) " +
                        "and " +
                        "(( [Withholding Tax Code on element] eq [Withholding Tax Code persisted] ) or not [Withholding Tax Code was submitted] ) " +
                        "and " +
                        "(( [Purchase Order Issue Option on element] eq [Purchase Order Issue Option persisted] ) or not [Purchase Order Issue Option was submitted] ) " +
                        "and " +
                        "(( [Multi-Supplier Supplier Link for PO Issue on element] eq [Multi-Supplier Supplier Link for PO Issue persisted] ) or not [Multi-Supplier Supplier Link for PO Issue was submitted] ) " +
                        "and " +
                        "(( [Shipping Terms on element] eq [Shipping Terms persisted] ) or not [Shipping Terms was submitted] ) " +
                        "and " +
                        "(( [Shipping Method on element] eq [Shipping Method persisted] ) or not [Shipping Method was submitted] ) " +
                        "and " +
                        "(( [Default Payment Terms on element] eq [Default Payment Terms persisted] ) or not [Default Payment Terms was submitted] ) " +
                        "and " +
                        "(( [Payment Type on element] eq [Payment Type persisted] ) or not  [Payment Type was submitted] ) " +
                        "and " +
                        "(( [Default Payment Type on element] eq [Default Payment Type persisted] ) or not [Default Payment Type was submitted] ) " +
                        "and " +
                        "(( [Worker Credit Card on element] eq [Worker Credit Card persisted] ) or not [Worker Credit Card was submitted] ) " +
                        "and " +
                        "(( [Suppliers as Children on element] eq [Suppliers as Children persisted] ) or not [Suppliers as Children was submitted] ) " +
                        "and " +
                        "(( [Proposed Suppliers as Children on element] eq [Proposed Suppliers as Children persisted] ) or not [Proposed Suppliers as Children was submitted] ) " +
                        "and " +
                        "(( [Default Currency on element] eq [Default Currency persisted] ) or not  [Default Currency was submitted] ) " +
                        "and " +
                        "(( [Integration System on element] eq [Integration System persisted] ) or not [Integration System was submitted] ) " +
                        "and " +
                        "(( [Spend Category or Hierarchy on element] eq [Spend Category or Hierarchy persisted] ) or not [Spend Category or Hierarchy was submitted] ) " +
                        "and " +
                        "(( [Taxpayer ID Number Type on element] eq [Taxpayer ID Number Type persisted] ) or not [Taxpayer ID Number Type was submitted] ) " +
                        "and " +
                        "(( [Deprecated 1099 Payee on element] eq [Deprecated 1099 Payee persisted] ) or not [Deprecated 1099 Payee was submitted] ) " +
                        "and " +
                        "(( [Default Additional Reference Type on element] eq [Default Additional Reference Type persisted] ) or not  " +
                        "[Default Additional Reference Type was submitted] ) " +
                        "and " +
                        "(( [Default Spend Category on element] eq [Default Spend Category persisted] ) or not  " +
                        "[Default Spend Category was submitted] ) " +
                        "and " +
                        "(( [Supplier Minimum Order Amount on element] eq [Supplier Minimum Order Amount persisted] ) or not [Supplier Minimum Order Amount was submitted] ) " +
                        "and " +
                        "(( [Minimum Order Amount Currency on element] eq [Minimum Order Amount Currency persisted] ) or not [Minimum Order Amount Currency was submitted] ) " +
                        "and " +
                        "(( [Change Order Issue Option on element] eq [Change Order Issue Option persisted] ) or not [Change Order Issue Option was submitted] ) " +
                        "and " +
                        "(( [Supplier Change Source on element] eq [Supplier Change Source persisted] ) or not [Supplier Change Source was submitted] ) " +
                        "and " +
                        "(( [Edit Portal Taxes on element] eq [Edit Portal Taxes persisted] ) or not [Edit Portal Taxes was submitted] ) " +
                        "and " +
                        "(( [Supplier Note Follow-Up Date on element] eq [Supplier Note Follow-Up Date persisted] ) or not [Supplier Note Follow-Up Date was submitted] ) " +
                        "and " +
                        "(( [Default Procurement Item on element] eq [Default Procurement Item persisted] ) or not  " +
                        "[Default Procurement Item was submitted] ) " +
                        "and " +
                        "(( [Payment Terms Based on Invoice Received Date on element] eq [Payment Terms Based on Invoice Received Date Persisted] ) or not  " +
                        "[Payment Terms Based on Invoice Received Date was submitted] ) " +
                        "and " +
                        "([Restricted Companies for Supplier Contacts are Not Changed or Not Submitted] )");
        // Note that we do not verify the parsed expression result as it is too large to be practical.
        // Instead, we check that the expression was parsed without error, and returned a non-null result.
        assertNotNull(result);
    }

    /**
     * Ensures that complex expressions are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_9_Issue_263() {
        final var expected = Disjunction.of(
                Conjunction.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("isMeasureExpression"))),
                        Disjunction.of(
                                AnyInCommon.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("thisInstance"))),
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("calculation"))
                                        )
                                ),
                                AnyInCommon.of(
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("thisInstance"))),
                                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("constant"))
                                        )
                                )
                        )
                ),
                Conjunction.of(
                        Negation.of(
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("isMeasureExpression"))
                                )
                        ),
                        AnyInCommon.of(
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("thisInstance"))),
                                VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("calculation"))
                                )
                        )
                )
        );
        final var result = parser.parse("( ([isMeasureExpression]) and (([thisInstance] ac [calculation]) or ([thisInstance] ac [constant])) ) or (not ([isMeasureExpression]) and ([thisInstance] ac [calculation]) )");
        assertGraph(expected, result);
    }

    /**
     * Ensures that complex expressions are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_10_Issue_263() {
        final var expected = Disjunction.of(
                EqualTo.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("external association by name"))),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("current external association")))
                ),
                Negation.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("external association by name")))
                )
        );
        final var result = parser.parse("([external association by name] eq [current external association]) or not [external association by name]");
        assertGraph(expected, result);
    }

    /**
     * Ensures that complex expressions are parsed correctly.
     */
    @Test
    void shouldCorrectlyParseExpression_11_Issue_263() {
        final var expected = Conjunction.of(
                EqualTo.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("Top Level of this Level"))),
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("Top Level parm")))
                ),
                NotEqualTo.of(
                        VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("Level")))
                        , VariableUsage.of(codeModel, VariableName.of(IrreducibleName.of("Top Level parm")))
                )
        );
        final var result = parser.parse("([Top Level of this Level] eq [Top Level parm]) and ([Level] ne [Top Level parm])");
        assertGraph(expected, result);
    }

    private static Stream<Arguments> provideStringsForShouldThrowOnUnknownToken() {
        return Stream.of(
                Arguments.of("1 + 2 # 3", 7),
                Arguments.of("1 thing 2", 3),
                Arguments.of("{1 + 1}", 1),
                Arguments.of("1 + 2)", 6),
                Arguments.of("(1 + 2))", 8),
                Arguments.of("1 2", 3)
        );
    }

    private void assertGraph(Expression expected, Expression actual) {

        var expectedClass = expected.getClass();
        var actualClass = actual.getClass();

        if (!expectedClass.isAssignableFrom(actualClass)) {
            throw new RuntimeException("Unmatched node");
        }

        final var leftMethod = safeGetMethod(expectedClass, "left");
        final var rightMethod = safeGetMethod(expectedClass, "right");

        if (leftMethod != null && rightMethod != null) {
            try
            {
                assertGraph((Expression)leftMethod.invoke(expected), (Expression)leftMethod.invoke(actual));
                assertGraph((Expression)rightMethod.invoke(expected), (Expression)rightMethod.invoke(actual));
            }
            catch (Exception e) {
                throw new RuntimeException("Unmatched node");
            }
            return;
        }
        else if (leftMethod != null || rightMethod != null) {
            throw new RuntimeException("Unmatched node");
        }

        final var expressionMethod = safeGetMethod(expectedClass, "expression");
        if (expressionMethod != null) {
            try
            {
                assertGraph((Expression)expressionMethod.invoke(expected), (Expression)expressionMethod.invoke(actual));
            }
            catch (Exception e) {
                throw new RuntimeException("Unmatched node");
            }
            return;
        }

        if (expected instanceof FunctionUsage expectedFunction) {
            if (actual instanceof FunctionUsage actualFunction) {
                assertEquals(expectedFunction.functionName(), actualFunction.functionName());
                final var expectedArgs = expectedFunction.arguments().toArray();
                final var actualArgs = actualFunction.arguments().toArray();
                assertEquals(expectedArgs.length, actualArgs.length);
                for (var i = 0; i < expectedArgs.length - 1; i++) {
                    assertGraph((Expression)expectedArgs[i], (Expression)actualArgs[i]);
                }
            }
        }
        else if (expected instanceof Literal<?> ||
            expected instanceof VariableUsage) {
            assertEquals(expected, actual);
        }
        else {
            throw new RuntimeException("Unmatched node");
        }
    }

    private Method safeGetMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }
}
