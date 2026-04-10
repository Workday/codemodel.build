package build.codemodel.jdk;

import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.imperative.Return;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.Identifier;
import build.codemodel.jdk.expression.Symbol;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Symbol} resolution on {@link Identifier} expressions.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class SymbolResolutionTests {

    @Test
    void shouldResolveLocalVariable() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String bar() {
                    String result = "hello";
                    return result;
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
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) ret.expression().orElseThrow();
        assertThat(identifier.name()).isEqualTo("result");

        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.LocalVariable.class);
        final var local = (Symbol.LocalVariable) symbol;
        assertThat(local.declaredType()).isInstanceOf(NamedTypeUsage.class);
        assertThat(local.declaredType().toString()).contains("String");
    }

    @Test
    void shouldResolveParameter() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String bar(String input) {
                    return input;
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
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) ret.expression().orElseThrow();
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.Parameter.class);
        assertThat(((Symbol.Parameter) symbol).declaredType().toString()).contains("String");
    }

    @Test
    void shouldResolveField() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                private String value = "hello";
                public String bar() {
                    return value;
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
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) ret.expression().orElseThrow();
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.Field.class);
        assertThat(((Symbol.Field) symbol).declaredType().toString()).contains("String");
    }

    @Test
    void shouldResolveThisReference() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Foo bar() {
                    return this;
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
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) ret.expression().orElseThrow();
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.ThisReference.class);
        assertThat(((Symbol.ThisReference) symbol).declaredType().toString()).contains("Foo");
    }

    @Test
    void shouldResolveTypeReference() {
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
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        // String.valueOf(42) is a MethodInvocation whose receiver is a MemberSelect on "String"
        // The MemberSelect target is an Identifier("String") tagged as TypeReference
        final var methodInvocation = ret.expression().orElseThrow();
        assertThat(methodInvocation).isInstanceOf(build.codemodel.jdk.expression.MethodInvocation.class);
        final var invocation = (build.codemodel.jdk.expression.MethodInvocation) methodInvocation;
        final var receiver = (Identifier) invocation.target().orElseThrow();
        assertThat(receiver.name()).isEqualTo("String");
        final var symbol = receiver.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.TypeReference.class);
    }
}
