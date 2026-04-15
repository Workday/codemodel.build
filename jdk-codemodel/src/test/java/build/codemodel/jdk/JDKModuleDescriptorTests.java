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

import build.base.parsing.ParseException;
import build.base.version.Version;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.RequiresModuleDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.jdk.descriptor.ExportsDescriptor;
import build.codemodel.jdk.descriptor.JDKModuleDescriptor;
import build.codemodel.jdk.descriptor.OpenModule;
import build.codemodel.jdk.descriptor.OpensDescriptor;
import build.codemodel.jdk.descriptor.ProvidesDescriptor;
import build.codemodel.jdk.descriptor.RequiresModifier;
import build.codemodel.jdk.descriptor.UsesDescriptor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JDKModuleDescriptor} — source parsing via {@link JDKModuleDescriptor#parse}
 * and JAR extraction via {@link JDKModuleDescriptor#extract}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class JDKModuleDescriptorTests {

    @TempDir
    Path tempDir;

    private static CodeModel newCodeModel() {
        return new JDKCodeModel(new NonCachingNameProvider());
    }

    private static JDKModuleDescriptor parse(final String source) throws ParseException {
        return JDKModuleDescriptor.parse(newCodeModel(), source);
    }

    // ---- module name / open flag -----------------------------------------

    @Test
    void parseSimpleModuleName() throws ParseException {
        final var md = parse("module com.example { }");
        assertThat(md.moduleName().toString()).isEqualTo("com.example");
    }

    @Test
    void parseOpenModule() throws ParseException {
        final var md = parse("open module com.example { }");
        assertThat(md.hasTrait(OpenModule.class)).isTrue();
    }

    @Test
    void parseClosedModule() throws ParseException {
        final var md = parse("module com.example { }");
        assertThat(md.hasTrait(OpenModule.class)).isFalse();
    }

    // ---- requires --------------------------------------------------------

    @Test
    void parseRequiresPlain() throws ParseException {
        final var md = parse("""
            module com.example {
                requires java.base;
            }
            """);
        assertThat(md.traits(RequiresModuleDescriptor.class)
            .map(r -> r.requiresModuleName().toString())
            .toList())
            .containsExactly("java.base");

        final var req = md.traits(RequiresModuleDescriptor.class).findFirst().orElseThrow();
        assertThat(req.hasTrait(RequiresModifier.class)).isFalse();
    }

    @Test
    void parseRequiresTransitive() throws ParseException {
        final var md = parse("""
            module com.example {
                requires transitive com.lib;
            }
            """);
        final var req = md.traits(RequiresModuleDescriptor.class).findFirst().orElseThrow();
        assertThat(req.traits(RequiresModifier.class).toList()).contains(RequiresModifier.TRANSITIVE);
        assertThat(req.traits(RequiresModifier.class).toList()).doesNotContain(RequiresModifier.STATIC);
    }

    @Test
    void parseRequiresStatic() throws ParseException {
        final var md = parse("""
            module com.example {
                requires static com.lib;
            }
            """);
        final var req = md.traits(RequiresModuleDescriptor.class).findFirst().orElseThrow();
        assertThat(req.traits(RequiresModifier.class).toList()).contains(RequiresModifier.STATIC);
        assertThat(req.traits(RequiresModifier.class).toList()).doesNotContain(RequiresModifier.TRANSITIVE);
    }

    @Test
    void parseMultipleRequires() throws ParseException {
        final var md = parse("""
            module com.example {
                requires java.base;
                requires transitive java.logging;
                requires static java.compiler;
            }
            """);
        assertThat(md.traits(RequiresModuleDescriptor.class).count()).isEqualTo(3);
    }

    // ---- exports ---------------------------------------------------------

    @Test
    void parseUnqualifiedExport() throws ParseException {
        final var md = parse("""
            module com.example {
                exports com.example.api;
            }
            """);
        final ExportsDescriptor exp = md.traits(ExportsDescriptor.class).findFirst().orElseThrow();
        assertThat(exp.packageName().toString()).isEqualTo("com.example.api");
        assertThat(exp.targetModuleNames()).isEmpty();
    }

    @Test
    void parseQualifiedExport() throws ParseException {
        final var md = parse("""
            module com.example {
                exports com.example.internal to com.consumer, com.tests;
            }
            """);
        final ExportsDescriptor exp = md.traits(ExportsDescriptor.class).findFirst().orElseThrow();
        assertThat(exp.packageName().toString()).isEqualTo("com.example.internal");
        assertThat(exp.targetModuleNames().stream().map(Object::toString).toList())
            .containsExactlyInAnyOrder("com.consumer", "com.tests");
    }

    // ---- opens -----------------------------------------------------------

    @Test
    void parseUnqualifiedOpens() throws ParseException {
        final var md = parse("""
            module com.example {
                opens com.example.impl;
            }
            """);
        final OpensDescriptor op = md.traits(OpensDescriptor.class).findFirst().orElseThrow();
        assertThat(op.packageName().toString()).isEqualTo("com.example.impl");
        assertThat(op.targetModuleNames()).isEmpty();
    }

    @Test
    void parseQualifiedOpens() throws ParseException {
        final var md = parse("""
            module com.example {
                opens com.example.impl to com.framework;
            }
            """);
        final OpensDescriptor op = md.traits(OpensDescriptor.class).findFirst().orElseThrow();
        assertThat(op.targetModuleNames().stream().map(Object::toString).toList())
            .containsExactly("com.framework");
    }

    // ---- uses / provides -------------------------------------------------

    @Test
    void parseUses() throws ParseException {
        final var md = parse("""
            module com.example {
                uses com.example.spi.MyService;
            }
            """);
        assertThat(md.traits(UsesDescriptor.class)
            .map(u -> u.serviceType().toString())
            .toList())
            .containsExactly("com.example.spi.MyService");
    }

    @Test
    void parseProvides() throws ParseException {
        final var md = parse("""
            module com.example {
                provides com.example.spi.MyService with com.example.impl.MyServiceImpl;
            }
            """);
        final ProvidesDescriptor prov = md.traits(ProvidesDescriptor.class).findFirst().orElseThrow();
        assertThat(prov.serviceType().toString()).isEqualTo("com.example.spi.MyService");
        assertThat(prov.implementationTypes().stream().map(Object::toString).toList())
            .containsExactly("com.example.impl.MyServiceImpl");
    }

    @Test
    void parseProvidesWithMultipleImplementations() throws ParseException {
        final var md = parse("""
            module com.example {
                provides com.spi.Service with com.impl.Impl1, com.impl.Impl2;
            }
            """);
        final ProvidesDescriptor prov = md.traits(ProvidesDescriptor.class).findFirst().orElseThrow();
        assertThat(prov.implementationTypes()).hasSize(2);
    }

    // ---- comments / imports ----------------------------------------------

    @Test
    void parseIgnoresSingleLineComments() throws ParseException {
        final var md = parse("""
            // this is a module
            module com.example {
                requires java.base; // always needed
            }
            """);
        assertThat(md.moduleName().toString()).isEqualTo("com.example");
        assertThat(md.traits(RequiresModuleDescriptor.class).count()).isEqualTo(1);
    }

    @Test
    void parseIgnoresMultilineComments() throws ParseException {
        final var md = parse("""
            /* copyright notice */
            module com.example {
                /* description */
                requires java.base;
            }
            """);
        assertThat(md.traits(RequiresModuleDescriptor.class).count()).isEqualTo(1);
    }

    @Test
    void parseSkipsImportStatements() throws ParseException {
        final var md = parse("""
            import com.example.SomeAnnotation;
            module com.example {
                requires java.base;
            }
            """);
        assertThat(md.moduleName().toString()).isEqualTo("com.example");
    }

    // ---- error handling --------------------------------------------------

    @Test
    void parseThrowsOnInvalidSyntax() {
        assertThatThrownBy(() -> parse("not a module"))
            .isInstanceOf(ParseException.class);
    }

    // ---- registered in CodeModel -----------------------------------------

    @Test
    void parsedDescriptorIsRegisteredInCodeModel() throws ParseException {
        final var codeModel = newCodeModel();
        JDKModuleDescriptor.parse(codeModel, "module com.registered { }");

        final var moduleName = codeModel.getNameProvider().getModuleName("com.registered").orElseThrow();
        assertThat(codeModel.getModuleDescriptor(moduleName)).isPresent();
    }

    // ---- VersionTrait ----------------------------------------------------

    @Test
    void versionTraitWrapsVersion() {
        final var trait = VersionTrait.of(Version.parse("1.2.3"));
        assertThat(trait.version()).isEqualTo(Version.parse("1.2.3"));
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

    // ---- extract: Automatic-Module-Name fallback -------------------------

    @Test
    void extractAutoModuleName() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            autoModuleJar("com.auto.module", null));
        assertThat(result).isPresent();
        assertThat(result.get().moduleName().toString()).isEqualTo("com.auto.module");
        assertThat(result.get().hasTrait(VersionTrait.class)).isFalse();
    }

    @Test
    void extractAutoModuleVersion() throws Exception {
        final var result = JDKModuleDescriptor.extract(newCodeModel(),
            autoModuleJar("com.auto.module", "3.1.0"));
        assertThat(result.get().hasTrait(VersionTrait.class)).isTrue();
        final var vt = result.get().traits(VersionTrait.class).findFirst().orElseThrow();
        assertThat(vt.version()).isEqualTo(Version.parse("3.1.0"));
    }

    // ---- extract: edge cases --------------------------------------------

    @Test
    void extractFromPlainJarReturnsEmpty() throws Exception {
        final Path jar = Files.createTempFile(tempDir, "plain", ".jar");
        try (var jos = new JarOutputStream(Files.newOutputStream(jar))) {
            jos.putNextEntry(new JarEntry("com/example/Foo.class"));
            jos.write(new byte[0]);
            jos.closeEntry();
        }
        assertThat(JDKModuleDescriptor.extract(newCodeModel(), jar)).isEmpty();
    }

    @Test
    void extractFromNonexistentPathThrows() {
        assertThatThrownBy(() ->
            JDKModuleDescriptor.extract(newCodeModel(), Path.of("no-such.jar")))
            .isInstanceOf(IllegalArgumentException.class);
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

    private Path autoModuleJar(final String moduleName, final String version) throws IOException {
        final Path jar = Files.createTempFile(tempDir, "auto-module", ".jar");
        final var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Automatic-Module-Name", moduleName);
        if (version != null) {
            manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, version);
        }
        try (var jos = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            // intentionally empty — only the manifest matters
        }
        return jar;
    }
}
