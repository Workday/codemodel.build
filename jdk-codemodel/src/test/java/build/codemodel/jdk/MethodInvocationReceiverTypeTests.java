package build.codemodel.jdk;

import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.MethodInvocation;
import build.codemodel.jdk.statement.ExpressionStatement;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MethodInvocation#receiverType()} resolution via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class MethodInvocationReceiverTypeTests {

    @Test
    void shouldResolveReceiverTypeForExplicitReceiver() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Caller", """
                package build.codemodel.jdk.example;
                public class Caller {
                    public void run(StringBuilder sb) {
                        sb.append("hello");
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
        final var stmt = (ExpressionStatement) body.statements().findFirst().orElseThrow();
        final var invocation = (MethodInvocation) stmt.expression();

        assertThat(invocation.methodName()).isEqualTo("append");
        assertThat(invocation.receiverType()).isPresent();
        assertThat(invocation.receiverType().get()).isInstanceOf(NamedTypeUsage.class);
        final var receiverTypeName = ((NamedTypeUsage) invocation.receiverType().get()).typeName();
        assertThat(receiverTypeName.name().toString()).isEqualTo("StringBuilder");
    }

    @Test
    void shouldResolveReceiverTypeForChainedCall() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Caller", """
                package build.codemodel.jdk.example;
                public class Caller {
                    public void run(StringBuilder sb) {
                        sb.toString().length();
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
        final var stmt = (ExpressionStatement) body.statements().findFirst().orElseThrow();
        // sb.toString().length() — outermost call is .length() on a String
        final var outerInvocation = (MethodInvocation) stmt.expression();

        assertThat(outerInvocation.methodName()).isEqualTo("length");
        assertThat(outerInvocation.receiverType()).isPresent();
        assertThat(outerInvocation.receiverType().get()).isInstanceOf(NamedTypeUsage.class);
        final var receiverTypeName = ((NamedTypeUsage) outerInvocation.receiverType().get()).typeName();
        assertThat(receiverTypeName.name().toString()).isEqualTo("String");
    }

    @Test
    void shouldReturnEmptyReceiverTypeForUnqualifiedCall() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Caller", """
                package build.codemodel.jdk.example;
                public class Caller {
                    public void bar() {}
                    public void run() {
                        bar();
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
        final var stmt = (ExpressionStatement) body.statements().findFirst().orElseThrow();
        final var invocation = (MethodInvocation) stmt.expression();

        assertThat(invocation.methodName()).isEqualTo("bar");
        assertThat(invocation.receiverType()).isEmpty();
    }
}
