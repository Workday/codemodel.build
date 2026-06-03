package build.codemodel.annotation.processing;

import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests to ensure discovery of types using the {@link AnnotationProcessor}.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
public class DiscoveryTests
    extends AnnotationProcessorTests {

    /**
     * Ensure the {@code Discoverable} annotation triggers discovery of a type.
     */
    @Test
    void shouldCompileDiscoverableInterface() {

        final var source =
            """
                    import build.codemodel.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public interface Discover {
                
                    }                                    
                """;
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, "Discover", source);

        final var codeModel = annotationProcessor.getCodeModel()
            .orElseThrow();

        assertThat(codeModel.typeDescriptors().count())
            .isGreaterThan(0L);

        final var typeName = codeModel.getNameProvider()
            .getTypeName(Optional.empty(), "Discover");

        final var typeDescriptor = codeModel.getTypeDescriptor(typeName)
            .orElseThrow();

        assertThat(typeDescriptor)
            .isInstanceOf(JDKTypeDescriptor.class);

        assertThat(typeDescriptor.typeName())
            .isEqualTo(typeName);

        assertThat(typeDescriptor.traits(MethodDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(ExtendsTypeDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.getTrait(Classification.class))
            .contains(Classification.ABSTRACT);
    }
}
