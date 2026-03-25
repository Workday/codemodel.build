package build.codemodel.expression.parsing.resolvers;

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

import build.codemodel.expression.Expression;
import build.codemodel.expression.parsing.Token;

import java.util.Stack;
import java.util.function.Function;

/**
 * A {@link NodeResolver} that can resolve an {@link Expression} from a unary operation.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class UnaryNodeResolver extends AbstractNodeResolver {

    private final Function<Expression, Expression> createNodeRunnable;

    private NodeResolver operand;

    /**
     * Constructs a new {@link UnaryNodeResolver}.
     *
     * @param token the {@link Token} to resolve
     * @param createNodeRunnable the {@link Function} that will create the {@link Expression} based on the operand {@link Expression}
     */
    public UnaryNodeResolver(final Token token, final Function<Expression, Expression> createNodeRunnable) {
        super(token);
        this.createNodeRunnable = createNodeRunnable;
    }

    /**
     * Consolidates two {@link NodeResolver}s from the stack into a single {@link NodeResolver}.
     *
     * @param stack the stack of {@link NodeResolver}s
     * @throws RuntimeException if the stack is not in a known state
     */
    @Override
    public void consolidate(final Stack<NodeResolver> stack) {
        if (this != stack.get(stack.size() - 2)) {
            throw new RuntimeException("This UnaryNodeResolver should be the second to last element in the stack.");
        }
        if (this.isConsolidated) {
            throw new RuntimeException("This UnaryNodeResolver has already been consolidated.");
        }
        if (stack.size() < 2) {
            throw new RuntimeException("There should be at least 2 elements in the stack.");
        }

        this.operand = stack.pop();

        super.consolidate(stack);
    }

    /**
     * Resolves the {@link Expression} from the operand {@link Expression}.
     *
     * @return the resolved {@link Expression}
     * @throws RuntimeException if the {@link NodeResolver} has not been consolidated
     */
    @Override
    public Expression resolve() {
        if (!this.isConsolidated) {
            throw new RuntimeException("The NodeResolver has not been consolidated.");
        }
        return this.createNodeRunnable.apply(this.operand.resolve());
    }
}
