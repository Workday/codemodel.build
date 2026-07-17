package build.codemodel.jdk.annotation.processor;

import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests to ensure discovery of recursively defined types using the {@link AnnotationProcessor}.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
public class RecursivelyDefinedTypeDiscoveryTests
    extends AnnotationProcessorTests {

    /**
     * Ensure the {@code Discoverable} annotation triggers discovery of a recursively defined type.
     */
    @Test
    void shouldCompileDiscoverableInterface() {

        final var source =
            """
                    import build.codemodel.jdk.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public interface Discover<T extends Discover<T>> {
                        Discover<T> discover();      
                    }                                    
                """;
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, "Discover", source);

        final var codeModel = annotationProcessor.getCodeModel()
            .orElseThrow();

        assertThat(codeModel.typeDescriptors().count())
            .isGreaterThan(0L);

        final var typeName = codeModel.getEmptyModuleTypeName("Discover");

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
