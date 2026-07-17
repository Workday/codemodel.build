package build.codemodel.jdk.expression;

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

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.base.mereology.Composite;
import build.codemodel.expression.AbstractExpression;
import build.codemodel.expression.Expression;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.usage.TypeUsage;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An {@code instanceof} expression: {@code expr instanceof Type}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public final class InstanceOf
    extends AbstractExpression {

    /**
     * A component of a record deconstruction pattern (Java 21+ record patterns), e.g. each of
     * {@code int x}, {@code int y} in {@code case Point(int x, int y) ->}.
     *
     * <p>A component is either a simple {@link Binding} (a type-pattern variable) or another
     * nested {@link Record} deconstruction, recursively.
     */
    public sealed interface Pattern
        permits Pattern.Binding, Pattern.Record {

        /**
         * The type this component's value is checked/deconstructed as.
         *
         * @return the {@link TypeUsage}
         */
        TypeUsage type();

        /**
         * A simple binding component, e.g. {@code int x}.
         *
         * @param type the declared type of the binding
         * @param name the binding variable name
         */
        record Binding(TypeUsage type, String name) implements Pattern {

            @Unmarshal
            public Binding(final Marshaller marshaller,
                           final Marshalled<TypeUsage> type,
                           final String name) {
                this(marshaller.unmarshal(type), name);
            }

            @Marshal
            public void destructor(final Marshaller marshaller,
                                   final Out<Marshalled<TypeUsage>> type,
                                   final Out<String> name) {
                type.set(marshaller.marshal(this.type));
                name.set(this.name);
            }

            static {
                Marshalling.register(Binding.class, MethodHandles.lookup());
            }
        }

        /**
         * A nested record-deconstruction component, e.g. {@code Point(int x, int y)} nested
         * inside another deconstruction pattern.
         *
         * @param type       the deconstructed record type
         * @param components the nested component {@link Pattern}s, in declaration order
         */
        record Record(TypeUsage type, List<Pattern> components) implements Pattern {

            public Record {
                components = List.copyOf(components);
            }

            @Unmarshal
            public Record(final Marshaller marshaller,
                          final Marshalled<TypeUsage> type,
                          final Stream<Marshalled<Pattern>> components) {
                this(marshaller.unmarshal(type), components.map(marshaller::unmarshal).toList());
            }

            @Marshal
            public void destructor(final Marshaller marshaller,
                                   final Out<Marshalled<TypeUsage>> type,
                                   final Out<Stream<Marshalled<Pattern>>> components) {
                type.set(marshaller.marshal(this.type));
                components.set(this.components.stream().map(marshaller::marshal));
            }

            static {
                Marshalling.register(Record.class, MethodHandles.lookup());
            }
        }
    }

    /**
     * The expression being tested.
     */
    private final Expression expression;

    /**
     * The resolved type on the right-hand side of {@code instanceof}.
     */
    private final TypeUsage type;

    /**
     * The optional pattern-binding variable name (Java 16+ pattern matching).
     */
    private final Optional<String> bindingVariable;

    /**
     * The nested component {@link Pattern}s of a record-deconstruction pattern
     * (Java 21+), empty unless this {@link InstanceOf} represents one.
     */
    private final List<Pattern> components;

    private InstanceOf(final Expression expression,
                       final TypeUsage type,
                       final Optional<String> bindingVariable,
                       final List<Pattern> components) {
        super(Objects.requireNonNull(expression, "expression must not be null").codeModel());
        this.expression = expression;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.bindingVariable = bindingVariable == null ? Optional.empty() : bindingVariable;
        this.components = components == null ? List.of() : List.copyOf(components);
    }

    @Unmarshal
    public InstanceOf(@Bound final CodeModel codeModel,
                      final Marshaller marshaller,
                      final Stream<Marshalled<Trait>> traits,
                      final Marshalled<Expression> expression,
                      final Marshalled<TypeUsage> type,
                      final Optional<String> bindingVariable,
                      final Stream<Marshalled<Pattern>> components) {
        super(codeModel, marshaller, traits);
        this.expression = marshaller.unmarshal(expression);
        this.type = marshaller.unmarshal(type);
        this.bindingVariable = bindingVariable == null ? Optional.empty() : bindingVariable;
        this.components = components == null ? List.of() : components.map(marshaller::unmarshal).toList();
    }

    @Marshal
    public void destructor(final Marshaller marshaller,
                           final Out<Stream<Marshalled<Trait>>> traits,
                           final Out<Marshalled<Expression>> expression,
                           final Out<Marshalled<TypeUsage>> type,
                           final Out<Optional<String>> bindingVariable,
                           final Out<Stream<Marshalled<Pattern>>> components) {
        super.destructor(marshaller, traits);
        expression.set(marshaller.marshal(this.expression));
        type.set(marshaller.marshal(this.type));
        bindingVariable.set(this.bindingVariable);
        components.set(this.components.stream().map(marshaller::marshal));
    }

    /**
     * Obtains the expression being tested.
     *
     * @return the tested {@link Expression}
     */
    public Expression expression() {
        return this.expression;
    }

    /**
     * Obtains the resolved type on the right-hand side of {@code instanceof}.
     *
     * @return the {@link TypeUsage}
     */
    public TypeUsage checkedType() {
        return this.type;
    }

    /**
     * Obtains the optional pattern-binding variable name (Java 16+ pattern matching).
     *
     * @return an {@link Optional} binding variable name, or {@link Optional#empty()} for classic {@code instanceof}
     */
    public Optional<String> bindingVariable() {
        return this.bindingVariable;
    }

    /**
     * Obtains the nested component {@link Pattern}s of a record-deconstruction pattern
     * (Java 21+ record patterns), e.g. {@code int x}, {@code int y} in
     * {@code case Point(int x, int y) ->}. Empty unless this {@link InstanceOf} represents a
     * deconstruction pattern.
     *
     * @return the component {@link Pattern}s, in declaration order
     */
    public List<Pattern> components() {
        return this.components;
    }

    @Override
    public Stream<? extends Composite> compositeChildren() {
        return Stream.concat(Stream.of(expression, type), components.stream().flatMap(InstanceOf::patternTypes));
    }

    private static Stream<TypeUsage> patternTypes(final Pattern pattern) {
        return switch (pattern) {
            case Pattern.Binding(var type, var name) -> Stream.of(type);
            case Pattern.Record(var type, var nested) ->
                Stream.concat(Stream.of(type), nested.stream().flatMap(InstanceOf::patternTypes));
        };
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof InstanceOf other
            && Objects.equals(this.expression, other.expression)
            && Objects.equals(this.type, other.type)
            && Objects.equals(this.bindingVariable, other.bindingVariable)
            && Objects.equals(this.components, other.components)
            && super.equals(other);
    }

    /**
     * Creates an {@link InstanceOf} expression.
     *
     * @param expression      the expression being tested
     * @param type            the resolved {@link TypeUsage} on the right-hand side
     * @param bindingVariable the optional pattern-binding variable name
     * @return a new {@link InstanceOf}
     */
    public static InstanceOf of(final Expression expression,
                                final TypeUsage type,
                                final Optional<String> bindingVariable) {
        return new InstanceOf(expression, type, bindingVariable, List.of());
    }

    /**
     * Creates a classic (non-pattern) {@link InstanceOf} expression.
     *
     * @param expression the expression being tested
     * @param type       the resolved {@link TypeUsage} on the right-hand side
     * @return a new {@link InstanceOf}
     */
    public static InstanceOf of(final Expression expression, final TypeUsage type) {
        return new InstanceOf(expression, type, Optional.empty(), List.of());
    }

    /**
     * Creates a record-deconstruction {@link InstanceOf} expression (Java 21+ record patterns),
     * e.g. {@code case Point(int x, int y) ->}.
     *
     * @param expression the expression being tested
     * @param type       the deconstructed record {@link TypeUsage}
     * @param components the nested component {@link Pattern}s, in declaration order
     * @return a new {@link InstanceOf}
     */
    public static InstanceOf ofDeconstruction(final Expression expression,
                                              final TypeUsage type,
                                              final List<Pattern> components) {
        return new InstanceOf(expression, type, Optional.empty(), components);
    }

    static {
        Marshalling.register(InstanceOf.class, MethodHandles.lookup());
    }
}
