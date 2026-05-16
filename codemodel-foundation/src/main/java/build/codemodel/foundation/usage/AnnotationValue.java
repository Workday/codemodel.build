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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.base.mereology.Composite;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.TypeName;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The value of an <i>Annotation</i> as part of an {@link AnnotationTypeUsage}.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public class AnnotationValue
    extends AbstractTraitable
    implements Traitable {

    /**
     * A typed representation of an annotation element value.
     *
     * <p>Annotation element types are restricted by the JLS: primitives, {@link String},
     * {@link Class} references, enum constants, nested annotations, and arrays of any of these.
     */
    public sealed interface Value
        permits Value.Literal, Value.ClassRef, Value.EnumConstant, Value.Nested, Value.Array {

        /**
         * A primitive or {@link String} literal.
         */
        record Literal(Object value) implements Value {
            @Override
            public String toString() {
                return value.toString();
            }
        }

        /**
         * A class literal, e.g. {@code Foo.class}.
         *
         * @param typeName the codemodel {@link TypeName} for the referenced class
         */
        record ClassRef(TypeName typeName) implements Value {
            @Override
            public String toString() {
                return typeName.canonicalName() + ".class";
            }
        }

        /**
         * An enum constant reference.
         *
         * @param typeName     the enum type
         * @param constantName the constant name
         */
        record EnumConstant(TypeName typeName, String constantName) implements Value {
            @Override
            public String toString() {
                return typeName.canonicalName() + "." + constantName;
            }
        }

        /**
         * A nested annotation.
         *
         * @param annotation the {@link AnnotationTypeUsage} representing the nested annotation
         */
        record Nested(AnnotationTypeUsage annotation) implements Value {
            @Override
            public String toString() {
                return annotation.toString();
            }
        }

        /**
         * An array of annotation element values.
         *
         * @param elements the elements
         */
        record Array(List<Value> elements) implements Value {
            @Override
            public String toString() {
                return elements.toString();
            }
        }
    }

    // --- Marshalling conversion helpers ---

    /**
     * Converts a raw marshalled {@link Object} back to a typed {@link Value}.
     * Handles the legacy format (plain Object) and the structured format (AnnotationTypeUsage, List).
     */
    private static Value toValue(final Object raw) {
        return switch (raw) {
            case Value v -> v;
            case AnnotationTypeUsage a -> new Value.Nested(a);
            case List<?> list -> new Value.Array(list.stream()
                .map(AnnotationValue::toValue)
                .toList());
            default -> new Value.Literal(raw);
        };
    }

    /**
     * Converts a {@link Value} back to the raw object used in the marshal format.
     */
    private static Object fromValue(final Value value) {
        return switch (value) {
            case Value.Literal(var v) -> v;
            case Value.Nested(var a) -> a;
            case Value.Array(var elements) -> elements.stream()
                .map(AnnotationValue::fromValue)
                .toList();
            case Value.ClassRef(var typeName) -> typeName;
            case Value.EnumConstant(var typeName, var constantName) -> typeName.canonicalName() + "." + constantName;
        };
    }

    // ---

    private final IrreducibleName name;
    private final Value value;

    private AnnotationValue(final CodeModel codeModel,
                            final IrreducibleName name,
                            final Value value) {
        super(codeModel);
        this.name = Objects.requireNonNull(name, "The AnnotationValue name must not be null");
        this.value = Objects.requireNonNull(value, "The AnnotationValue value must not be null");
    }

    @Unmarshal
    public AnnotationValue(@Bound final CodeModel codeModel,
                           final Marshaller marshaller,
                           final Stream<Marshalled<Trait>> traits,
                           final IrreducibleName name,
                           final Object value) {
        super(codeModel, marshaller, traits);
        this.name = name;
        this.value = toValue(value);
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<IrreducibleName> name,
                           final Out<Object> value) {
        super.destructor(marshaller, traits);
        name.set(this.name);
        value.set(fromValue(this.value));
    }

    public IrreducibleName name() {
        return this.name;
    }

    public Value value() {
        return this.value;
    }

    @Override
    protected Stream<? extends Composite> compositeChildren() {
        return switch (this.value) {
            case Value.Nested(var annotation) -> Stream.of(annotation);
            case Value.Array(var elements) -> elements.stream()
                .flatMap(e -> switch (e) {
                    case Value.Nested(var a) -> Stream.of(a);
                    default -> Stream.empty();
                });
            default -> Stream.empty();
        };
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof AnnotationValue that
            && Objects.equals(this.name, that.name())
            && Objects.equals(this.value, that.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    public static AnnotationValue of(final CodeModel codeModel,
                                     final IrreducibleName name,
                                     final Value value) {
        return new AnnotationValue(codeModel, name, value);
    }

    public static AnnotationValue of(final CodeModel codeModel,
                                     final String name,
                                     final Value value) {
        Objects.requireNonNull(codeModel, "The CodeModel must not be null");
        return new AnnotationValue(codeModel, codeModel.getNameProvider().getIrreducibleName(name), value);
    }

    public static AnnotationValue of(final CodeModel codeModel,
                                     final String name,
                                     final String value) {
        Objects.requireNonNull(codeModel, "The CodeModel must not be null");
        return new AnnotationValue(codeModel, codeModel.getNameProvider().getIrreducibleName(name), new Value.Literal(value));
    }

    static {
        Marshalling.register(AnnotationValue.class, MethodHandles.lookup());
    }
}
