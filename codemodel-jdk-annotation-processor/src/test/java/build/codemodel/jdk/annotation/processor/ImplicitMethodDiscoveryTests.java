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

import build.codemodel.expression.naming.AbstractCallableName;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link AnnotationProcessor} models compiler-synthesized methods that never gain a
 * {@code MethodTree} — a record's component accessors and overridden {@code toString}/{@code
 * equals}/{@code hashCode}, and an enum's {@code values()}/{@code valueOf(String)}.
 *
 * <p>Unlike {@code JdkInitializer} (which walks the javac {@code ClassTree} and needs an explicit
 * pass to fill these in from {@code Elements} directly, since the tree only reflects written
 * source), {@link AnnotationProcessor} iterates {@code TypeElement.getEnclosedElements()}
 * directly — an {@code Element} always exposes the full API surface, implicit members included —
 * so no special-casing is needed. This test guards that behavior against regressing.
 */
class ImplicitMethodDiscoveryTests extends AnnotationProcessorTests {

    @Test
    void shouldModelImplicitRecordMethods() {
        final var source =
            """
                    import build.codemodel.jdk.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public record Point(int x, int y) {
                    }
                """;
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, "Point", source);

        final var codeModel = annotationProcessor.getCodeModel().orElseThrow();
        final var naming = codeModel.getNameProvider();
        final var typeName = naming.getEmptyModuleTypeName("Point");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(MethodDescriptor::methodName)
            .map(name -> name.name().toString()))
            .contains("x", "y", "toString", "equals", "hashCode");
    }

    @Test
    void shouldModelImplicitEnumMethods() {
        final var source =
            """
                    import build.codemodel.jdk.annotation.discovery.Discoverable;
                
                    @Discoverable
                    public enum Color {
                        RED, GREEN, BLUE
                    }
                """;
        final var annotationProcessor = new AnnotationProcessor();
        compile(annotationProcessor, "Color", source);

        final var codeModel = annotationProcessor.getCodeModel().orElseThrow();
        final var naming = codeModel.getNameProvider();
        final var typeName = naming.getEmptyModuleTypeName("Color");
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElseThrow();

        assertThat(typeDescriptor.traits(MethodDescriptor.class)
            .map(MethodDescriptor::methodName)
            .map(AbstractCallableName::name)
            .map(IrreducibleName::toString))
            .contains("values", "valueOf");
    }
}
