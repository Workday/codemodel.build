package build.codemodel.jdk;

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.usage.VoidTypeUsage;
import build.codemodel.jdk.descriptor.MethodImplementationDescriptor;
import build.codemodel.jdk.descriptor.Varargs;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for discovering methods via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class MethodDiscoveryTests {

    @Test
    void shouldCreateMethodDescriptors() {
        final var source = JavaFileObjects.forSourceString("Discover", """
            public interface Discover {
                void findableVoidMethod();
            
                default void findableDefaultMethod() {
                }
            
                int methodWithParameters(String name, double size);
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var naming = codeModel.getNameProvider();
        final var typeName = naming.getTypeName(Optional.empty(), "Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.traits(FieldDescriptor.class)).isEmpty();
        assertThat(typeDescriptor.traits(MethodDescriptor.class)).hasSize(3);

        // Two void methods
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(MethodDescriptor::returnType)
            .filter(VoidTypeUsage.class::isInstance))
            .hasSize(2);

        // Method names
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(m -> m.methodName().name().toString()).toList())
            .contains("findableVoidMethod", "findableDefaultMethod", "methodWithParameters");

        // Classifications: two abstract, one concrete (default)
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(m -> m.getTrait(Classification.class).orElseThrow()).toList())
            .containsExactlyInAnyOrder(Classification.ABSTRACT, Classification.CONCRETE, Classification.ABSTRACT);

        // Only findableDefaultMethod has MethodImplementationDescriptor
        final var defaultMethod = typeDescriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("findableDefaultMethod"))
            .findFirst().orElseThrow();
        assertThat(defaultMethod.traits(MethodImplementationDescriptor.class)).isNotEmpty();

        // Non-default methods do not have MethodImplementationDescriptor
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .filter(m -> !m.methodName().name().toString().equals("findableDefaultMethod"))
            .allMatch(m -> m.traits(MethodImplementationDescriptor.class).findFirst().isEmpty()))
            .isTrue();
    }

    @Test
    void shouldMarkVarargsParameterWithVarargsTrait() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Printer", """
                package com.example;
                public class Printer {
                    public void print(String prefix, String... lines) {}
                    public void fixed(String a, String b) {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Printer");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var printMethod = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("print"))
            .findFirst().orElseThrow();
        final var params = printMethod.formalParameters().toList();

        assertThat(params.get(0).hasTrait(Varargs.class)).as("prefix is not varargs").isFalse();
        assertThat(params.get(1).hasTrait(Varargs.class)).as("lines is varargs").isTrue();

        // fixed() has no varargs parameters at all
        final var fixedMethod = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("fixed"))
            .findFirst().orElseThrow();
        assertThat(fixedMethod.formalParameters().noneMatch(p -> p.hasTrait(Varargs.class)))
            .as("fixed() has no varargs params").isTrue();
    }
}
