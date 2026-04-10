package build.codemodel.jdk;

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class JdkInitializerTests {

    /**
     * Helper used by DiscoveryTests, FieldDiscoveryTests, etc. to run a {@link JdkInitializer}
     * against a fresh {@link JDKCodeModel} and return the resulting {@link build.codemodel.foundation.CodeModel}.
     *
     * @param initializer the initializer to run
     * @return the populated type system
     */
    public static build.codemodel.foundation.CodeModel runInternal(final JdkInitializer initializer) {
        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);
        initializer.initialize(codeModel);
        return codeModel;
    }

    /**
     * Ensure {@link JdkInitializer#initialize} throws if called a second time.
     */
    @Test
    void shouldThrowWhenInitializedTwice() {
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of());
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        initializer.initialize(codeModel);

        final var anotherCodeModel = new JDKCodeModel(new NonCachingNameProvider());
        assertThatThrownBy(() -> initializer.initialize(anotherCodeModel))
            .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Ensure a CodeModel can be built from Java source files using {@link JdkInitializer}.
     */
    @Test
    void shouldBuildCodeModelFromSources() {
        final var sources = List.of(
            new File("src/test/java/build/codemodel/jdk/example/AbstractPerson.java"),
            new File("src/test/java/build/codemodel/jdk/example/Description.java"),
            new File("src/test/java/build/codemodel/jdk/example/NonAbstractPerson.java"));

        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);

        final var initializer = new JdkInitializer(sources, List.of(), List.of());
        initializer.initialize(codeModel);

        // Look up type names using the same convention as JdkInitializer (Optional.empty(), fqn)
        final var abstractPersonName =
            nameProvider.getTypeName(Optional.empty(),
                "build.codemodel.jdk.example.AbstractPerson");
        final var nonAbstractPersonName =
            nameProvider.getTypeName(Optional.empty(),
                "build.codemodel.jdk.example.NonAbstractPerson");

        final var abstractPersonDescriptor =
            codeModel.getTypeDescriptor(abstractPersonName).orElseThrow();
        final var nonAbstractPersonDescriptor =
            codeModel.getTypeDescriptor(nonAbstractPersonName).orElseThrow();

        // Fields on AbstractPerson
        assertThat(abstractPersonDescriptor.traits(FieldDescriptor.class)
            .map(f -> f.fieldName().toString())
            .toList())
            .contains("firstName", "lastName", "age", "tall");

        // Methods on AbstractPerson
        assertThat(abstractPersonDescriptor.traits(MethodDescriptor.class)
            .map(m -> m.methodName().name().toString())
            .toList())
            .contains("getFirstName", "getLastName", "getAge", "setTall", "isTall");

        // Classification on AbstractPerson
        assertThat(abstractPersonDescriptor.traits(Classification.class))
            .anyMatch(c -> c == Classification.ABSTRACT);

        // AccessModifier on AbstractPerson
        assertThat(abstractPersonDescriptor.traits(AccessModifier.class))
            .anyMatch(a -> a == AccessModifier.PUBLIC);

        // Constructor on NonAbstractPerson — stored as ConstructorDescriptor, not MethodDescriptor
        assertThat(nonAbstractPersonDescriptor.traits(ConstructorDescriptor.class).toList())
            .isNotEmpty();

        // fullName method on NonAbstractPerson — stored as MethodDescriptor
        assertThat(nonAbstractPersonDescriptor.traits(MethodDescriptor.class)
            .map(m -> m.methodName().name().toString())
            .toList())
            .contains("fullName");

        // Annotations on fullName
        final var fullNameMethod = nonAbstractPersonDescriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("fullName"))
            .findFirst()
            .orElseThrow();

        assertThat(fullNameMethod.traits(AnnotationTypeUsage.class)
            .map(a -> a.typeName().name().toString())
            .toList())
            .contains("Deprecated", "Description");
    }

    @Test
    void shouldCaptureFinalOnParameters() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public class Foo { public void bar(final int x, int y) {} }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var params = method.formalParameters().toList();
        assertThat(params.get(0).hasTrait(build.codemodel.jdk.descriptor.Final.class))
            .as("x is final").isTrue();
        assertThat(params.get(1).hasTrait(build.codemodel.jdk.descriptor.Final.class))
            .as("y is not final").isFalse();
    }

    @Test
    void shouldNotWriteJavacErrorsToStderrWhenSourceReferencesUnresolvableType() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            """
            package com.example;
            public class Foo {
                private com.example.Missing dependency;
            }
            """);

        final var captured = new ByteArrayOutputStream();
        final var original = System.err;
        System.setErr(new PrintStream(captured));
        final CodeModel codeModel;
        try {
            codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        } finally {
            System.setErr(original);
        }

        assertThat(captured.toString())
            .as("javac diagnostics must not leak to stderr")
            .isEmpty();

        // analysis still completed — Foo was discovered and the unresolvable field type degraded gracefully
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var field = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dependency"))
            .findFirst()
            .orElseThrow();
        assertThat(field.type()).isInstanceOf(UnknownTypeUsage.class);
    }

    @Test
    void shouldCaptureAnnotationsOnParameters() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            """
            package com.example;
            public class Foo {
                public void bar(@Deprecated String x) {}
            }
            """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var param = method.formalParameters().findFirst().orElseThrow();
        assertThat(param.traits(AnnotationTypeUsage.class)
            .map(a -> a.typeName().name().toString())
            .toList())
            .contains("Deprecated");
    }
}
