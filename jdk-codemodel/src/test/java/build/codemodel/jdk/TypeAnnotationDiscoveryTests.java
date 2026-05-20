package build.codemodel.jdk;

import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for annotation preservation on TypeUsage nodes via {@link JdkInitializer}.
 * Each test corresponds to a bug where the old code dropped type-use annotations in a specific
 * visitor branch before the unified {@link build.codemodel.jdk.TypeMirrorResolver} was introduced.
 */
class TypeAnnotationDiscoveryTests {

    // -------------------------------------------------------------------------
    // #71 / #76 — TypeVariable usage dropped annotations
    // -------------------------------------------------------------------------

    @Test
    void shouldPreserveAnnotationOnTypeVariableUsage() {
        final var source = JavaFileObjects.forSourceString("Foo", """
            import java.lang.annotation.*;
            
            @Target(ElementType.TYPE_USE)
            @interface NonNull {}
            
            public class Foo<T> {
                public @NonNull T value;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("value"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(TypeVariableUsage.class);
        assertThat(field.type().traits(AnnotationTypeUsage.class)
            .map(a -> a.typeName().name().toString())
            .toList())
            .contains("NonNull");
    }

    // -------------------------------------------------------------------------
    // #69 — Generic declared TypeUsage (type argument) dropped annotations
    // -------------------------------------------------------------------------

    @Test
    void shouldPreserveAnnotationOnGenericTypeArgument() {
        final var source = JavaFileObjects.forSourceString("Foo", """
            import java.lang.annotation.*;
            import java.util.List;
            
            @Target(ElementType.TYPE_USE)
            @interface NonNull {}
            
            public class Foo {
                public List<@NonNull String> items;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("items"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(GenericTypeUsage.class);
        final var arg = ((GenericTypeUsage) field.type()).parameters().findFirst().orElseThrow();
        assertThat(arg.traits(AnnotationTypeUsage.class)
            .map(a -> a.typeName().name().toString())
            .toList())
            .contains("NonNull");
    }

    // -------------------------------------------------------------------------
    // #70 — Wildcard usage dropped annotations
    // -------------------------------------------------------------------------

    @Test
    void shouldPreserveAnnotationOnWildcardUsage() {
        final var source = JavaFileObjects.forSourceString("Foo", """
            import java.lang.annotation.*;
            import java.util.List;
            
            @Target(ElementType.TYPE_USE)
            @interface NonNull {}
            
            public class Foo {
                public List<@NonNull ? extends Number> items;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("items"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(GenericTypeUsage.class);
        final var wildcard = ((GenericTypeUsage) field.type()).parameters().findFirst().orElseThrow();
        assertThat(wildcard).isInstanceOf(WildcardTypeUsage.class);
        assertThat(wildcard.traits(AnnotationTypeUsage.class)
            .map(a -> a.typeName().name().toString())
            .toList())
            .contains("NonNull");
    }

    // -------------------------------------------------------------------------
    // #73 — JdkInitializer ErrorType silently became UnknownTypeUsage
    //        (no ISE; graceful degradation is the correct behavior here)
    // -------------------------------------------------------------------------

    @Test
    void shouldDegradeUnresolvableTypeToUnknownTypeUsage() {
        final var source = JavaFileObjects.forSourceString("Foo", """
            public class Foo {
                public com.example.Missing dependency;
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dependency"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(UnknownTypeUsage.class);
    }
}
