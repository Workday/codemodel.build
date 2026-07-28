package build.codemodel.jdk;

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

import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.jdk.example.OrderedExample;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.DeclarationOrder;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confirms the reflection-based population path in {@link JDKCodeModel} (as opposed to the source-tree
 * path in {@code JdkInitializer}, covered by {@code MemberOrderTests} in {@code codemodel-jdk-populator})
 * stamps every field, constructor, and method with a {@link DeclarationOrder} trait.
 *
 * <p>{@link Class#getDeclaredFields()}/{@code getDeclaredMethods()}/{@code getDeclaredConstructors()} do
 * not formally guarantee declaration order, so this only asserts that each kind of member is stamped with
 * a complete, distinct set of orders — not a specific sequence within the kind — to avoid coupling the
 * test to JVM-specific reflection ordering. The counter is shared across constructors, methods, and fields
 * (assigned in that fixed traversal order, matching {@code JDKCodeModel}) so that {@link DeclarationOrder}
 * reflects each member's position among all members of the type, not just among its own kind; that fixed
 * traversal order between kinds is what pins each kind's set of orders to a known, non-overlapping range.
 */
class JDKCodeModelDeclarationOrderTests {

    @Test
    void everyFieldConstructorAndMethodIsStampedWithADistinctDeclarationOrder() {
        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);

        final var typeDescriptor = codeModel.getJDKTypeDescriptor(OrderedExample.class).orElseThrow();

        final var constructorOrders = typeDescriptor.traits(ConstructorDescriptor.class)
            .map(c -> c.trait(DeclarationOrder.class).order())
            .toList();
        assertThat(constructorOrders).containsExactlyInAnyOrder(0, 1);

        final var methodOrders = typeDescriptor.traits(MethodDescriptor.class)
            .map(m -> m.trait(DeclarationOrder.class).order())
            .toList();
        assertThat(methodOrders).containsExactlyInAnyOrder(2, 3, 4);

        final var fieldOrders = typeDescriptor.traits(FieldDescriptor.class)
            .map(f -> f.trait(DeclarationOrder.class).order())
            .toList();
        assertThat(fieldOrders).containsExactlyInAnyOrder(5, 6, 7);
    }
}
