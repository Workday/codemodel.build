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

import build.codemodel.expression.Cast;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.imperative.Return;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.AssignmentOperator;
import build.codemodel.jdk.expression.BitwiseBinary;
import build.codemodel.jdk.expression.BitwiseOperator;
import build.codemodel.jdk.expression.CompoundAssignment;
import build.codemodel.jdk.expression.InstanceOf;
import build.codemodel.jdk.expression.NewArray;
import build.codemodel.jdk.expression.NewObject;
import build.codemodel.jdk.expression.PostfixOperator;
import build.codemodel.jdk.expression.PostfixUnary;
import build.codemodel.jdk.expression.PrefixOperator;
import build.codemodel.jdk.expression.PrefixUnary;
import build.codemodel.jdk.statement.ExpressionStatement;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for correct capture of source-tree information in {@link JdkExpressionConverter}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class ExpressionCaptureFidelityTests {

    @Test
    void shouldCaptureCastTargetType() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Caster", """
                package build.codemodel.jdk.example;
                public class Caster {
                    public void run(Object obj) {
                        String s = (String) obj;
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Caster");
        final var run = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = run.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();
        final var cast = (Cast) decl.initializer().orElseThrow();

        assertThat(cast.targetType()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) cast.targetType()).typeName().canonicalName())
            .isEqualTo("java.lang.String");
    }

    @Test
    void shouldCaptureInstanceOfPatternBindingVariable() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.PatternChecker", """
                package build.codemodel.jdk.example;
                public class PatternChecker {
                    public boolean check(Object obj) {
                        return obj instanceof String s;
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.PatternChecker");
        final var check = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("check"))
            .findFirst().orElseThrow();
        final var body = check.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var returnStmt = (Return) body.statements().findFirst().orElseThrow();
        final var instanceOf = (InstanceOf) returnStmt.expression().orElseThrow();

        assertThat(instanceOf.checkedType()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) instanceOf.checkedType()).typeName().canonicalName()).isEqualTo("java.lang.String");
        assertThat(instanceOf.bindingVariable()).isEqualTo(Optional.of("s"));
    }

    @Test
    void shouldCaptureNewObjectTypeArguments() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.ListFactory", """
                package build.codemodel.jdk.example;
                import java.util.ArrayList;
                public class ListFactory {
                    public ArrayList<String> create() {
                        return new ArrayList<String>();
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.ListFactory");
        final var create = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("create"))
            .findFirst().orElseThrow();
        final var body = create.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var returnStmt = (Return) body.statements().findFirst().orElseThrow();
        final var newObject = (NewObject) returnStmt.expression().orElseThrow();

        assertThat(newObject.instantiatedType()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) newObject.instantiatedType()).typeName().canonicalName()).isEqualTo("java.util.ArrayList");

        final var typeArgs = newObject.typeArguments().toList();
        assertThat(typeArgs).hasSize(1);
        assertThat(typeArgs.getFirst()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) typeArgs.getFirst()).typeName().canonicalName()).isEqualTo("java.lang.String");
    }

    @Test
    void shouldCaptureNewArrayElementType() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.ArrayFactory", """
                package build.codemodel.jdk.example;
                public class ArrayFactory {
                    public String[] create(int n) {
                        return new String[n];
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.ArrayFactory");
        final var create = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("create"))
            .findFirst().orElseThrow();
        final var body = create.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var returnStmt = (Return) body.statements().findFirst().orElseThrow();
        final var newArray = (NewArray) returnStmt.expression().orElseThrow();

        assertThat(newArray.elementType()).isInstanceOf(NamedTypeUsage.class);
        assertThat(((NamedTypeUsage) newArray.elementType()).typeName().canonicalName()).isEqualTo("java.lang.String");
    }

    @Test
    void shouldCaptureBitwiseOperator() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Bits", """
                package build.codemodel.jdk.example;
                public class Bits {
                    public int run(int a, int b) { return a << b; }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Bits");
        final var body = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow()
            .getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var bitwise = (BitwiseBinary) ((Return) body.statements().findFirst().orElseThrow()).expression().orElseThrow();

        assertThat(bitwise.operator()).isEqualTo(BitwiseOperator.LEFT_SHIFT);
    }

    @Test
    void shouldCapturePrefixOperator() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Counter", """
                package build.codemodel.jdk.example;
                public class Counter {
                    public int run(int x) { return ++x; }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Counter");
        final var body = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow()
            .getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var prefix = (PrefixUnary) ((Return) body.statements().findFirst().orElseThrow()).expression().orElseThrow();

        assertThat(prefix.operator()).isEqualTo(PrefixOperator.INCREMENT);
    }

    @Test
    void shouldCapturePostfixOperator() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Postfix", """
                package build.codemodel.jdk.example;
                public class Postfix {
                    public void run(int[] arr) { arr[0]--; }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Postfix");
        final var body = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow()
            .getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var postfix = (PostfixUnary) ((ExpressionStatement) body.statements().findFirst().orElseThrow()).expression();

        assertThat(postfix.operator()).isEqualTo(PostfixOperator.DECREMENT);
    }

    @Test
    void shouldCaptureAssignmentOperator() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.Assigner", """
                package build.codemodel.jdk.example;
                public class Assigner {
                    public void run(int[] arr) { arr[0] += 1; }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "build.codemodel.jdk.example.Assigner");
        final var body = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class).findFirst().orElseThrow()
            .getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var assign = (CompoundAssignment) ((ExpressionStatement) body.statements().findFirst().orElseThrow()).expression();

        assertThat(assign.operator()).isEqualTo(AssignmentOperator.PLUS);
    }
}
