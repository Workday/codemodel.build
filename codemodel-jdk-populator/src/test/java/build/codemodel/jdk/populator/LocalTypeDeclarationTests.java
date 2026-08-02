package build.codemodel.jdk.populator;

/*-
 * #%L
 * JDK Code Model Populator
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
import build.codemodel.jdk.descriptor.EnumConstantDescriptor;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.populator.descriptor.SourceLocation;
import build.codemodel.jdk.statement.LocalTypeDeclaration;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for local class/interface/enum/record declaration statements via
 * {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class LocalTypeDeclarationTests {

    @Test
    void shouldCaptureLocalClassDeclarationAsStatement() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.LocalClassHost", """
                package build.codemodel.jdk.example;
                public class LocalClassHost {
                    public void run() {
                        class Counter {
                            int value;
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.LocalClassHost");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var localTypeDeclaration = body.statements()
            .filter(s -> s instanceof LocalTypeDeclaration)
            .map(s -> (LocalTypeDeclaration) s)
            .findFirst().orElseThrow();
        assertThat(localTypeDeclaration.getTrait(SourceLocation.FilePosition.class)).isPresent();

        // the declared local type itself is registered in the CodeModel and fully populated
        final var localDescriptor = codeModel.getTypeDescriptor(localTypeDeclaration.typeName()).orElseThrow();
        assertThat(localDescriptor.typeName().name().toString()).isEqualTo("Counter");
        final var field = localDescriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("value"))
            .findFirst().orElseThrow();
        assertThat(field.fieldName().toString()).isEqualTo("value");
    }

    @Test
    void shouldCaptureLocalRecordDeclarationAsStatement() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.LocalRecordHost", """
                package build.codemodel.jdk.example;
                public class LocalRecordHost {
                    public void run() {
                        record Point(int x, int y) {
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.LocalRecordHost");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var localTypeDeclaration = body.statements()
            .filter(s -> s instanceof LocalTypeDeclaration)
            .map(s -> (LocalTypeDeclaration) s)
            .findFirst().orElseThrow();

        final var localDescriptor = codeModel.getTypeDescriptor(localTypeDeclaration.typeName()).orElseThrow();
        assertThat(localDescriptor.typeName().name().toString()).isEqualTo("Point");
    }

    @Test
    void shouldCaptureLocalInterfaceDeclarationAsStatement() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.LocalInterfaceHost", """
                package build.codemodel.jdk.example;
                public class LocalInterfaceHost {
                    public void run() {
                        interface Talker {
                            void speak();
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.LocalInterfaceHost");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var localTypeDeclaration = body.statements()
            .filter(s -> s instanceof LocalTypeDeclaration)
            .map(s -> (LocalTypeDeclaration) s)
            .findFirst().orElseThrow();

        final var localDescriptor = codeModel.getTypeDescriptor(localTypeDeclaration.typeName()).orElseThrow();
        assertThat(localDescriptor.typeName().name().toString()).isEqualTo("Talker");
        final var speakMethod = localDescriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("speak"))
            .findFirst().orElseThrow();
        assertThat(speakMethod.methodName().name().toString()).isEqualTo("speak");
    }

    @Test
    void shouldCaptureLocalEnumDeclarationAsStatement() {
        final var source = JavaFileObjects.forSourceString(
            "build.codemodel.jdk.example.LocalEnumHost", """
                package build.codemodel.jdk.example;
                public class LocalEnumHost {
                    public void run() {
                        enum Color {
                            RED, GREEN, BLUE
                        }
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.LocalEnumHost");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var method = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var localTypeDeclaration = body.statements()
            .filter(s -> s instanceof LocalTypeDeclaration)
            .map(s -> (LocalTypeDeclaration) s)
            .findFirst().orElseThrow();

        final var localDescriptor = codeModel.getTypeDescriptor(localTypeDeclaration.typeName()).orElseThrow();
        assertThat(localDescriptor.typeName().name().toString()).isEqualTo("Color");
        final var constantNames = localDescriptor.traits(EnumConstantDescriptor.class)
            .map(c -> c.name().toString())
            .toList();
        assertThat(constantNames).containsExactlyInAnyOrder("RED", "GREEN", "BLUE");
    }
}
