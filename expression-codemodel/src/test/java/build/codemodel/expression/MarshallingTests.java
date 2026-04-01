package build.codemodel.expression;

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.JsonTransport;
import build.codemodel.expression.naming.FunctionName;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.expression.parsing.EmptyExpression;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.PolymorphicTypeDescriptor;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.transport.IrreducibleNameTransformer;
import build.codemodel.foundation.transport.ModuleNameTransformer;
import build.codemodel.foundation.transport.NamespaceTransformer;
import build.codemodel.foundation.transport.TypeNameTransformer;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.VoidTypeUsage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;

/**
 * Marshalling tests for various {@link Marshal}able classes.
 *
 * @author tim.berston
 * @since Apr-2025
 */
class MarshallingTests {

    private NameProvider nameProvider;
    private CodeModel codeModel;

    @BeforeEach
    void init() {
        nameProvider = new NonCachingNameProvider();
        codeModel = new ConceptualCodeModel(nameProvider);
    }

    /**
     * Ensures that {@link NumericLiteral} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallNumericLiteral()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(NumericLiteral.of(codeModel, 15));
    }

    /**
     * Ensures that {@link StringLiteral} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallStringLiteral()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(StringLiteral.of(codeModel, "test"));
    }

    /**
     * Ensures that {@link BooleanLiteral} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshalBooleanLiteral()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(BooleanLiteral.of(codeModel, true));
    }

    /**
     * Ensures that {@link VariableUsage} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallVariableUsage()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    IrreducibleName.of("test"))));
    }

    /**
     * Ensures that {@link FunctionUsage} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallFunctionUsage()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(
            FunctionUsage.of(
                codeModel,
                FunctionName.of(
                    IrreducibleName.of("test"))));
        marshallAndTransportAndUnMarshalAndAssert(
            FunctionUsage.of(
                codeModel,
                FunctionName.of(
                    IrreducibleName.of("test")),
                Stream.of(
                    NumericLiteral.of(codeModel, 15),
                    StringLiteral.of(codeModel, "bob"))));
    }

    /**
     * Ensures that {@link Negation} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallNegation()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Negation.of(BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link Negative} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallNegative()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Negative.of(NumericLiteral.of(codeModel, 15)));
    }

    /**
     * Ensures that {@link Addition} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallAddition()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Addition.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Subtraction} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallSubtraction()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Subtraction.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Division} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallDivision()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Division.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Multiplication} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallMultiplication()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Multiplication.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Modulo} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallModulo()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Modulo.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Exponent} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallExponent()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Exponent.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Conjunction} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallConjunction()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Conjunction.of(
            BooleanLiteral.of(codeModel, true),
            BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link Disjunction} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallDisjunction()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Disjunction.of(
            BooleanLiteral.of(codeModel, true),
            BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link ExclusiveDisjunction} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallExclusiveDisjunction()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(ExclusiveDisjunction.of(
            BooleanLiteral.of(codeModel, true),
            BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link Then} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallThen()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Then.of(
            BooleanLiteral.of(codeModel, true),
            BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link EqualTo} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallEqualTo()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(EqualTo.of(
            BooleanLiteral.of(codeModel, true),
            BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link NotEqualTo} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallNotEqualTo()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(NotEqualTo.of(
            BooleanLiteral.of(codeModel, true),
            BooleanLiteral.of(codeModel, true)));
    }

    /**
     * Ensures that {@link GreaterThan} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallGreaterThan()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(GreaterThan.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link GreaterThanOrEqualTo} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallGreaterThanOrEqualTo()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(GreaterThanOrEqualTo.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link LessThan} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallLessThan()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(LessThan.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link LessThanOrEqualTo} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallLessThanOrEqualTo()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(LessThanOrEqualTo.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link AnyInCommon} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallAnyInCommon()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(AnyInCommon.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link NoneInCommon} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallNoneInCommon()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(NoneInCommon.of(
            NumericLiteral.of(codeModel, 15),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link Cast} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallCast()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Cast.of(
            UnknownTypeUsage.create(codeModel),
            NumericLiteral.of(codeModel, 20)));
    }

    /**
     * Ensures that {@link TypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallFunctionDescriptor()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(FunctionDescriptor.of(
            PolymorphicTypeDescriptor.of(
                codeModel,
                TypeName.of(
                    ModuleName.of("some.module", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("MyType"))),
            FunctionName.of(
                IrreducibleName.of("test")),
            UnknownTypeUsage.create(codeModel),
            Stream.of(
                FormalParameterDescriptor.of(
                    codeModel,
                    Optional.of(IrreducibleName.of("name")),
                    VoidTypeUsage.create(codeModel)))));
    }

    /**
     * Ensures that {@link TemplateExpression} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallTemplateExpression()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(TemplateExpression.of(
            List.of(
                StringLiteral.of(codeModel, "string1"),
                VariableUsage.of(
                    codeModel,
                    VariableName.of(
                        IrreducibleName.of("var1"))))));
    }

    /**
     * Ensures that {@link EmptyExpression} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallEmptyExpression()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(EmptyExpression.of(codeModel));
    }

    /**
     * Ensures that logical expressions retain their {@code Boolean} type after a marshalling round-trip.
     * Previously the {@code @Unmarshal} constructor did not re-derive the type when absent,
     * causing {@code type()} to return {@code Optional.empty()} after deserialization.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldPreserveBooleanTypeAfterMarshallingLogicalExpressions()
        throws IOException {

        final var booleanTypeName = nameProvider.getTypeName(Boolean.class);

        for (final var expression : new Expression[] {
            Conjunction.of(BooleanLiteral.of(codeModel, true), BooleanLiteral.of(codeModel, false)),
            Disjunction.of(BooleanLiteral.of(codeModel, true), BooleanLiteral.of(codeModel, false)),
            ExclusiveDisjunction.of(BooleanLiteral.of(codeModel, true), BooleanLiteral.of(codeModel, false)),
            Negation.of(BooleanLiteral.of(codeModel, true)),
            EqualTo.of(BooleanLiteral.of(codeModel, true), BooleanLiteral.of(codeModel, false)),
            LessThan.of(NumericLiteral.of(codeModel, 1), NumericLiteral.of(codeModel, 2))
        }) {
            final var marshaller = Marshalling.newMarshaller();
            final var marshalled = marshaller.marshal(expression);

            final var transport = new build.base.transport.json.JsonTransport();
            transport.register(new build.codemodel.foundation.transport.IrreducibleNameTransformer(nameProvider));
            transport.register(new build.codemodel.foundation.transport.ModuleNameTransformer(nameProvider));
            transport.register(new build.codemodel.foundation.transport.NamespaceTransformer(nameProvider));
            transport.register(new build.codemodel.foundation.transport.TypeNameTransformer(nameProvider));

            final var factory = com.fasterxml.jackson.core.JsonFactory.builder()
                .enable(com.fasterxml.jackson.core.StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
            final var writer = new java.io.StringWriter();
            final var generator = factory.createGenerator(writer);
            transport.write(marshalled, generator);
            generator.close();

            final var otherCodeModel = new build.codemodel.foundation.ConceptualCodeModel(nameProvider);
            marshaller.bind(build.codemodel.foundation.CodeModel.class).to(otherCodeModel);

            final var parser = factory.createParser(new java.io.StringReader(writer.toString()));
            final build.base.marshalling.Marshalled<Expression> transported = transport.read(parser, marshaller);
            final var unmarshalled = marshaller.unmarshal(transported);

            final var typeName = expression.getClass().getSimpleName();
            assertTrue(unmarshalled.type().isPresent(),
                "type() must be present after round-trip for " + typeName);
            assertTrue(unmarshalled.type().orElseThrow().toString().contains(booleanTypeName.toString()),
                "type() must be Boolean after round-trip for " + typeName);
        }
    }

    private <T> void marshallAndTransportAndUnMarshalAndAssert(T original)
        throws IOException {
        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();

        // include the CodeModel specific Transformers
        // (encoding/decoding some CodeModel types as primitive types)
        transport.register(new IrreducibleNameTransformer(nameProvider));
        transport.register(new ModuleNameTransformer(nameProvider));
        transport.register(new NamespaceTransformer(nameProvider));
        transport.register(new TypeNameTransformer(nameProvider));

        // establish a JsonFactory for writing/reading Json
        final var factory = JsonFactory.builder()
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
            .build();

        // establish a String-based Writer into which to write the Json
        final var writer = new StringWriter();
        final var generator = factory.createGenerator(writer);

        // write the Marshalled<CodeModel> using the Transport
        transport.write(marshalled, generator);

        generator.close();

        final var otherCodeModel = new ConceptualCodeModel(nameProvider);
        marshaller.bind(CodeModel.class).to(otherCodeModel);

        // establish a String-based Reader from which to read (parse) the Json
        final var reader = new StringReader(writer.toString());

        final var parser = factory.createParser(reader);
        final Marshalled<T> transported = transport.read(parser, marshaller);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertEquals(original, unmarshalled);
    }
}
