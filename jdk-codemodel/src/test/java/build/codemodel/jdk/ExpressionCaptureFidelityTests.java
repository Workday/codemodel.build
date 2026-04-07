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
import build.codemodel.jdk.expression.Identifier;
import build.codemodel.jdk.expression.InstanceOf;
import build.codemodel.jdk.expression.NewObject;
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
        assertThat(((NamedTypeUsage) cast.targetType()).typeName().name().toString())
            .isEqualTo("String");
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
        final var instanceOf = (InstanceOf) returnStmt.expression();

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
        final var newObject = (NewObject) returnStmt.expression();

        final var typeArgs = newObject.typeArguments().toList();
        assertThat(typeArgs).hasSize(1);
        assertThat(typeArgs.getFirst()).isInstanceOf(Identifier.class);
        assertThat(((Identifier) typeArgs.getFirst()).name()).isEqualTo("String");
    }
}
