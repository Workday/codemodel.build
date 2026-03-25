package build.codemodel.expression.naming;

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.JsonTransport;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.transport.IrreducibleNameTransformer;
import build.codemodel.foundation.transport.ModuleNameTransformer;
import build.codemodel.foundation.transport.NamespaceTransformer;
import build.codemodel.foundation.transport.TypeNameTransformer;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;

/**
 * Marshalling tests for various {@link Marshal}able classes.
 *
 * @author tim.berston
 * @since May-2025
 */
class MarshallingTests {

    private NameProvider nameProvider;

    @BeforeEach
    void init() {
        nameProvider = new NonCachingNameProvider();
    }

    /**
     * Ensures that {@link VariableName} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallVariableName()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(VariableName.of(IrreducibleName.of("test")));
        marshallAndTransportAndUnMarshalAndAssert(VariableName.of(
            ModuleName.of("module", this.nameProvider),
            this.nameProvider.getNamespace("namespace"),
            Optional.of(TypeName.of(
                ModuleName.of("module", this.nameProvider),
                Optional.empty(),
                Optional.empty(),
                IrreducibleName.of("type"))),
            IrreducibleName.of("test")));
    }

    /**
     * Ensures that {@link FunctionName} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallFunctionName()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(FunctionName.of(IrreducibleName.of("test")));
        marshallAndTransportAndUnMarshalAndAssert(FunctionName.of(
            ModuleName.of("module", this.nameProvider),
            this.nameProvider.getNamespace("namespace"),
            Optional.of(TypeName.of(
                ModuleName.of("module", this.nameProvider),
                Optional.empty(),
                Optional.empty(),
                IrreducibleName.of("type"))),
            IrreducibleName.of("test")));
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
