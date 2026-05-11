package build.codemodel.foundation.usage;

/*-
 * #%L
 * Code Model Foundation
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

import build.base.foundation.Lazy;
import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeUsage#format()}, covering all four primary branches and contrasting with {@link Object#toString()}.
 * The key difference: {@code format()} always returns canonical (module-free) names, while {@code toString()} preserves
 * module-qualified names as stored in the {@link build.codemodel.foundation.CodeModel}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class TypeUsageFormatTests {

    private final NonCachingNameProvider naming = new NonCachingNameProvider();
    private final ConceptualCodeModel codeModel = new ConceptualCodeModel(naming);
    private final Optional<ModuleName> javaBase = naming.getModuleName("java.base");

    // --- NamedTypeUsage (via SpecificTypeUsage) ---

    @Test
    void namedTypeUsage_formatAndToString_areIdentical_whenNoModule() {
        final var name = naming.getTypeName(Optional.empty(), "java.lang.String");
        final var usage = SpecificTypeUsage.of(codeModel, name);

        assertThat(usage.toString()).isEqualTo("java.lang.String");
        assertThat(usage.canonicalName()).isEqualTo("java.lang.String");
    }

    @Test
    void namedTypeUsage_format_stripsModule_whileToString_preservesIt() {
        final var name = naming.getTypeName(javaBase, "java.lang.String");
        final var usage = SpecificTypeUsage.of(codeModel, name);

        assertThat(usage.toString()).isEqualTo("java.base/java.lang.String");
        assertThat(usage.canonicalName()).isEqualTo("java.lang.String");
    }

    // --- GenericTypeUsage ---

    @Test
    void genericTypeUsage_format_stripsModuleFromRawTypeAndParameters() {
        final var mapName = naming.getTypeName(javaBase, "java.util.Map");
        final var stringUsage = SpecificTypeUsage.of(codeModel, naming.getTypeName(javaBase, "java.lang.String"));
        final var integerUsage = SpecificTypeUsage.of(codeModel, naming.getTypeName(javaBase, "java.lang.Integer"));
        final var usage = GenericTypeUsage.of(codeModel, mapName, stringUsage, integerUsage);

        assertThat(usage.toString()).isEqualTo("java.base/java.util.Map<java.base/java.lang.String,java.base/java.lang.Integer>");
        assertThat(usage.canonicalName()).isEqualTo("java.util.Map<java.lang.String,java.lang.Integer>");
    }

    // --- TypeVariableUsage ---

    @Test
    void typeVariableUsage_formatAndToString_areIdentical_whenUnbounded() {
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.empty());

        assertThat(usage.toString()).isEqualTo("T");
        assertThat(usage.canonicalName()).isEqualTo("T");
    }

    @Test
    void typeVariableUsage_format_includesBounds_withModuleStripped() {
        // T extends Number — format() keeps the bound but strips modules (none here, same result)
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var numberUsage = SpecificTypeUsage.of(codeModel, naming.getTypeName(Optional.empty(), "java.lang.Number"));
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.of(Lazy.of(numberUsage)));

        assertThat(usage.toString()).isEqualTo("T extends java.lang.Number");
        assertThat(usage.canonicalName()).isEqualTo("T extends java.lang.Number");
    }

    @Test
    void typeVariableUsage_format_stripsModuleFromBound() {
        // T extends Number — format() keeps the bound but strips the module from it
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var numberUsage = SpecificTypeUsage.of(codeModel, naming.getTypeName(javaBase, "java.lang.Number"));
        final var usage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.of(Lazy.of(numberUsage)));

        assertThat(usage.toString()).isEqualTo("T extends java.base/java.lang.Number");
        assertThat(usage.canonicalName()).isEqualTo("T extends java.lang.Number");
    }

    // --- AnnotationTypeUsage ---

    @Test
    void annotationTypeUsage_noValues_noModule() {
        final var name = naming.getTypeName(Optional.empty(), "com.example.Qualifier");
        final var usage = AnnotationTypeUsage.of(codeModel, name);

        assertThat(usage.toString()).isEqualTo("@com.example.Qualifier()");
        assertThat(usage.canonicalName()).isEqualTo("@com.example.Qualifier()");
    }

    @Test
    void annotationTypeUsage_noValues_stripsModule() {
        final var name = naming.getTypeName(javaBase, "java.lang.annotation.Retention");
        final var usage = AnnotationTypeUsage.of(codeModel, name);

        assertThat(usage.toString()).isEqualTo("@java.base/java.lang.annotation.Retention()");
        assertThat(usage.canonicalName()).isEqualTo("@java.lang.annotation.Retention()");
    }

    @Test
    void annotationTypeUsage_withValues() {
        final var name = naming.getTypeName(Optional.empty(), "com.example.Named");
        final var usage = AnnotationTypeUsage.of(codeModel, name,
            AnnotationValue.of(codeModel, "value", "foo"));

        assertThat(usage.toString()).isEqualTo("@com.example.Named(foo)");
        assertThat(usage.canonicalName()).isEqualTo("@com.example.Named(foo)");
    }

    // --- ArrayTypeUsage ---

    @Test
    void arrayTypeUsage_formatAndToString_areIdentical_whenNoModule() {
        final var stringUsage = SpecificTypeUsage.of(codeModel, naming.getTypeName(Optional.empty(), "java.lang.String"));
        final var usage = ArrayTypeUsage.of(codeModel, Lazy.of(stringUsage));

        assertThat(usage.toString()).isEqualTo("java.lang.String[]");
        assertThat(usage.canonicalName()).isEqualTo("java.lang.String[]");
    }

    @Test
    void arrayTypeUsage_format_stripsModuleFromComponent() {
        final var stringUsage = SpecificTypeUsage.of(codeModel, naming.getTypeName(javaBase, "java.lang.String"));
        final var usage = ArrayTypeUsage.of(codeModel, Lazy.of(stringUsage));

        assertThat(usage.toString()).isEqualTo("java.base/java.lang.String[]");
        assertThat(usage.canonicalName()).isEqualTo("java.lang.String[]");
    }
}
