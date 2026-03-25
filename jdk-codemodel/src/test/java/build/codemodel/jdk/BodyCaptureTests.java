package build.codemodel.jdk;

import build.codemodel.expression.NumericLiteral;
import build.codemodel.expression.StringLiteral;
import build.codemodel.jdk.descriptor.FieldInitializerDescriptor;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
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
}
