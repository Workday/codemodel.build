package build.codemodel.jdk;

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.jdk.expression.NewObject;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

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
     * against a fresh {@link JDKCodeModel} and return the resulting {@link JDKCodeModel}.
     *
     * @param initializer the initializer to run
     * @return the populated type system
     */
    public static JDKCodeModel runInternal(final JdkInitializer initializer) {
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
    void shouldForwardDiagnosticsToCustomListener() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Broken",
            """
                package com.example;
                public class Broken {
                    private com.example.Missing dep;
                }
                """);

        final List<Diagnostic<? extends JavaFileObject>> captured = new ArrayList<>();
        runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source))
                .withDiagnosticListener(captured::add));

        assertThat(captured)
            .as("listener installed via withDiagnosticListener must receive javac diagnostics")
            .isNotEmpty();
    }

    @Test
    void shouldResolveTypesFromClasspath() throws Exception {
        // Compile a helper class to a temp directory, then pass that directory as the classpath.
        // Without classpath support the field type would be UnknownTypeUsage.
        final Path classpathDir = Files.createTempDirectory("jdk-initializer-test-cp");

        final var helperSource = JavaFileObjects.forSourceString(
            "com.example.Helper",
            "package com.example; public class Helper {}");
        final var compiler = ToolProvider.getSystemJavaCompiler();
        try (var fm = compiler.getStandardFileManager(null, null, null)) {
            compiler.getTask(null, fm, diagnostic -> {
                },
                List.of("-d", classpathDir.toString()), null, List.of(helperSource)).call();
        }

        final var consumer = JavaFileObjects.forSourceString(
            "com.example.Consumer",
            """
                package com.example;
                public class Consumer {
                    private com.example.Helper helper;
                }
                """);

        final var codeModel = runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(consumer), List.of(classpathDir), List.of()));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Consumer");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var field = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("helper"))
            .findFirst()
            .orElseThrow();

        assertThat(field.type())
            .as("type from classpath entry should resolve, not degrade to UnknownTypeUsage")
            .isNotInstanceOf(UnknownTypeUsage.class);
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

    // -------------------------------------------------------------------------
    // Mereology integration — parts() and composition() over initialized descriptors
    // -------------------------------------------------------------------------

    @Test
    void typeDescriptorPartsContainsItsTraitsAfterInitialization() {
        final var sources = List.of(
            new File("src/test/java/build/codemodel/jdk/example/AbstractPerson.java"),
            new File("src/test/java/build/codemodel/jdk/example/Description.java"),
            new File("src/test/java/build/codemodel/jdk/example/NonAbstractPerson.java"));

        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);
        new JdkInitializer(sources, List.of(), List.of()).initialize(codeModel);

        final var typeName = nameProvider.getTypeName(Optional.empty(),
            "build.codemodel.jdk.example.AbstractPerson");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var parts = descriptor.parts().toList();
        assertThat(parts).isNotEmpty();
        assertThat(parts).anyMatch(p -> p instanceof FieldDescriptor f
            && f.fieldName().toString().equals("firstName"));
        assertThat(parts).anyMatch(p -> p instanceof MethodDescriptor m
            && m.methodName().name().toString().equals("getFirstName"));
    }

    @Test
    void fieldDescriptorPartsContainsItsTypeUsage() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Box",
            "package com.example; public class Box { public String label; }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Box");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var field = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("label"))
            .findFirst().orElseThrow();

        final var parts = field.parts().toList();
        assertThat(parts).isNotEmpty();
        assertThat(parts).anyMatch(p -> p instanceof SpecificTypeUsage s
            && s.typeName().name().toString().equals("String"));
    }

    @Test
    void genericFieldPartsContainsTypeParameter() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Wrapper",
            "package com.example; import java.util.List; public class Wrapper { public List<String> items; }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Wrapper");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var field = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("items"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(GenericTypeUsage.class);
        final var generic = (GenericTypeUsage) field.type();
        assertThat(generic.parts().toList()).isNotEmpty();
        assertThat(generic.parts().toList()).anyMatch(p -> p instanceof SpecificTypeUsage s
            && s.typeName().name().toString().equals("String"));
    }

    @Test
    void typeDescriptorCompositionTransitivelyReachesFieldTypeUsages() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Pair",
            "package com.example; import java.util.List; public class Pair { public String first; public List<Integer> second; }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Pair");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var allTypeUsages = descriptor.composition(TypeUsage.class).toList();
        assertThat(allTypeUsages).isNotEmpty();
        // String field contributes a SpecificTypeUsage
        assertThat(allTypeUsages).anyMatch(u -> u instanceof SpecificTypeUsage s
            && s.typeName().name().toString().equals("String"));
        // List<Integer> contributes a GenericTypeUsage for List and a SpecificTypeUsage for Integer
        assertThat(allTypeUsages).anyMatch(u -> u instanceof GenericTypeUsage g
            && g.typeName().name().toString().equals("List"));
        assertThat(allTypeUsages).anyMatch(u -> u instanceof SpecificTypeUsage s
            && s.typeName().name().toString().equals("Integer"));
    }

    @Test
    void localVariableInitializerTypeIsCorrectInPersonFactory() {
        final var sources = List.of(
            new File("src/test/java/build/codemodel/jdk/example/AbstractPerson.java"),
            new File("src/test/java/build/codemodel/jdk/example/Description.java"),
            new File("src/test/java/build/codemodel/jdk/example/NonAbstractPerson.java"),
            new File("src/test/java/build/codemodel/jdk/example/PersonFactory.java"));

        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);
        new JdkInitializer(sources, List.of(), List.of()).initialize(codeModel);

        final var factoryName = nameProvider.getTypeName(Optional.empty(),
            "build.codemodel.jdk.example.PersonFactory");
        final var factoryDescriptor = codeModel.getTypeDescriptor(factoryName).orElseThrow();

        final var newPersonDecl = factoryDescriptor.composition(LocalVariableDeclaration.class)
            .findFirst()
            .orElseThrow();
        // The inferred var type should resolve to NonAbstractPerson
        assertThat(newPersonDecl.type()).isInstanceOf(SpecificTypeUsage.class);
        assertThat(((SpecificTypeUsage) newPersonDecl.type()).typeName().name().toString())
            .isEqualTo("NonAbstractPerson");

        // The initializer should be a NewObject whose type is also NonAbstractPerson
        // we could of course get this from the declaration, but that's not the point
        final var initializer = factoryDescriptor.composition(NewObject.class)
            .findFirst()
            .orElseThrow();
        final var initializerType = initializer.type().orElseThrow();
        assertThat(initializerType).isInstanceOf(SpecificTypeUsage.class);
        assertThat(initializerType.as(NamedTypeUsage.class).orElseThrow().typeName().name().toString())
            .isEqualTo("NonAbstractPerson");
    }

    @Test
    void shouldTraverseMethods() {
        final var sources = List.of(
            new File("src/test/java/build/codemodel/jdk/example/AbstractPerson.java"),
            new File("src/test/java/build/codemodel/jdk/example/Description.java"),
            new File("src/test/java/build/codemodel/jdk/example/NonAbstractPerson.java"),
            new File("src/test/java/build/codemodel/jdk/example/PersonFactory.java"));

        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);

        final var initializer = new JdkInitializer(sources, List.of(), List.of());
        initializer.initialize(codeModel);

        final var personFactoryName = nameProvider.getTypeName(Optional.empty(),
            "build.codemodel.jdk.example.PersonFactory");
        final var personFactoryTd = codeModel.getTypeDescriptor(personFactoryName).orElseThrow();
        final var parts = personFactoryTd.parts().toList();
        final var composition = personFactoryTd.composition().toList();
        final var typeUsages = personFactoryTd.parts(TypeUsage.class).toList();
        final var allTypeUsages = personFactoryTd.composition(TypeUsage.class).toList();
        System.out.println();
    }

    @Test
    void withOptions_shouldForwardOptionsToJavac() {
        // var was introduced in Java 10; --release 8 must reject it
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                void bar() { var x = 1; }
            }
            """);
        final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();
        runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source))
                .withOptions(List.of("--release", "8"))
                .withDiagnosticListener(diagnostics::add));

        assertThat(diagnostics)
            .as("--release 8 must reject 'var' (Java 10+)")
            .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR);
    }

    @Test
    void withOptions_calledTwice_accumulatesBothSets() {
        // First call sets --release 17; second call adds --Werror.
        // If the second call replaced instead of appended, --release 17 would be lost
        // and the source would compile under the current release, masking the test intent.
        final var source = JavaFileObjects.forSourceString("com.example.Chained", """
            package com.example;
            public class Chained {
                void bar() { var x = 1; }
            }
            """);
        final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();
        runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source))
                .withOptions(List.of("--release", "8"))
                .withOptions(List.of("-Xlint:all"))
                .withDiagnosticListener(diagnostics::add));

        // --release 8 must still be in effect (not replaced by the second withOptions call)
        assertThat(diagnostics)
            .as("--release 8 must still reject 'var' after a second withOptions call")
            .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR);
    }

    @Test
    void withOptions_noOptionsNeededForPlainJavaSource() {
        // Confirm that withOptions is not required for ordinary source — the default path
        // (no extra options) must still produce a working CodeModel.
        final var source = JavaFileObjects.forSourceString("com.example.Plain", """
            package com.example;
            public class Plain {
                private final String value;
                public Plain(String value) { this.value = value; }
                public String getValue() { return value; }
            }
            """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Plain");
        assertThat(codeModel.getTypeDescriptor(typeName)).isPresent();
    }

    // Primitive patterns (instanceof int i) are a Java 25 preview feature — they require
    // --enable-preview and fail without it, making them a reliable signal for these tests.

    @Test
    void withEnablePreview_withoutFlag_rejectsPreviewSyntax() {
        final var source = primitivePatternSource("com.example.WithoutPreview");
        final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();
        runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source))
                .withDiagnosticListener(diagnostics::add));

        assertThat(diagnostics)
            .as("primitive patterns must be rejected without --enable-preview")
            .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR);
    }

    @Test
    void withEnablePreview_noArg_compilesPreviewSyntax() {
        final var source = primitivePatternSource("com.example.WithPreview");
        final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();
        runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source))
                .withEnablePreview()
                .withDiagnosticListener(diagnostics::add));

        assertThat(diagnostics.stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR))
            .as("withEnablePreview() should compile primitive patterns without errors")
            .isEmpty();
    }

    @Test
    void withEnablePreview_explicitVersion_compilesPreviewSyntax() {
        final var source = primitivePatternSource("com.example.WithPreviewExplicit");
        final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();
        runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source))
                .withEnablePreview(Runtime.version().feature())
                .withDiagnosticListener(diagnostics::add));

        assertThat(diagnostics.stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR))
            .as("withEnablePreview(version) should compile primitive patterns without errors")
            .isEmpty();
    }

    private static JavaFileObject primitivePatternSource(final String className) {
        return JavaFileObjects.forSourceString(className, """
            package com.example;
            public class %s {
                static boolean isSmallInt(Object o) {
                    return o instanceof int i && i < 100;
                }
            }
            """.formatted(className.substring(className.lastIndexOf('.') + 1)));
    }

    @Test
    void shouldStoreRepeatableAnnotationValuesAsAnnotationTypeUsages() {
        // When @Tag is @Repeatable(Tags.class) and a class uses @Tag twice, the compiler wraps
        // them in @Tags({@Tag(...), @Tag(...)}). The nested @Tag values must be stored as
        // AnnotationTypeUsage instances, not raw AnnotationMirror objects.
        final var source = JavaFileObjects.forSourceString("com.example.Annotated", """
            package com.example;
            
            import java.lang.annotation.*;
            
            @Retention(RetentionPolicy.RUNTIME)
            @Repeatable(Tags.class)
            @interface Tag { String value(); }
            
            @Retention(RetentionPolicy.RUNTIME)
            @interface Tags { Tag[] value(); }
            
            @Tag("foo")
            @Tag("bar")
            public class Annotated {}
            """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Annotated");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        // The type should have @Tags as its top-level annotation
        final var tagsUsage = descriptor.traits(AnnotationTypeUsage.class)
            .filter(a -> a.typeName().name().toString().equals("Tags"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected @Tags annotation"));

        // The value attribute inside @Tags should be a List of AnnotationTypeUsage, not raw mirrors
        final var valueAttr = tagsUsage.values()
            .filter(av -> av.name().toString().equals("value"))
            .findFirst()
            .orElseThrow();

        assertThat(valueAttr.value()).isInstanceOf(AnnotationValue.Value.Array.class);
        final var array = (AnnotationValue.Value.Array) valueAttr.value();
        assertThat(array.elements()).hasSize(2);
        assertThat(array.elements()).allSatisfy(item ->
            assertThat(item).as("nested annotation value should be Value.Nested, not a raw mirror")
                .isInstanceOf(AnnotationValue.Value.Nested.class));

        // And the nested @Tag types should have the right name and values
        final var tagNames = array.elements().stream()
            .map(AnnotationValue.Value.Nested.class::cast)
            .map(n -> n.annotation().typeName().name().toString())
            .toList();
        assertThat(tagNames).containsExactly("Tag", "Tag");

        final var tagValues = array.elements().stream()
            .map(AnnotationValue.Value.Nested.class::cast)
            .map(AnnotationValue.Value.Nested::annotation)
            .flatMap(AnnotationTypeUsage::values)
            .map(av -> av.value().toString())
            .toList();
        assertThat(tagValues).containsExactlyInAnyOrder("foo", "bar");
    }
}
