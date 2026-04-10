package build.codemodel.jdk;

import build.codemodel.imperative.Return;
import build.codemodel.jdk.expression.MethodInvocation;
import build.codemodel.jdk.expression.ResolvedMethod;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ResolvedMethod} resolution on {@link MethodInvocation} expressions.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class MethodResolutionTests {

    @Test
    void shouldResolveSameTypeMethod() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String greet() {
                    return "hello";
                }
                public String bar() {
                    return greet();
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(build.codemodel.jdk.descriptor.MethodBodyDescriptor.class)
            .orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var invocation = (MethodInvocation) ret.expression().orElseThrow();
        assertThat(invocation.methodName()).isEqualTo("greet");

        final var resolved = invocation.getTrait(ResolvedMethod.class).orElseThrow();
        assertThat(resolved.descriptor().methodName().name().toString()).isEqualTo("greet");
        assertThat(resolved.descriptor().typeDescriptor().typeName())
            .isEqualTo(typeName);
    }

    @Test
    void shouldResolveInheritedMethod() {
        final var base = JavaFileObjects.forSourceString("com.example.Base", """
            package com.example;
            public class Base {
                public String greet() {
                    return "hello";
                }
            }
            """);
        final var sub = JavaFileObjects.forSourceString("com.example.Sub", """
            package com.example;
            public class Sub extends Base {
                public String bar() {
                    return greet();
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(base, sub)));

        final var subName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Sub");
        final var baseName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Base");

        final var subDescriptor = codeModel.getTypeDescriptor(subName).orElseThrow();
        final var method = subDescriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(build.codemodel.jdk.descriptor.MethodBodyDescriptor.class)
            .orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var invocation = (MethodInvocation) ret.expression().orElseThrow();
        final var resolved = invocation.getTrait(ResolvedMethod.class).orElseThrow();

        assertThat(resolved.descriptor().methodName().name().toString()).isEqualTo("greet");
        assertThat(resolved.descriptor().typeDescriptor().typeName()).isEqualTo(baseName);
    }

    @Test
    void shouldNotResolveJdkMethod() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String bar() {
                    return String.valueOf(42);
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(build.codemodel.jdk.descriptor.MethodBodyDescriptor.class)
            .orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var invocation = (MethodInvocation) ret.expression().orElseThrow();
        assertThat(invocation.getTrait(ResolvedMethod.class)).isEmpty();
    }
}
