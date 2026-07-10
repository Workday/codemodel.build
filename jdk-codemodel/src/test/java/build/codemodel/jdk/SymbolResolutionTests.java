package build.codemodel.jdk;

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.imperative.If;
import build.codemodel.imperative.Return;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.CompoundAssignment;
import build.codemodel.jdk.expression.Identifier;
import build.codemodel.jdk.expression.InstanceOf;
import build.codemodel.jdk.expression.Symbol;
import build.codemodel.jdk.statement.EnhancedFor;
import build.codemodel.jdk.statement.ExpressionStatement;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.jdk.statement.SwitchStatement;
import build.codemodel.jdk.statement.Throw;
import build.codemodel.jdk.statement.Try;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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

        final var decl = (LocalVariableDeclaration) body.statements()
            .filter(s -> s instanceof LocalVariableDeclaration)
            .findFirst().orElseThrow();
        assertThat(local.declaration()).contains(decl);
    }

    @Test
    void shouldResolveShadowedLocalVariableToItsOwnDeclaration() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int bar() {
                    int value = 1;
                    {
                        int value2 = 2;
                        System.out.println(value2);
                    }
                    return value;
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var outerDecl = (LocalVariableDeclaration) body.statements()
            .filter(s -> s instanceof LocalVariableDeclaration)
            .findFirst().orElseThrow();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();
        final var identifier = (Identifier) ret.expression().orElseThrow();
        final var local = (Symbol.LocalVariable) identifier.getTrait(Symbol.class).orElseThrow();

        assertThat(local.declaration()).contains(outerDecl);
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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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
        final var parameter = (Symbol.Parameter) symbol;
        assertThat(parameter.declaredType().toString()).contains("String");
        assertThat(parameter.descriptor().name().orElseThrow().toString()).isEqualTo("input");
    }

    @Test
    void shouldResolveConstructorParameter() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                private String value;
                public Foo(String value) {
                    this.value = value;
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var constructor = descriptor.traits(ConstructorDescriptor.class).findFirst().orElseThrow();
        final var body = constructor.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var assignment = body.statements()
            .filter(s -> s instanceof ExpressionStatement)
            .map(s -> (CompoundAssignment) ((ExpressionStatement) s).expression())
            .findFirst().orElseThrow();

        final var identifier = (Identifier) assignment.value();
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.Parameter.class);
        final var parameter = (Symbol.Parameter) symbol;
        assertThat(parameter.declaredType().toString()).contains("String");
        assertThat(parameter.descriptor().name().orElseThrow().toString()).isEqualTo("value");
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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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
        final var field = (Symbol.Field) symbol;
        assertThat(field.declaredType().toString()).contains("String");
        assertThat(field.descriptor().orElseThrow().fieldName().toString()).isEqualTo("value");
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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
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

    @Test
    void shouldResolveEnhancedForVariable() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int bar(java.util.List<Integer> xs) {
                    for (Integer x : xs) {
                        return x;
                    }
                    return 0;
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var enhancedFor = (EnhancedFor) body.statements()
            .filter(s -> s instanceof EnhancedFor)
            .findFirst().orElseThrow();
        final var innerReturn = (Return) ((build.codemodel.imperative.Block) enhancedFor.body()).statements()
            .filter(s -> s instanceof Return)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) innerReturn.expression().orElseThrow();
        assertThat(identifier.name()).isEqualTo("x");
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.EnhancedForVariable.class);
        final var enhancedForVariable = (Symbol.EnhancedForVariable) symbol;
        assertThat(enhancedForVariable.declaredType().toString()).contains("Integer");
        assertThat(enhancedForVariable.declaration()).contains(enhancedFor);
    }

    @Test
    void shouldResolveCatchParameter() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public void bar() {
                    try {
                        doSomething();
                    } catch (java.io.IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                private void doSomething() throws java.io.IOException {
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var tryStatement = (Try) body.statements()
            .filter(s -> s instanceof Try)
            .findFirst().orElseThrow();
        final var catchClause = tryStatement.catches().findFirst().orElseThrow();
        final var throwStatement = (Throw) catchClause.body().statements()
            .filter(s -> s instanceof Throw)
            .findFirst().orElseThrow();

        final var newException = (build.codemodel.jdk.expression.NewObject) throwStatement.expression();
        final var identifier = (Identifier) newException.args().findFirst().orElseThrow();
        assertThat(identifier.name()).isEqualTo("e");
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.CatchParameter.class);
        final var catchParameter = (Symbol.CatchParameter) symbol;
        assertThat(catchParameter.declaredType().toString()).contains("IOException");
        assertThat(catchParameter.declaration()).contains(catchClause);
    }

    @Test
    void shouldResolveInstanceOfPatternBinding() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int bar(Object o) {
                    if (o instanceof String s) {
                        return s.length();
                    }
                    return 0;
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ifStatement = (If) body.statements()
            .filter(s -> s instanceof If)
            .findFirst().orElseThrow();
        final var instanceOf = (InstanceOf) ((build.codemodel.jdk.expression.Parenthesized) ifStatement.condition()).inner();
        final var innerReturn = (Return) ((build.codemodel.imperative.Block) ifStatement.thenStatement()).statements()
            .filter(s -> s instanceof Return)
            .findFirst().orElseThrow();

        final var invocation = (build.codemodel.jdk.expression.MethodInvocation) innerReturn.expression().orElseThrow();
        final var identifier = (Identifier) invocation.target().orElseThrow();
        assertThat(identifier.name()).isEqualTo("s");
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.PatternBinding.class);
        final var patternBinding = (Symbol.PatternBinding) symbol;
        assertThat(patternBinding.declaredType().toString()).contains("String");
        assertThat(patternBinding.declaration()).contains(instanceOf);
    }

    @Test
    void shouldResolveSwitchPatternBinding() {
        final var source = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int bar(Object o) {
                    switch (o) {
                        case String s -> {
                            return s.length();
                        }
                        default -> {
                            return 0;
                        }
                    }
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();
        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("bar"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var switchStatement = (SwitchStatement) body.statements()
            .filter(s -> s instanceof SwitchStatement)
            .findFirst().orElseThrow();
        final var stringCase = switchStatement.cases()
            .filter(c -> c.labels().findFirst().orElseThrow() instanceof InstanceOf)
            .findFirst().orElseThrow();
        final var instanceOf = (InstanceOf) stringCase.labels().findFirst().orElseThrow();
        final var innerReturn = (Return) stringCase.statements()
            .filter(s -> s instanceof Return)
            .findFirst().orElseThrow();

        final var invocation = (build.codemodel.jdk.expression.MethodInvocation) innerReturn.expression().orElseThrow();
        final var identifier = (Identifier) invocation.target().orElseThrow();
        assertThat(identifier.name()).isEqualTo("s");
        final var symbol = identifier.getTrait(Symbol.class).orElseThrow();
        assertThat(symbol).isInstanceOf(Symbol.PatternBinding.class);
        final var patternBinding = (Symbol.PatternBinding) symbol;
        assertThat(patternBinding.declaredType().toString()).contains("String");
        assertThat(patternBinding.declaration()).contains(instanceOf);
    }
}
