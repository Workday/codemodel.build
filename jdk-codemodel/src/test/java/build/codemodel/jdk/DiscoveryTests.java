package build.codemodel.jdk;

import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for discovering types via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class DiscoveryTests {

    @Test
    void shouldDiscoverSimpleInterface() {
        final var source = JavaFileObjects.forSourceString("Discover", """
            public interface Discover {
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        assertThat(codeModel.typeDescriptors().count()).isGreaterThan(0L);

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor).isInstanceOf(JDKTypeDescriptor.class);
        assertThat(typeDescriptor.typeName()).isEqualTo(typeName);
        assertThat(typeDescriptor.traits(MethodDescriptor.class)).isEmpty();
        assertThat(typeDescriptor.traits(FieldDescriptor.class)).isEmpty();
        assertThat(typeDescriptor.traits(ExtendsTypeDescriptor.class)).isEmpty();
        assertThat(typeDescriptor.getTrait(Classification.class)).contains(Classification.ABSTRACT);
    }

    @Test
    void shouldAddExtendsDescriptorForExplicitSuperclass() {
        // Two non-public classes in one compilation unit
        final var source = JavaFileObjects.forSourceString("Child", """
            class Base { }
            class Child extends Base { }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var naming = codeModel.getNameProvider();
        final var childName = naming.getTypeName(Optional.empty(), "Child");
        final var childDescriptor = codeModel.getTypeDescriptor(childName).orElseThrow();
        final var baseName = naming.getTypeName(Optional.empty(), "Base");

        assertThat(childDescriptor.traits(ExtendsTypeDescriptor.class)).hasSize(1);
        assertThat(childDescriptor.traits(ExtendsTypeDescriptor.class)
            .map(ExtendsTypeDescriptor::parentTypeUsage)
            .map(t -> t.typeName()))
            .containsExactly(baseName);

        // Base itself has no explicit superclass — no ExtendsTypeDescriptor
        final var baseDescriptor = codeModel.getTypeDescriptor(baseName).orElseThrow();
        assertThat(baseDescriptor.traits(ExtendsTypeDescriptor.class)).isEmpty();
    }

    @Test
    void shouldNotAddExtendsDescriptorForImplicitObjectSuperclass() {
        final var source = JavaFileObjects.forSourceString("Discover", """
            public class Discover {
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        // java.lang.Object is the implicit superclass — no ExtendsTypeDescriptor should be emitted
        assertThat(typeDescriptor.traits(ExtendsTypeDescriptor.class)).isEmpty();
    }
}
