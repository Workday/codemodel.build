package build.codemodel.jdk.populator;

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        final var typeName = naming.getEmptyModuleTypeName("Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.traits(MethodDescriptor.class)).isEmpty();
        assertThat(typeDescriptor.traits(FieldDescriptor.class)).hasSize(4);

        // All fields are String — type names are resolved with their JPMS module
        final var javaBase = naming.getModuleName("java.base");
        final var stringTypeName = naming.getTypeName(javaBase, "java.lang.String");
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

    @Test
    void shouldAttachClassificationToFields() {
        final var source = JavaFileObjects.forSourceString("Discover", """
            public class Discover {
                static final int MAX = 10;
                int count;
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var naming = codeModel.getNameProvider();
        final var typeName = naming.getEmptyModuleTypeName("Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var max = typeDescriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("MAX"))
            .findFirst().orElseThrow();
        assertThat(max.getTrait(Classification.class)).as("MAX is final").contains(Classification.FINAL);
        assertThat(max.hasTrait(Static.class)).as("MAX is static").isTrue();

        final var count = typeDescriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("count"))
            .findFirst().orElseThrow();
        assertThat(count.getTrait(Classification.class)).as("count is not final").contains(Classification.CONCRETE);
        assertThat(count.hasTrait(Static.class)).as("count is not static").isFalse();
    }
}
