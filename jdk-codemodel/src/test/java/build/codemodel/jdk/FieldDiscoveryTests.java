package build.codemodel.jdk;

import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.testing.compile.JavaFileObjects;

/**
 * Tests for discovering fields via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class FieldDiscoveryTests {

    @Test
    void shouldCreateFieldDescriptors() {
        final var source = JavaFileObjects.forSourceString("Discover", """
            public class Discover {
                public String publicString;
                private String privateString;
                protected String protectedString;
                String packageString;
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var naming = codeModel.getNameProvider();
        final var typeName = naming.getTypeName(Optional.empty(), "Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.traits(MethodDescriptor.class)).isEmpty();
        assertThat(typeDescriptor.traits(FieldDescriptor.class)).hasSize(4);

        // All fields are String
        final var stringTypeName = naming.getTypeName(Optional.empty(), "java.lang.String");
        assertThat(typeDescriptor.traits(FieldDescriptor.class)
            .map(FieldDescriptor::type)
            .filter(NamedTypeUsage.class::isInstance)
            .map(NamedTypeUsage.class::cast)
            .map(NamedTypeUsage::typeName))
            .allMatch(stringTypeName::equals);

        // Field names
        assertThat(typeDescriptor.traits(FieldDescriptor.class)
            .map(f -> f.fieldName().toString()).toList())
            .contains("publicString", "privateString", "protectedString", "packageString");

        // Access modifiers
        assertThat(typeDescriptor.traits(FieldDescriptor.class)
            .map(f -> f.getTrait(AccessModifier.class)).toList())
            .contains(
                Optional.of(AccessModifier.PUBLIC),
                Optional.of(AccessModifier.PRIVATE),
                Optional.of(AccessModifier.PROTECTED),
                Optional.empty());
    }
}
