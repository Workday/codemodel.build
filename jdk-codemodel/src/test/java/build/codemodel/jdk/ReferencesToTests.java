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
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JDKCodeModel#referencesTo}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class ReferencesToTests {

    @Test
    void shouldFindExtendsReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Child", """
                package com.example;
                public class Child extends Parent {}
                """);
        final var parent = JavaFileObjects.forSourceString(
            "com.example.Parent", """
                package com.example;
                public class Parent {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, parent)));

        final var parentName = codeModel.getEmptyModuleTypeName("com.example.Parent");
        final var refs = codeModel.referencesTo(parentName, ReferenceKind.EXTENDS).toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.EXTENDS);
        assertThat(refs.getFirst().owner().typeName().toString()).contains("Child");
    }

    @Test
    void shouldFindImplementsReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Impl", """
                package com.example;
                public class Impl implements Svc {}
                """);
        final var svc = JavaFileObjects.forSourceString(
            "com.example.Svc", """
                package com.example;
                public interface Svc {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, svc)));

        final var svcName = codeModel.getEmptyModuleTypeName("com.example.Svc");
        final var refs = codeModel.referencesTo(svcName, ReferenceKind.IMPLEMENTS).toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.IMPLEMENTS);
    }

    @Test
    void shouldFindFieldTypeReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Holder", """
                package com.example;
                public class Holder {
                    public Target value;
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.FIELD_TYPE).toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.FIELD_TYPE);
        assertThat(refs.getFirst().member()).isPresent();
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(FieldDescriptor.class);
    }

    @Test
    void shouldFindReturnTypeReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Factory", """
                package com.example;
                public class Factory {
                    public Target create() { return new Target(); }
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.RETURN_TYPE).toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.RETURN_TYPE);
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(MethodDescriptor.class);
    }

    @Test
    void shouldFindParameterTypeReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Consumer", """
                package com.example;
                public class Consumer {
                    public void accept(Target t) {}
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.PARAMETER_TYPE)
            .filter(r -> r.owner().typeName().toString().contains("Consumer"))
            .toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.PARAMETER_TYPE);
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(MethodDescriptor.class);
    }

    @Test
    void shouldFindMethodBodyReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.User", """
                package com.example;
                public class User {
                    public void run() {
                        Target t = new Target();
                    }
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.METHOD_BODY).toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.METHOD_BODY);
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(MethodDescriptor.class);
    }

    @Test
    void shouldFindConstructorBodyReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Builder", """
                package com.example;
                public class Builder {
                    public Builder() {
                        Target t = new Target();
                    }
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.METHOD_BODY)
            .filter(r -> r.owner().typeName().toString().contains("Builder"))
            .toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.METHOD_BODY);
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(ConstructorDescriptor.class);
    }

    @Test
    void unfilteredReferencesToReturnsAllReferenceKinds() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Multi", """
                package com.example;
                public class Multi extends Target {
                    public Target field;
                    public Target method() { return null; }
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName).toList();

        final var kinds = refs.stream().map(TypeReference::kind).distinct().toList();
        assertThat(kinds).contains(ReferenceKind.EXTENDS, ReferenceKind.FIELD_TYPE, ReferenceKind.RETURN_TYPE);
    }

    @Test
    void noSelfReferenceForSimpleClass() {
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var selfRefs = codeModel.referencesTo(targetName)
            .filter(r -> r.owner().typeName().equals(targetName))
            .toList();
        assertThat(selfRefs).isEmpty();
    }

    @Test
    void noReferencesReturnsEmptyStream() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Unrelated", """
                package com.example;
                public class Unrelated {}
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName)
            .filter(r -> !r.owner().typeName().equals(targetName))
            .toList();
        assertThat(refs).isEmpty();
    }

    @Test
    void shouldFindConstructorParameterTypeReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Wrapper", """
                package com.example;
                public class Wrapper {
                    public Wrapper(Target t) {}
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.PARAMETER_TYPE)
            .filter(r -> r.owner().typeName().toString().contains("Wrapper"))
            .toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.PARAMETER_TYPE);
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(ConstructorDescriptor.class);
    }

    @Test
    void shouldFindTypeNestedInsideGenericTypeArgument() {
        // Regression: Target appears as a type argument of a type argument (e.g. Wrapper<List<Target>>).
        // The old traversal-based implementation crashed with "No structural ancestor found" because
        // FieldDescriptor is not a NamedTypeUsage and was never pushed onto the mereology stack.
        final var source = JavaFileObjects.forSourceString(
            "com.example.Holder", """
                package com.example;
                import java.util.Map;
                import java.util.List;
                public class Holder {
                    public Map<String, List<Target>> index;
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.FIELD_TYPE)
            .filter(r -> r.owner().typeName().toString().contains("Holder"))
            .toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().member().orElseThrow()).isInstanceOf(FieldDescriptor.class);
    }

    @Test
    void shouldFindStaticInitializerReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Registry", """
                package com.example;
                public class Registry {
                    static {
                        Target t = new Target();
                    }
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName, ReferenceKind.METHOD_BODY)
            .filter(r -> r.owner().typeName().toString().contains("Registry"))
            .toList();

        assertThat(refs).hasSize(1);
        assertThat(refs.getFirst().kind()).isEqualTo(ReferenceKind.METHOD_BODY);
        assertThat(refs.getFirst().member()).isEmpty();
    }

    @Test
    void shouldFindParameterizedTypeReference() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Holder", """
                package com.example;
                import java.util.List;
                public class Holder {
                    public List<Target> items;
                    public List<Target> getItems() { return items; }
                }
                """);
        final var target = JavaFileObjects.forSourceString(
            "com.example.Target", """
                package com.example;
                public class Target {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source, target)));

        final var targetName = codeModel.getEmptyModuleTypeName("com.example.Target");
        final var refs = codeModel.referencesTo(targetName)
            .filter(r -> r.owner().typeName().toString().contains("Holder"))
            .toList();

        final var kinds = refs.stream().map(TypeReference::kind).distinct().toList();
        assertThat(kinds).contains(ReferenceKind.FIELD_TYPE, ReferenceKind.RETURN_TYPE);
        assertThat(refs.stream().filter(r -> r.kind() == ReferenceKind.FIELD_TYPE)
            .map(r -> r.member().orElseThrow())
            .allMatch(m -> m instanceof FieldDescriptor)).isTrue();
        assertThat(refs.stream().filter(r -> r.kind() == ReferenceKind.RETURN_TYPE)
            .map(r -> r.member().orElseThrow())
            .allMatch(m -> m instanceof MethodDescriptor)).isTrue();
    }
}
