package build.codemodel.jdk;

import build.codemodel.foundation.descriptor.ThrowableDescriptor;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.Varargs;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for discovering constructors via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 */
public class ConstructorDiscoveryTests {

    @Test
    void shouldCaptureConstructorThrowsDeclarations() {
        final var source = JavaFileObjects.forSourceString("com.example.Reader", """
            package com.example;
            import java.io.IOException;
            public class Reader {
                public Reader(String path) throws IOException {}
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Reader");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var ctor = descriptor.getTrait(ConstructorDescriptor.class).orElseThrow();

        assertThat(ctor.traits(ThrowableDescriptor.class)).hasSize(1);
        assertThat(((NamedTypeUsage) ctor.traits(ThrowableDescriptor.class)
            .findFirst().orElseThrow().throwable()).typeName().canonicalName())
            .isEqualTo("java.io.IOException");

        // The ThrowableDescriptor must NOT appear on the type itself
        assertThat(descriptor.traits(ThrowableDescriptor.class)).isEmpty();
    }

    @Test
    void shouldCaptureGenericConstructorTypeParameters() {
        final var source = JavaFileObjects.forSourceString("com.example.Wrapper", """
            package com.example;
            public class Wrapper {
                public <T> Wrapper(T value) {}
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Wrapper");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var ctor = descriptor.getTrait(ConstructorDescriptor.class).orElseThrow();

        assertThat(ctor.getTrait(ParameterizedTypeDescriptor.class))
            .as("generic constructor carries a ParameterizedTypeDescriptor").isPresent();
        assertThat(ctor.getTrait(ParameterizedTypeDescriptor.class).orElseThrow()
            .typeVariables().findFirst().orElseThrow().typeName().name().toString())
            .isEqualTo("T");
    }

    @Test
    void shouldCaptureConstructorAccessModifierAndVarargs() {
        final var source = JavaFileObjects.forSourceString("com.example.Printer", """
            package com.example;
            public class Printer {
                protected Printer(String prefix, String... lines) {}
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Printer");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var ctor = descriptor.getTrait(ConstructorDescriptor.class).orElseThrow();

        assertThat(ctor.getTrait(AccessModifier.class)).contains(AccessModifier.PROTECTED);

        final var params = ctor.formalParameters().toList();
        assertThat(params.get(0).hasTrait(Varargs.class)).as("prefix is not varargs").isFalse();
        assertThat(params.get(1).hasTrait(Varargs.class)).as("lines is varargs").isTrue();
    }
}
