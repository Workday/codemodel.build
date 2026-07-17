package build.codemodel.jdk;

import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JdkInitializer#ofDirectory(Path)}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class DirectoryDiscoveryTests {

    @Test
    void shouldDiscoverTypesFromDirectory() {
        final var directory = Path.of(System.getProperty("user.dir"), "src/test/java/build/codemodel/jdk/example");
        final var initializer = JdkInitializer.ofDirectory(directory);
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var naming = codeModel.getNameProvider();

        // AbstractPerson lives in the example directory — it should be discovered
        final var abstractPersonName = naming.getTypeName(
            "build.codemodel.jdk.example.AbstractPerson");

        assertThat(codeModel.getTypeDescriptor(abstractPersonName))
            .isPresent();

        final var descriptor = codeModel.getTypeDescriptor(abstractPersonName).orElseThrow();

        assertThat(descriptor.traits(FieldDescriptor.class).count())
            .isGreaterThan(0);

        assertThat(descriptor.traits(MethodDescriptor.class).count())
            .isGreaterThan(0);
    }

    @Test
    void shouldDiscoverMultipleTypesFromDirectory() {
        final var directory = Path.of(System.getProperty("user.dir"), "src/test/java/build/codemodel/jdk/example");
        final var initializer = JdkInitializer.ofDirectory(directory);
        final var codeModel = JdkInitializerTests.runInternal(initializer);

        final var naming = codeModel.getNameProvider();

        // Both AbstractPerson and NonAbstractPerson should be discovered from the same directory
        assertThat(codeModel.getTypeDescriptor(
            naming.getTypeName("build.codemodel.jdk.example.AbstractPerson"))).isPresent();
        assertThat(codeModel.getTypeDescriptor(
            naming.getTypeName("build.codemodel.jdk.example.NonAbstractPerson"))).isPresent();
        assertThat(codeModel.getTypeDescriptor(
            naming.getTypeName("build.codemodel.jdk.example.Description"))).isPresent();
    }
}
