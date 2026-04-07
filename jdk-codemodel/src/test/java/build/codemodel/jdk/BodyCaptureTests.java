package build.codemodel.jdk;

import build.codemodel.expression.NumericLiteral;
import build.codemodel.expression.StringLiteral;
import build.codemodel.imperative.Block;
import build.codemodel.imperative.Return;
import build.codemodel.jdk.descriptor.FieldInitializerDescriptor;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.Lambda;
import build.codemodel.jdk.statement.CatchClause;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.jdk.statement.Try;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for method body and field initializer capture via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class BodyCaptureTests {

    @Test
    void shouldCaptureMethodBodyAsBlock() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.SimplePerson", """
                package build.codemodel.jdk.example;
                public class SimplePerson {
                    private String name;
                    public SimplePerson(String name) { this.name = name; }
                    public String getName() { return name; }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.SimplePerson");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        // constructor has body
        final var ctor = descriptor.traits(ConstructorDescriptor.class).findFirst().orElseThrow();
        assertThat(ctor.getTrait(MethodBodyDescriptor.class)).isPresent();
        assertThat(ctor.getTrait(MethodBodyDescriptor.class).get().body().statements()).isNotEmpty();

        // getName has body
        final var getName = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("getName"))
            .findFirst().orElseThrow();
        assertThat(getName.getTrait(MethodBodyDescriptor.class)).isPresent();
        assertThat(getName.getTrait(MethodBodyDescriptor.class).get().body().statements()).isNotEmpty();

        // field 'name' has no initializer
        final var nameField = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("name"))
            .findFirst().orElseThrow();
        assertThat(nameField.getTrait(FieldInitializerDescriptor.class)).isEmpty();
    }

    @Test
    void shouldCaptureFieldInitializer() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Defaults", """
                package build.codemodel.jdk.example;
                public class Defaults {
                    private int count = 0;
                    private String label = "hello";
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Defaults");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var count = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("count"))
            .findFirst().orElseThrow();
        assertThat(count.getTrait(FieldInitializerDescriptor.class)).isPresent();
        assertThat(count.getTrait(FieldInitializerDescriptor.class).get().initializer())
            .isInstanceOf(NumericLiteral.class);

        final var label = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("label"))
            .findFirst().orElseThrow();
        assertThat(label.getTrait(FieldInitializerDescriptor.class)).isPresent();
        assertThat(label.getTrait(FieldInitializerDescriptor.class).get().initializer())
            .isInstanceOf(StringLiteral.class);
    }

    @Test
    void shouldCaptureLambdaParameters() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Sorter", """
                package build.codemodel.jdk.example;
                import java.util.Comparator;
                public class Sorter {
                    public Comparator<String> comparator() {
                        return (String a, String b) -> a.compareTo(b);
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Sorter");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("comparator"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var lambda = body.statements()
            .map(s -> s instanceof Return r ? r.expression() : null)
            .filter(e -> e instanceof Lambda)
            .map(e -> (Lambda) e)
            .findFirst().orElseThrow();

        final var params = lambda.parameters().toList();
        assertThat(params).hasSize(2);
        assertThat(params.get(0).name()).isEqualTo("a");
        assertThat(params.get(0).typeName()).isEqualTo("String");
        assertThat(params.get(1).name()).isEqualTo("b");
        assertThat(params.get(1).typeName()).isEqualTo("String");
    }

    @Test
    void shouldCaptureTryWithResources() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.ResourceUser", """
                package build.codemodel.jdk.example;
                import java.io.InputStream;
                import java.io.IOException;
                public class ResourceUser {
                    public InputStream open() { return null; }
                    public void use() throws IOException {
                        try (InputStream is = open()) {
                            is.read();
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.ResourceUser");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("use"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var tryStmt = body.statements()
            .filter(s -> s instanceof Try)
            .map(s -> (Try) s)
            .findFirst().orElseThrow();

        final var resources = tryStmt.resources().toList();
        assertThat(resources).hasSize(1);
        assertThat(resources.getFirst()).isInstanceOf(LocalVariableDeclaration.class);
        assertThat(((LocalVariableDeclaration) resources.getFirst()).name()).isEqualTo("is");
    }

    @Test
    void shouldCaptureMultiCatchExceptionTypes() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.MultiCatcher", """
                package build.codemodel.jdk.example;
                import java.io.IOException;
                public class MultiCatcher {
                    public void run() {
                        try {
                            throw new IOException();
                        } catch (IOException | RuntimeException e) {
                            // handle
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.MultiCatcher");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var tryStmt = body.statements()
            .filter(s -> s instanceof Try)
            .map(s -> (Try) s)
            .findFirst().orElseThrow();

        final var catchClause = tryStmt.catches().findFirst().orElseThrow();
        final var exTypeNames = catchClause.exceptionTypeNames().toList();
        assertThat(exTypeNames).containsExactlyInAnyOrder("IOException", "RuntimeException");
    }
}
