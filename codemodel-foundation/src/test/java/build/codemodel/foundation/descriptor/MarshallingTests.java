package build.codemodel.foundation.descriptor;

import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.JsonTransport;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.transport.IrreducibleNameTransformer;
import build.codemodel.foundation.transport.ModuleNameTransformer;
import build.codemodel.foundation.transport.NamespaceTransformer;
import build.codemodel.foundation.transport.TypeNameTransformer;
import build.codemodel.foundation.usage.VoidTypeUsage;
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
 * Marshalling tests for various {@link Marshal}able classes.
 *
 * @author tim.berston
 * @since May-2025
 */
class MarshallingTests {

    private NameProvider nameProvider;
    private CodeModel codeModel;

    @BeforeEach
    void init() {
        this.nameProvider = new NonCachingNameProvider();
        this.codeModel = new ConceptualCodeModel(this.nameProvider);
    }

    /**
     * Ensures that {@link FormalParameterDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallFormalParameterDescriptor()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(FormalParameterDescriptor.of(
            codeModel,
            Optional.of(IrreducibleName.of("name")),
            VoidTypeUsage.create(codeModel)));
    }

    /**
     * Ensures that {@link PolymorphicTypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallPolymorphicTypeDescriptor()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(PolymorphicTypeDescriptor.of(
            this.codeModel,
            TypeName.of(
                ModuleName.of("some.module", this.nameProvider),
                Optional.empty(),
                Optional.empty(),
                IrreducibleName.of("MyType"))));
    }

    /**
     * Ensures that {@link ThrowableDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallThrowableDescriptor()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(ThrowableDescriptor.of(VoidTypeUsage.create(this.codeModel)));
    }

    /**
     * Ensures that {@link RequiresModuleDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallRequiresModuleDescriptor()
        throws IOException {

        final var requires = ModuleName.of("required.module", this.nameProvider).orElseThrow();
        final var descriptor = RequiresModuleDescriptor.of(this.codeModel, requires);

        marshallAndTransportAndUnMarshalAndAssert(descriptor);
    }

    private <T> void marshallAndTransportAndUnMarshalAndAssert(T original)
        throws IOException {
        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();

        // include the CodeModel specific Transformers
        // (encoding/decoding some CodeModel types as primitive types)
        transport.register(new IrreducibleNameTransformer(this.nameProvider));
        transport.register(new ModuleNameTransformer(this.nameProvider));
        transport.register(new NamespaceTransformer(this.nameProvider));
        transport.register(new TypeNameTransformer(this.nameProvider));

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

        final var otherCodeModel = new ConceptualCodeModel(this.nameProvider);
        marshaller.bind(CodeModel.class).to(otherCodeModel);

        // establish a String-based Reader from which to read (parse) the Json
        final var reader = new StringReader(writer.toString());

        final var parser = factory.createParser(reader);
        final Marshalled<T> transported = transport.read(parser, marshaller);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertEquals(original, unmarshalled);
    }
}
