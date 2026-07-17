package build.codemodel.jdk.annotation.processor;

import build.codemodel.foundation.descriptor.ThrowableDescriptor;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to ensure {@link AnnotationProcessor} discovers constructors.
 *
 * @author reed.vonredwitz
 */
public class ConstructorDiscoveryTests extends AnnotationProcessorTests {

    @Test
    void shouldDiscoverConstructorDescriptors() {
        final var source = """
            import build.codemodel.jdk.annotation.discovery.Discoverable;
            
            @Discoverable
            public class Discover {
                public Discover() {}
                private Discover(String name) {}
            }
            """;

        final var processor = new AnnotationProcessor();
        compile(processor, "Discover", source);

        final var codeModel = processor.getCodeModel().orElseThrow();
        final var typeName = codeModel.getEmptyModuleTypeName("Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.traits(ConstructorDescriptor.class)).hasSize(2);

        final var publicCtor = typeDescriptor.traits(ConstructorDescriptor.class)
            .filter(c -> c.getTrait(AccessModifier.class).filter(AccessModifier.PUBLIC::equals).isPresent())
            .findFirst().orElseThrow();
        assertThat(publicCtor.formalParameters()).isEmpty();

        final var privateCtor = typeDescriptor.traits(ConstructorDescriptor.class)
            .filter(c -> c.getTrait(AccessModifier.class).filter(AccessModifier.PRIVATE::equals).isPresent())
            .findFirst().orElseThrow();
        assertThat(privateCtor.formalParameters()).hasSize(1);
    }

    @Test
    void shouldDiscoverConstructorThrowsAndTypeParameters() {
        final var source = """
            import build.codemodel.jdk.annotation.discovery.Discoverable;
            import java.io.IOException;
            
            @Discoverable
            public class Discover {
                public <T> Discover(T value) throws IOException {}
            }
            """;

        final var processor = new AnnotationProcessor();
        compile(processor, "Discover", source);

        final var codeModel = processor.getCodeModel().orElseThrow();
        final var typeName = codeModel.getEmptyModuleTypeName("Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var ctor = typeDescriptor.getTrait(ConstructorDescriptor.class).orElseThrow();

        assertThat(ctor.traits(ThrowableDescriptor.class)).hasSize(1);
        assertThat(((NamedTypeUsage) ctor.traits(ThrowableDescriptor.class)
            .findFirst().orElseThrow().throwable()).typeName().canonicalName())
            .isEqualTo("java.io.IOException");

        assertThat(ctor.getTrait(ParameterizedTypeDescriptor.class))
            .as("generic constructor carries ParameterizedTypeDescriptor").isPresent();
        assertThat(ctor.getTrait(ParameterizedTypeDescriptor.class).orElseThrow()
            .typeVariables().findFirst().orElseThrow().typeName().name().toString())
            .isEqualTo("T");
    }

    @Test
    void shouldQueueConstructorParameterTypesForTransitiveDiscovery() {
        final var source = """
            import build.codemodel.jdk.annotation.discovery.Discoverable;
            
            @Discoverable
            public class Discover {
                public Discover(Helper helper) {}
            }
            
            class Helper {}
            """;

        final var processor = new AnnotationProcessor();
        compile(processor, "Discover", source);

        final var codeModel = processor.getCodeModel().orElseThrow();
        final var naming = codeModel.getNameProvider();

        // Helper was referenced only by the constructor parameter, so discovery must have queued it
        assertThat(codeModel.getTypeDescriptor(naming.getEmptyModuleTypeName("Helper")))
            .as("Helper was transitively discovered via constructor parameter").isPresent();
    }
}
