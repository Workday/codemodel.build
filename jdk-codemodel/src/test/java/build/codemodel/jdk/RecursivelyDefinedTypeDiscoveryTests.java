package build.codemodel.jdk;

import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for handling recursively defined types via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class RecursivelyDefinedTypeDiscoveryTests {

    @Test
    void shouldHandleRecursiveGenericType() {
        final var moduleInfo = JavaFileObjects.forSourceString("module-info", """
            module com.example {
            }
            """);
        final var source = JavaFileObjects.forSourceString("Discover", """
            package com.example;
            public interface Discover<T extends Discover<T>> {
                Discover<T> discover();
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source, moduleInfo));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example");
        final var typeName = codeModel.getNameProvider().getTypeName(moduleName, "com.example.Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.typeName()).isEqualTo(typeName);
        assertThat(typeDescriptor.traits(MethodDescriptor.class)).isNotEmpty();
        assertThat(typeDescriptor.traits(FieldDescriptor.class)).isEmpty();

        final var typeVar = typeDescriptor.getTrait(ParameterizedTypeDescriptor.class)
            .orElseThrow().typeVariables().findFirst().orElseThrow();
        assertThat(typeVar.toString()).isEqualTo("T extends com.example/com.example.Discover<T>");
        assertThat(typeVar.canonicalName()).isEqualTo("T extends com.example.Discover<T>");
    }
}
