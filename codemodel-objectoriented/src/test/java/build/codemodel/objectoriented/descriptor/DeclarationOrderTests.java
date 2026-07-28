package build.codemodel.objectoriented.descriptor;

/*-
 * #%L
 * Object-Oriented Code Model
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

import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.objectoriented.ObjectOrientedCodeModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks down {@link DeclarationOrder}: a {@code @Singular} {@link build.codemodel.foundation.descriptor.Trait}
 * recording a member's zero-based position among its siblings in declaration order, since
 * {@code Traitable} member storage (a {@code Set}) does not itself preserve insertion order.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class DeclarationOrderTests {

    @Test
    void orderReturnsTheConstructedValue() {
        final var declarationOrder = new DeclarationOrder(3);

        assertThat(declarationOrder.order()).isEqualTo(3);
    }

    @Test
    void recordEqualityIsBasedOnOrder() {
        assertThat(new DeclarationOrder(2)).isEqualTo(new DeclarationOrder(2));
        assertThat(new DeclarationOrder(2)).isNotEqualTo(new DeclarationOrder(3));
    }

    @Test
    void addTraitAndGetTraitRoundTripOnAField() {
        final var naming = new NonCachingNameProvider();
        final var codeModel = new ObjectOrientedCodeModel(naming);
        final var type = SpecificTypeUsage.of(codeModel, naming.getEmptyModuleTypeName("java.lang.String"));
        final var field = FieldDescriptor.of(codeModel, IrreducibleName.of("name"), type);

        field.addTrait(new DeclarationOrder(5));

        assertThat(field.getTrait(DeclarationOrder.class))
            .map(DeclarationOrder::order)
            .contains(5);
    }
}
