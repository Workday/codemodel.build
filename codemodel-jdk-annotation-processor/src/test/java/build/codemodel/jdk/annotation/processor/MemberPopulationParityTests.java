package build.codemodel.jdk.annotation.processor;

/*-
 * #%L
 * JDK Annotation Processor
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

import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.annotation.processor.fixture.ClassificationFixture;
import build.codemodel.jdk.descriptor.Final;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.jdk.populator.JdkInitializer;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the three disjoint member-population paths — reflection ({@link JDKCodeModel}),
 * source parsing ({@link JdkInitializer}), and annotation processing ({@link AnnotationProcessor})
 * — attach the same {@link Classification}, {@link Static}, and {@link AccessModifier} traits to
 * the same fields, constructors, and methods of a single shared fixture.
 *
 * <p>These paths share almost no code (see {@code docs/TODO.md}, "three disjoint population
 * paths"), so a trait added to one is easy to forget in another. This test would have caught the
 * previous gap where field {@link Classification} was populated by {@link JDKCodeModel} but not
 * by {@code TypeMirrorResolver} — and therefore missing from both {@link JdkInitializer} and
 * {@link AnnotationProcessor}, which delegate to it.
 *
 * @see ClassificationFixture
 */
class MemberPopulationParityTests extends AnnotationProcessorTests {

    private static final String FIXTURE_TYPE_NAME =
        "build.codemodel.jdk.annotation.processor.fixture.ClassificationFixture";
    private static final Path FIXTURE_SOURCE_PATH = Path.of(
        "src/test/java/build/codemodel/jdk/annotation/processor/fixture/ClassificationFixture.java");

    @Test
    void shouldAgreeOnFieldConstructorAndMethodTraitsAcrossAllThreePopulationPaths() throws IOException {
        final var reflectionDescriptor = populateViaReflection();
        final var sourceDescriptor = populateViaJdkInitializer();
        final var processorDescriptor = populateViaAnnotationProcessor();

        assertFieldTraitsMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertFieldTraitsMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");

        assertMethodTraitsMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertMethodTraitsMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");

        assertConstructorTraitsMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertConstructorTraitsMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");
    }

    /**
     * {@code ClassificationFixture<E extends ClassificationFixture<E>>} — the type variable
     * {@code E}'s own upper bound ({@code ClassificationFixture<E>}) contains {@code E} again.
     * Reflection ({@link JDKCodeModel#getTypeUsage}) and {@code TypeMirrorResolver} (shared by
     * {@link JdkInitializer} and {@link AnnotationProcessor}) both break that cycle by registering
     * a skeleton {@link TypeVariableUsage} keyed by the reflective/mirror identity of {@code E}
     * before resolving its bound, so the nested {@code E} encountered while resolving the outer
     * bound resolves to the very same instance — the nested bound is therefore present and equal
     * to the outer one on both paths, which this asserts via {@link TypeUsage#canonicalName()}
     * rather than mere presence.
     */
    @Test
    void shouldAgreeOnSelfReferentialTypeVariableBoundShapeAcrossAllThreePopulationPaths() throws IOException {
        final var reflectionDescriptor = populateViaReflection();
        final var sourceDescriptor = populateViaJdkInitializer();
        final var processorDescriptor = populateViaAnnotationProcessor();

        assertNestedTypeVariableBoundShape(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertNestedTypeVariableBoundShape(reflectionDescriptor, processorDescriptor,
            "reflection", "annotation-processor");
    }

    private void assertNestedTypeVariableBoundShape(final TypeDescriptor expected, final TypeDescriptor actual,
                                                     final String expectedLabel, final String actualLabel) {
        assertThat(nestedTypeVariableBoundCanonicalName(actual))
            .as("nested self-referential E's upperBound shape via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(nestedTypeVariableBoundCanonicalName(expected));
    }

    private String nestedTypeVariableBoundCanonicalName(final TypeDescriptor typeDescriptor) {
        final var outerE = (TypeVariableUsage) typeDescriptor.getTrait(ParameterizedTypeDescriptor.class)
            .orElseThrow()
            .typeVariables()
            .findFirst()
            .orElseThrow();
        final var outerUpperBound = (GenericTypeUsage) outerE.upperBound().orElseThrow();
        final var nestedE = (TypeVariableUsage) outerUpperBound.parameters().findFirst().orElseThrow();
        return nestedE.upperBound().orElseThrow().canonicalName();
    }

    private TypeDescriptor populateViaReflection() {
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        return codeModel.getJDKTypeDescriptor(ClassificationFixture.class).orElseThrow();
    }

    private TypeDescriptor populateViaJdkInitializer() {
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        final var initializer = new JdkInitializer(List.of(FIXTURE_SOURCE_PATH.toFile()), List.of(), List.of());
        initializer.initialize(codeModel);
        return codeModel.getTypeDescriptor(codeModel.getEmptyModuleTypeName(FIXTURE_TYPE_NAME)).orElseThrow();
    }

    private TypeDescriptor populateViaAnnotationProcessor() throws IOException {
        final var source = Files.readString(FIXTURE_SOURCE_PATH);
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, FIXTURE_TYPE_NAME, source);
        final var codeModel = annotationProcessor.getCodeModel().orElseThrow();
        return codeModel.getTypeDescriptor(codeModel.getEmptyModuleTypeName(FIXTURE_TYPE_NAME)).orElseThrow();
    }

    private void assertFieldTraitsMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                        final String expectedLabel, final String actualLabel) {
        final var expectedByName = byFieldName(expected);
        final var actualByName = byFieldName(actual);

        assertThat(actualByName.keySet())
            .as("field names via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedByName.keySet());

        expectedByName.forEach((name, expectedField) -> {
            final var actualField = actualByName.get(name);
            assertThat(actualField.getTrait(Classification.class))
                .as("Classification of field '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedField.getTrait(Classification.class));
            assertThat(actualField.hasTrait(Static.class))
                .as("Static of field '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedField.hasTrait(Static.class));
            assertThat(actualField.getTrait(AccessModifier.class))
                .as("AccessModifier of field '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedField.getTrait(AccessModifier.class));
        });
    }

    private void assertMethodTraitsMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                         final String expectedLabel, final String actualLabel) {
        final var expectedByName = byMethodName(expected);
        final var actualByName = byMethodName(actual);

        assertThat(actualByName.keySet())
            .as("method names via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedByName.keySet());

        expectedByName.forEach((name, expectedMethod) -> {
            final var actualMethod = actualByName.get(name);
            assertThat(actualMethod.getTrait(Classification.class))
                .as("Classification of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.getTrait(Classification.class));
            assertThat(actualMethod.hasTrait(Static.class))
                .as("Static of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.hasTrait(Static.class));
            assertThat(actualMethod.getTrait(AccessModifier.class))
                .as("AccessModifier of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.getTrait(AccessModifier.class));
        });
    }

    private void assertConstructorTraitsMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                              final String expectedLabel, final String actualLabel) {
        final var expectedCtor = expected.getTrait(ConstructorDescriptor.class).orElseThrow();
        final var actualCtor = actual.getTrait(ConstructorDescriptor.class).orElseThrow();

        assertThat(actualCtor.getTrait(AccessModifier.class))
            .as("AccessModifier of constructor via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedCtor.getTrait(AccessModifier.class));

        final var expectedParams = expectedCtor.formalParameters().toList();
        final var actualParams = actualCtor.formalParameters().toList();
        assertThat(actualParams)
            .as("parameter count of constructor via %s vs %s", expectedLabel, actualLabel)
            .hasSameSizeAs(expectedParams);

        for (int i = 0; i < expectedParams.size(); i++) {
            assertThat(actualParams.get(i).hasTrait(Final.class))
                .as("Final of constructor param %d via %s vs %s", i, expectedLabel, actualLabel)
                .isEqualTo(expectedParams.get(i).hasTrait(Final.class));
        }
    }

    private Map<String, FieldDescriptor> byFieldName(final TypeDescriptor typeDescriptor) {
        return typeDescriptor.traits(FieldDescriptor.class)
            .collect(Collectors.toMap(f -> f.fieldName().toString(), Function.identity()));
    }

    private Map<String, MethodDescriptor> byMethodName(final TypeDescriptor typeDescriptor) {
        return typeDescriptor.traits(MethodDescriptor.class)
            .collect(Collectors.toMap(m -> m.methodName().name().toString(), Function.identity()));
    }
}
