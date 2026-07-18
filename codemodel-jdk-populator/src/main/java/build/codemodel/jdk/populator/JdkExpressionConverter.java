package build.codemodel.jdk.populator;

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
import build.codemodel.foundation.descriptor.CallableDescriptor;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.imperative.Block;
import build.codemodel.imperative.Statement;
import build.codemodel.jdk.expression.ArrayAccess;
import build.codemodel.jdk.expression.AssignmentOperator;
import build.codemodel.jdk.expression.BitwiseBinary;
import build.codemodel.jdk.expression.BitwiseOperator;
import build.codemodel.jdk.expression.CharLiteral;
import build.codemodel.jdk.expression.ClassLiteral;
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
import build.codemodel.jdk.expression.PostfixOperator;
import build.codemodel.jdk.expression.PostfixUnary;
import build.codemodel.jdk.expression.PrefixOperator;
import build.codemodel.jdk.expression.PrefixUnary;
import build.codemodel.jdk.expression.ResolvedMethod;
import build.codemodel.jdk.expression.SwitchExpression;
import build.codemodel.jdk.expression.Symbol;
import build.codemodel.jdk.expression.Ternary;
import build.codemodel.jdk.expression.UnknownExpression;
import build.codemodel.jdk.populator.descriptor.SourceLocation;
import build.codemodel.jdk.statement.CatchClause;
import build.codemodel.jdk.statement.EnhancedFor;
import build.codemodel.jdk.statement.ExpressionStatement;
import build.codemodel.jdk.statement.LocalVariableDeclaration;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DeconstructionPatternTree;
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
import com.sun.source.tree.PatternTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

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
    private Function<TypeElement, TypeName> typeNameResolver;
    private TypeUsage enclosingType;

    /**
     * Maps a resolved javac {@link Element} for a local variable to the {@link LocalVariableDeclaration}
     * that declared it, populated as {@link JdkStatementConverter} converts each declaration so that later
     * identifier usages within the same body conversion can be linked back to their declaring statement.
     */
    private final Map<Element, LocalVariableDeclaration> localVariableDeclarations = new HashMap<>();

    /**
     * Maps a resolved javac {@link Element} for an enhanced-for loop variable to the
     * {@link EnhancedFor} that declared it, populated as {@link JdkStatementConverter} converts
     * each enhanced-for loop so that later identifier usages within the same body conversion can
     * be linked back to their declaring loop.
     */
    private final Map<Element, EnhancedFor> enhancedForDeclarations = new HashMap<>();

    /**
     * Maps a resolved javac {@link Element} for a catch-clause exception parameter to the
     * {@link CatchClause} that declared it, populated as {@link JdkStatementConverter} converts
     * each catch clause so that later identifier usages within the same body conversion can be
     * linked back to their declaring clause.
     */
    private final Map<Element, CatchClause> catchParameterDeclarations = new HashMap<>();

    /**
     * Maps a resolved javac {@link Element} for an {@code instanceof}/switch pattern binding
     * variable to the {@link InstanceOf} that declared it, populated as each pattern test is
     * converted so that later identifier usages within the same body conversion can be linked
     * back to their declaring test.
     */
    private final Map<Element, InstanceOf> patternBindingDeclarations = new HashMap<>();

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
     * @param trees            the javac {@link Trees} utility
     * @param compilationUnit  the {@link CompilationUnitTree} being processed
     * @param typeResolver     a function mapping a {@link TypeMirror} to a {@link TypeUsage}
     * @param typeNameResolver a function mapping a {@link TypeElement} to its module-qualified
     *                         {@link TypeName}, matching how that type's descriptor was registered
     */
    public void setTypeContext(final Trees trees,
                               final CompilationUnitTree compilationUnit,
                               final Function<TypeMirror, TypeUsage> typeResolver,
                               final Function<TypeElement, TypeName> typeNameResolver) {
        this.trees = trees;
        this.compilationUnit = compilationUnit;
        this.typeResolver = typeResolver;
        this.typeNameResolver = typeNameResolver;
    }

    /**
     * Sets the type of the class currently being processed, used as the implicit receiver type for
     * unqualified method calls (e.g. {@code foo()} where the receiver is {@code this}).
     *
     * @param enclosingType the {@link TypeUsage} of the enclosing class
     */
    public void setEnclosingType(final TypeUsage enclosingType) {
        this.enclosingType = enclosingType;
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
        final var identifier = Identifier.of(codeModel, t.getName().toString());
        resolveSymbol(t).ifPresent(identifier::addTrait);
        addSourceLocation(t).ifPresent(identifier::addTrait);
        return identifier;
    }

    Optional<SourceLocation.FilePosition> addSourceLocation(final Tree tree) {
        if (trees == null || compilationUnit == null) {
            return Optional.empty();
        }
        final var srcPositions = trees.getSourcePositions();
        final var start = srcPositions.getStartPosition(compilationUnit, tree);
        final var end = srcPositions.getEndPosition(compilationUnit, tree);
        if (start == Diagnostic.NOPOS) {
            return Optional.empty();
        }
        return Optional.of(SourceLocation.filePosition(compilationUnit.getSourceFile().toUri(), start, end));
    }

    /**
     * Records that the given local variable {@link VariableTree} declares {@code declaration}, so that
     * later {@link Symbol.LocalVariable} resolution can link an identifier usage back to it.
     *
     * @param tree        the {@link VariableTree} declaring the local variable
     * @param declaration the {@link LocalVariableDeclaration} converted from {@code tree}
     */
    void registerLocalVariableDeclaration(final VariableTree tree, final LocalVariableDeclaration declaration) {
        if (trees == null || compilationUnit == null) {
            return;
        }
        final var path = trees.getPath(compilationUnit, tree);
        if (path == null) {
            return;
        }
        final var element = trees.getElement(path);
        if (element != null) {
            localVariableDeclarations.put(element, declaration);
        }
    }

    /**
     * Records that the given enhanced-for {@link VariableTree} declares {@code loop}'s loop
     * variable, so that later {@link Symbol.EnhancedForVariable} resolution can link an identifier
     * usage back to it.
     *
     * @param tree the {@link VariableTree} declaring the enhanced-for loop variable
     * @param loop the {@link EnhancedFor} converted from the enclosing loop
     */
    void registerEnhancedForVariable(final VariableTree tree, final EnhancedFor loop) {
        elementOf(tree).ifPresent(element -> enhancedForDeclarations.put(element, loop));
    }

    /**
     * Records that the given catch parameter {@link VariableTree} declares {@code catchClause}'s
     * exception parameter, so that later {@link Symbol.CatchParameter} resolution can link an
     * identifier usage back to it.
     *
     * @param tree        the {@link VariableTree} declaring the catch parameter
     * @param catchClause the {@link CatchClause} converted from the enclosing catch clause
     */
    void registerCatchParameter(final VariableTree tree, final CatchClause catchClause) {
        elementOf(tree).ifPresent(element -> catchParameterDeclarations.put(element, catchClause));
    }

    /**
     * Records that the given pattern-binding {@link VariableTree} declares {@code instanceOf}'s
     * binding variable, so that later {@link Symbol.PatternBinding} resolution can link an
     * identifier usage back to it.
     *
     * @param tree       the {@link VariableTree} declaring the pattern-binding variable
     * @param instanceOf the {@link InstanceOf} pattern test that declared this binding
     */
    void registerPatternBinding(final VariableTree tree, final InstanceOf instanceOf) {
        elementOf(tree).ifPresent(element -> patternBindingDeclarations.put(element, instanceOf));
    }

    private Optional<Element> elementOf(final VariableTree tree) {
        if (trees == null || compilationUnit == null) {
            return Optional.empty();
        }
        final var path = trees.getPath(compilationUnit, tree);
        return path == null ? Optional.empty() : Optional.ofNullable(trees.getElement(path));
    }

    private Optional<Symbol> resolveSymbol(final IdentifierTree t) {
        if (trees == null || compilationUnit == null) {
            return Optional.empty();
        }
        final var name = t.getName().toString();
        final var path = trees.getPath(compilationUnit, t);
        if (path == null) {
            return Optional.empty();
        }
        final var typeMirror = trees.getTypeMirror(path);
        final TypeUsage typeUsage = typeMirror != null && typeMirror.getKind() != TypeKind.ERROR
            ? typeResolver.apply(typeMirror)
            : UnknownTypeUsage.create(codeModel);

        if ("this".equals(name)) {
            return Optional.of(new Symbol.ThisReference(typeUsage));
        }
        if ("super".equals(name)) {
            return Optional.of(new Symbol.SuperReference(typeUsage));
        }

        final Element element = trees.getElement(path);
        if (element == null) {
            return Optional.empty();
        }
        return switch (element.getKind()) {
            case LOCAL_VARIABLE -> Optional.<Symbol>of(enhancedForDeclarations.containsKey(element)
                ? new Symbol.EnhancedForVariable(typeUsage, Optional.of(enhancedForDeclarations.get(element)))
                : new Symbol.LocalVariable(typeUsage, Optional.ofNullable(localVariableDeclarations.get(element))));
            case EXCEPTION_PARAMETER -> Optional.<Symbol>of(new Symbol.CatchParameter(
                typeUsage, Optional.ofNullable(catchParameterDeclarations.get(element))));
            case BINDING_VARIABLE -> Optional.<Symbol>of(new Symbol.PatternBinding(
                typeUsage, Optional.ofNullable(patternBindingDeclarations.get(element))));
            case PARAMETER -> resolveParameter(element).map(Symbol.class::cast);
            case FIELD, ENUM_CONSTANT -> resolveField(element, typeUsage).map(Symbol.class::cast);
            case CLASS, INTERFACE, ENUM, ANNOTATION_TYPE, RECORD ->
                Optional.<Symbol>of(new Symbol.TypeReference(typeUsage));
            default -> Optional.empty();
        };
    }

    private Optional<Symbol.Field> resolveField(final Element element, final TypeUsage declaredType) {
        if (!(element.getEnclosingElement() instanceof TypeElement typeElement)) {
            return Optional.empty();
        }
        final var typeName = typeNameResolver.apply(typeElement);
        if (codeModel.getTypeDescriptor(typeName).isEmpty()) {
            return Optional.empty();
        }
        final var simpleName = element.getSimpleName().toString();
        final var field = new Symbol.Field(declaredType, codeModel, typeName, simpleName);
        return field.descriptor().isPresent() ? Optional.of(field) : Optional.empty();
    }

    private Optional<Symbol.Parameter> resolveParameter(final Element element) {
        if (!(element.getEnclosingElement() instanceof ExecutableElement executableElement)) {
            return Optional.empty();
        }
        // A lambda expression's parameters are owned by the enclosing method/constructor rather than
        // a symbol of their own, so they won't appear here — leave them without a Symbol trait.
        final var index = executableElement.getParameters().indexOf(element);
        if (index < 0) {
            return Optional.empty();
        }
        if (!(executableElement.getEnclosingElement() instanceof TypeElement typeElement)) {
            return Optional.empty();
        }
        final var typeName = typeNameResolver.apply(typeElement);
        final var typeDescriptor = codeModel.getTypeDescriptor(typeName).orElse(null);
        if (typeDescriptor == null) {
            return Optional.empty();
        }
        final var paramTypes = executableElement.getParameters().stream()
            .map(p -> typeResolver.apply(p.asType()))
            .toList();
        final var arity = paramTypes.size();
        final Optional<? extends CallableDescriptor> callable = executableElement.getKind() == ElementKind.CONSTRUCTOR
            ? typeDescriptor.traits(ConstructorDescriptor.class)
            .filter(cd -> cd.getFormalParameterCount() == arity)
            .filter(cd -> parametersMatch(cd, paramTypes))
            .findFirst()
            : typeDescriptor.traits(MethodDescriptor.class)
            .filter(md -> md.methodName().name().toString().equals(executableElement.getSimpleName().toString()))
            .filter(md -> md.getFormalParameterCount() == arity)
            .filter(md -> parametersMatch(md, paramTypes))
            .findFirst();
        return callable
            .map(c -> c.getFormalParameter(index))
            .map(Symbol.Parameter::new);
    }

    private Optional<ResolvedMethod> resolveMethod(final MethodInvocationTree t) {
        if (trees == null || compilationUnit == null || typeResolver == null) {
            return Optional.empty();
        }
        try {
            final var selectPath = TreePath.getPath(compilationUnit, t.getMethodSelect());
            if (selectPath == null) {
                return Optional.empty();
            }
            final var element = trees.getElement(selectPath);
            return resolveMethod(element);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private Optional<ResolvedMethod> resolveMethod(final MemberReferenceTree t) {
        if (trees == null || compilationUnit == null || typeResolver == null) {
            return Optional.empty();
        }
        try {
            final var path = TreePath.getPath(compilationUnit, t);
            if (path == null) {
                return Optional.empty();
            }
            final var element = trees.getElement(path);
            return resolveMethod(element);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private Optional<ResolvedMethod> resolveMethod(final Element element) {
        if (!(element instanceof ExecutableElement executableElement)) {
            return Optional.empty();
        }
        if (!(executableElement.getEnclosingElement() instanceof TypeElement typeElement)) {
            return Optional.empty();
        }
        final var typeName = typeNameResolver.apply(typeElement);
        if (codeModel.getTypeDescriptor(typeName).isEmpty()) {
            return Optional.empty();
        }
        final var simpleName = executableElement.getSimpleName().toString();
        final var paramTypes = executableElement.getParameters().stream()
            .map(p -> typeResolver.apply(p.asType()))
            .toList();
        final var resolvedMethod = new ResolvedMethod(codeModel, typeName, simpleName, paramTypes);
        return resolvedMethod.descriptor().isPresent() ? Optional.of(resolvedMethod) : Optional.empty();
    }

    private boolean parametersMatch(final CallableDescriptor cd, final List<TypeUsage> paramTypes) {
        final var formals = cd.formalParameters().toList();
        for (int i = 0; i < formals.size(); i++) {
            if (!typeUsageNamesMatch(formals.get(i).type(), paramTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean typeUsageNamesMatch(final TypeUsage a, final TypeUsage b) {
        if (a instanceof NamedTypeUsage na && b instanceof NamedTypeUsage nb) {
            return na.typeName().canonicalName().equals(nb.typeName().canonicalName());
        }
        return a.toString().equals(b.toString());
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
            receiverType = resolveReceiverType(receiverExpr).or(target::type);
        } else {
            target = null;
            methodName = t.getMethodSelect().toString();
            receiverType = Optional.ofNullable(enclosingType);
        }
        final var args = t.getArguments().stream()
            .map(this::convert)
            .toList();
        final var invocation = MethodInvocation.of(codeModel, Optional.ofNullable(target), methodName,
            args.stream(), receiverType);
        resolveMethod(t).ifPresent(invocation::addTrait);
        addSourceLocation(t.getMethodSelect()).ifPresent(invocation::addTrait);
        return invocation;
    }

    TypeUsage resolveTypeUsage(final Tree typeTree) {
        return resolveTypeMirror(typeTree)
            .map(typeResolver)
            .orElseGet(() -> UnknownTypeUsage.create(codeModel));
    }

    private Optional<TypeUsage> resolveLambdaParameterType(final VariableTree p) {
        final var tree = p.getType() != null ? p.getType() : p;
        return resolveTypeMirror(tree).map(typeResolver);
    }

    private Optional<TypeUsage> resolveReceiverType(final ExpressionTree receiverExpr) {
        return resolveTypeMirror(receiverExpr).map(typeResolver);
    }

    private Optional<TypeMirror> resolveTypeMirror(final Tree tree) {
        if (tree == null || trees == null || compilationUnit == null || typeResolver == null) {
            return Optional.empty();
        }
        try {
            final var path = TreePath.getPath(compilationUnit, tree);
            if (path == null) {
                return Optional.empty();
            }
            final var mirror = trees.getTypeMirror(path);
            if (mirror == null
                || mirror.getKind() == TypeKind.ERROR
                || mirror.getKind() == TypeKind.NONE
                || mirror.getKind() == TypeKind.OTHER) {
                return Optional.empty();
            }
            return Optional.of(mirror);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Expression visitMemberSelect(final MemberSelectTree t, final Void v) {
        if ("class".equals(t.getIdentifier().toString())) {
            final var type = resolveTypeUsage(t.getExpression());
            addSourceLocation(t.getExpression()).ifPresent(type::addTrait);
            return ClassLiteral.of(codeModel, type);
        }
        final var receiverExpr = t.getExpression();
        final var fieldAccess = FieldAccess.of(
            convert(receiverExpr),
            t.getIdentifier().toString(),
            resolveReceiverType(receiverExpr));
        addSourceLocation(t).ifPresent(fieldAccess::addTrait);
        return fieldAccess;
    }

    @Override
    public Expression visitNewClass(final NewClassTree t, final Void v) {
        final var args = t.getArguments().stream()
            .map(this::convert)
            .toList();
        final TypeUsage type;
        final List<TypeUsage> typeArgs;
        if (t.getIdentifier() instanceof ParameterizedTypeTree pt) {
            type = resolveTypeUsage(pt.getType());
            addSourceLocation(pt.getType()).ifPresent(type::addTrait);
            typeArgs = pt.getTypeArguments().stream()
                .map(argTree -> {
                    final var argType = resolveTypeUsage(argTree);
                    addSourceLocation(argTree).ifPresent(argType::addTrait);
                    return argType;
                })
                .toList();
        } else {
            type = resolveTypeUsage(t.getIdentifier());
            addSourceLocation(t.getIdentifier()).ifPresent(type::addTrait);
            typeArgs = List.of();
        }
        final var newObject = NewObject.of(codeModel, type, args.stream(), typeArgs.stream());
        addSourceLocation(t).ifPresent(newObject::addTrait);
        return newObject;
    }

    @Override
    public Expression visitNewArray(final NewArrayTree t, final Void v) {
        final List<Expression> dims = t.getDimensions() == null
            ? List.of()
            : t.getDimensions().stream().map(this::convert).toList();
        final var type = resolveTypeUsage(t.getType());
        addSourceLocation(t.getType()).ifPresent(type::addTrait);
        return NewArray.of(codeModel, type, dims.stream());
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
        return CompoundAssignment.of(codeModel, AssignmentOperator.ASSIGN,
            convert(t.getVariable()),
            convert(t.getExpression()));
    }

    @Override
    public Expression visitCompoundAssignment(final CompoundAssignmentTree t, final Void v) {
        final var op = switch (t.getKind()) {
            case PLUS_ASSIGNMENT -> AssignmentOperator.PLUS;
            case MINUS_ASSIGNMENT -> AssignmentOperator.MINUS;
            case MULTIPLY_ASSIGNMENT -> AssignmentOperator.MULTIPLY;
            case DIVIDE_ASSIGNMENT -> AssignmentOperator.DIVIDE;
            case REMAINDER_ASSIGNMENT -> AssignmentOperator.REMAINDER;
            case AND_ASSIGNMENT -> AssignmentOperator.AND;
            case OR_ASSIGNMENT -> AssignmentOperator.OR;
            case XOR_ASSIGNMENT -> AssignmentOperator.XOR;
            case LEFT_SHIFT_ASSIGNMENT -> AssignmentOperator.LEFT_SHIFT;
            case RIGHT_SHIFT_ASSIGNMENT -> AssignmentOperator.RIGHT_SHIFT;
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> AssignmentOperator.UNSIGNED_RIGHT_SHIFT;
            default -> throw new IllegalArgumentException("Unexpected compound assignment kind: " + t.getKind());
        };
        return CompoundAssignment.of(codeModel, op,
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
            case AND -> BitwiseBinary.of(codeModel, BitwiseOperator.AND, left, right);
            case OR -> BitwiseBinary.of(codeModel, BitwiseOperator.OR, left, right);
            case XOR -> BitwiseBinary.of(codeModel, BitwiseOperator.XOR, left, right);
            case LEFT_SHIFT -> BitwiseBinary.of(codeModel, BitwiseOperator.LEFT_SHIFT, left, right);
            case RIGHT_SHIFT -> BitwiseBinary.of(codeModel, BitwiseOperator.RIGHT_SHIFT, left, right);
            case UNSIGNED_RIGHT_SHIFT -> BitwiseBinary.of(codeModel, BitwiseOperator.UNSIGNED_RIGHT_SHIFT, left, right);
            default -> UnknownExpression.of(codeModel);
        };
    }

    @Override
    public Expression visitUnary(final UnaryTree t, final Void v) {
        final var operand = convert(t.getExpression());
        return switch (t.getKind()) {
            case PREFIX_INCREMENT -> PrefixUnary.of(codeModel, PrefixOperator.INCREMENT, operand);
            case PREFIX_DECREMENT -> PrefixUnary.of(codeModel, PrefixOperator.DECREMENT, operand);
            case BITWISE_COMPLEMENT -> PrefixUnary.of(codeModel, PrefixOperator.BITWISE_COMPLEMENT, operand);
            case UNARY_PLUS -> PrefixUnary.of(codeModel, PrefixOperator.UNARY_PLUS, operand);
            case POSTFIX_INCREMENT -> PostfixUnary.of(codeModel, PostfixOperator.INCREMENT, operand);
            case POSTFIX_DECREMENT -> PostfixUnary.of(codeModel, PostfixOperator.DECREMENT, operand);
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
        final var type = resolveTypeUsage(t.getType());
        addSourceLocation(t.getType()).ifPresent(type::addTrait);
        return Cast.of(type, convert(t.getExpression()));
    }

    @Override
    public Expression visitInstanceOf(final InstanceOfTree t, final Void v) {
        if (t.getPattern() instanceof DeconstructionPatternTree dp) {
            final var type = resolveTypeUsage(dp.getDeconstructor());
            addSourceLocation(dp.getDeconstructor()).ifPresent(type::addTrait);
            final var components = dp.getNestedPatterns().stream()
                .map(this::convertPattern)
                .toList();
            final var instanceOf = InstanceOf.ofDeconstruction(convert(t.getExpression()), type, components);
            dp.getNestedPatterns().forEach(nested -> registerNestedPatternBindings(nested, instanceOf));
            return instanceOf;
        }
        final Optional<String> bindingVariable;
        final Tree typeTree;
        if (t.getPattern() instanceof BindingPatternTree bp) {
            bindingVariable = Optional.of(bp.getVariable().getName().toString());
            typeTree = t.getType();
        } else {
            bindingVariable = Optional.empty();
            typeTree = t.getType();
        }
        final var type = resolveTypeUsage(typeTree);
        addSourceLocation(typeTree).ifPresent(type::addTrait);
        final var instanceOf = InstanceOf.of(
            convert(t.getExpression()),
            type,
            bindingVariable);
        if (t.getPattern() instanceof BindingPatternTree bp) {
            registerPatternBinding(bp.getVariable(), instanceOf);
        }
        return instanceOf;
    }

    /**
     * Recursively converts a nested {@link PatternTree} (a component of a record-deconstruction
     * pattern) to an {@link InstanceOf.Pattern}. A component is either a {@link BindingPatternTree}
     * (a simple type-pattern variable) or another {@link DeconstructionPatternTree} nested
     * arbitrarily deep.
     */
    InstanceOf.Pattern convertPattern(final PatternTree pattern) {
        if (pattern instanceof BindingPatternTree binding) {
            final var type = resolveTypeUsage(binding.getVariable().getType());
            addSourceLocation(binding.getVariable().getType()).ifPresent(type::addTrait);
            return new InstanceOf.Pattern.Binding(type, binding.getVariable().getName().toString());
        }
        if (pattern instanceof DeconstructionPatternTree deconstruction) {
            final var type = resolveTypeUsage(deconstruction.getDeconstructor());
            addSourceLocation(deconstruction.getDeconstructor()).ifPresent(type::addTrait);
            final var components = deconstruction.getNestedPatterns().stream()
                .map(this::convertPattern)
                .toList();
            return new InstanceOf.Pattern.Record(type, components);
        }
        throw new IllegalStateException("Unsupported nested pattern kind: " + pattern.getClass());
    }

    /**
     * Recursively registers every {@link BindingPatternTree} nested within a record-deconstruction
     * pattern against {@code instanceOf}, so that later {@link Symbol.PatternBinding} resolution
     * can link identifier usages of those component variables back to the pattern test that
     * declared them.
     */
    void registerNestedPatternBindings(final PatternTree pattern, final InstanceOf instanceOf) {
        if (pattern instanceof BindingPatternTree binding) {
            registerPatternBinding(binding.getVariable(), instanceOf);
        } else if (pattern instanceof DeconstructionPatternTree deconstruction) {
            deconstruction.getNestedPatterns()
                .forEach(nested -> registerNestedPatternBindings(nested, instanceOf));
        }
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
            .map(p -> {
                final var param = new LambdaParameter(codeModel, resolveLambdaParameterType(p), p.getName().toString());
                addSourceLocation(p).ifPresent(param::addTrait);
                return param;
            })
            .toList();
        return Lambda.of(codeModel, params, body);
    }

    @Override
    public Expression visitSwitchExpression(final SwitchExpressionTree t, final Void v) {
        if (stmtConverter == null) {
            return UnknownExpression.of(codeModel);
        }
        final var selector = convert(t.getExpression());
        final var cases = t.getCases().stream().map(c -> stmtConverter.convertCase(c, selector)).toList();
        return SwitchExpression.of(selector, cases.stream());
    }

    @Override
    public Expression visitMemberReference(final MemberReferenceTree t, final Void v) {
        final var qualifierExpr = t.getQualifierExpression();
        final var reference = MethodReference.of(
            convert(qualifierExpr),
            t.getName().toString(),
            resolveReceiverType(qualifierExpr));
        resolveMethod(t).ifPresent(reference::addTrait);
        addSourceLocation(t).ifPresent(reference::addTrait);
        return reference;
    }

    @Override
    protected Expression defaultAction(final Tree node, final Void v) {
        return UnknownExpression.of(codeModel);
    }
}
