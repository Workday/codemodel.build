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

import build.codemodel.foundation.ConceptualCodeModel;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link IntersectionTypeUsage}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class IntersectionTypeUsageTests {

    private ConceptualCodeModel codeModel;

    @BeforeEach
    void onBeforeEach() {
        this.codeModel = new ConceptualCodeModel(new NonCachingNameProvider());
    }

    /**
     * Ensures that {@link IntersectionTypeUsage#dependencies()} includes its member types,
     * consistent with {@link UnionTypeUsage#dependencies()}.
     */
    @Test
    void shouldIncludeMemberTypesInDependencies() {
        final var naming = this.codeModel.getNameProvider();

        final var serializableName = naming.getTypeName(java.io.Serializable.class);
        final var comparableName = naming.getTypeName(Comparable.class);

        final var serializable = SpecificTypeUsage.of(this.codeModel, serializableName);
        final var comparable = SpecificTypeUsage.of(this.codeModel, comparableName);

        final var intersection = IntersectionTypeUsage.of(this.codeModel, serializable, comparable);

        assertThat(intersection.dependencies())
            .as("IntersectionTypeUsage.dependencies() should include all member types")
            .containsExactly(serializable, comparable);
    }

    /**
     * Ensures that {@link IntersectionTypeUsage#dependencies()} is consistent with
     * {@link UnionTypeUsage#dependencies()} for the same member types.
     */
    @Test
    void shouldHaveSameDependencyBehaviorAsUnionTypeUsage() {
        final var naming = this.codeModel.getNameProvider();

        final var fooName = naming.getTypeName(String.class);
        final var barName = naming.getTypeName(Integer.class);

        final var foo = SpecificTypeUsage.of(this.codeModel, fooName);
        final var bar = SpecificTypeUsage.of(this.codeModel, barName);

        final var intersection = IntersectionTypeUsage.of(this.codeModel, foo, bar);
        final var union = UnionTypeUsage.of(this.codeModel, foo, bar);

        assertThat(intersection.dependencies().toList())
            .as("IntersectionTypeUsage and UnionTypeUsage should both include member types in dependencies()")
            .isEqualTo(union.dependencies().toList());
    }
}
