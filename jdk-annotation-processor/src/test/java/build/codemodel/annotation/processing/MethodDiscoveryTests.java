package build.codemodel.annotation.processing;

import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.MethodImplementationDescriptor;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.naming.MethodName;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests to ensure discovery of <i>constructor</i> types using the {@link AnnotationProcessor}.
 *
 * @author brian.oliver
 * @since May-2024
 */
public class MethodDiscoveryTests {

    /**
     * Ensure the {@link MethodDescriptor}s are created for <i>methods</i>.
     */
    @Test
    void shouldCreateMethodDescriptors() {

        final var source =
            """
                    import build.codemodel.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public interface Discover {
                        void findableVoidMethod();
                
                        default void findableDefaultMethod() {
                        }           
                
                        int methodWithParameters(String name, double size);
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

        assertThat(compilation.status())
            .isEqualTo(Compilation.Status.SUCCESS);

        final var codeModel = annotationProcessor.getCodeModel()
            .orElseThrow();

        assertThat(codeModel.typeDescriptors().count())
            .isGreaterThan(0);

        final var naming = codeModel.getNameProvider();

        final var typeName = naming
            .getTypeName(Optional.empty(), "Discover");

        final var typeDescriptor = codeModel.getTypeDescriptor(typeName)
            .orElseThrow();

        assertThat(typeDescriptor.typeName())
            .isEqualTo(typeName);

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(MethodDescriptor.class))
            .hasSize(3);

        // ensure all return types are void
        final var voidTypeUsage = naming.getTypeName(void.class);
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(MethodDescriptor::returnType)
            .filter(NamedTypeUsage.class::isInstance)
            .map(NamedTypeUsage.class::cast)
            .map(NamedTypeUsage::typeName))
            .filteredOn(voidTypeUsage::equals)
            .hasSize(2);

        // ensure the names are as expected
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(MethodDescriptor::methodName)
            .map(MethodName::name)
            .map(IrreducibleName::toString))
            .contains("findableVoidMethod", "findableDefaultMethod", "methodWithParameters");

        // ensure the Classifications are as expected
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(methodDescriptor -> methodDescriptor.getTrait(Classification.class).orElseThrow()))
            .contains(Classification.ABSTRACT, Classification.CONCRETE, Classification.ABSTRACT);

        // ensure the default methods have an implementation
        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .filter(methodDescriptor -> methodDescriptor.traits(MethodImplementationDescriptor.class)
                .findFirst()
                .isPresent()))
            .hasSize(1);
    }
}
