package build.codemodel.dependency.injection;

/*-
 * #%L
 * Dependency Injection
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

import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Dependency#signatureOf}.
 *
 * @author reed.vonredwitz
 * @see IndependentDependency
 */
class DependencyTests
    implements ContextualTesting {

    /**
     * With no qualifier {@link build.codemodel.foundation.usage.AnnotationTypeUsage}s, the signature is
     * simply the {@link build.codemodel.foundation.usage.TypeUsage}'s canonical name.
     */
    @Test
    void shouldReturnCanonicalNameWhenNoQualifiersPresent() {
        final var codeModel = createCodeModel();
        final var typeUsage = codeModel.getTypeUsage(String.class);

        final var signature = Dependency.signatureOf(typeUsage, Stream.empty());

        assertThat(signature)
            .isEqualTo(String.class.getCanonicalName());
    }

    /**
     * The signature must be stable regardless of the order in which qualifier annotations were declared, so
     * that two independently-derived signatures for the "same qualified type" (e.g. an
     * {@link IndependentDependency} and a {@code @Provides} method) are recognized as equal.
     */
    @Test
    void shouldProduceSameSignatureRegardlessOfQualifierDeclarationOrder() throws NoSuchFieldException {
        final var codeModel = createCodeModel();
        final var typeUsage = codeModel.getTypeUsage(String.class);

        final var field = QualifierFixture.class.getDeclaredField("field");
        final var named = codeModel.getAnnotation(field.getAnnotation(Named.class));
        final var deprecated = codeModel.getAnnotation(field.getAnnotation(Deprecated.class));

        final var forward = Dependency.signatureOf(typeUsage, Stream.of(named, deprecated));
        final var reversed = Dependency.signatureOf(typeUsage, Stream.of(deprecated, named));

        assertThat(forward)
            .isEqualTo(reversed)
            .startsWith(String.class.getCanonicalName());
    }

    /**
     * More than one qualifier annotation of the same type makes the signature ambiguous (e.g. two
     * differently-valued {@code @Named} annotations), so it must be rejected rather than silently picking one.
     */
    @Test
    void shouldRejectMoreThanOneQualifierAnnotationOfTheSameType() throws NoSuchFieldException {
        final var codeModel = createCodeModel();
        final var typeUsage = codeModel.getTypeUsage(String.class);

        final var firstField = QualifierFixture.class.getDeclaredField("field");
        final var secondField = OtherQualifierFixture.class.getDeclaredField("field");

        final var firstNamed = codeModel.getAnnotation(firstField.getAnnotation(Named.class));
        final var secondNamed = codeModel.getAnnotation(secondField.getAnnotation(Named.class));

        assertThatThrownBy(() -> Dependency.signatureOf(typeUsage, Stream.of(firstNamed, secondNamed)))
            .isInstanceOf(DuplicateQualifierException.class);
    }

    private static class QualifierFixture {
        @Named("first")
        @Deprecated
        Object field;
    }

    private static class OtherQualifierFixture {
        @Named("second")
        Object field;
    }
}
