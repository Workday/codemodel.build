package build.codemodel.jdk;

import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.JsonTransport;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.transport.ModuleNameTransformer;
import build.codemodel.foundation.transport.NamespaceTransformer;
import build.codemodel.foundation.transport.TypeNameTransformer;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;

/**
 * Tests to transport {@link CodeModel}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
class CodeModelTransportTests {

    /**
     * Ensure a {@link CodeModel} initialized with some JDK platform types can be marshalled, transported and
     * unmarshalled.
     *
     * @throws IOException should transporting fail
     */
    @Test
    void shouldTransportInitializedCodeModel()
        throws IOException {

        // establish a CodeModel
        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);

        // establish a Marshaller with the NameProvider for marshalling/unmarshalling CodeModels
        // (CodeModels have a natural dependency on NameProviders)
        final var marshaller = Marshalling.newMarshaller();
        marshaller.bind(NameProvider.class).to(nameProvider);

        // marshal the CodeModel into a Marshalled<CodeModel>
        final var marshalled = marshaller.marshal(codeModel);

        assertThat(marshalled)
            .isNotNull();

        // establish the JsonTransport to transport the CodeModel using Json
        final var transport = new JsonTransport();

        // include the CodeModel specific Transformers
        // (encoding/decoding some CodeModel types as primitive types)
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

        // establish a String-based Reader from which to read (parse) the Json
        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        // read the Marshalled<CodeModel> using the Transport
        final Marshalled<CodeModel> transported = transport.read(parser, marshaller);

        // unmarshal the CodeModel from the Marshalled<CodeModel>
        final var unmarshalled = marshaller.unmarshal(transported);

        // at least ensure that the unmarshalled isn't null!
        assertThat(unmarshalled)
            .isNotNull();

        // ensure that the TypeDescriptors are present
        assertThat(unmarshalled.typeDescriptors()
            .map(TypeDescriptor::typeName))
            .containsExactlyInAnyOrder(codeModel
                .typeDescriptors()
                .map(TypeDescriptor::typeName)
                .toArray(TypeName[]::new));
    }
}
