package build.codemodel.jdk.annotation.processor;

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

import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.DeclarationOrder;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confirms the annotation-processing population path in {@link AnnotationProcessor} (the third of three
 * population paths, alongside the source-tree path in {@code JdkInitializer} and the reflection path in
 * {@code JDKCodeModel}) stamps every field, constructor, and method with a {@link DeclarationOrder} trait
 * matching {@link javax.lang.model.element.Element} encounter order.
 *
 * <p>The counter is shared across fields, constructors, and methods (rather than restarting at zero for
 * each kind) so that {@link DeclarationOrder} reflects each member's position among all members of the
 * type, matching the semantics of the source-tree path in {@code JdkInitializer}.
 */
class AnnotationProcessorDeclarationOrderTests
    extends AnnotationProcessorTests {

    @Test
    void everyFieldConstructorAndMethodIsStampedWithADeclarationOrder() {
        final var source =
            """
                import build.codemodel.jdk.annotation.discovery.Discoverable;
                
                @Discoverable
                public class Ordered {
                    public int alpha;
                    public int beta;
                    public int gamma;
                
                    public Ordered() {
                    }
                
                    public Ordered(final int alpha) {
                        this.alpha = alpha;
                    }
                
                    public void first() {
                    }
                
                    public void second() {
                    }
                
                    public void third() {
                    }
                }
                """;
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, "Ordered", source);

        final var codeModel = annotationProcessor.getCodeModel().orElseThrow();
        final var naming = codeModel.getNameProvider();
        final var typeDescriptor = codeModel.getTypeDescriptor(naming.getEmptyModuleTypeName("Ordered"))
            .orElseThrow();

        final var fieldOrders = typeDescriptor.traits(FieldDescriptor.class)
            .map(f -> f.trait(DeclarationOrder.class).order())
            .toList();
        assertThat(fieldOrders).containsExactlyInAnyOrder(0, 1, 2);

        final var constructorOrders = typeDescriptor.traits(ConstructorDescriptor.class)
            .map(c -> c.trait(DeclarationOrder.class).order())
            .toList();
        assertThat(constructorOrders).containsExactlyInAnyOrder(3, 4);

        final var methodOrders = typeDescriptor.traits(MethodDescriptor.class)
            .map(m -> m.trait(DeclarationOrder.class).order())
            .toList();
        assertThat(methodOrders).containsExactlyInAnyOrder(5, 6, 7);
    }
}
