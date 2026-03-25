package build.codemodel.jdk;

import build.codemodel.expression.ExpressionType;
import build.codemodel.expression.StringLiteral;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.Identifier;
import build.codemodel.jdk.expression.MethodInvocation;
import build.codemodel.jdk.statement.ExpressionStatement;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ExpressionType} resolution via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class ExpressionTypeResolutionTests {

    @Test
    void shouldResolveTypeForStringLiteral() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Example", """
                package build.codemodel.jdk.example;
                public class Example {
                    public void run() {
                        String s = "hello";
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Example");
        final var run = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();
        final var literal = (StringLiteral) decl.initializer().orElseThrow();

        assertThat(literal.type()).isPresent();
        assertThat(literal.type().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) literal.type().get()).typeName().name().toString())
            .isEqualTo("String");
    }

    @Test
    void shouldResolveTypeForIdentifier() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Example", """
                package build.codemodel.jdk.example;
                public class Example {
                    public void run(StringBuilder sb) {
                        sb.length();
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Example");
        final var run = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var stmt = (ExpressionStatement) body.statements().findFirst().orElseThrow();
        final var invocation = (MethodInvocation) stmt.expression();
        final var identifier = (Identifier) invocation.target().orElseThrow();

        assertThat(identifier.type()).isPresent();
        assertThat(identifier.type().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) identifier.type().get()).typeName().name().toString())
            .isEqualTo("StringBuilder");
    }

    @Test
    void shouldResolveTypeForMethodInvocationResult() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Example", """
                package build.codemodel.jdk.example;
                public class Example {
                    public void run(StringBuilder sb) {
                        String s = sb.toString();
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Example");
        final var run = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();
        final var invocation = (MethodInvocation) decl.initializer().orElseThrow();

        assertThat(invocation.type()).isPresent();
        assertThat(invocation.type().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) invocation.type().get()).typeName().name().toString())
            .isEqualTo("String");
    }

    @Test
    void shouldResolveTypeForChainedSubExpression() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Example", """
                package build.codemodel.jdk.example;
                public class Example {
                    public void run(StringBuilder sb) {
                        int n = sb.toString().length();
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Example");
        final var run = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();

        // outer: sb.toString().length() → int
        final var outerInvocation = (MethodInvocation) decl.initializer().orElseThrow();
        assertThat(outerInvocation.type()).isPresent();
        assertThat(outerInvocation.type().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) outerInvocation.type().get()).typeName().name().toString())
            .isEqualTo("int");

        // inner target: sb.toString() → String
        final var innerInvocation = (MethodInvocation) outerInvocation.target().orElseThrow();
        assertThat(innerInvocation.type()).isPresent();
        assertThat(innerInvocation.type().get()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) innerInvocation.type().get()).typeName().name().toString())
            .isEqualTo("String");
    }
}
