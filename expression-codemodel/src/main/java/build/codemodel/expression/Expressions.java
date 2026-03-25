package build.codemodel.expression;

/*-
 * #%L
 * Expression Code Model
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

import build.codemodel.foundation.CodeModel;
import build.codemodel.expression.naming.FunctionName;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link CodeModel} wrapper for working with various {@link Expression}s.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Expressions {

    /**
     * The {@link CodeModel} for which {@link Expression}s will be created.
     */
    private final CodeModel codeModel;

    /**
     * Constructs an {@link Expressions} helper for the specified {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel}
     */
    public Expressions(final CodeModel codeModel) {
        this.codeModel = Objects.requireNonNull(codeModel, "The CodeModel must not be null");
    }

    /**
     * Obtains the {@link CodeModel} for which {@link Expression}s will be created.
     *
     * @return the {@link CodeModel}
     */
    public CodeModel codeModel() {
        return codeModel;
    }

    /**
     * Creates a {@link Literal} for the specified <i>value</i>.
     *
     * @param <T>   the type of value
     * @param value the value
     * @return a {@link Literal}
     */
    @SuppressWarnings("unchecked")
    public <T> Literal<T> valueOf(final T value) {

        // attempt to use an appropriate representation of the Value
        if (value instanceof Literal<?> literal) {
            return (Literal<T>) literal;
        }
        else if (value instanceof Boolean booleanValue) {
            return (Literal<T>) valueOf(booleanValue);
        }

        return Literal.of(this.codeModel, value);
    }

    /**
     * Creates a {@link BooleanLiteral} representation for the specified {@link Boolean}.
     *
     * @param value the {@link Boolean} value
     * @return a {@link BooleanLiteral}
     */
    public BooleanLiteral valueOf(final boolean value) {
        return BooleanLiteral.of(this.codeModel, value);
    }

    /**
     * Creates a {@link NumericLiteral} representation for the specified {@link Boolean}.
     *
     * @param value the {@link Number} value
     * @return a {@link NumericLiteral}
     */
    public NumericLiteral valueOf(final Number value) {
        return NumericLiteral.of(this.codeModel, value);
    }

    /**
     * Creates a {@link Conjunction} of two {@link LogicalExpression}s.
     *
     * @param left  the left-hand-side {@link LogicalExpression}
     * @param right the right-hand-side {@link LogicalExpression}
     * @return a new {@link Conjunction}
     */
    public Conjunction and(final LogicalExpression left,
                           final LogicalExpression right) {

        return Conjunction.of(left, right);
    }

    /**
     * Creates a {@link Disjunction} of two {@link LogicalExpression}s.
     *
     * @param left  the left-hand-side {@link LogicalExpression}
     * @param right the right-hand-side {@link LogicalExpression}
     * @return a new {@link Disjunction}
     */
    public Disjunction or(final LogicalExpression left,
                          final LogicalExpression right) {

        return Disjunction.of(left, right);
    }

    /**
     * Creates a {@link Negation} of a {@link LogicalExpression}s.
     *
     * @param expression the {@link LogicalExpression}
     * @return a new {@link Negation}
     */
    public Negation not(final LogicalExpression expression) {
        return Negation.of(expression);
    }

    /**
     * Creates an {@link EqualTo} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link EqualTo}
     */
    public EqualTo equalTo(final Expression left,
                           final Expression right) {

        return EqualTo.of(left, right);
    }

    /**
     * Creates a {@link NotEqualTo} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link NotEqualTo}
     */
    public NotEqualTo notEqualTo(final Expression left,
                                 final Expression right) {

        return NotEqualTo.of(left, right);
    }

    /**
     * Creates a {@link LessThan} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link LessThan}
     */
    public LessThan lessThan(final Expression left,
                             final Expression right) {

        return LessThan.of(left, right);
    }

    /**
     * Creates a {@link LessThanOrEqualTo} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link LessThanOrEqualTo}
     */
    public LessThanOrEqualTo lessThanOrEqualTo(final Expression left,
                                               final Expression right) {

        return LessThanOrEqualTo.of(left, right);
    }

    /**
     * Creates a {@link GreaterThan} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link GreaterThan}
     */
    public GreaterThan greaterThan(final Expression left,
                                   final Expression right) {

        return GreaterThan.of(left, right);
    }

    /**
     * Creates a {@link GreaterThanOrEqualTo} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link GreaterThanOrEqualTo}
     */
    public GreaterThanOrEqualTo greaterThanOrEqualTo(final Expression left,
                                                     final Expression right) {

        return GreaterThanOrEqualTo.of(left, right);
    }

    /**
     * Creates an {@link Addition} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link Addition}
     */
    public Addition add(final Expression left,
                        final Expression right) {

        return Addition.of(left, right);
    }

    /**
     * Creates a {@link Subtraction} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link Subtraction}
     */
    public Subtraction subtract(final Expression left,
                                final Expression right) {

        return Subtraction.of(left, right);
    }

    /**
     * Creates a {@link Multiplication} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link Multiplication}
     */
    public Multiplication multiply(final Expression left,
                                   final Expression right) {

        return Multiplication.of(left, right);
    }

    /**
     * Creates a {@link Division} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link Division}
     */
    public Division divide(final Expression left,
                           final Expression right) {

        return Division.of(left, right);
    }

    /**
     * Creates a {@link Modulo} of two {@link Expression}s.
     *
     * @param left  the left-hand-side {@link Expression}
     * @param right the right-hand-side {@link Expression}
     * @return a new {@link Modulo}
     */
    public Modulo modulo(final Expression left,
                         final Expression right) {

        return Modulo.of(left, right);
    }

    /**
     * Creates a {@link Negative} of an {@link Expression}.
     *
     * @param expression the left-hand-side {@link Expression}
     * @return a new {@link Negative}
     */
    public Negative negative(final Expression expression) {
        return Negative.of(expression);
    }

    /**
     * Creates a {@link FunctionUsage} based on the application of the specified {@link FunctionName}.
     *
     * @param name      the {@link FunctionName}
     * @param arguments the actual-parameter {@link Expression} arguments
     * @return a new {@link FunctionUsage}
     */
    public FunctionUsage apply(final FunctionName name,
                               final Expression... arguments) {

        return FunctionUsage.of(this.codeModel, name, arguments);
    }

    /**
     * Creates a {@link FunctionUsage} based on the application of the specified {@link FunctionName}.
     *
     * @param name      the {@link FunctionName}
     * @param arguments the actual-parameter {@link Expression} arguments
     * @return a new {@link FunctionUsage}
     */
    public FunctionUsage apply(final FunctionName name,
                               final Stream<Expression> arguments) {

        return FunctionUsage.of(this.codeModel, name, arguments);
    }

    /**
     * Creates a {@link VariableUsage} for the specified {@link VariableName} defined by the {@link Optional}ly
     * provided {@link TypeUsage}.
     *
     * @param name the {@link VariableName}
     * @param type the {@link Optional} {@link TypeUsage}
     * @return a new {@link VariableUsage}
     */
    public VariableUsage variableOf(final VariableName name,
                                    final Optional<TypeUsage> type) {

        return VariableUsage.of(this.codeModel, name, type);
    }

    /**
     * Creates a {@link VariableUsage} for the specified {@link VariableName} defined by the provided
     * {@link TypeUsage}.
     *
     * @param name the {@link VariableName}
     * @param type the {@link TypeUsage}
     * @return a new {@link VariableUsage}
     */
    public VariableUsage variableOf(final VariableName name,
                                    final TypeUsage type) {

        return VariableUsage.of(this.codeModel, name, type);
    }

    /**
     * Creates a {@link VariableUsage} for the specified {@link VariableName}, without a defined {@link TypeUsage}.
     *
     * @param name the {@link VariableName}
     * @return a new {@link VariableUsage}
     */
    public VariableUsage variableOf(final VariableName name) {
        return VariableUsage.of(this.codeModel, name);
    }

    /**
     * Creates a {@link Cast} {@link Expression} for a defined {@link TypeUsage}.
     *
     * @param targetType  the {@link TypeUsage} specifying the target <i>Type</i>
     * @param expression the {@link Expression} to cast
     * @return a new {@link VariableUsage}
     */
    public Cast cast(final TypeUsage targetType,
                     final Expression expression) {

        return Cast.of(targetType, expression);
    }
}
