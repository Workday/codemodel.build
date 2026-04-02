package build.codemodel.annotation.processing;

import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests to ensure discovery of recursively defined types using the {@link AnnotationProcessor}.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
public class RecursivelyDefinedTypeDiscoveryTests {

    /**
     * Ensure the {@code Discoverable} annotation triggers discovery of a recursively defined type.
     */
    @Test
    void shouldCompileDiscoverableInterface() {

        final var source =
            """
                    import build.codemodel.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public interface Discover<T extends Discover<T>> {
                        Discover<T> discover();      
                    }                                    
                """;

        final var fileSystem = FileSystems.getDefault();

        final var classPath = Arrays.stream(System.getProperty("java.class.path").split(":"))
            .map(file -> fileSystem.getPath(file).toFile())
            .toList();

        final var annotationProcessor = new AnnotationProcessor();

        assertThat(annotationProcessor.getCodeModel().isEmpty())
            .isTrue();

        var compiler = Compiler.javac()
            .withClasspath(classPath)
            .withProcessors(annotationProcessor);

        final var modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null) {
            compiler = compiler
                .withOptions("--module-path=" + modulePath,
                    "--add-modules=build.codemodel.foundation,build.codemodel.jdk.annotation.discovery");
        }

        final var compilation = compiler
            .compile(JavaFileObjects.forSourceString("Discover", source));

        System.out.println("Module Path:" + modulePath);
        System.out.println("Class Path:" + classPath);

        compilation.errors().forEach(System.err::println);

        assertThat(compilation.status())
            .isEqualTo(Compilation.Status.SUCCESS);

        final var codeModel = annotationProcessor.getCodeModel()
            .orElseThrow();

        assertThat(codeModel.typeDescriptors().count())
            .isGreaterThan(0L);

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "Discover");

        final var typeDescriptor = codeModel.getTypeDescriptor(typeName)
            .orElseThrow();

        assertThat(typeDescriptor.typeName())
            .isEqualTo(typeName);

        assertThat(typeDescriptor.traits(MethodDescriptor.class))
            .isNotEmpty();

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .isEmpty();
    }
}
