package build.codemodel.jdk.populator;

/*-
 * #%L
 * JDK Code Model
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for {@code @Repeatable} annotation unwrapping: per JLS/javac semantics, a
 * {@code @Repeatable} annotation applied 2+ times is reported by {@code getAnnotationMirrors()} as
 * a single compiler-synthesized container mirror (e.g. {@code @Foos({@Foo(1), @Foo(2)})}), not the
 * individual repetitions. {@link TypeMirrorResolver} must unwrap that container back into one
 * {@link AnnotationTypeUsage} per repetition to match what was actually written.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class RepeatableAnnotationTests {

    private static final String REPEATABLE_DECLARATIONS = """
        @Retention(RetentionPolicy.RUNTIME)
        @Repeatable(Foos.class)
        @interface Foo {
            int value();
        }
        
        @Retention(RetentionPolicy.RUNTIME)
        @interface Foos {
            Foo[] value();
        }
        """;

    @Test
    void shouldUnwrapRepeatedAnnotationOnField() {
        final var source = JavaFileObjects.forSourceString("build.codemodel.jdk.example.Foo", """
            package build.codemodel.jdk.example;
            import java.lang.annotation.*;
            
            %s
            
            public class Holder {
                @Foo(1) @Foo(2)
                private String field;
            }
            """.formatted(REPEATABLE_DECLARATIONS));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.Holder");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("field"))
            .findFirst().orElseThrow();

        final var annotations = field.traits(AnnotationTypeUsage.class).toList();
        assertThat(annotations).extracting(a -> a.typeName().name().toString())
            .containsExactly("Foo", "Foo");
        assertThat(annotations).extracting(RepeatableAnnotationTests::firstValueLiteral)
            .containsExactly(1, 2);
    }

    @Test
    void shouldUnwrapRepeatedAnnotationOnMethod() {
        final var source = JavaFileObjects.forSourceString("build.codemodel.jdk.example.Foo", """
            package build.codemodel.jdk.example;
            import java.lang.annotation.*;
            
            %s
            
            public class Holder {
                @Foo(1) @Foo(2)
                public void run() {
                }
            }
            """.formatted(REPEATABLE_DECLARATIONS));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.Holder");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();

        final var annotations = method.traits(AnnotationTypeUsage.class).toList();
        assertThat(annotations).extracting(a -> a.typeName().name().toString())
            .containsExactly("Foo", "Foo");
        assertThat(annotations).extracting(RepeatableAnnotationTests::firstValueLiteral)
            .containsExactly(1, 2);
    }

    @Test
    void shouldUnwrapRepeatedAnnotationOnFormalParameter() {
        final var source = JavaFileObjects.forSourceString("build.codemodel.jdk.example.Foo", """
            package build.codemodel.jdk.example;
            import java.lang.annotation.*;
            
            %s
            
            public class Holder {
                public void run(@Foo(1) @Foo(2) String s) {
                }
            }
            """.formatted(REPEATABLE_DECLARATIONS));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.Holder");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var parameter = method.formalParameters().findFirst().orElseThrow();

        final var annotations = parameter.traits(AnnotationTypeUsage.class).toList();
        assertThat(annotations).extracting(a -> a.typeName().name().toString())
            .containsExactly("Foo", "Foo");
        assertThat(annotations).extracting(RepeatableAnnotationTests::firstValueLiteral)
            .containsExactly(1, 2);
    }

    // javac itself never resolves a position for the synthesized container annotation on a
    // repeated type-parameter declaration, so there is no data available at any API level to
    // attribute it to the right type parameter (confirmed against javac 25.0.3 source).
    // com.sun.tools.javac.comp.Annotate#processRepeatedAnnotations
    @Disabled("javac bug: repeated type-parameter annotations get an unresolved TypeAnnotationPosition")
    @Test
    void shouldUnwrapRepeatedAnnotationOnTypeParameter() {
        final var source = JavaFileObjects.forSourceString("build.codemodel.jdk.example.Holder", """
            package build.codemodel.jdk.example;
            import java.lang.annotation.*;
            
            %s
            
            public class Holder<@Foo(1) @Foo(2) T> {
            }
            """.formatted("""
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE_PARAMETER)
            @Repeatable(Foos.class)
            @interface Foo {
                int value();
            }
            
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE_PARAMETER)
            @interface Foos {
                Foo[] value();
            }
            """));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.Holder");
        final var typeParameter = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .getTrait(ParameterizedTypeDescriptor.class).orElseThrow()
            .typeVariables().findFirst().orElseThrow();

        final var annotations = typeParameter.traits(AnnotationTypeUsage.class).toList();
        assertThat(annotations).extracting(a -> a.typeName().name().toString())
            .containsExactly("Foo", "Foo");
        assertThat(annotations).extracting(RepeatableAnnotationTests::firstValueLiteral)
            .containsExactly(1, 2);
    }

    @Test
    void shouldNotUnwrapSingleNonRepeatedAnnotation() {
        final var source = JavaFileObjects.forSourceString("build.codemodel.jdk.example.Foo", """
            package build.codemodel.jdk.example;
            import java.lang.annotation.*;
            
            %s
            
            public class Holder {
                @Foo(1)
                private String field;
            }
            """.formatted(REPEATABLE_DECLARATIONS));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.Holder");
        final var field = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("field"))
            .findFirst().orElseThrow();

        final var annotations = field.traits(AnnotationTypeUsage.class).toList();
        assertThat(annotations).extracting(a -> a.typeName().name().toString())
            .containsExactly("Foo");
        assertThat(annotations).extracting(RepeatableAnnotationTests::firstValueLiteral)
            .containsExactly(1);
    }

    @Test
    void shouldUnwrapRepeatedAnnotationOnLocalVariable() {
        final var source = JavaFileObjects.forSourceString("build.codemodel.jdk.example.Foo", """
            package build.codemodel.jdk.example;
            import java.lang.annotation.*;
            
            %s
            
            public class Holder {
                public void run() {
                    @Foo(1) @Foo(2) String s = "x";
                }
            }
            """.formatted(REPEATABLE_DECLARATIONS));
        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(source)));

        final var typeName = codeModel.getEmptyModuleTypeName("build.codemodel.jdk.example.Holder");
        final var method = codeModel.getTypeDescriptor(typeName).orElseThrow()
            .traits(MethodDescriptor.class)
            .filter(m -> m.methodName().name().toString().equals("run"))
            .findFirst().orElseThrow();
        final var body = method.getTrait(MethodBodyDescriptor.class).orElseThrow().body();
        final var decl = (LocalVariableDeclaration) body.statements().findFirst().orElseThrow();

        final var annotations = decl.traits(AnnotationTypeUsage.class).toList();
        assertThat(annotations).extracting(a -> a.typeName().name().toString())
            .containsExactly("Foo", "Foo");
        assertThat(annotations).extracting(RepeatableAnnotationTests::firstValueLiteral)
            .containsExactly(1, 2);
    }

    private static Object firstValueLiteral(final AnnotationTypeUsage usage) {
        final var value = usage.values().findFirst().orElseThrow().value();
        return value instanceof AnnotationValue.Value.Literal literal ? literal.value() : value;
    }
}
