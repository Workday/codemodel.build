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

import build.base.version.Version;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.RequiresModuleDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.descriptor.ExportsDescriptor;
import build.codemodel.jdk.descriptor.JDKModuleDescriptor;
import build.codemodel.jdk.descriptor.OpenModule;
import build.codemodel.jdk.descriptor.RequiresModifier;
import build.codemodel.jdk.descriptor.VersionTrait;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JDKModuleDescriptor}'s bytecode-extraction path ({@link JDKModuleDescriptor#extract}
 * / {@link JDKModuleDescriptor#extractFresh}) that require compiling a fixture {@code module-info.class}
 * via {@code javax.tools.ToolProvider}. Split out from {@code JDKModuleDescriptorTests} in
 * {@code codemodel-jdk} because that module no longer depends on {@code java.compiler}; this module
 * already requires it for source parsing.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class JDKModuleDescriptorExtractTests {

    @TempDir
    Path tempDir;

    private static CodeModel newCodeModel() {
        return new JDKCodeModel(new NonCachingNameProvider());
    }

    // ---- extract: module-info.class --------------------------------------

    @Test
    void extractBasicModuleName() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { }"));
        assertThat(result).isPresent();
        assertThat(result.get().moduleName().toString()).isEqualTo("com.example");
    }

    @Test
    void extractOpenModuleFlag() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("open module com.example { }"));
        assertThat(result.get().hasTrait(OpenModule.class)).isTrue();
    }

    @Test
    void extractClosedModuleFlag() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { }"));
        assertThat(result.get().hasTrait(OpenModule.class)).isFalse();
    }

    @Test
    void extractModuleVersion() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { }", "--module-version", "2.3.4"));
        assertThat(result.get().hasTrait(VersionTrait.class)).isTrue();
        final var vt = result.get().traits(VersionTrait.class).findFirst().orElseThrow();
        assertThat(vt.version()).isEqualTo(Version.parse("2.3.4"));
    }

    @Test
    void extractRequiresTransitive() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { requires transitive java.logging; }"));
        final var req = result.get().traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.logging"))
            .findFirst();
        assertThat(req).isPresent();
        assertThat(req.get().traits(RequiresModifier.class).toList())
            .contains(RequiresModifier.TRANSITIVE)
            .doesNotContain(RequiresModifier.STATIC);
    }

    @Test
    void extractRequiresStatic() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { requires static java.compiler; }"));
        final var req = result.get().traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.compiler"))
            .findFirst();
        assertThat(req).isPresent();
        assertThat(req.get().traits(RequiresModifier.class).toList())
            .contains(RequiresModifier.STATIC)
            .doesNotContain(RequiresModifier.TRANSITIVE);
    }

    @Test
    void extractUnqualifiedExports() throws Exception {
        stubPackage("com.example.api");
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { exports com.example.api; }"));
        final var exp = result.get().traits(ExportsDescriptor.class).findFirst();
        assertThat(exp).isPresent();
        assertThat(exp.get().packageName().toString()).isEqualTo("com.example.api");
        assertThat(exp.get().targetModuleNames()).isEmpty();
    }

    @Test
    void extractQualifiedExports() throws Exception {
        stubPackage("com.example.internal");
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { exports com.example.internal to java.base; }"));
        final var exp = result.get().traits(ExportsDescriptor.class).findFirst();
        assertThat(exp).isPresent();
        assertThat(exp.get().targetModuleNames().stream().map(Object::toString).toList())
            .containsExactly("java.base");
    }

    @Test
    void extractRegistersInCodeModel() throws Exception {
        final CodeModel codeModel = newCodeModel();
        JDKModuleDescriptor.extract(codeModel, compileAndJar("module com.registered.jar { }"));
        final var moduleName = codeModel.getNameProvider()
            .getModuleName("com.registered.jar").orElseThrow();
        assertThat(codeModel.getModuleDescriptor(moduleName)).isPresent();
    }

    // ---- extract: multi-release JARs ------------------------------------

    @Test
    void extractMultiReleaseJarWithVersionedModuleInfo() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            multiReleaseJar("module com.mr.example { }", 9));
        assertThat(result).isPresent();
        assertThat(result.get().moduleName().toString()).isEqualTo("com.mr.example");
    }

    // ---- convenience accessors ------------------------------------------

    @Test
    void versionAccessorReturnsVersion() throws Exception {
        final var withVersion = JDKModuleDescriptor.extract(newCodeModel(),
            compileAndJar("module com.example { }", "--module-version", "4.5.6"));
        assertThat(withVersion.get().version()).contains(Version.parse("4.5.6"));
    }

    // ---- extractFresh() --------------------------------------------------

    @Test
    void extractFreshReturnsDescriptorNotRegisteredInCodeModel() throws Exception {
        final CodeModel codeModel = newCodeModel();
        final var result = JDKModuleDescriptor.extractFresh(codeModel,
            compileAndJar("module com.fresh.example { }"));
        assertThat(result).isPresent();
        assertThat(result.get().moduleName().toString()).isEqualTo("com.fresh.example");
        final var moduleName = codeModel.getNameProvider()
            .getModuleName("com.fresh.example").orElseThrow();
        assertThat(codeModel.getModuleDescriptor(moduleName)).isEmpty();
    }

    @Test
    void extractFreshReturnsSeparateDescriptorsForSameModuleName() throws Exception {
        final CodeModel codeModel = newCodeModel();
        final Path jar1 = compileAndJar("module com.shared { }", "--module-version", "1.0.0");
        final Path jar2 = compileAndJar("module com.shared { }", "--module-version", "2.0.0");
        final var d1 = JDKModuleDescriptor.extractFresh(codeModel, jar1).orElseThrow();
        final var d2 = JDKModuleDescriptor.extractFresh(codeModel, jar2).orElseThrow();
        assertThat(d1).isNotSameAs(d2);
        assertThat(d1.version()).contains(Version.parse("1.0.0"));
        assertThat(d2.version()).contains(Version.parse("2.0.0"));
    }

    // ---- add-helper idempotency ------------------------------------------

    @Test
    void extractCalledTwiceForSameModuleProducesNoDuplicateDirectives() throws Exception {
        stubPackage("com.example.api");
        final Path jar = compileAndJar("""
            module com.example {
                requires java.logging;
                exports com.example.api;
            }
            """);
        final CodeModel codeModel = newCodeModel();
        JDKModuleDescriptor.extract(codeModel, jar);
        final var descriptor = JDKModuleDescriptor.extract(codeModel, jar).orElseThrow();
        assertThat(descriptor.requiresClauses()
            .filter(r -> r.requiresModuleName().toString().equals("java.logging"))
            .count()).isEqualTo(1);
        assertThat(descriptor.exportsClauses().count()).isEqualTo(1);
    }

    // ---- compile helpers ------------------------------------------------

    /**
     * Creates a dummy class so javac accepts the package in exports/opens directives.
     */
    private void stubPackage(final String pkg) throws IOException {
        final Path pkgDir = tempDir.resolve(pkg.replace('.', '/'));
        Files.createDirectories(pkgDir);
        Files.writeString(pkgDir.resolve("Stub.java"),
            "package " + pkg + "; public class Stub {}");
    }

    // ---- JAR-building helpers -------------------------------------------

    private Path compileAndJar(final String source, final String... extraArgs) throws Exception {
        Files.writeString(tempDir.resolve("module-info.java"), source);
        final var args = new ArrayList<String>(Arrays.asList(extraArgs));
        args.addAll(java.util.List.of("-d", tempDir.toString()));
        try (var walk = Files.walk(tempDir)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                .map(Path::toString)
                .forEach(args::add);
        }
        final int rc = ToolProvider.getSystemJavaCompiler()
            .run(null, null, null, args.toArray(String[]::new));
        if (rc != 0) {
            throw new IllegalStateException("Compilation failed (exit " + rc + ")");
        }
        final Path jar = Files.createTempFile(tempDir, "test-module", ".jar");
        try (var jos = new JarOutputStream(Files.newOutputStream(jar))) {
            jos.putNextEntry(new JarEntry("module-info.class"));
            jos.write(Files.readAllBytes(tempDir.resolve("module-info.class")));
            jos.closeEntry();
        }
        return jar;
    }

    /**
     * Builds a multi-release JAR whose {@code module-info.class} lives only under
     * {@code META-INF/versions/<version>/} — no base entry. Requires MR-aware opening
     * ({@code JarFile(path, true, ZipFile.OPEN_READ, Runtime.version())}) to resolve it.
     */
    private Path multiReleaseJar(final String source, final int version) throws Exception {
        final Path srcFile = tempDir.resolve("module-info.java");
        Files.writeString(srcFile, source);
        final Path classDir = Files.createTempDirectory(tempDir, "mr-classes");
        final int rc = ToolProvider.getSystemJavaCompiler()
            .run(null, null, null, "-d", classDir.toString(), srcFile.toString());
        if (rc != 0) {
            throw new IllegalStateException("Compilation failed (exit " + rc + ")");
        }
        final Path jar = Files.createTempFile(tempDir, "mr-module", ".jar");
        final var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Multi-Release", "true");
        try (var jos = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            jos.putNextEntry(new JarEntry("META-INF/versions/" + version + "/module-info.class"));
            jos.write(Files.readAllBytes(classDir.resolve("module-info.class")));
            jos.closeEntry();
        }
        return jar;
    }
}
