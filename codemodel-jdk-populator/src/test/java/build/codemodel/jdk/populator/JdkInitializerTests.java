package build.codemodel.jdk.populator;

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
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.descriptor.Default;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.descriptor.Native;
import build.codemodel.jdk.descriptor.NonSealed;
import build.codemodel.jdk.descriptor.PermitsTypeDescriptor;
import build.codemodel.jdk.descriptor.Sealed;
import build.codemodel.jdk.descriptor.Strictfp;
import build.codemodel.jdk.descriptor.Synchronized;
import build.codemodel.jdk.descriptor.Transient;
import build.codemodel.jdk.descriptor.Volatile;
import build.codemodel.jdk.expression.NewObject;
import build.codemodel.jdk.populator.descriptor.SourceLocation;
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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.element.ExecutableElement;
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Consumer");
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Box");
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Wrapper");
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Pair");
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

        // the static factory method is directly reachable as a part of its declaring type
        final var ofMethod = personFactoryTd.parts(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("of"))
            .findFirst()
            .orElseThrow();

        assertThat(ofMethod.getFormalParameterCount()).isZero();
        assertThat(ofMethod.returnType()).isInstanceOf(SpecificTypeUsage.class);
        assertThat(((SpecificTypeUsage) ofMethod.returnType()).typeName().name().toString())
            .isEqualTo("AbstractPerson");

        // traversing the method's own parts reaches its return type as a TypeUsage
        assertThat(ofMethod.parts().toList()).contains(ofMethod.returnType());
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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Plain");
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
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Annotated");
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

    /**
     * A record with no explicit canonical/compact constructor still gets a synthesized
     * {@code MethodTree} for it once attribution runs, stamped with a real {@code start} (the
     * record name's own position) but no {@code end} at all — it was never actually written. That
     * used to leak through as a spurious constructor position duplicating the record's own (e.g. in
     * {@code documentSymbol}); it should now be dropped instead of stamped.
     */
    @Test
    void shouldNotStampPositionOnImplicitRecordConstructor() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Config",
            """
                package com.example;
                public record Config(boolean http, java.util.List<String> roots) {
                }
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Config");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var ctor = descriptor.traits(ConstructorDescriptor.class).findFirst().orElseThrow();

        assertThat(ctor.getTrait(SourceLocation.FilePosition.class))
            .as("implicit canonical constructor has no real source extent")
            .isEmpty();
    }

    /**
     * An explicit compact constructor, by contrast, was actually written and has a real body —
     * it must still get a proper position.
     */
    @Test
    void shouldStampPositionOnExplicitCompactConstructor() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Config",
            """
                package com.example;
                public record Config(boolean http) {
                    public Config {
                    }
                }
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Config");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var ctor = descriptor.traits(ConstructorDescriptor.class).findFirst().orElseThrow();

        assertThat(ctor.getTrait(SourceLocation.FilePosition.class))
            .as("explicit compact constructor has a real, written source extent")
            .isPresent();
    }

    /**
     * A record with no explicit members should still expose the compiler-synthesized accessor
     * methods (one per component) and the overridden {@code toString}/{@code equals}/
     * {@code hashCode} methods as {@link MethodDescriptor} traits. Currently these implicit
     * members are missing entirely because {@code processMembers} only walks the source-level
     * {@code ClassTree}, which never contains members the author didn't write.
     */
    @Test
    void shouldExposeImplicitRecordMembers() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Point",
            """
                package com.example;
                public record Point(int x, int y) {
                }
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Point");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var methodNames = descriptor.traits(MethodDescriptor.class)
            .map(m -> m.methodName().name().toString())
            .toList();

        assertThat(methodNames)
            .as("implicit record accessors and Object overrides should be modeled as methods")
            .contains("x", "y", "toString", "equals", "hashCode");
    }

    /**
     * When a record author writes their own accessor or {@code Object} override, that method is
     * already modeled from its {@code MethodTree} via the normal explicit-member path. The
     * implicit-member pass must recognize it as already written (by {@link ExecutableElement}
     * identity) and skip it, rather than adding a second, source-less {@link MethodDescriptor} for
     * the same method.
     */
    @Test
    void shouldNotDuplicateExplicitlyOverriddenRecordAccessor() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Point",
            """
                package com.example;
                public record Point(int x, int y) {
                    @Override
                    public String toString() {
                        return "(" + x + ", " + y + ")";
                    }
                }
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Point");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var toStringMethods = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("toString"))
            .toList();

        assertThat(toStringMethods)
            .as("an explicitly written toString should not also be modeled as an implicit member")
            .hasSize(1);
        assertThat(toStringMethods.getFirst().getTrait(SourceLocation.FilePosition.class))
            .as("the surviving toString descriptor should be the explicit, source-backed one")
            .isPresent();
    }

    /**
     * Implicit record members were never written, so unlike explicit methods they must not carry a
     * {@link SourceLocation.FilePosition} or a {@link MethodBodyDescriptor}.
     */
    @Test
    void shouldNotStampSourceLocationOrBodyOnImplicitRecordMember() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Point",
            """
                package com.example;
                public record Point(int x, int y) {
                }
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Point");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var xAccessor = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("x"))
            .findFirst()
            .orElseThrow();

        assertThat(xAccessor.getTrait(SourceLocation.FilePosition.class))
            .as("implicit accessor has no real source extent")
            .isEmpty();
        assertThat(xAccessor.getTrait(MethodBodyDescriptor.class))
            .as("implicit accessor has no modeled body")
            .isEmpty();
    }

    /**
     * Enums get the same kind of compiler-synthesized methods as records ({@code values()},
     * {@code valueOf(String)}) that never gain a {@code MethodTree}, but {@code processMembers}
     * only calls {@code processImplicitRecordMethods} when {@code classTree.getKind() ==
     * Tree.Kind.RECORD} — there is no analogous {@code Tree.Kind.ENUM} branch, so these implicit
     * members are missing entirely.
     */
    @Test
    void shouldExposeImplicitEnumMembers() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Color",
            """
                package com.example;
                public enum Color {
                    RED, GREEN, BLUE;
                }
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Color");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var methodNames = descriptor.traits(MethodDescriptor.class)
            .map(m -> m.methodName().name().toString())
            .toList();

        assertThat(methodNames)
            .as("implicit enum values()/valueOf() should be modeled as methods")
            .contains("values", "valueOf");
    }

    @Test
    void shouldCaptureTransientOnField() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public class Foo { private transient int x; private int y; }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var x = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("x"))
            .findFirst().orElseThrow();
        final var y = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("y"))
            .findFirst().orElseThrow();

        assertThat(x.getTrait(Transient.class))
            .as("transient field x should carry the Transient trait")
            .contains(Transient.TRANSIENT);
        assertThat(y.getTrait(Transient.class))
            .as("non-transient field y should not carry the Transient trait")
            .isEmpty();
    }

    @Test
    void shouldCaptureVolatileOnField() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public class Foo { private volatile int x; private int y; }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var x = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("x"))
            .findFirst().orElseThrow();
        final var y = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("y"))
            .findFirst().orElseThrow();

        assertThat(x.getTrait(Volatile.class))
            .as("volatile field x should carry the Volatile trait")
            .contains(Volatile.VOLATILE);
        assertThat(y.getTrait(Volatile.class))
            .as("non-volatile field y should not carry the Volatile trait")
            .isEmpty();
    }

    @Test
    void shouldCaptureSynchronizedOnMethod() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public class Foo { public synchronized void bar() {} public void baz() {} }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var bar = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var baz = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("baz"))
            .findFirst().orElseThrow();

        assertThat(bar.getTrait(Synchronized.class))
            .as("synchronized method bar() should carry the Synchronized trait")
            .contains(Synchronized.SYNCHRONIZED);
        assertThat(baz.getTrait(Synchronized.class))
            .as("non-synchronized method baz() should not carry the Synchronized trait")
            .isEmpty();
    }

    @Test
    void shouldCaptureNativeOnMethod() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public class Foo { public native void bar(); public void baz() {} }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var bar = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var baz = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("baz"))
            .findFirst().orElseThrow();

        assertThat(bar.getTrait(Native.class))
            .as("native method bar() should carry the Native trait")
            .contains(Native.NATIVE);
        assertThat(baz.getTrait(Native.class))
            .as("non-native method baz() should not carry the Native trait")
            .isEmpty();
    }

    @Test
    void shouldCaptureStrictfpOnMethod() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public class Foo { public strictfp void bar() {} public void baz() {} }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var bar = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var baz = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("baz"))
            .findFirst().orElseThrow();

        assertThat(bar.getTrait(Strictfp.class))
            .as("strictfp method bar() should carry the Strictfp trait")
            .contains(Strictfp.STRICTFP);
        assertThat(baz.getTrait(Strictfp.class))
            .as("non-strictfp method baz() should not carry the Strictfp trait")
            .isEmpty();
    }

    @Test
    void shouldCaptureSealedAndPermitsOnType() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            """
                package com.example;
                public sealed class Foo permits Bar {}
                final class Bar extends Foo {}
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(descriptor.getTrait(Sealed.class))
            .as("sealed type Foo should carry the Sealed trait")
            .contains(Sealed.SEALED);

        final var permittedTypeNames = descriptor.traits(PermitsTypeDescriptor.class)
            .map(permits -> permits.parentTypeUsage().typeName().name().toString())
            .toList();

        assertThat(permittedTypeNames)
            .as("sealed type Foo should record Bar as a permitted subtype")
            .containsExactly("Bar");
    }

    @Test
    void shouldCaptureNonSealedOnType() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            """
                package com.example;
                public sealed class Foo permits Bar {}
                non-sealed class Bar extends Foo {}
                """);
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Bar");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(descriptor.getTrait(NonSealed.class))
            .as("non-sealed type Bar should carry the NonSealed trait")
            .contains(NonSealed.NON_SEALED);
    }

    @Test
    void shouldCaptureDefaultOnInterfaceMethod() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo",
            "package com.example; public interface Foo { default void bar() {} void baz(); }");
        final var codeModel = runInternal(new JdkInitializer(List.of(), List.of(), List.of(source)));
        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var bar = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var baz = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("baz"))
            .findFirst().orElseThrow();

        assertThat(bar.getTrait(Default.class))
            .as("default method bar() should carry the Default trait")
            .contains(Default.DEFAULT);
        assertThat(baz.getTrait(Default.class))
            .as("non-default method baz() should not carry the Default trait")
            .isEmpty();
    }

    /**
     * Regression test for a stability gap: {@link JdkInitializer#initialize} only resets the
     * {@code initialized} guard when the scan fails with {@link java.io.IOException}. Any other
     * exception raised mid-scan (an NPE/CCE, or here a listener-thrown {@link RuntimeException})
     * leaves the guard permanently {@code true}, so a retry on the same instance wrongly hits
     * {@link IllegalStateException} instead of re-attempting the scan the way an IOException
     * failure would allow.
     */
    @Test
    void shouldAllowRetryAfterNonIOExceptionDuringScan() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Broken",
            """
                package com.example;
                public class Broken {
                    private com.example.Missing dep;
                }
                """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source))
            .withDiagnosticListener(d -> {
                if (d.getKind() == Diagnostic.Kind.ERROR) {
                    throw new RuntimeException("boom");
                }
            });

        final var firstCodeModel = new JDKCodeModel(new NonCachingNameProvider());
        assertThatThrownBy(() -> initializer.initialize(firstCodeModel))
            .as("a non-IOException raised mid-scan should propagate out of initialize()");

        final var secondCodeModel = new JDKCodeModel(new NonCachingNameProvider());
        assertThatThrownBy(() -> initializer.initialize(secondCodeModel))
            .as("JdkInitializer should reset its guard after ANY scan failure, not just "
                + "IOException, the same way it already does for IOException -- today it stays "
                + "permanently \"initialized\" and every retry hits IllegalStateException instead "
                + "of re-attempting the scan")
            .isNotInstanceOf(IllegalStateException.class);
    }

    /**
     * Best-effort regression test for the non-atomic check-then-set on {@code initialized}: two
     * threads are barrier-released together to race into {@link JdkInitializer#initialize} on the
     * same instance. With no synchronization, both can observe {@code initialized == false} before
     * either writes {@code true}, letting both proceed instead of exactly one winning and the
     * other hitting {@link IllegalStateException}.
     * <p>
     * Note: the unsynchronized race window is a handful of bytecode instructions wide, so this
     * test may not reliably fail against the current buggy implementation on every run/machine --
     * it is a probabilistic detector, not a guaranteed one. Once the guard is made atomic
     * (e.g. {@code AtomicBoolean.compareAndSet}), the invariant holds structurally and this test
     * passes deterministically, so it remains a valid permanent regression guard either way.
     */
    @Test
    void shouldAtomicallyGuardConcurrentInitializeCalls() throws InterruptedException {
        final int iterations = 50;
        for (int i = 0; i < iterations; i++) {
            final var initializer = new JdkInitializer(List.of(), List.of(), List.of());
            final var barrier = new CyclicBarrier(2);
            final var successCount = new AtomicInteger();
            final var illegalStateCount = new AtomicInteger();
            final Runnable attempt = () -> {
                try {
                    barrier.await();
                    initializer.initialize(new JDKCodeModel(new NonCachingNameProvider()));
                    successCount.incrementAndGet();
                } catch (final IllegalStateException e) {
                    illegalStateCount.incrementAndGet();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            };
            final var t1 = new Thread(attempt);
            final var t2 = new Thread(attempt);
            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertThat(successCount.get())
                .as("exactly one of two racing initialize() calls on the same instance should "
                    + "succeed -- a non-atomic check-then-set guard can let both past the check "
                    + "before either flips the flag (iteration %d)", i)
                .isEqualTo(1);
            assertThat(illegalStateCount.get())
                .as("iteration %d", i)
                .isEqualTo(1);
        }
    }
}
