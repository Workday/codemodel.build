package build.codemodel.annotation.processing;

import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for annotation preservation on TypeUsage nodes via the {@link AnnotationProcessor}.
 * Each test corresponds to a bug where the old code dropped type-use annotations in a specific visitor
 * branch, or caused an {@link IllegalStateException} when an unresolvable type was encountered.
 */
class TypeAnnotationDiscoveryTests
    extends AnnotationProcessorTests {

    // -------------------------------------------------------------------------
    // #71 / #76 — TypeVariable usage dropped annotations
    // -------------------------------------------------------------------------

    @Test
    void shouldPreserveAnnotationOnTypeVariableUsage() {
        final var source = """
            import java.lang.annotation.*;
            import build.codemodel.annotation.discovery.Discoverable;
            
            @Target(ElementType.TYPE_USE)
            @interface NonNull {}
            
            @Discoverable
            public class Foo<T> {
                public @NonNull T value;
            }
            """;
        final var processor = new AnnotationProcessor();
        compile(processor, "Foo", source);

        final var codeModel = processor.getCodeModel().orElseThrow();
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
        final var source = """
            import java.lang.annotation.*;
            import java.util.List;
            import build.codemodel.annotation.discovery.Discoverable;
            
            @Target(ElementType.TYPE_USE)
            @interface NonNull {}
            
            @Discoverable
            public class Foo {
                public List<@NonNull String> items;
            }
            """;
        final var processor = new AnnotationProcessor();
        compile(processor, "Foo", source);

        final var codeModel = processor.getCodeModel().orElseThrow();
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
        final var source = """
            import java.lang.annotation.*;
            import java.util.List;
            import build.codemodel.annotation.discovery.Discoverable;
            
            @Target(ElementType.TYPE_USE)
            @interface NonNull {}
            
            @Discoverable
            public class Foo {
                public List<@NonNull ? extends Number> items;
            }
            """;
        final var processor = new AnnotationProcessor();
        compile(processor, "Foo", source);

        final var codeModel = processor.getCodeModel().orElseThrow();
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
    // #77 — AP visitError recorded a diagnostic but never set the lazy TypeUsage,
    //        causing ISE when the resolved map was accessed afterward
    // -------------------------------------------------------------------------

    @Test
    void shouldNotThrowWhenProcessingUnresolvableFieldType() {
        final var source = """
            import build.codemodel.annotation.discovery.Discoverable;
            
            @Discoverable
            public class Foo {
                public com.example.Missing dependency;
            }
            """;
        final var processor = new AnnotationProcessor();
        // Compilation fails (cannot find symbol), but the AP must not throw ISE
        run(processor, "Foo", source);

        assertThat(processor.getCodeModel()).isPresent();
        final var codeModel = processor.getCodeModel().orElseThrow();
        final var typeName = codeModel.getNameProvider().getTypeName(Optional.empty(), "Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dependency"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(UnknownTypeUsage.class);
    }
}
