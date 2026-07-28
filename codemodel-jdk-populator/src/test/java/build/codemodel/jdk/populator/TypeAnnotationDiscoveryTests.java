package build.codemodel.jdk.populator;

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.ExplicitAnnotationParens;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for annotation preservation on TypeUsage nodes via {@link JdkInitializer}.
 * Each test corresponds to a bug where the old code dropped type-use annotations in a specific
 * visitor branch before the unified {@link build.codemodel.jdk.populator.TypeMirrorResolver} was introduced.
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

        final var typeName = codeModel.getEmptyModuleTypeName("Foo");
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

        final var typeName = codeModel.getEmptyModuleTypeName("Foo");
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

        final var typeName = codeModel.getEmptyModuleTypeName("Foo");
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

        final var typeName = codeModel.getEmptyModuleTypeName("Foo");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("dependency"))
            .findFirst().orElseThrow();

        assertThat(field.type()).isInstanceOf(UnknownTypeUsage.class);
    }

    // -------------------------------------------------------------------------
    // `@TestAnnotation` vs `@TestAnnotation()` both resolve to an AnnotationMirror
    // with an empty element-value map, so AnnotationTypeUsage.equals() treats them
    // as equal. ExplicitAnnotationParens is the trait that preserves the distinction.
    // -------------------------------------------------------------------------

    @Test
    void shouldCaptureParens() {
        final var source = JavaFileObjects.forSourceString("Foo", """
            import java.lang.annotation.*;
            import java.util.List;
            
            @Target(ElementType.METHOD)
            @interface TestAnnotation {}
            
            @Target(ElementType.METHOD)
            @interface RequiresValue {
                String value();
            }
            
            public class Foo {
                @TestAnnotation
                public static void empty() {
                }
            
                @TestAnnotation()
                public static void nonEmpty() {
                }
            
                @RequiresValue("hello")
                public static void withValue() {
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var emptyAnnotation = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("empty"))
            .findFirst().orElseThrow()
            .traits(AnnotationTypeUsage.class)
            .findFirst().orElseThrow();

        final var nonEmptyAnnotation = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("nonEmpty"))
            .findFirst().orElseThrow()
            .traits(AnnotationTypeUsage.class)
            .findFirst().orElseThrow();

        final var withValueAnnotation = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("withValue"))
            .findFirst().orElseThrow()
            .traits(AnnotationTypeUsage.class)
            .findFirst().orElseThrow();

        assertThat(emptyAnnotation.traits(ExplicitAnnotationParens.class)).isEmpty();
        assertThat(nonEmptyAnnotation.traits(ExplicitAnnotationParens.class))
            .containsExactly(ExplicitAnnotationParens.EXPLICIT_ANNOTATION_PARENS);

        // an annotation with a required value is necessarily explicit, and its argument is
        // captured as an AnnotationValue regardless of the ExplicitAnnotationParens trait
        assertThat(withValueAnnotation.traits(ExplicitAnnotationParens.class))
            .containsExactly(ExplicitAnnotationParens.EXPLICIT_ANNOTATION_PARENS);
        final var value = withValueAnnotation.values().findFirst().orElseThrow();
        assertThat(value.name().toString()).isEqualTo("value");
        assertThat(value.value()).isEqualTo(new AnnotationValue.Value.Literal("hello"));
    }

    // -------------------------------------------------------------------------
    // Multi-value annotations: javax.lang.model.element.AnnotationMirror.getElementValues()
    // guarantees "the order of the map matches the order in which the values appear in the
    // annotation's source" — deliberately written out of declaration order below to prove the
    // model preserves source order rather than annotation-interface declaration order.
    // -------------------------------------------------------------------------

    @Test
    void shouldPreserveMultiValueAnnotationSourceOrder() {
        final var source = JavaFileObjects.forSourceString("Foo", """
            import java.lang.annotation.*;
            
            @Target(ElementType.METHOD)
            @interface Multi {
                String first();
                String second();
                String third();
            }
            
            public class Foo {
                @Multi(third = "3", first = "1", second = "2")
                public static void multi() {
                }
            }
            """);
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("Foo");
        final var descriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        final var annotation = descriptor.traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("multi"))
            .findFirst().orElseThrow()
            .traits(AnnotationTypeUsage.class)
            .findFirst().orElseThrow();

        assertThat(annotation.values().map(v -> v.name().toString()).toList())
            .containsExactly("third", "first", "second");
    }
}
