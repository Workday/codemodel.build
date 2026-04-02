package build.codemodel.objectoriented.descriptor;

import build.base.foundation.Lazy;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.JsonTransport;
import build.codemodel.expression.Expression;
import build.codemodel.expression.StringLiteral;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
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
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.VoidTypeUsage;
import build.codemodel.objectoriented.ObjectOrientedCodeModel;
import build.codemodel.objectoriented.naming.MethodName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Marshalling tests for various {@link Marshal}able classes in the objectoriented-codemodel descriptor package.
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
        codeModel = new ObjectOrientedCodeModel(nameProvider);
    }

    /**
     * Ensures that {@link MethodDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallMethodDescriptor()
        throws IOException {

        final var typeName = TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestClass"));

        final var typeDescriptor = ClassTypeDescriptor.of(codeModel, typeName);

        final var methodName = MethodName.of(
            typeName.moduleName(),
            typeName.namespace(),
            Optional.of(typeName),
            IrreducibleName.of("testMethod"));

        final var returnType = VoidTypeUsage.create(codeModel);
        final var formalParameters = Stream.of(
            FormalParameterDescriptor.of(
                codeModel,
                Optional.of(IrreducibleName.of("param1")),
                VoidTypeUsage.create(codeModel)));

        typeDescriptor.addTrait(MethodDescriptor.of(
            typeDescriptor,
            methodName,
            returnType,
            formalParameters));

        marshallAndTransportAndUnMarshalAndAssert(typeDescriptor);
    }

    /**
     * Ensures that {@link FieldDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallFieldDescriptor()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(FieldDescriptor.of(
            codeModel,
            IrreducibleName.of("testField"),
            VoidTypeUsage.create(codeModel)));
    }

    /**
     * Ensures that {@link ConstructorDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallConstructorDescriptor()
        throws IOException {

        final var typeName = TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestClass"));

        final var typeDescriptor = ClassTypeDescriptor.of(codeModel, typeName);

        final var formalParameters = Stream.of(
            FormalParameterDescriptor.of(
                codeModel,
                Optional.of(IrreducibleName.of("param1")),
                VoidTypeUsage.create(codeModel)));

        marshallAndTransportAndUnMarshalAndAssert(ConstructorDescriptor.of(
            typeDescriptor,
            formalParameters));
    }

    /**
     * Ensures that {@link ParameterizedTypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallParameterizedTypeDescriptor()
        throws IOException {

        final var typeVariables = Stream.of(
            TypeVariableUsage.of(
                codeModel,
                TypeName.of(
                    ModuleName.of("com.example", this.nameProvider),
                    Optional.empty(),
                    Optional.empty(),
                    IrreducibleName.of("T")),
                // TODO: This doesn't handle Optional.empty()
                Optional.of(Lazy.of(VoidTypeUsage.create(codeModel))),
                Optional.of(Lazy.of(UnknownTypeUsage.create(codeModel)))));

        marshallAndTransportAndUnMarshalAndAssert(ParameterizedTypeDescriptor.of(
            codeModel,
            typeVariables));
    }

    /**
     * Ensures that {@link MethodUsage} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallMethodUsage()
        throws IOException {

        final var typeName = TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestClass"));

        final var expression = StringLiteral.of(codeModel, "test");

        final var methodName = MethodName.of(
            typeName.moduleName(),
            typeName.namespace(),
            Optional.of(typeName),
            IrreducibleName.of("testMethod"));

        final var arguments = Stream.<Expression>of(
            StringLiteral.of(codeModel, "arg1"));

        marshallAndTransportAndUnMarshalAndAssert(MethodUsage.of(
            expression,
            methodName,
            arguments));
    }

    /**
     * Ensures that {@link ClassTypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallClassTypeDescriptor()
        throws IOException {

        final var typeName = TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestClass"));

        marshallAndTransportAndUnMarshalAndAssert(ClassTypeDescriptor.of(
            codeModel,
            typeName));
    }

    /**
     * Ensures that {@link InterfaceTypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallInterfaceTypeDescriptor()
        throws IOException {

        final var typeName = TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestInterface"));

        marshallAndTransportAndUnMarshalAndAssert(InterfaceTypeDescriptor.of(
            codeModel,
            typeName));
    }

    /**
     * Ensures that {@link ThisUsage} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallThisUsage()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(ThisUsage.of(codeModel));
    }

    /**
     * Ensures that {@link SuperUsage} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallSuperUsage()
        throws IOException {

        marshallAndTransportAndUnMarshalAndAssert(SuperUsage.of(codeModel));
    }

    /**
     * Ensures that {@link ImplementsTypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallImplementsTypeDescriptor()
        throws IOException {

        var namedTypeUsage = GenericTypeUsage.of(codeModel, TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestInterface")));

        marshallAndTransportAndUnMarshalAndAssert(ImplementsTypeDescriptor.of(namedTypeUsage));
    }

    /**
     * Ensures that {@link ExtendsTypeDescriptor} can be marshalled, transported and unmarshalled using a {@link JsonTransport}.
     *
     * @throws IOException if an error occurs during marshalling, transport or unmarshalling
     */
    @Test
    void shouldMarshallAndTransportAndUnmarshallExtendsTypeDescriptor()
        throws IOException {

        var namedTypeUsage = GenericTypeUsage.of(codeModel, TypeName.of(
            ModuleName.of("com.example", this.nameProvider),
            Optional.empty(),
            Optional.empty(),
            IrreducibleName.of("TestInterface")));

        marshallAndTransportAndUnMarshalAndAssert(ExtendsTypeDescriptor.of(namedTypeUsage));
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

        final var otherCodeModel = new ObjectOrientedCodeModel(nameProvider);
        marshaller.bind(CodeModel.class).to(otherCodeModel);

        // establish a String-based Reader from which to read (parse) the Json
        final var reader = new StringReader(writer.toString());

        final var parser = factory.createParser(reader);
        final Marshalled<T> transported = transport.read(parser, marshaller);

        final var unmarshalled = marshaller.unmarshal(transported);

        if (original instanceof TypeDescriptor otd && unmarshalled instanceof TypeDescriptor utd) {
            // TODO: Replace this with TypeDescriptor equality when it is repaired
            assertThat(otd.typeName())
                .isEqualTo(utd.typeName());
        }
        else {
            assertThat(original)
                .isEqualTo(unmarshalled);
        }
    }
} 
