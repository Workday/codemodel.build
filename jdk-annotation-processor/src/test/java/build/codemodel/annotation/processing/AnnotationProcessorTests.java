package build.codemodel.annotation.processing;

import build.base.compile.testing.Compilation;
import build.base.compile.testing.Compiler;
import build.base.compile.testing.JavaFileObjects;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AnnotationProcessorTests {

    protected static Compilation compile(final AnnotationProcessor processor,
                                         final String typeName,
                                         final String source) {
        final var compilation = makeCompiler(processor)
            .compile(JavaFileObjects.forSourceString(typeName, source));

        compilation.diagnostics()
            .forEach(d -> System.out.printf("[%s] %s%n", d.getKind(), d.getMessage(Locale.ROOT)));

        assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);
        return compilation;
    }

    // Like compile(), but does not assert success — use for tests that exercise error paths.
    protected static Compilation run(final AnnotationProcessor processor,
                                     final String typeName,
                                     final String source) {
        final var compilation = makeCompiler(processor)
            .compile(JavaFileObjects.forSourceString(typeName, source));

        compilation.diagnostics()
            .forEach(d -> System.out.printf("[%s] %s%n", d.getKind(), d.getMessage(Locale.ROOT)));

        return compilation;
    }

    private static Compiler makeCompiler(final AnnotationProcessor processor) {
        final var classPath = Arrays.stream(System.getProperty("java.class.path", "").split(File.pathSeparator))
            .filter(s -> !s.isBlank())
            .map(p -> FileSystems.getDefault().getPath(p))
            .toList();

        var compiler = Compiler.javac()
            .withClasspath(classPath)
            .withProcessors(processor);

        final var modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null && !modulePath.isBlank()) {
            compiler = compiler.withOptions(
                "--module-path=" + modulePath,
                "--add-modules=build.codemodel.foundation,build.codemodel.jdk.annotation.discovery");
        }

        return compiler;
    }
}
