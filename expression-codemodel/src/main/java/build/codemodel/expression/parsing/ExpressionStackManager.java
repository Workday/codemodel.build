package build.codemodel.expression.parsing;

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

import build.codemodel.expression.parsing.resolvers.AtomNodeResolver;
import build.codemodel.expression.parsing.resolvers.NodeResolver;
import build.codemodel.expression.parsing.resolvers.SectionBeginNodeResolver;
import build.codemodel.expression.parsing.resolvers.SectionEndNodeResolver;

import java.util.Optional;
import java.util.Stack;

/**
 * Manages the stack(s) of {@link NodeResolver}s during expression parsing.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class ExpressionStackManager {

    private final Stack<Stack<NodeResolver>> stackOfStacks;
    private Stack<NodeResolver> activeResolverStack;

    /**
     * Constructs a new {@link ExpressionStackManager}.
     */
    public ExpressionStackManager() {
        this.stackOfStacks = new Stack<>();
        this.activeResolverStack = new Stack<>();
    }

    /**
     * Pushes a {@link NodeResolver} onto the stack.
     * <p>
     * Before pushing the {@link NodeResolver}, the stack is checked to see if a previous {@link NodeResolver}
     * should be consolidated.
     *
     * @param nodeResolver the {@link NodeResolver} to push
     */
    public void push(final NodeResolver nodeResolver) {

        if (nodeResolver instanceof SectionBeginNodeResolver) {
            this.beginSection();
            return;
        }

        final var activeNodeResolver = nodeResolver instanceof SectionEndNodeResolver
                ? this.completeSection((SectionEndNodeResolver) nodeResolver)
                : nodeResolver;

        if (!(activeNodeResolver instanceof AtomNodeResolver)) {
            var lastOperator = getLastRelevantOperator();
            while (lastOperator.isPresent()) {
                final var lastOperatorResolver = lastOperator.get();
                if (activeNodeResolver.getTokenParser().getPrecedence() >= lastOperatorResolver.getTokenParser().getPrecedence()) {
                    lastOperatorResolver.consolidate(this.activeResolverStack);
                    lastOperator = getLastRelevantOperator();
                }
                else {
                    break;
                }
            }
        }
        this.activeResolverStack.push(activeNodeResolver);
    }

    /**
     * Gets the root {@link NodeResolver} for the expression.
     * <p>
     * The stack is consolidated until only the root {@link NodeResolver} remains.
     *
     * @return the root {@link NodeResolver}
     * @throws ExpressionParserException if the expression is malformed
     */
    public NodeResolver getExpressionRoot() {
        final var MALFORMED_EXPRESSION = "The expression is malformed";

        if (!this.stackOfStacks.isEmpty()) {
            throw new ExpressionParserException(MALFORMED_EXPRESSION);
        }

        return this.getStackRoot();
    }

    /**
     * Gets the root {@link NodeResolver} for the active section of the expression.
     * <p>
     * The stack is consolidated until only the root {@link NodeResolver} remains.
     *
     * @return the root {@link NodeResolver}
     * @throws ExpressionParserException if the expression is malformed
     */
    private NodeResolver getStackRoot() {
        final var MALFORMED_EXPRESSION = "The expression is malformed";

        while (this.activeResolverStack.size() > 1) {
            final var operator = this.getLastRelevantOperator();
            if (operator.isEmpty()) {
                throw new ExpressionParserException(MALFORMED_EXPRESSION);
            }
            operator.get().consolidate(this.activeResolverStack);
        }

        final var root = this.activeResolverStack.pop();
        if (!root.getIsConsolidated()) {
            throw new ExpressionParserException(MALFORMED_EXPRESSION);
        }
        return root;
    }

    /**
     * Begins a new section of the expression.
     * <p>
     * The current stack is pushed onto the stack of stacks, and a new stack is created for the active section.
     */
    private void beginSection() {
        this.stackOfStacks.push(this.activeResolverStack);
        this.activeResolverStack = new Stack<>();
    }

    /**
     * Returning the root {@link NodeResolver} and discard the stack for the active section.
     *
     * @param nodeResolver the {@link SectionEndNodeResolver} that completes the active section
     * @return the root {@link NodeResolver} for the section
     */
    private NodeResolver completeSection(final SectionEndNodeResolver nodeResolver) {
        if (this.stackOfStacks.isEmpty()) {
            throw new ExpressionParserException("The expression contains an unknown or unexpected token at " + nodeResolver.getTokenLocation());
        }
        final var rootNodeResolver = this.getStackRoot();
        this.activeResolverStack = this.stackOfStacks.pop();
        return rootNodeResolver;
    }

    /**
     * Gets the last relevant operator from the stack.
     *
     * @return the last relevant operator, or an empty {@link Optional} if there is no relevant operator
     */
    private Optional<NodeResolver> getLastRelevantOperator() {
        final var stackSize = this.activeResolverStack.size();

        if (stackSize >= 2) {
            final var lastResolver = this.activeResolverStack.get(stackSize - 1);
            if (!lastResolver.getIsConsolidated()) {
                return Optional.empty();
            }

            final var resolver = this.activeResolverStack.get(stackSize - 2);
            if (resolver instanceof AtomNodeResolver || resolver.getIsConsolidated()) {
                return Optional.empty();
            }

            return Optional.of(resolver);
        }

        return Optional.empty();
    }
}
