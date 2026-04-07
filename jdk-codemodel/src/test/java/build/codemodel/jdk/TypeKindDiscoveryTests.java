package build.codemodel.jdk;

import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.jdk.descriptor.AnnotationType;
import build.codemodel.jdk.descriptor.EnumType;
import build.codemodel.jdk.descriptor.RecordType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for enum, record, and annotation type-kind discovery via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class TypeKindDiscoveryTests {

    @Test
    void shouldDetectEnumType() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Color",
            """
            package com.example;
            public enum Color { RED, GREEN, BLUE }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);
        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Color");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        assertThat(descriptor.hasTrait(EnumType.class)).isTrue();
    }

    @Test
    void shouldDetectRecordType() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Point",
            """
            package com.example;
            public record Point(int x, int y) {}
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);
        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Point");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        assertThat(descriptor.hasTrait(RecordType.class)).isTrue();
    }

    @Test
    void shouldDetectAnnotationType() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Marker",
            """
            package com.example;
            public @interface Marker {}
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);
        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Marker");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        assertThat(descriptor.hasTrait(AnnotationType.class)).isTrue();
    }

    @Test
    void shouldCaptureEnumConstants() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Color",
            """
            package com.example;
            public enum Color { RED, GREEN, BLUE }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);
        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Color");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var constants = descriptor.traits(build.codemodel.jdk.descriptor.EnumConstantDescriptor.class)
            .map(c -> c.name().toString())
            .toList();
        assertThat(constants).containsExactlyInAnyOrder("RED", "GREEN", "BLUE");
    }

    @Test
    void shouldCaptureRecordComponents() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Point",
            """
            package com.example;
            public record Point(int x, int y) {}
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);
        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Point");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var components = descriptor.traits(build.codemodel.jdk.descriptor.RecordComponentDescriptor.class)
            .toList();
        assertThat(components).hasSize(2);
        assertThat(components.stream().map(c -> c.name().toString()).toList())
            .containsExactlyInAnyOrder("x", "y");
    }

    @Test
    void shouldCaptureAnnotationMemberDefault() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Marker",
            """
            package com.example;
            public @interface Marker {
                String value() default "hello";
                int count() default 1;
            }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);
        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Marker");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var valueMethod = descriptor.traits(build.codemodel.objectoriented.descriptor.MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("value"))
            .findFirst().orElseThrow();
        assertThat(valueMethod.getTrait(build.codemodel.jdk.descriptor.AnnotationMemberDefaultValue.class))
            .isPresent();
        assertThat(valueMethod.getTrait(build.codemodel.jdk.descriptor.AnnotationMemberDefaultValue.class)
            .orElseThrow().value())
            .isEqualTo("hello");

        final var countMethod = descriptor.traits(build.codemodel.objectoriented.descriptor.MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("count"))
            .findFirst().orElseThrow();
        assertThat(countMethod.getTrait(build.codemodel.jdk.descriptor.AnnotationMemberDefaultValue.class)
            .orElseThrow().value())
            .isEqualTo(1);
    }

    @Test
    void shouldCaptureEnclosingTypeForNestedClass() {
        final var source = com.google.testing.compile.JavaFileObjects.forSourceString(
            "com.example.Outer",
            """
            package com.example;
            public class Outer {
                public static class Inner {}
            }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(source))
            .initialize(codeModel);

        // Nested classes are registered under their canonical name (dot separator)
        final var innerName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "com.example.Outer.Inner");
        final var innerDescriptor = codeModel.getTypeDescriptor(innerName).orElseThrow();
        assertThat(innerDescriptor.hasTrait(build.codemodel.jdk.descriptor.EnclosingTypeDescriptor.class))
            .isTrue();

        final var enclosing = innerDescriptor
            .getTrait(build.codemodel.jdk.descriptor.EnclosingTypeDescriptor.class)
            .orElseThrow()
            .enclosingType();
        assertThat(enclosing.name().toString()).isEqualTo("Outer");
    }
}
