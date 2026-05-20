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

import build.codemodel.jdk.descriptor.SourceLocation;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for source position capture ({@link LocationTrait}) via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class LocationTraitTests {

    @Test
    void fieldShouldCarryLocationTrait() {
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
    void methodShouldCarryLocationTrait() {
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
}
