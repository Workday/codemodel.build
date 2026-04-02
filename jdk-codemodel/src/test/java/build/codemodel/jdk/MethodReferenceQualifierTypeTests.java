package build.codemodel.jdk;

import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.MethodReference;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MethodReference#qualifierType()} resolution via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class MethodReferenceQualifierTypeTests {

    @Test
    void shouldResolveQualifierTypeForInstanceMethodReference() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Caller", """
                package build.codemodel.jdk.example;
                import java.util.function.Supplier;
                public class Caller {
                    public void run(StringBuilder sb) {
                        Supplier<String> s = sb::toString;
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Caller");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var run = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (build.codemodel.jdk.statement.LocalVariableDeclaration)
            body.statements().findFirst().orElseThrow();
        final var ref = (MethodReference) decl.initializer().orElseThrow();

        assertThat(ref.methodName()).isEqualTo("toString");
        assertThat(ref.qualifierType()).isPresent();
        assertThat(ref.qualifierType().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) ref.qualifierType().get()).typeName().name().toString())
            .isEqualTo("StringBuilder");
    }

    @Test
    void shouldResolveQualifierTypeForStaticMethodReference() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Caller", """
                package build.codemodel.jdk.example;
                import java.util.function.Function;
                public class Caller {
                    public void run() {
                        Function<String, Integer> f = Integer::parseInt;
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Caller");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var run = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (build.codemodel.jdk.statement.LocalVariableDeclaration)
            body.statements().findFirst().orElseThrow();
        final var ref = (MethodReference) decl.initializer().orElseThrow();

        assertThat(ref.methodName()).isEqualTo("parseInt");
        assertThat(ref.qualifierType()).isPresent();
        assertThat(ref.qualifierType().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) ref.qualifierType().get()).typeName().name().toString())
            .isEqualTo("Integer");
    }
}
