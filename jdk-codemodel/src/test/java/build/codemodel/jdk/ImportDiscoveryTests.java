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
import build.codemodel.jdk.descriptor.ImportDeclaration;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for import declaration capture via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class ImportDiscoveryTests {

    @Test
    void shouldCaptureImportDeclarationsInSourceOrder() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                import java.util.List;
                import java.util.Map;
                import java.io.IOException;
                public class Foo {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var imports = descriptor.traits(ImportDeclaration.class)
            .sorted(Comparator.comparingInt(ImportDeclaration::order))
            .map(ImportDeclaration::qualifiedName)
            .toList();

        assertThat(imports).containsExactly("java.util.List", "java.util.Map", "java.io.IOException");
    }

    @Test
    void shouldCaptureStaticImportDeclarations() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                import static java.lang.Math.PI;
                import static java.util.Collections.emptyList;
                public class Foo {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var staticImports = descriptor.traits(ImportDeclaration.class)
            .filter(ImportDeclaration::isStatic)
            .sorted(Comparator.comparingInt(ImportDeclaration::order))
            .map(ImportDeclaration::qualifiedName)
            .toList();

        assertThat(staticImports).containsExactly("java.lang.Math.PI", "java.util.Collections.emptyList");
    }

    @Test
    void shouldCaptureOnDemandImportDeclarations() {
        final var source = JavaFileObjects.forSourceString(
            "com.example.Foo", """
                package com.example;
                import java.util.*;
                public class Foo {}
                """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var onDemand = descriptor.traits(ImportDeclaration.class)
            .filter(ImportDeclaration::isOnDemand)
            .toList();

        assertThat(onDemand).hasSize(1);
        assertThat(onDemand.getFirst().qualifiedName()).isEqualTo("java.util");
    }
}
