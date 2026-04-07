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

import build.codemodel.expression.Addition;
import build.codemodel.expression.BooleanLiteral;
import build.codemodel.expression.Cast;
import build.codemodel.expression.Conjunction;
import build.codemodel.expression.Disjunction;
import build.codemodel.expression.Division;
import build.codemodel.expression.EqualTo;
import build.codemodel.expression.Expression;
import build.codemodel.expression.ExpressionType;
import build.codemodel.expression.GreaterThan;
import build.codemodel.expression.GreaterThanOrEqualTo;
import build.codemodel.expression.LessThan;
import build.codemodel.expression.LessThanOrEqualTo;
import build.codemodel.expression.Modulo;
import build.codemodel.expression.Multiplication;
import build.codemodel.expression.Negation;
import build.codemodel.expression.Negative;
import build.codemodel.expression.NotEqualTo;
import build.codemodel.expression.NumericLiteral;
import build.codemodel.expression.StringLiteral;
import build.codemodel.expression.Subtraction;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.imperative.Block;
import build.codemodel.imperative.Statement;
import build.codemodel.jdk.expression.ArrayAccess;
import build.codemodel.jdk.expression.BitwiseBinary;
import build.codemodel.jdk.expression.CharLiteral;
import build.codemodel.jdk.expression.CompoundAssignment;
import build.codemodel.jdk.expression.FieldAccess;
import build.codemodel.jdk.expression.Identifier;
import build.codemodel.jdk.expression.InstanceOf;
import build.codemodel.jdk.expression.Lambda;
import build.codemodel.jdk.expression.LambdaParameter;
import build.codemodel.jdk.expression.MethodInvocation;
import build.codemodel.jdk.expression.MethodReference;
import build.codemodel.jdk.expression.NewArray;
import build.codemodel.jdk.expression.NewObject;
import build.codemodel.jdk.expression.NullLiteral;
import build.codemodel.jdk.expression.Parenthesized;
import build.codemodel.jdk.expression.PostfixUnary;
import build.codemodel.jdk.expression.PrefixUnary;
import build.codemodel.jdk.expression.SwitchExpression;
import build.codemodel.jdk.expression.Ternary;
import build.codemodel.jdk.expression.UnknownExpression;
import build.codemodel.jdk.statement.ExpressionStatement;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Converts {@link ExpressionTree} nodes from the javac tree API to model {@link Expression} nodes.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class JdkExpressionConverter
    extends SimpleTreeVisitor<Expression, Void> {

    private final CodeModel codeModel;
    private JdkStatementConverter stmtConverter;
    private Trees trees;
    private CompilationUnitTree compilationUnit;
    private Function<TypeMirror, TypeUsage> typeResolver;

    /**
     * Creates a {@link JdkExpressionConverter}.
     *
     * @param codeModel the {@link CodeModel} used to construct expression nodes
     */
    public JdkExpressionConverter(final CodeModel codeModel) {
        this.codeModel = codeModel;
    }

    /**
     * Wires the companion {@link JdkStatementConverter} for converting lambda bodies and other
     * statement-bearing expressions.
     *
     * @param stmtConverter the {@link JdkStatementConverter} to use
     */
    public void setStmtConverter(final JdkStatementConverter stmtConverter) {
        this.stmtConverter = stmtConverter;
    }

    /**
     * Provides the type-resolution context required to tag expressions with their
     * {@link build.codemodel.expression.ExpressionType} trait.
     *
     * @param trees           the javac {@link Trees} utility
     * @param compilationUnit the {@link CompilationUnitTree} being processed
     * @param typeResolver    a function mapping a {@link TypeMirror} to a {@link TypeUsage}
     */
    public void setTypeContext(final Trees trees,
                               final CompilationUnitTree compilationUnit,
                               final Function<TypeMirror, TypeUsage> typeResolver) {
        this.trees = trees;
        this.compilationUnit = compilationUnit;
        this.typeResolver = typeResolver;
    }

    /**
     * Converts the given {@link ExpressionTree} to a model {@link Expression}.
     * Returns {@link NullLiteral} for a {@code null} tree.
     * Tags the resulting expression with its resolved {@link build.codemodel.foundation.usage.TypeUsage}
     * via the {@link ExpressionType} trait when type context is available.
     */
    public Expression convert(final ExpressionTree tree) {
        if (tree == null) {
            return NullLiteral.of(codeModel);
        }
        final var expr = tree.accept(this, null);
        tagExpressionType(tree, expr);
        return expr;
    }

    private void tagExpressionType(final ExpressionTree tree, final Expression expr) {
        if (trees == null || compilationUnit == null || typeResolver == null) {
            return;
        }
        try {
            final var path = TreePath.getPath(compilationUnit, tree);
            if (path == null) {
                return;
            }
            final var typeMirror = trees.getTypeMirror(path);
            if (typeMirror == null
                    || typeMirror.getKind() == TypeKind.ERROR
                    || typeMirror.getKind() == TypeKind.NONE
                    || typeMirror.getKind() == TypeKind.OTHER) {
                return;
            }
            expr.addTrait(ExpressionType.of(typeResolver.apply(typeMirror)));
        } catch (final Exception e) {
            // type resolution is best-effort
        }
    }

    @Override
    public Expression visitLiteral(final LiteralTree t, final Void v) {
        return switch (t.getKind()) {
            case STRING_LITERAL -> StringLiteral.of(codeModel, (String) t.getValue());
            case INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL ->
                NumericLiteral.of(codeModel, (Number) t.getValue());
            case BOOLEAN_LITERAL -> BooleanLiteral.of(codeModel, (Boolean) t.getValue());
            case CHAR_LITERAL -> CharLiteral.of(codeModel, (char) (Character) t.getValue());
            case NULL_LITERAL -> NullLiteral.of(codeModel);
            default -> UnknownExpression.of(codeModel);
        };
    }

    @Override
    public Expression visitIdentifier(final IdentifierTree t, final Void v) {
        return Identifier.of(codeModel, t.getName().toString());
    }

    @Override
    public Expression visitMethodInvocation(final MethodInvocationTree t, final Void v) {
        final Expression target;
        final String methodName;
        Optional<TypeUsage> receiverType = Optional.empty();
        if (t.getMethodSelect() instanceof MemberSelectTree ms) {
            final var receiverExpr = ms.getExpression();
            target = convert(receiverExpr);
            methodName = ms.getIdentifier().toString();
            receiverType = resolveReceiverType(receiverExpr);
        } else {
            target = null;
            methodName = t.getMethodSelect().toString();
        }
        final var args = t.getArguments().stream()
            .map(this::convert)
            .toList();
        return MethodInvocation.of(codeModel, Optional.ofNullable(target), methodName, args.stream(),
            receiverType);
    }

    private Optional<TypeUsage> resolveReceiverType(final ExpressionTree receiverExpr) {
        if (trees == null || compilationUnit == null || typeResolver == null) {
            return Optional.empty();
        }
        try {
            final var path = TreePath.getPath(compilationUnit, receiverExpr);
            if (path == null) {
                return Optional.empty();
            }
            final var typeMirror = trees.getTypeMirror(path);
            if (typeMirror == null
                    || typeMirror.getKind() == TypeKind.ERROR
                    || typeMirror.getKind() == TypeKind.NONE
                    || typeMirror.getKind() == TypeKind.OTHER) {
                return Optional.empty();
            }
            return Optional.of(typeResolver.apply(typeMirror));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Expression visitMemberSelect(final MemberSelectTree t, final Void v) {
        final var receiverExpr = t.getExpression();
        return FieldAccess.of(
            convert(receiverExpr),
            t.getIdentifier().toString(),
            resolveReceiverType(receiverExpr));
    }

    @Override
    public Expression visitNewClass(final NewClassTree t, final Void v) {
        final var args = t.getArguments().stream()
            .map(this::convert)
            .toList();
        final Expression typeExpr;
        final List<Expression> typeArgs;
        if (t.getIdentifier() instanceof ParameterizedTypeTree pt) {
            typeExpr = Identifier.of(codeModel, pt.getType().toString());
            typeArgs = pt.getTypeArguments().stream()
                .map(typeArg -> (Expression) Identifier.of(codeModel, typeArg.toString()))
                .toList();
        } else {
            typeExpr = Identifier.of(codeModel, t.getIdentifier().toString());
            typeArgs = List.of();
        }
        return NewObject.of(codeModel, typeExpr, args.stream(), typeArgs.stream());
    }

    @Override
    public Expression visitNewArray(final NewArrayTree t, final Void v) {
        final List<Expression> dims = t.getDimensions() == null
            ? List.of()
            : t.getDimensions().stream().map(this::convert).toList();
        final String elementTypeName = t.getType() == null ? "?" : t.getType().toString();
        return NewArray.of(codeModel, Identifier.of(codeModel, elementTypeName), dims.stream());
    }

    @Override
    public Expression visitArrayAccess(final ArrayAccessTree t, final Void v) {
        final var arrayExpr = t.getExpression();
        return ArrayAccess.of(
            convert(arrayExpr),
            convert(t.getIndex()),
            resolveReceiverType(arrayExpr));
    }

    @Override
    public Expression visitConditionalExpression(final ConditionalExpressionTree t, final Void v) {
        return Ternary.of(
            convert(t.getCondition()),
            convert(t.getTrueExpression()),
            convert(t.getFalseExpression()));
    }

    @Override
    public Expression visitAssignment(final AssignmentTree t, final Void v) {
        return CompoundAssignment.of(codeModel, "ASSIGNMENT",
            convert(t.getVariable()),
            convert(t.getExpression()));
    }

    @Override
    public Expression visitCompoundAssignment(final CompoundAssignmentTree t, final Void v) {
        return CompoundAssignment.of(codeModel,
            t.getKind().toString(),
            convert(t.getVariable()),
            convert(t.getExpression()));
    }

    @Override
    public Expression visitBinary(final BinaryTree t, final Void v) {
        final var left = convert(t.getLeftOperand());
        final var right = convert(t.getRightOperand());
        return switch (t.getKind()) {
            case PLUS -> Addition.of(left, right);
            case MINUS -> Subtraction.of(left, right);
            case MULTIPLY -> Multiplication.of(left, right);
            case DIVIDE -> Division.of(left, right);
            case REMAINDER -> Modulo.of(left, right);
            case EQUAL_TO -> EqualTo.of(left, right);
            case NOT_EQUAL_TO -> NotEqualTo.of(left, right);
            case LESS_THAN -> LessThan.of(left, right);
            case LESS_THAN_EQUAL -> LessThanOrEqualTo.of(left, right);
            case GREATER_THAN -> GreaterThan.of(left, right);
            case GREATER_THAN_EQUAL -> GreaterThanOrEqualTo.of(left, right);
            case CONDITIONAL_AND -> Conjunction.of(left, right);
            case CONDITIONAL_OR -> Disjunction.of(left, right);
            case AND, OR, XOR, LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT ->
                BitwiseBinary.of(codeModel, t.getKind().toString(), left, right);
            default -> UnknownExpression.of(codeModel);
        };
    }

    @Override
    public Expression visitUnary(final UnaryTree t, final Void v) {
        final var operand = convert(t.getExpression());
        return switch (t.getKind()) {
            case PREFIX_INCREMENT, PREFIX_DECREMENT, BITWISE_COMPLEMENT, UNARY_PLUS ->
                PrefixUnary.of(codeModel, t.getKind().toString(), operand);
            case POSTFIX_INCREMENT, POSTFIX_DECREMENT ->
                PostfixUnary.of(codeModel, t.getKind().toString(), operand);
            case UNARY_MINUS -> Negative.of(operand);
            case LOGICAL_COMPLEMENT -> Negation.of(operand);
            default -> UnknownExpression.of(codeModel);
        };
    }

    @Override
    public Expression visitParenthesized(final ParenthesizedTree t, final Void v) {
        return Parenthesized.of(convert(t.getExpression()));
    }

    @Override
    public Expression visitTypeCast(final TypeCastTree t, final Void v) {
        final TypeUsage targetType;
        if (trees != null && compilationUnit != null && typeResolver != null) {
            try {
                final var path = TreePath.getPath(compilationUnit, t.getType());
                if (path != null) {
                    final var typeMirror = trees.getTypeMirror(path);
                    if (typeMirror != null
                            && typeMirror.getKind() != TypeKind.ERROR
                            && typeMirror.getKind() != TypeKind.NONE
                            && typeMirror.getKind() != TypeKind.OTHER) {
                        targetType = typeResolver.apply(typeMirror);
                    } else {
                        targetType = UnknownTypeUsage.create(codeModel);
                    }
                } else {
                    targetType = UnknownTypeUsage.create(codeModel);
                }
            } catch (final Exception e) {
                return Cast.of(UnknownTypeUsage.create(codeModel), convert(t.getExpression()));
            }
        } else {
            targetType = UnknownTypeUsage.create(codeModel);
        }
        return Cast.of(targetType, convert(t.getExpression()));
    }

    @Override
    public Expression visitInstanceOf(final InstanceOfTree t, final Void v) {
        final Optional<String> bindingVariable;
        if (t.getPattern() instanceof BindingPatternTree bp) {
            bindingVariable = Optional.of(bp.getVariable().getName().toString());
        } else {
            bindingVariable = Optional.empty();
        }
        return InstanceOf.of(
            convert(t.getExpression()),
            Identifier.of(codeModel, t.getType().toString()),
            bindingVariable);
    }

    @Override
    public Expression visitLambdaExpression(final LambdaExpressionTree t, final Void v) {
        final Statement body;
        if (t.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
            body = ExpressionStatement.of(convert((ExpressionTree) t.getBody()));
        } else if (stmtConverter != null) {
            body = stmtConverter.convert((StatementTree) t.getBody());
        } else {
            body = Block.empty(codeModel);
        }
        final var params = t.getParameters().stream()
            .map(p -> new LambdaParameter(p.getType().toString(), p.getName().toString()))
            .toList();
        return Lambda.of(codeModel, params, body);
    }

    @Override
    public Expression visitSwitchExpression(final SwitchExpressionTree t, final Void v) {
        if (stmtConverter == null) {
            return UnknownExpression.of(codeModel);
        }
        final var cases = t.getCases().stream().map(stmtConverter::convertCase).toList();
        return SwitchExpression.of(convert(t.getExpression()), cases.stream());
    }

    @Override
    public Expression visitMemberReference(final MemberReferenceTree t, final Void v) {
        final var qualifierExpr = t.getQualifierExpression();
        return MethodReference.of(
            convert(qualifierExpr),
            t.getName().toString(),
            resolveReceiverType(qualifierExpr));
    }

    @Override
    protected Expression defaultAction(final Tree node, final Void v) {
        return UnknownExpression.of(codeModel);
    }
}
