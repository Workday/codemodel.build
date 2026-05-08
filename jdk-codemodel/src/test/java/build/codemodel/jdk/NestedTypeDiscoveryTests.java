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

import build.codemodel.jdk.descriptor.EnclosingTypeDescriptor;
import build.codemodel.jdk.descriptor.MemberTypeDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for inner and nested type capture via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class NestedTypeDiscoveryTests {

    @Test
    void outerTypeShouldListItsNestedTypes() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Outer", """
                package com.example;
                public class Outer {
                    public static class StaticNested {}
                    public class Inner {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var naming = codeModel.getNameProvider();
        final var outerName = naming.getTypeName(Optional.empty(), "com.example.Outer");
        final var outerDescriptor = codeModel.getTypeDescriptor(outerName).orElseThrow();

        final var memberNames = outerDescriptor.traits(MemberTypeDescriptor.class)
            .map(m -> m.memberTypeName().toString())
            .toList();

        assertThat(memberNames).hasSize(2);
        assertThat(memberNames).anyMatch(n -> n.contains("StaticNested"));
        assertThat(memberNames).anyMatch(n -> n.contains("Inner"));
    }

    @Test
    void nestedTypeShouldCarryEnclosingTypeDescriptor() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Outer", """
                package com.example;
                public class Outer {
                    public static class Nested {}
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var naming = codeModel.getNameProvider();
        final var outerName = naming.getTypeName(Optional.empty(), "com.example.Outer");
        final var outerDescriptor = codeModel.getTypeDescriptor(outerName).orElseThrow();
        final var nestedName = outerDescriptor.traits(MemberTypeDescriptor.class)
            .map(MemberTypeDescriptor::memberTypeName)
            .filter(n -> n.name().toString().equals("Nested"))
            .findFirst().orElseThrow();
        final var nestedDescriptor = codeModel.getTypeDescriptor(nestedName).orElseThrow();

        assertThat(nestedDescriptor.getTrait(EnclosingTypeDescriptor.class)).isPresent();
        assertThat(nestedDescriptor.getTrait(EnclosingTypeDescriptor.class)
            .orElseThrow().enclosingType().toString())
            .contains("Outer");
    }

    @Test
    void outerTypeShouldNotListMembersOfDeepNestedTypes() {
        // Outer.MemberTypeDescriptor should only list *directly* declared nested types
        final var source = JavaFileObjects.forSourceString(
            "com.example.Outer", """
                package com.example;
                public class Outer {
                    public static class Middle {
                        public static class Deep {}
                    }
                }
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var naming = codeModel.getNameProvider();
        final var outerName = naming.getTypeName(Optional.empty(), "com.example.Outer");
        final var outerDescriptor = codeModel.getTypeDescriptor(outerName).orElseThrow();

        final var memberNames = outerDescriptor.traits(MemberTypeDescriptor.class)
            .map(m -> m.memberTypeName().toString())
            .toList();

        assertThat(memberNames).hasSize(1);
        assertThat(memberNames.getFirst()).contains("Middle");
    }
}
