package build.codemodel.imperative;

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.JsonTransport;
import build.codemodel.expression.BooleanLiteral;
import build.codemodel.expression.NumericLiteral;
import build.codemodel.expression.VariableUsage;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.transport.IrreducibleNameTransformer;
import build.codemodel.foundation.transport.ModuleNameTransformer;
import build.codemodel.foundation.transport.NamespaceTransformer;
import build.codemodel.foundation.transport.TypeNameTransformer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Marshalling tests for various {@link Marshal}able classes in the objectoriented-codemodel naming package.
 *
 * @author tim.berston
 * @since May-2025
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
     * Ensures that {@link Assignment} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallAssignment()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(Assignment.of(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    ModuleName.of("java.lang", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("Object"))),
            NumericLiteral.of(
                codeModel,
                10)));
    }

    /**
     * Ensures that {@link Block} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallBlock()
        throws IOException {

        final var firstAssignment = Assignment.of(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    ModuleName.of("java.lang", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("x"))),
            NumericLiteral.of(
                codeModel,
                5));

        final var secondAssignment = Assignment.of(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    ModuleName.of("java.lang", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("y"))),
            NumericLiteral.of(
                codeModel,
                10));

        final var block = Block.of(firstAssignment, secondAssignment);

        marshallAndTransportAndUnMarshalAndAssert(block);
    }

    /**
     * Ensures that {@link If} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallIf()
        throws IOException {

        final var condition = BooleanLiteral.of(codeModel, true);

        final var thenStatement = Assignment.of(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    ModuleName.of("java.lang", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("result"))),
            NumericLiteral.of(
                codeModel,
                1));

        final var elseStatement = Assignment.of(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    ModuleName.of("java.lang", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("result"))),
            NumericLiteral.of(
                codeModel,
                0));

        final var ifStatement = If.of(condition, thenStatement, Optional.of(elseStatement));

        marshallAndTransportAndUnMarshalAndAssert(ifStatement);
    }

    /**
     * Ensures that {@link Return} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallReturn()
        throws IOException {

        final var returnStatement = Return.of(
            NumericLiteral.of(
                codeModel,
                42));

        marshallAndTransportAndUnMarshalAndAssert(returnStatement);
    }

    /**
     * Ensures that {@link While} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallWhile()
        throws IOException {

        final var condition = BooleanLiteral.of(codeModel, true);

        final var loopBody = Assignment.of(
            VariableUsage.of(
                codeModel,
                VariableName.of(
                    ModuleName.of("java.lang", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("counter"))),
            NumericLiteral.of(
                codeModel,
                1));

        final var whileStatement = While.of(condition, loopBody);

        marshallAndTransportAndUnMarshalAndAssert(whileStatement);
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
