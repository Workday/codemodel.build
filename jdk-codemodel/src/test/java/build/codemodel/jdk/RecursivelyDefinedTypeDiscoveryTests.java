package build.codemodel.jdk;

import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for handling recursively defined types via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class RecursivelyDefinedTypeDiscoveryTests {

    @Test
    void shouldHandleRecursiveGenericType() {
        final var source = JavaFileObjects.forSourceString("Discover", """
            public interface Discover<T extends Discover<T>> {
                Discover<T> discover();
            }
            """);
        final var initializer = new JdkInitializer(List.of(), List.of(), List.of(source));
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Discover");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.typeName()).isEqualTo(typeName);
        assertThat(typeDescriptor.traits(MethodDescriptor.class)).isNotEmpty();
        assertThat(typeDescriptor.traits(FieldDescriptor.class)).isEmpty();
    }
}
