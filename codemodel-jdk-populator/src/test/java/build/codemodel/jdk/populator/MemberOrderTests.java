package build.codemodel.jdk.populator;

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
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.DeclarationOrder;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that member declaration order matches source order via {@link JdkInitializer}, as recorded by the
 * {@link DeclarationOrder} trait each member is stamped with.
 *
 * <p>Assertions sort by {@link DeclarationOrder#order()} specifically (not by
 * {@code SourceLocation.FilePosition}, which every member also carries) so that a regression in the trait
 * itself — rather than just a regression in source-position tracking — fails these tests.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class MemberOrderTests {

    @Test
    void fieldsShouldBeReturnedInSourceDeclarationOrder() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Ordered", """
                package com.example;
                public class Ordered {
                    public int alpha;
                    public int beta;
                    public int gamma;
                    public int delta;
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Ordered");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var fieldNames = descriptor.traits(FieldDescriptor.class)
            .sorted(Comparator.comparingInt(f -> f.trait(DeclarationOrder.class).order()))
            .map(f -> f.fieldName().toString())
            .toList();

        assertThat(fieldNames).containsExactly("alpha", "beta", "gamma", "delta");
    }

    @Test
    void methodsShouldBeReturnedInSourceDeclarationOrder() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Ordered", """
                package com.example;
                public class Ordered {
                    public void first() {}
                    public void second() {}
                    public void third() {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Ordered");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var methodNames = descriptor.traits(MethodDescriptor.class)
            .sorted(Comparator.comparingInt(m -> m.trait(DeclarationOrder.class).order()))
            .map(m -> m.methodName().name().toString())
            .toList();

        assertThat(methodNames).containsExactly("first", "second", "third");
    }

    @Test
    void constructorsShouldBeReturnedInSourceDeclarationOrder() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Ordered", """
                package com.example;
                public class Ordered {
                    public Ordered() {}
                    public Ordered(int a) {}
                    public Ordered(int a, int b) {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("com.example.Ordered");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var constructorParameterCounts = descriptor.traits(ConstructorDescriptor.class)
            .sorted(Comparator.comparingInt(c -> c.trait(DeclarationOrder.class).order()))
            .map(ConstructorDescriptor::getFormalParameterCount)
            .toList();

        assertThat(constructorParameterCounts).containsExactly(0, 1, 2);
    }
}
