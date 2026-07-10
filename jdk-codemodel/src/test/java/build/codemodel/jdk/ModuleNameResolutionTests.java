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
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.jdk.descriptor.MemberTypeDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.List;
import java.util.Optional;
import javax.tools.JavaFileObject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that {@link JdkInitializer} propagates module name onto {@link build.codemodel.foundation.naming.TypeName}
 * and that {@link JDKCodeModel#getJDKTypeDescriptor(String)} resolves types regardless of module membership.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class ModuleNameResolutionTests {

    private static final String MODULE_NAME = "build.codemodel.jdk.example";
    private static final String OUTER_FQN = MODULE_NAME + ".NestedExample";
    private static final String INNER_BINARY_NAME = OUTER_FQN + "$Inner$VeryInner";

    private static JDKCodeModel buildCodeModel(final boolean withModule) {
        final var sources = List.of(
            new File("src/test/java/build/codemodel/jdk/example/NestedExample.java"));
        final var moduleFiles = withModule
            ? List.of(JavaFileObjects.forSourceString("module-info", "module " + MODULE_NAME + " {}"))
            : List.<JavaFileObject>of();
        return JdkInitializerTests.runInternal(new JdkInitializer(sources, List.of(), moduleFiles));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getJDKTypeDescriptorByBinaryNameFindsType(final boolean withModule) {
        final var codeModel = buildCodeModel(withModule);

        final var descriptor = codeModel.getJDKTypeDescriptor(OUTER_FQN);
        assertThat(descriptor).isPresent();

        if (withModule) {
            assertThat(descriptor.orElseThrow().typeName().moduleName())
                .as("TypeName should carry the module from module-info.java")
                .hasValueSatisfying(mn -> assertThat(mn.toString()).isEqualTo(MODULE_NAME));

            final var innerTypeName = codeModel.getJDKTypeDescriptor(INNER_BINARY_NAME).orElseThrow().typeName();
            assertThat(innerTypeName.toString()).isEqualTo(MODULE_NAME + "/" + INNER_BINARY_NAME);
        } else {
            assertThat(descriptor.orElseThrow().typeName().moduleName())
                .as("TypeName should have no module when no module-info.java is present")
                .isEmpty();

            final var innerTypeName = codeModel.getJDKTypeDescriptor(INNER_BINARY_NAME).orElseThrow().typeName();
            assertThat(innerTypeName.toString()).isEqualTo(INNER_BINARY_NAME);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void typeNameFromSourceAndFromStringPathShouldAgree(final boolean withModule) {
        final var codeModel = buildCodeModel(withModule);
        final var nameProvider = codeModel.getNameProvider();

        final var moduleName = withModule
            ? nameProvider.getModuleName(MODULE_NAME)
            : Optional.<ModuleName>empty();

        // source path: TypeName built by JdkInitializer via resolveTypeName (element walking)
        final var fromSource = codeModel.getJDKTypeDescriptor(INNER_BINARY_NAME).orElseThrow().typeName();

        // string path: TypeName built by NameProvider.getTypeNameFromBinary directly
        final var fromString = nameProvider.getTypeNameFromBinary(moduleName, INNER_BINARY_NAME);

        assertThat(fromSource.toString()).isEqualTo(fromString.toString());
        assertThat(fromSource.canonicalName()).isEqualTo(fromString.canonicalName());
        assertThat(fromSource.binaryName()).isEqualTo(fromString.binaryName());
        assertThat(fromSource.moduleName()).isEqualTo(fromString.moduleName());

        assertThat(fromSource.name().toString()).isEqualTo("VeryInner");
        assertThat(fromSource.enclosingTypeName())
            .isPresent()
            .hasValueSatisfying(inner -> {
                assertThat(inner.name().toString()).isEqualTo("Inner");
                assertThat(inner.enclosingTypeName())
                    .isPresent()
                    .hasValueSatisfying(outer -> assertThat(outer.name().toString()).isEqualTo("NestedExample"));
            });
    }

    @Test
    void getJDKTypeDescriptorByFqnFindsNestedTypeInNamedModule() {
        final var moduleInfo = JavaFileObjects.forSourceString("module-info", """
            module com.example {
            }
            """);
        final var typeSource = JavaFileObjects.forSourceString("com.example.Outer", """
            package com.example;
            public class Outer {
                public static class Inner {}
            }
            """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(moduleInfo, typeSource)));

        final var outerDescriptor = codeModel.getJDKTypeDescriptor("com.example.Outer").orElseThrow();
        final var innerTypeName = outerDescriptor.traits(MemberTypeDescriptor.class)
            .map(MemberTypeDescriptor::memberTypeName)
            .filter(n -> n.name().toString().equals("Inner"))
            .findFirst().orElseThrow();

        final var descriptor = codeModel.getJDKTypeDescriptor(innerTypeName.binaryName());
        assertThat(descriptor).isPresent();
        assertThat(descriptor.orElseThrow().typeName().moduleName())
            .as("nested TypeName should carry the module from module-info.java")
            .hasValueSatisfying(mn -> assertThat(mn.toString()).isEqualTo("com.example"));
    }
}
