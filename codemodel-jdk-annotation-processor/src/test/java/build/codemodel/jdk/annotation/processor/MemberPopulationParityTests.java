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

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.ExplicitAnnotationParens;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.jdk.annotation.processor.fixture.ClassificationFixture;
import build.codemodel.jdk.annotation.processor.fixture.DefaultMethodFixture;
import build.codemodel.jdk.annotation.processor.fixture.EnumConstantFixture;
import build.codemodel.jdk.annotation.processor.fixture.ModifierFixture;
import build.codemodel.jdk.annotation.processor.fixture.NestedTypeFixture;
import build.codemodel.jdk.annotation.processor.fixture.RecordComponentFixture;
import build.codemodel.jdk.annotation.processor.fixture.SealedFixtureCircle;
import build.codemodel.jdk.annotation.processor.fixture.SealedFixtureShape;
import build.codemodel.jdk.annotation.processor.fixture.SealedFixtureSquare;
import build.codemodel.jdk.descriptor.Default;
import build.codemodel.jdk.descriptor.EnumConstantDescriptor;
import build.codemodel.jdk.descriptor.Final;
import build.codemodel.jdk.descriptor.MemberTypeDescriptor;
import build.codemodel.jdk.descriptor.Native;
import build.codemodel.jdk.descriptor.NonSealed;
import build.codemodel.jdk.descriptor.PermitsTypeDescriptor;
import build.codemodel.jdk.descriptor.RecordComponentDescriptor;
import build.codemodel.jdk.descriptor.Sealed;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.jdk.descriptor.Strictfp;
import build.codemodel.jdk.descriptor.Synchronized;
import build.codemodel.jdk.descriptor.Transient;
import build.codemodel.jdk.descriptor.Volatile;
import build.codemodel.jdk.populator.JdkInitializer;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.DeclarationOrder;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.tools.JavaFileObject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the three disjoint member-population paths — reflection ({@link JDKCodeModel}),
 * source parsing ({@link JdkInitializer}), and annotation processing ({@link AnnotationProcessor})
 * — attach the same {@link Classification}, {@link Static}, and {@link AccessModifier} traits to
 * the same fields, constructors, and methods of a single shared fixture. {@link DeclarationOrder}
 * and {@link ExplicitAnnotationParens} are also checked, but only between the two source-based
 * paths ({@link JdkInitializer}, {@link AnnotationProcessor}) — see the dedicated tests below for
 * why reflection is excluded from those two.
 *
 * <p>These paths share almost no code (see {@code docs/TODO.md}, "three disjoint population
 * paths"), so a trait added to one is easy to forget in another. This test would have caught the
 * previous gap where field {@link Classification} was populated by {@link JDKCodeModel} but not
 * by {@code TypeMirrorResolver} — and therefore missing from both {@link JdkInitializer} and
 * {@link AnnotationProcessor}, which delegate to it.
 *
 * @see ClassificationFixture
 */
public class MemberPopulationParityTests extends AnnotationProcessorTests {

    private static final String FIXTURE_PACKAGE = "build.codemodel.jdk.annotation.processor.fixture";
    private static final String FIXTURE_TYPE_NAME = FIXTURE_PACKAGE + ".ClassificationFixture";
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
     * {@link DeclarationOrder} is only comparable between the two source-based paths — {@link JdkInitializer}
     * (which sorts all members by source position) and {@link AnnotationProcessor} (which walks
     * {@code TypeElement.getEnclosedElements()}, which reflects encounter/declaration order) — both of which
     * assign a single counter shared across fields, constructors, and methods in source-declaration order.
     * {@link JDKCodeModel}'s reflection path assigns its shared counter in a fixed kind-grouped traversal
     * order instead (all constructors, then all methods, then all fields — see
     * {@code JDKCodeModelDeclarationOrderTests}), so its {@link DeclarationOrder} values are not expected to
     * agree with the source-based paths' numbering at all, only to be internally distinct.
     */
    @Test
    void shouldAgreeOnDeclarationOrderBetweenSourceBasedPopulationPaths() throws IOException {
        final var sourceDescriptor = populateViaJdkInitializer();
        final var processorDescriptor = populateViaAnnotationProcessor();

        assertDeclarationOrderMatches(sourceDescriptor, processorDescriptor);
    }

    private void assertDeclarationOrderMatches(final TypeDescriptor expected, final TypeDescriptor actual) {
        final var expectedFieldsByName = byFieldName(expected);
        final var actualFieldsByName = byFieldName(actual);
        assertThat(actualFieldsByName.keySet())
            .as("field names via source vs annotation-processor")
            .isEqualTo(expectedFieldsByName.keySet());
        expectedFieldsByName.forEach((name, expectedField) -> assertThat(
            actualFieldsByName.get(name).trait(DeclarationOrder.class).order())
            .as("DeclarationOrder of field '%s' via source vs annotation-processor", name)
            .isEqualTo(expectedField.trait(DeclarationOrder.class).order()));

        final var expectedMethodsByName = byMethodName(expected);
        final var actualMethodsByName = byMethodName(actual);
        assertThat(actualMethodsByName.keySet())
            .as("method names via source vs annotation-processor")
            .isEqualTo(expectedMethodsByName.keySet());
        expectedMethodsByName.forEach((name, expectedMethod) -> assertThat(
            actualMethodsByName.get(name).trait(DeclarationOrder.class).order())
            .as("DeclarationOrder of method '%s' via source vs annotation-processor", name)
            .isEqualTo(expectedMethod.trait(DeclarationOrder.class).order()));

        final var expectedCtor = expected.getTrait(ConstructorDescriptor.class).orElseThrow();
        final var actualCtor = actual.getTrait(ConstructorDescriptor.class).orElseThrow();
        assertThat(actualCtor.trait(DeclarationOrder.class).order())
            .as("DeclarationOrder of constructor via source vs annotation-processor")
            .isEqualTo(expectedCtor.trait(DeclarationOrder.class).order());
    }

    /**
     * {@link ExplicitAnnotationParens} can only ever be recovered from source ({@code com.sun.source.util.Trees}),
     * so unlike the other traits above, it is compared between the two source-based paths only —
     * {@link JdkInitializer} and {@link AnnotationProcessor} — never against {@link JDKCodeModel}'s reflection
     * descriptor, which never attaches this trait at all.
     */
    @Test
    void shouldAgreeOnExplicitAnnotationParensBetweenSourceBasedPopulationPaths() throws IOException {
        final var sourceDescriptor = populateViaJdkInitializer();
        final var processorDescriptor = populateViaAnnotationProcessor();

        assertExplicitAnnotationParensMatch(sourceDescriptor, processorDescriptor);
    }

    private void assertExplicitAnnotationParensMatch(final TypeDescriptor expected, final TypeDescriptor actual) {
        final var expectedByName = byMethodName(expected);
        final var actualByName = byMethodName(actual);
        assertThat(actualByName.keySet())
            .as("method names via source vs annotation-processor")
            .isEqualTo(expectedByName.keySet());

        expectedByName.forEach((name, expectedMethod) -> {
            final var actualMethod = actualByName.get(name);
            assertThat(hasExplicitAnnotationParens(actualMethod))
                .as("ExplicitAnnotationParens of method '%s' via source vs annotation-processor", name)
                .isEqualTo(hasExplicitAnnotationParens(expectedMethod));
        });
    }

    private boolean hasExplicitAnnotationParens(final MethodDescriptor methodDescriptor) {
        return methodDescriptor.traits(AnnotationTypeUsage.class)
            .anyMatch(annotation -> annotation.hasTrait(ExplicitAnnotationParens.class));
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

    /**
     * {@code EnumConstantDescriptor} name/order agreement across all three population paths —
     * {@link JDKCodeModel} only just gained enum constant modeling (reflection), as did
     * {@link AnnotationProcessor} (source-parsing already had it via {@code TypeMirrorResolver}).
     */
    @Test
    void shouldAgreeOnEnumConstantsAcrossAllThreePopulationPaths() throws IOException {
        final var typeName = FIXTURE_PACKAGE + ".EnumConstantFixture";
        final var sourcePath = fixturePath("EnumConstantFixture.java");

        final var reflectionDescriptor = populateViaReflection(EnumConstantFixture.class);
        final var sourceDescriptor = populateViaJdkInitializer(typeName, sourcePath);
        final var processorDescriptor = populateViaAnnotationProcessor(typeName, sourcePath);

        assertEnumConstantsMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertEnumConstantsMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");
    }

    private void assertEnumConstantsMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                          final String expectedLabel, final String actualLabel) {
        final var expectedByName = expected.traits(EnumConstantDescriptor.class)
            .collect(Collectors.toMap(c -> c.name().toString(), EnumConstantDescriptor::order));
        final var actualByName = actual.traits(EnumConstantDescriptor.class)
            .collect(Collectors.toMap(c -> c.name().toString(), EnumConstantDescriptor::order));

        assertThat(actualByName)
            .as("enum constant name/order via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedByName);
    }

    /**
     * {@code RecordComponentDescriptor} name/type agreement across all three population paths —
     * {@link JDKCodeModel} only just gained record component modeling via reflection.
     */
    @Test
    void shouldAgreeOnRecordComponentsAcrossAllThreePopulationPaths() throws IOException {
        final var typeName = FIXTURE_PACKAGE + ".RecordComponentFixture";
        final var sourcePath = fixturePath("RecordComponentFixture.java");

        final var reflectionDescriptor = populateViaReflection(RecordComponentFixture.class);
        final var sourceDescriptor = populateViaJdkInitializer(typeName, sourcePath);
        final var processorDescriptor = populateViaAnnotationProcessor(typeName, sourcePath);

        assertRecordComponentsMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertRecordComponentsMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");
    }

    private void assertRecordComponentsMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                             final String expectedLabel, final String actualLabel) {
        final var expectedByName = expected.traits(RecordComponentDescriptor.class)
            .collect(Collectors.toMap(c -> c.name().toString(), c -> c.type().canonicalName()));
        final var actualByName = actual.traits(RecordComponentDescriptor.class)
            .collect(Collectors.toMap(c -> c.name().toString(), c -> c.type().canonicalName()));

        assertThat(actualByName)
            .as("record component name/type via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedByName);
    }

    /**
     * {@code MemberTypeDescriptor} name agreement across all three population paths —
     * {@link JDKCodeModel} only just gained nested/member type modeling via reflection.
     */
    @Test
    void shouldAgreeOnNestedMemberTypesAcrossAllThreePopulationPaths() throws IOException {
        final var typeName = FIXTURE_PACKAGE + ".NestedTypeFixture";
        final var sourcePath = fixturePath("NestedTypeFixture.java");

        final var reflectionDescriptor = populateViaReflection(NestedTypeFixture.class);
        final var sourceDescriptor = populateViaJdkInitializer(typeName, sourcePath);
        final var processorDescriptor = populateViaAnnotationProcessor(typeName, sourcePath);

        assertNestedMemberTypesMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertNestedMemberTypesMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");
    }

    private void assertNestedMemberTypesMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                              final String expectedLabel, final String actualLabel) {
        assertThat(memberTypeSimpleNames(actual))
            .as("member type names via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(memberTypeSimpleNames(expected));
    }

    private Set<String> memberTypeSimpleNames(final TypeDescriptor typeDescriptor) {
        return typeDescriptor.traits(MemberTypeDescriptor.class)
            .map(m -> m.memberTypeName().name().toString())
            .collect(Collectors.toSet());
    }

    /**
     * {@code Transient}/{@code Volatile} (fields) and {@code Synchronized}/{@code Native}/
     * {@code Strictfp} (methods) agreement across all three population paths — all six were added
     * to all three paths together (#150), but only ever tested per-path.
     */
    @Test
    void shouldAgreeOnRemainingModifierTraitsAcrossAllThreePopulationPaths() throws IOException {
        final var typeName = FIXTURE_PACKAGE + ".ModifierFixture";
        final var sourcePath = fixturePath("ModifierFixture.java");

        final var reflectionDescriptor = populateViaReflection(ModifierFixture.class);
        final var sourceDescriptor = populateViaJdkInitializer(typeName, sourcePath);
        final var processorDescriptor = populateViaAnnotationProcessor(typeName, sourcePath);

        assertModifierTraitsMatch(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertModifierTraitsMatch(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");
    }

    private void assertModifierTraitsMatch(final TypeDescriptor expected, final TypeDescriptor actual,
                                           final String expectedLabel, final String actualLabel) {
        final var expectedFieldsByName = byFieldName(expected);
        final var actualFieldsByName = byFieldName(actual);
        assertThat(actualFieldsByName.keySet())
            .as("field names via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedFieldsByName.keySet());
        expectedFieldsByName.forEach((name, expectedField) -> {
            final var actualField = actualFieldsByName.get(name);
            assertThat(actualField.hasTrait(Transient.class))
                .as("Transient of field '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedField.hasTrait(Transient.class));
            assertThat(actualField.hasTrait(Volatile.class))
                .as("Volatile of field '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedField.hasTrait(Volatile.class));
        });

        final var expectedMethodsByName = byMethodName(expected);
        final var actualMethodsByName = byMethodName(actual);
        assertThat(actualMethodsByName.keySet())
            .as("method names via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedMethodsByName.keySet());
        expectedMethodsByName.forEach((name, expectedMethod) -> {
            final var actualMethod = actualMethodsByName.get(name);
            assertThat(actualMethod.hasTrait(Synchronized.class))
                .as("Synchronized of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.hasTrait(Synchronized.class));
            assertThat(actualMethod.hasTrait(Native.class))
                .as("Native of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.hasTrait(Native.class));
            assertThat(actualMethod.hasTrait(Strictfp.class))
                .as("Strictfp of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.hasTrait(Strictfp.class));
        });
    }

    /**
     * {@code Default} (interface default methods) agreement across all three population paths.
     */
    @Test
    void shouldAgreeOnDefaultMethodTraitAcrossAllThreePopulationPaths() throws IOException {
        final var typeName = FIXTURE_PACKAGE + ".DefaultMethodFixture";
        final var sourcePath = fixturePath("DefaultMethodFixture.java");

        final var reflectionDescriptor = populateViaReflection(DefaultMethodFixture.class);
        final var sourceDescriptor = populateViaJdkInitializer(typeName, sourcePath);
        final var processorDescriptor = populateViaAnnotationProcessor(typeName, sourcePath);

        assertDefaultTraitMatches(reflectionDescriptor, sourceDescriptor, "reflection", "source");
        assertDefaultTraitMatches(reflectionDescriptor, processorDescriptor, "reflection", "annotation-processor");
    }

    private void assertDefaultTraitMatches(final TypeDescriptor expected, final TypeDescriptor actual,
                                           final String expectedLabel, final String actualLabel) {
        final var expectedByName = byMethodName(expected);
        final var actualByName = byMethodName(actual);
        assertThat(actualByName.keySet())
            .as("method names via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expectedByName.keySet());
        expectedByName.forEach((name, expectedMethod) -> {
            final var actualMethod = actualByName.get(name);
            assertThat(actualMethod.hasTrait(Default.class))
                .as("Default of method '%s' via %s vs %s", name, expectedLabel, actualLabel)
                .isEqualTo(expectedMethod.hasTrait(Default.class));
        });
    }

    /**
     * {@code Sealed}/{@code NonSealed}/{@code PermitsTypeDescriptor} agreement across all three
     * population paths, using a three-file sealed hierarchy ({@link SealedFixtureShape} permits
     * {@link SealedFixtureCircle} and {@link SealedFixtureSquare}) so that {@code JdkInitializer}
     * and {@code AnnotationProcessor} both see every type in a single compilation.
     */
    @Test
    void shouldAgreeOnSealedNonSealedAndPermitsAcrossAllThreePopulationPaths() throws IOException {
        final var shapeTypeName = FIXTURE_PACKAGE + ".SealedFixtureShape";
        final var squareTypeName = FIXTURE_PACKAGE + ".SealedFixtureSquare";
        final var shapePath = fixturePath("SealedFixtureShape.java");
        final var circlePath = fixturePath("SealedFixtureCircle.java");
        final var squarePath = fixturePath("SealedFixtureSquare.java");

        final var reflectionShape = populateViaReflection(SealedFixtureShape.class);
        final var sourceShape = populateViaJdkInitializer(shapeTypeName, shapePath, circlePath, squarePath);
        final var processorShape = populateViaAnnotationProcessor(shapeTypeName, shapePath, circlePath, squarePath);

        assertSealedShapeMatches(reflectionShape, sourceShape, "reflection", "source");
        assertSealedShapeMatches(reflectionShape, processorShape, "reflection", "annotation-processor");

        final var reflectionSquare = populateViaReflection(SealedFixtureSquare.class);
        final var sourceSquare = populateViaJdkInitializer(squareTypeName, shapePath, circlePath, squarePath);
        final var processorSquare = populateViaAnnotationProcessor(squareTypeName, shapePath, circlePath, squarePath);

        assertThat(sourceSquare.hasTrait(NonSealed.class))
            .as("NonSealed of SealedFixtureSquare via reflection vs source")
            .isEqualTo(reflectionSquare.hasTrait(NonSealed.class));
        assertThat(processorSquare.hasTrait(NonSealed.class))
            .as("NonSealed of SealedFixtureSquare via reflection vs annotation-processor")
            .isEqualTo(reflectionSquare.hasTrait(NonSealed.class));
    }

    private void assertSealedShapeMatches(final TypeDescriptor expected, final TypeDescriptor actual,
                                          final String expectedLabel, final String actualLabel) {
        assertThat(actual.hasTrait(Sealed.class))
            .as("Sealed of SealedFixtureShape via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(expected.hasTrait(Sealed.class));

        assertThat(permittedSimpleNames(actual))
            .as("permitted subtype names of SealedFixtureShape via %s vs %s", expectedLabel, actualLabel)
            .isEqualTo(permittedSimpleNames(expected));
    }

    private Set<String> permittedSimpleNames(final TypeDescriptor typeDescriptor) {
        return typeDescriptor.traits(PermitsTypeDescriptor.class)
            .map(p -> p.parentTypeUsage().typeName().name().toString())
            .collect(Collectors.toSet());
    }

    private Path fixturePath(final String fileName) {
        return Path.of("src/test/java/" + FIXTURE_PACKAGE.replace('.', '/') + "/" + fileName);
    }

    private TypeDescriptor populateViaReflection() {
        return populateViaReflection(ClassificationFixture.class);
    }

    private TypeDescriptor populateViaJdkInitializer() {
        return populateViaJdkInitializer(FIXTURE_TYPE_NAME, FIXTURE_SOURCE_PATH);
    }

    private TypeDescriptor populateViaAnnotationProcessor() throws IOException {
        return populateViaAnnotationProcessor(FIXTURE_TYPE_NAME, FIXTURE_SOURCE_PATH);
    }

    private TypeDescriptor populateViaReflection(final Class<?> type) {
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        return codeModel.getJDKTypeDescriptor(type).orElseThrow();
    }

    /**
     * {@code sourcePaths} may list more than one file (e.g. a sealed hierarchy whose permitted
     * subtypes each live in their own top-level file) — every file is compiled together so
     * cross-file references resolve, and {@code typeName} selects which resulting descriptor to
     * return.
     */
    private TypeDescriptor populateViaJdkInitializer(final String typeName, final Path... sourcePaths) {
        final var codeModel = new JDKCodeModel(new NonCachingNameProvider());
        final var initializer = new JdkInitializer(
            Arrays.stream(sourcePaths).map(Path::toFile).toList(), List.of(), List.of());
        initializer.initialize(codeModel);
        return codeModel.getTypeDescriptor(codeModel.getEmptyModuleTypeName(typeName)).orElseThrow();
    }

    private TypeDescriptor populateViaAnnotationProcessor(final String typeName,
                                                          final Path... sourcePaths) throws IOException {
        final var files = new ArrayList<JavaFileObject>();
        for (final var sourcePath : sourcePaths) {
            final var simpleName = sourcePath.getFileName().toString().replace(".java", "");
            files.add(JavaFileObjects.forSourceString(
                FIXTURE_PACKAGE + "." + simpleName,
                Files.readString(sourcePath)));
        }
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, files.toArray(JavaFileObject[]::new));
        final var codeModel = annotationProcessor.getCodeModel().orElseThrow();
        return codeModel.getTypeDescriptor(codeModel.getEmptyModuleTypeName(typeName)).orElseThrow();
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
