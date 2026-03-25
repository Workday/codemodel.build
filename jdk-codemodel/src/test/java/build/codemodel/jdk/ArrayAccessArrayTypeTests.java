package build.codemodel.jdk;

import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.ArrayAccess;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ArrayAccess#arrayType()} resolution via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class ArrayAccessArrayTypeTests {

    @Test
    void shouldResolveArrayTypeForStringArray() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Accessor", """
                package build.codemodel.jdk.example;
                public class Accessor {
                    public void run(String[] items) {
                        String first = items[0];
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Accessor");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var run = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();
        final var access = (ArrayAccess) decl.initializer().orElseThrow();

        assertThat(access.arrayType()).isPresent();
        // The array expression type is String[] — an ArrayTypeUsage
        final var arrayTypeUsage = access.arrayType().get();
        assertThat(arrayTypeUsage).isInstanceOf(
            build.codemodel.foundation.usage.ArrayTypeUsage.class);
    }

    @Test
    void shouldResolveArrayTypeForIntArray() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Accessor", """
                package build.codemodel.jdk.example;
                public class Accessor {
                    public void run(int[] nums) {
                        int n = nums[0];
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Accessor");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var run = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();
        final var access = (ArrayAccess) decl.initializer().orElseThrow();

        assertThat(access.arrayType()).isPresent();
        assertThat(access.arrayType().get()).isInstanceOf(
            build.codemodel.foundation.usage.ArrayTypeUsage.class);
    }
}
