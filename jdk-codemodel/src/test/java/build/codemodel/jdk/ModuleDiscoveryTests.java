package build.codemodel.jdk;

import build.codemodel.foundation.descriptor.RequiresModuleDescriptor;
import build.codemodel.jdk.descriptor.RequiresModifier;
import build.codemodel.jdk.descriptor.ExportsDescriptor;
import build.codemodel.jdk.descriptor.OpenModule;
import build.codemodel.jdk.descriptor.OpensDescriptor;
import build.codemodel.jdk.descriptor.ProvidesDescriptor;
import build.codemodel.jdk.descriptor.UsesDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@code module-info.java} discovery via {@link JdkInitializer}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class ModuleDiscoveryTests {

    @Test
    void shouldDiscoverModuleWithRequires() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires java.base;
                requires java.logging;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        assertThat(descriptor.traits(RequiresModuleDescriptor.class)
            .map(r -> r.requiresModuleName().toString())
            .toList())
            .contains("java.base", "java.logging");
    }

    @Test
    void shouldCaptureRequiresTransitiveAndStaticModifiers() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                requires transitive java.logging;
                requires static java.compiler;
                requires java.base;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        final var transitiveReq = descriptor.traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.logging"))
            .findFirst().orElseThrow();
        assertThat(transitiveReq.hasTrait(RequiresModifier.class))
            .as("requires transitive should carry TRANSITIVE modifier").isTrue();
        assertThat(transitiveReq.traits(RequiresModifier.class).toList())
            .contains(RequiresModifier.TRANSITIVE);

        final var staticReq = descriptor.traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.compiler"))
            .findFirst().orElseThrow();
        assertThat(staticReq.traits(RequiresModifier.class).toList())
            .contains(RequiresModifier.STATIC);

        final var plainReq = descriptor.traits(RequiresModuleDescriptor.class)
            .filter(r -> r.requiresModuleName().toString().equals("java.base"))
            .findFirst().orElseThrow();
        assertThat(plainReq.hasTrait(RequiresModifier.class))
            .as("plain requires should carry no modifier").isFalse();
    }

    @Test
    void shouldDiscoverUnqualifiedExports() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                exports com.example.api;
                exports com.example.spi;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        assertThat(descriptor.traits(ExportsDescriptor.class)
            .map(e -> e.packageName().toString())
            .toList())
            .containsExactlyInAnyOrder("com.example.api", "com.example.spi");

        descriptor.traits(ExportsDescriptor.class)
            .forEach(e -> assertThat(e.targetModuleNames())
                .as("unqualified export should have no target modules")
                .isEmpty());
    }

    @Test
    void shouldDiscoverQualifiedExports() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                exports com.example.internal to com.example.consumer, com.example.tests;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        final var export = descriptor.traits(ExportsDescriptor.class).findFirst().orElseThrow();
        assertThat(export.packageName().toString()).isEqualTo("com.example.internal");
        assertThat(export.targetModuleNames().stream().map(Object::toString).toList())
            .containsExactlyInAnyOrder("com.example.consumer", "com.example.tests");
    }

    @Test
    void shouldDiscoverOpens() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                opens com.example.impl to com.example.framework;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        final var opens = descriptor.traits(OpensDescriptor.class).findFirst().orElseThrow();
        assertThat(opens.packageName().toString()).isEqualTo("com.example.impl");
        assertThat(opens.targetModuleNames().stream().map(Object::toString).toList())
            .containsExactly("com.example.framework");
    }

    @Test
    void shouldDiscoverUnqualifiedOpens() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                opens com.example.impl;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        final var opens = descriptor.traits(OpensDescriptor.class).findFirst().orElseThrow();
        assertThat(opens.packageName().toString()).isEqualTo("com.example.impl");
        assertThat(opens.targetModuleNames())
            .as("unqualified opens should have no target modules")
            .isEmpty();
    }

    @Test
    void shouldDiscoverUsesAndProvides() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            module com.example {
                uses com.example.spi.MyService;
                provides com.example.spi.MyService with com.example.impl.MyServiceImpl;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        final var uses = descriptor.traits(UsesDescriptor.class).findFirst().orElseThrow();
        assertThat(uses.serviceType().toString()).contains("MyService");

        final var provides = descriptor.traits(ProvidesDescriptor.class).findFirst().orElseThrow();
        assertThat(provides.serviceType().toString()).contains("MyService");
        assertThat(provides.implementationTypes().stream().map(Object::toString).toList())
            .anySatisfy(n -> assertThat(n).contains("MyServiceImpl"));
    }

    @Test
    void shouldCaptureOpenModuleModifier() {
        final var source = JavaFileObjects.forSourceString("module-info", """
            open module com.example {
                requires java.base;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var moduleName = codeModel.getNameProvider().getModuleName("com.example").orElseThrow();
        final var descriptor = codeModel.getModuleDescriptor(moduleName).orElseThrow();

        assertThat(descriptor.hasTrait(OpenModule.class)).isTrue();
    }
}
