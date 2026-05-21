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

import build.codemodel.foundation.descriptor.RequiresModuleDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.jdk.descriptor.RequiresModifier;
import build.codemodel.jdk.descriptor.SourceLocation;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for incremental rescan: dropping all descriptors and traits sourced from a changed file,
 * then re-analyzing the new version of that file in isolation.
 *
 * <p>The API under test is {@link JDKCodeModel#rescan(javax.tools.JavaFileObject)}, which
 * uses the {@link SourceLocation.FilePosition} URI attached to each descriptor to identify
 * which entries belong to the file being rescanned, evicts them, then re-runs source analysis
 * on the updated content.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class IncrementalRescanTests {

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private static JDKCodeModel populate(final javax.tools.JavaFileObject... sources) {
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(sources)).initialize(codeModel);
        return codeModel;
    }

    // ---------------------------------------------------------------------------
    // Basic rescan — single-file model
    // ---------------------------------------------------------------------------

    /**
     * A field that exists in v1 must be gone after rescanning with v2 that omits it.
     */
    @Test
    void removedFieldIsAbsentAfterRescan() {
        final var v1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int value;
            }
            """);
        final var codeModel = populate(v1);

        final var fooName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        assertThat(codeModel.getTypeDescriptor(fooName)).isPresent();
        assertThat(codeModel.getTypeDescriptor(fooName).orElseThrow().traits(FieldDescriptor.class)
            .map(f -> f.fieldName().toString())
            .toList()).contains("value");

        // Rescan with a version of Foo that has no fields
        final var v2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
            }
            """);
        codeModel.rescan(v2);

        assertThat(codeModel.getTypeDescriptor(fooName)).isPresent();
        assertThat(codeModel.getTypeDescriptor(fooName).orElseThrow().traits(FieldDescriptor.class).toList())
            .as("field 'value' must be absent after rescan")
            .isEmpty();
    }

    /**
     * A field added in v2 must be present after rescan; it must not duplicate the v1 field.
     * The post-rescan descriptor should reflect exactly the v2 content, no more.
     */
    @Test
    void renamedFieldReplacesOldFieldAfterRescan() {
        final var v1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int originalName;
            }
            """);
        final var codeModel = populate(v1);

        final var v2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String renamedField;
            }
            """);
        codeModel.rescan(v2);

        final var fooName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var fieldNames = codeModel.getTypeDescriptor(fooName).orElseThrow()
            .traits(FieldDescriptor.class)
            .map(f -> f.fieldName().toString())
            .toList();

        assertThat(fieldNames)
            .as("renamed field must be present")
            .contains("renamedField");
        assertThat(fieldNames)
            .as("old field must not survive rescan")
            .doesNotContain("originalName");
        assertThat(fieldNames)
            .as("descriptor must reflect only v2 content — no accumulated duplicates")
            .hasSize(1);
    }

    /**
     * A method added in v2 is discoverable after rescan.
     */
    @Test
    void addedMethodIsPresentAfterRescan() {
        final var v1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
            }
            """);
        final var codeModel = populate(v1);

        final var v2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public void hello() {}
            }
            """);
        codeModel.rescan(v2);

        final var fooName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        assertThat(codeModel.getTypeDescriptor(fooName).orElseThrow()
            .traits(MethodDescriptor.class)
            .map(m -> m.methodName().name().toString())
            .toList())
            .contains("hello");
    }

    /**
     * After rescan, the surviving descriptor for Foo carries a fresh {@link SourceLocation.FilePosition}
     * whose URI matches the updated file object rather than the original.
     * Since both v1 and v2 share the same URI (derived from the class name), the URI check
     * verifies that the descriptor was re-attached correctly.
     */
    @Test
    void rescanedTypeDescriptorCarriesRefreshedSourceLocation() {
        final var v1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int value;
            }
            """);
        final var codeModel = populate(v1);

        final var v2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public String name;
            }
            """);
        codeModel.rescan(v2);

        final var fooName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var descriptor = codeModel.getTypeDescriptor(fooName).orElseThrow();

        assertThat(descriptor.getTrait(SourceLocation.FilePosition.class))
            .as("type descriptor must carry a FilePosition after rescan")
            .isPresent();

        final var filePos = descriptor.getTrait(SourceLocation.FilePosition.class).orElseThrow();
        assertThat(filePos.uri()).isEqualTo(v2.toUri());
    }

    // ---------------------------------------------------------------------------
    // Multi-file model — descriptors from other files must survive untouched
    // ---------------------------------------------------------------------------

    /**
     * Rescanning Foo must not affect Bar, which lives in a separate file.
     */
    @Test
    void descriptorsFromOtherFilesAreUnaffectedByRescan() {
        final var fooSource = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int fooField;
            }
            """);
        final var barSource = JavaFileObjects.forSourceString("com.example.Bar", """
            package com.example;
            public class Bar {
                public String barField;
            }
            """);
        final var codeModel = populate(fooSource, barSource);

        final var barName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Bar");
        assertThat(codeModel.getTypeDescriptor(barName)).isPresent();

        final var fooV2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
            }
            """);
        codeModel.rescan(fooV2);

        // Bar must still be present, unchanged
        assertThat(codeModel.getTypeDescriptor(barName))
            .as("Bar descriptor must survive rescan of Foo")
            .isPresent();
        assertThat(codeModel.getTypeDescriptor(barName).orElseThrow()
            .traits(FieldDescriptor.class)
            .map(f -> f.fieldName().toString())
            .toList())
            .as("Bar's fields must be intact after rescanning Foo")
            .contains("barField");
    }

    // ---------------------------------------------------------------------------
    // Nested (inner) types
    // ---------------------------------------------------------------------------

    /**
     * An inner class present in v1 but absent from v2 must be evicted from the model.
     * Both {@code Foo} and {@code Foo.Inner} carry FilePosition URIs pointing to the
     * same source file, so both must be dropped and only the new v2 Foo re-added.
     */
    @Test
    void removedInnerTypeIsEvictedAfterRescan() {
        final var v1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public static class Inner {}
            }
            """);
        final var codeModel = populate(v1);

        final var naming = codeModel.getNameProvider();
        final var innerName = naming.getTypeName(Optional.empty(), "com.example.Foo$Inner");
        assertThat(codeModel.getTypeDescriptor(innerName))
            .as("Foo.Inner must be present in the initial model")
            .isPresent();

        final var v2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                // Inner removed
            }
            """);
        codeModel.rescan(v2);

        assertThat(codeModel.getTypeDescriptor(innerName))
            .as("Foo.Inner must be absent after rescanning without it")
            .isEmpty();
        assertThat(codeModel.getTypeDescriptor(naming.getTypeName(Optional.empty(), "com.example.Foo")))
            .as("Foo itself must still be present")
            .isPresent();
    }

    // ---------------------------------------------------------------------------
    // Cross-file references — TypeName-based resolution survives rescan
    // ---------------------------------------------------------------------------

    /**
     * {@code SpecificTypeUsage} holds a {@link build.codemodel.foundation.naming.TypeName} and a
     * {@link build.codemodel.foundation.CodeModel} reference, not a direct pointer to the
     * {@link build.codemodel.foundation.descriptor.TypeDescriptor}. So when Foo is rescanned and
     * its descriptor is replaced, Bar's field TypeUsage automatically resolves to the new descriptor
     * via the CodeModel lookup — no staleness.
     */
    @Test
    void crossFileTypeUsageResolvesUpdatedDescriptorAfterRescan() {
        final var fooSource = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {}
            """);
        final var barSource = JavaFileObjects.forSourceString("com.example.Bar", """
            package com.example;
            public class Bar {
                public Foo dependency;
            }
            """);
        final var codeModel = populate(fooSource, barSource);

        final var naming = codeModel.getNameProvider();
        final var fooName = naming.getTypeName(Optional.empty(), "com.example.Foo");
        final var barName = naming.getTypeName(Optional.empty(), "com.example.Bar");

        final var fooV2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public int newField;
            }
            """);
        codeModel.rescan(fooV2);

        // The new Foo descriptor must be in place
        final var newFooDescriptor = codeModel.getTypeDescriptor(fooName).orElseThrow();
        assertThat(newFooDescriptor.traits(FieldDescriptor.class)
            .map(f -> f.fieldName().toString())
            .toList())
            .contains("newField");

        // Bar's field TypeUsage holds a TypeName — resolving it through the model must
        // return the new Foo descriptor, not the evicted one
        final var dependencyField = codeModel.getTypeDescriptor(barName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dependency"))
            .findFirst().orElseThrow();

        assertThat(dependencyField.type())
            .isInstanceOf(SpecificTypeUsage.class);
        final var fieldTypeName = ((build.codemodel.foundation.usage.NamedTypeUsage) dependencyField.type()).typeName();
        assertThat(codeModel.getTypeDescriptor(fieldTypeName))
            .as("Bar's field TypeName must resolve to the updated Foo descriptor")
            .contains(newFooDescriptor);
    }

    // ---------------------------------------------------------------------------
    // Classpath — rescan must forward classpath to the re-analysis
    // ---------------------------------------------------------------------------

    /**
     * When the rescanned file references a type that lives on the classpath (not among the
     * source files), the rescan must pass that classpath through to the new {@link JdkInitializer}.
     * Without it the type degrades to {@link build.codemodel.foundation.usage.UnknownTypeUsage}.
     */
    @Test
    void rescanPreservesClasspathResolution() throws Exception {
        final var classpathDir = java.nio.file.Files.createTempDirectory("rescan-test-cp");

        // Compile Helper to a temp directory so it is only on the classpath, never a source file
        final var helperSource = JavaFileObjects.forSourceString(
            "com.example.Helper",
            "package com.example; public class Helper {}");
        final var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        try (final var fm = compiler.getStandardFileManager(null, null, null)) {
            compiler.getTask(null, fm, _ -> {
                }, List.of("-d", classpathDir.toString()),
                null, List.of(helperSource)).call();
        }

        // Initial populate: Foo references Helper via the classpath
        final var fooV1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Helper dep;
            }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(fooV1), List.of(classpathDir), List.of())
            .initialize(codeModel);

        final var fooName = codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo");
        final var depField = codeModel.getTypeDescriptor(fooName).orElseThrow()
            .traits(FieldDescriptor.class).findFirst().orElseThrow();
        assertThat(depField.type())
            .as("Helper must resolve on initial populate")
            .isNotInstanceOf(build.codemodel.foundation.usage.UnknownTypeUsage.class);

        // Rescan Foo with the classpath so Helper still resolves
        final var fooV2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Helper dep;
                public int extra;
            }
            """);
        codeModel.rescan(fooV2, List.of(classpathDir), List.of());

        final var depFieldAfter = codeModel.getTypeDescriptor(fooName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dep"))
            .findFirst().orElseThrow();
        assertThat(depFieldAfter.type())
            .as("Helper must still resolve after rescan when classpath is forwarded")
            .isNotInstanceOf(build.codemodel.foundation.usage.UnknownTypeUsage.class);
    }

    /**
     * Without forwarding the classpath, a rescan of a file that references a classpath-only type
     * degrades that field to {@link build.codemodel.foundation.usage.UnknownTypeUsage}.
     * This test documents the failure mode so callers know they must use the overload that
     * accepts classpath/modulePath.
     */
    // ---------------------------------------------------------------------------
    // Module rescan
    // ---------------------------------------------------------------------------

    /**
     * A {@code requires} directive present in v1 must be gone after rescanning with v2 that omits it.
     * Verifies that the module descriptor is evicted and re-created, not mutated in place.
     */
    @Test
    void removedRequiresIsAbsentAfterModuleRescan() {
        final var v1 = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires transitive java.logging;
                requires static java.compiler;
                requires java.base;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(v1)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        assertThat(codeModel.getModuleDescriptor(moduleName)).isPresent();
        assertThat(codeModel.getModuleDescriptor(moduleName).orElseThrow()
            .traits(RequiresModuleDescriptor.class)
            .map(r -> r.requiresModuleName().toString())
            .toList())
            .contains("java.logging", "java.compiler");

        final var v2 = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires java.base;
            }
            """);
        codeModel.rescan(v2);

        assertThat(codeModel.getModuleDescriptor(moduleName))
            .as("module descriptor must still be present after rescan")
            .isPresent();
        assertThat(codeModel.getModuleDescriptor(moduleName).orElseThrow()
            .traits(RequiresModuleDescriptor.class)
            .map(r -> r.requiresModuleName().toString())
            .toList())
            .as("java.logging must be gone after rescan")
            .doesNotContain("java.logging", "java.compiler");
    }

    /**
     * A {@code requires transitive} modifier added in v2 must be reflected after rescan,
     * and must not duplicate the directive from v1.
     */
    @Test
    void changedRequiresModifierIsReflectedAfterModuleRescan() {
        final var v1 = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires java.logging;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(v1)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var loggingV1 = codeModel.getModuleDescriptor(moduleName).orElseThrow()
            .traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.logging"))
            .findFirst().orElseThrow();
        assertThat(loggingV1.hasTrait(RequiresModifier.class))
            .as("java.logging must not be transitive in v1")
            .isFalse();

        final var v2 = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires transitive java.logging;
            }
            """);
        codeModel.rescan(v2);

        final var requires = codeModel.getModuleDescriptor(moduleName).orElseThrow()
            .traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.logging"))
            .toList();
        assertThat(requires).as("exactly one java.logging requires after rescan").hasSize(1);
        assertThat(requires.get(0).traits(RequiresModifier.class).toList())
            .as("java.logging must be transitive in v2")
            .contains(RequiresModifier.TRANSITIVE);
    }

    /**
     * Rescanning a module file must not affect type descriptors from other source files.
     */
    @Test
    void typeDescriptorsFromOtherFilesUnaffectedByModuleRescan() {
        final var moduleSource = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires java.base;
            }
            """);
        final var classSource = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo { public int value; }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(moduleSource, classSource)));

        final var fooName = "com.example.Foo";
        assertThat(codeModel.getJDKTypeDescriptor(fooName)).isPresent();

        final var moduleV2 = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires java.base;
                requires java.logging;
            }
            """);
        codeModel.rescan(moduleV2);

        assertThat(codeModel.getJDKTypeDescriptor(fooName))
            .as("Foo must survive a module-info rescan")
            .isPresent();
        assertThat(codeModel.getJDKTypeDescriptor(fooName).orElseThrow()
            .traits(FieldDescriptor.class).map(f -> f.fieldName().toString()).toList())
            .contains("value");
    }

    /**
     * Regression: when a method's return type is a generic whose type argument is defined in a
     * required module, rescanning the method's file without its {@code module-info.java} degrades
     * the return type to {@link build.codemodel.foundation.usage.UnknownTypeUsage} because javac
     * sees the cross-module type as an {@code ErrorType} when there are no {@code requires}
     * declarations in scope.
     *
     * <p>The failure mode observed in practice: after adding then removing an unrelated method
     * in {@code ResolveTypeOperation}, the {@code apply()} method's return type
     * {@code OperationResult<JDKTypeDescriptor>} degraded to {@code UnknownTypeUsage} with
     * canonical name {@code null}.
     *
     * <p>The fix passes {@code module-info.java} as a context file to
     * {@link JDKCodeModel#rescan(javax.tools.JavaFileObject, List, List, List)} so javac can
     * resolve the module's {@code requires} declarations during the single-file recompilation.
     */
    @Test
    void genericReturnTypeOfSourceDefinedTypeRemainsResolvedAfterRescan() throws Exception {
        // Compile Result<T> as a named module into an exploded module directory.
        // --module-path expects modules at <modulePathDir>/<moduleName>/.
        final var modulePathDir = java.nio.file.Files.createTempDirectory("rescan-test-modpath");
        final var resultModuleDir = modulePathDir.resolve("com.example.result");
        java.nio.file.Files.createDirectories(resultModuleDir);
        final var resultModuleInfo = JavaFileObjects.forSourceString("module-info", """
            module com.example.result {
                exports com.example.result;
            }
            """);
        final var resultSource = JavaFileObjects.forSourceString("com.example.result.Result", """
            package com.example.result;
            public class Result<T> {}
            """);
        final var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        try (final var fm = compiler.getStandardFileManager(null, null, null)) {
            compiler.getTask(null, fm, _ -> {
                },
                List.of("-d", resultModuleDir.toString()),
                null, List.of(resultModuleInfo, resultSource)).call();
        }

        // Consumer module's module-info.java — used both for initial populate and rescan context.
        final var consumerModuleInfo = JavaFileObjects.forSourceString("module-info", """
            module com.example.consumer {
                requires com.example.result;
            }
            """);

        // Initial populate: compile module-info and Foo together so javac resolves the module path.
        final var fooV1 = JavaFileObjects.forSourceString("com.example.consumer.Foo", """
            package com.example.consumer;
            import com.example.result.Result;
            public class Foo {
                public Result<String> items() { return null; }
            }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(consumerModuleInfo, fooV1),
            List.of(), List.of(modulePathDir))
            .initialize(codeModel);

        final var fooFqn = "com.example.consumer.Foo";

        final var initialItems = codeModel.getJDKTypeDescriptor(fooFqn).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("items"))
            .findFirst().orElseThrow();
        assertThat(initialItems.returnType())
            .as("items() return type must resolve on initial populate")
            .isNotInstanceOf(build.codemodel.foundation.usage.UnknownTypeUsage.class);

        // Add then remove an extra method, passing module-info as context so javac keeps module resolution.
        final var fooV2 = JavaFileObjects.forSourceString("com.example.consumer.Foo", """
            package com.example.consumer;
            import com.example.result.Result;
            public class Foo {
                public Result<String> items() { return null; }
                public String extra() { return null; }
            }
            """);
        codeModel.rescan(fooV2, List.of(consumerModuleInfo), List.of(), List.of(modulePathDir));

        final var fooV3 = JavaFileObjects.forSourceString("com.example.consumer.Foo", """
            package com.example.consumer;
            import com.example.result.Result;
            public class Foo {
                public Result<String> items() { return null; }
            }
            """);
        codeModel.rescan(fooV3, List.of(consumerModuleInfo), List.of(), List.of(modulePathDir));

        final var itemsAfterRescan = codeModel.getJDKTypeDescriptor(fooFqn).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("items"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("items() missing after second rescan"));

        assertThat(itemsAfterRescan.returnType())
            .as("items() return type must not degrade to UnknownTypeUsage after add-then-remove rescan")
            .isNotInstanceOf(build.codemodel.foundation.usage.UnknownTypeUsage.class);
        assertThat(itemsAfterRescan.returnType().canonicalName())
            .as("items() return type canonical name must not be null")
            .isNotNull();
    }

    @Test
    void rescanWithoutClasspathDegradesToUnknownTypeUsage() throws Exception {
        final var classpathDir = java.nio.file.Files.createTempDirectory("rescan-test-cp-missing");

        final var helperSource = JavaFileObjects.forSourceString(
            "com.example.Helper",
            "package com.example; public class Helper {}");
        final var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        try (final var fm = compiler.getStandardFileManager(null, null, null)) {
            compiler.getTask(null, fm, _ -> {
                }, List.of("-d", classpathDir.toString()),
                null, List.of(helperSource)).call();
        }

        final var fooV1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Helper dep;
            }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(fooV1), List.of(classpathDir), List.of())
            .initialize(codeModel);

        // Rescan without classpath — Helper is no longer resolvable
        final var fooV2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Helper dep;
                public int extra;
            }
            """);
        codeModel.rescan(fooV2);  // no classpath forwarded

        final var depFieldAfter = codeModel.getTypeDescriptor(
                codeModel.getNameProvider().getTypeName(Optional.empty(), "com.example.Foo"))
            .orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dep"))
            .findFirst().orElseThrow();
        assertThat(depFieldAfter.type())
            .as("Helper degrades to UnknownTypeUsage when classpath is not forwarded")
            .isInstanceOf(build.codemodel.foundation.usage.UnknownTypeUsage.class);
    }

    /**
     * Regression: types defined in sibling source files of the same module degrade to
     * {@link build.codemodel.foundation.usage.UnknownTypeUsage} after a rescan, because those
     * sibling files are neither on the classpath/module-path nor included in the rescan's
     * compilation unit.
     *
     * <p>During initial populate, all source files are compiled together so siblings resolve
     * freely. On rescan only the changed file is recompiled, and javac has no visibility into
     * the other source files of the same module unless they appear in the compilation or have
     * been pre-compiled to an output directory that is forwarded as classpath.
     *
     * <p>Fix: include the module's own compiled output directory (e.g. {@code target/classes})
     * in the rescan classpath so sibling types remain resolvable.
     */
    @Test
    void sameModuleSiblingTypeDegradesToUnknownTypeUsageAfterRescan() {
        // Two classes compiled together in the same initial populate — sibling resolves fine.
        final var helperSource = JavaFileObjects.forSourceString("com.example.Helper", """
            package com.example;
            public class Helper {}
            """);
        final var fooV1 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Helper dep;
            }
            """);
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        new JdkInitializer(List.of(), List.of(), List.of(helperSource, fooV1))
            .initialize(codeModel);

        final var fooName = "com.example.Foo";
        final var initialDep = codeModel.getJDKTypeDescriptor(fooName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dep"))
            .findFirst().orElseThrow();
        assertThat(initialDep.type())
            .as("Helper must resolve on initial populate when compiled together")
            .isNotInstanceOf(UnknownTypeUsage.class);

        // Rescan Foo with Helper as a context file — compiled together so javac resolves Helper,
        // but the registration filter ensures Helper's descriptor is not re-registered.
        final var fooV2 = JavaFileObjects.forSourceString("com.example.Foo", """
            package com.example;
            public class Foo {
                public Helper dep;
                public int extra;
            }
            """);
        codeModel.rescan(fooV2, List.of(helperSource), List.of(), List.of());

        final var depAfterRescan = codeModel.getJDKTypeDescriptor(fooName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dep"))
            .findFirst().orElseThrow();
        assertThat(depAfterRescan.type())
            .as("Helper must not degrade after rescan — sibling types should remain resolvable")
            .isNotInstanceOf(UnknownTypeUsage.class);
    }
}
