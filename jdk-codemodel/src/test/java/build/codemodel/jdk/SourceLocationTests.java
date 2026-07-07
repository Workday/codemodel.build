package build.codemodel.jdk;

/*-
 * #%L
 * JDK Code Model
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.expression.Expression;
import build.codemodel.imperative.Return;
import build.codemodel.jdk.descriptor.EnumConstantDescriptor;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.descriptor.SourceLocation;
import build.codemodel.jdk.expression.Lambda;
import build.codemodel.jdk.statement.EnhancedFor;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.jdk.statement.Try;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for source position capture ({@link SourceLocation}) via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class SourceLocationTests {

    @Test
    void fieldShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                public class Foo {
                    public int value;
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class).findFirst().orElseThrow();

        assertThat(field.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var location = field.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(location.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(location.endPosition()).isGreaterThan(location.startPosition());
    }

    @Test
    void methodShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                public class Foo {
                    public void hello() {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow();

        assertThat(method.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var location = method.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(location.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(location.endPosition()).isGreaterThan(location.startPosition());
    }

    @Test
    void laterDeclaredMemberShouldHaveHigherStartPosition() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Ordered", """
                package com.example;
                public class Ordered {
                    public int first;
                    public int second;
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Ordered");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var firstPos = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("first"))
            .findFirst().orElseThrow()
            .getTrait(SourceLocation.FilePosition.class).orElseThrow().startPosition();

        final var secondPos = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("second"))
            .findFirst().orElseThrow()
            .getTrait(SourceLocation.FilePosition.class).orElseThrow().startPosition();

        assertThat(secondPos).isGreaterThan(firstPos);
    }

    @Test
    void methodParameterShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                public class Foo {
                    public void hello(String name) {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow();
        final var param = method.formalParameters().findFirst().orElseThrow();

        assertThat(param.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var location = param.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(location.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(location.endPosition()).isGreaterThan(location.startPosition());
    }

    @Test
    void constructorParameterShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                public class Foo {
                    public Foo(String name) {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var ctor = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(ConstructorDescriptor.class).findFirst().orElseThrow();
        final var param = ctor.formalParameters().findFirst().orElseThrow();

        assertThat(param.getTrait(SourceLocation.FilePosition.class)).isPresent();
    }

    @Test
    void localVariableDeclarationShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                public class Foo {
                    public void run() {
                        String value = "hello";
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();

        assertThat(decl.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var location = decl.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(location.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(location.endPosition()).isGreaterThan(location.startPosition());
    }

    @Test
    void enhancedForLoopVariableShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                import java.util.List;
                public class Foo {
                    public void run(List<String> values) {
                        for (String value : values) {}
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var loop = (EnhancedFor) body.statements().findFirst().orElseThrow();

        assertThat(loop.getTrait(SourceLocation.FilePosition.class)).isPresent();
    }

    @Test
    void lambdaParameterShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                import java.util.Comparator;
                public class Foo {
                    public Comparator<String> comparator() {
                        return (String a, String b) -> a.compareTo(b);
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var lambda = (Lambda) body.statements()
            .map(s -> s instanceof Return r ? r.expression().orElse(null) : (Expression) null)
            .filter(e -> e instanceof Lambda)
            .findFirst().orElseThrow();

        final var param = lambda.parameters().findFirst().orElseThrow();
        assertThat(param.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var location = param.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(location.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(location.endPosition()).isGreaterThan(location.startPosition());
    }

    @Test
    void enumConstantShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Color", """
                package com.example;
                public enum Color {
                    RED,
                    GREEN
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Color");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var red = descriptor.traits(EnumConstantDescriptor.class)
            .filter(c -> c.name().toString().equals("RED"))
            .findFirst().orElseThrow();
        final var green = descriptor.traits(EnumConstantDescriptor.class)
            .filter(c -> c.name().toString().equals("GREEN"))
            .findFirst().orElseThrow();

        assertThat(red.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var redLocation = red.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(redLocation.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(redLocation.endPosition()).isGreaterThan(redLocation.startPosition());

        final var greenLocation = green.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(greenLocation.startPosition()).isGreaterThan(redLocation.startPosition());
    }

    @Test
    void catchClauseExceptionParameterShouldCarrySourceLocation() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                public class Foo {
                    public void run() {
                        try {
                            System.out.println();
                        } catch (RuntimeException e) {
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var tryStatement = (Try) body.statements().findFirst().orElseThrow();
        final var catchClause = tryStatement.catches().findFirst().orElseThrow();

        assertThat(catchClause.getTrait(SourceLocation.FilePosition.class)).isPresent();
        final var location = catchClause.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(location.startPosition()).isGreaterThanOrEqualTo(0);
        assertThat(location.endPosition()).isGreaterThan(location.startPosition());
    }
}
