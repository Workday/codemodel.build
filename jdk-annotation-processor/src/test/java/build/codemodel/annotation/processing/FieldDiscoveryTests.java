package build.codemodel.annotation.processing;

import build.base.mereology.Strategy;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.objectoriented.descriptor.AccessModifier;
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
 * Unit tests to ensure discovery of <i>field</i> types using the {@link AnnotationProcessor}.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
public class FieldDiscoveryTests {

    /**
     * Ensure the {@link FieldDescriptor}s are created for <i>fields</i>.
     */
    @Test
    void shouldCreateFieldDescriptors() {

        final var source =
            """
                    import build.codemodel.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public class Discover {
                        public String publicString;
                        private String privateString;
                        protected String protectedString;
                        final String finalString = "Final";          
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

        assertThat(typeDescriptor.traits(MethodDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .hasSize(4);

        // ensure all types are Strings
        final var stringTypeName = naming.getTypeName(String.class);
        assertThat(typeDescriptor.traits(FieldDescriptor.class)
            .map(FieldDescriptor::type)
            .filter(NamedTypeUsage.class::isInstance)
            .map(NamedTypeUsage.class::cast)
            .map(NamedTypeUsage::typeName))
            .allMatch(stringTypeName::equals);

        // ensure the names are as expected
        assertThat(typeDescriptor.traits(FieldDescriptor.class)
            .map(FieldDescriptor::fieldName)
            .map(IrreducibleName::toString))
            .contains("publicString", "privateString", "protectedString", "finalString");

        // ensure the AccessModifiers are as expected
        assertThat(typeDescriptor.traits(FieldDescriptor.class)
            .map(fieldDescriptor -> fieldDescriptor.getTrait(AccessModifier.class)))
            .contains(
                Optional.of(AccessModifier.PUBLIC),
                Optional.of(AccessModifier.PRIVATE),
                Optional.of(AccessModifier.PROTECTED),
                Optional.empty());

        assertThat(typeDescriptor.traverse(FieldDescriptor.class)
            .strategy(Strategy.Direct)
            .stream())
            .hasSize(4);
    }
}
