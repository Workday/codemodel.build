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
import java.util.function.BiFunction;

/**
 * A {@link NodeResolver} that can resolve an {@link Expression} from a binary operation.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class BinaryNodeResolver extends AbstractNodeResolver {

    private final BiFunction<Expression, Expression, Expression> createNodeRunnable;

    private NodeResolver left;
    private NodeResolver right;

    /**
     * Constructs a new {@link BinaryNodeResolver}.
     *
     * @param token the {@link Token} to resolve
     * @param createNodeRunnable the {@link BiFunction} that will create the {@link Expression} based on left and right {@link Expression}s
     */
    public BinaryNodeResolver(final Token token, final BiFunction<Expression, Expression, Expression> createNodeRunnable) {
        super(token);
        this.createNodeRunnable = createNodeRunnable;
    }

    /**
     * Consolidates three {@link NodeResolver}s from the stack into a single {@link NodeResolver}.
     *
     * @param stack the stack of {@link NodeResolver}s
     * @throws RuntimeException if the stack is not in a known state
     */
    @Override
    public void consolidate(final Stack<NodeResolver> stack) {
        if (this != stack.get(stack.size() - 2)) {
            throw new RuntimeException("This BinaryNodeResolver should be the second to last element in the stack.");
        }
        if (this.isConsolidated) {
            throw new RuntimeException("This BinaryNodeResolver has already been consolidated.");
        }
        if (stack.size() < 3) {
            throw new RuntimeException("There should be at least 3 elements in the stack.");
        }

        this.right = stack.pop();
        stack.pop(); // self
        this.left = stack.pop();

        stack.push(this);

        super.consolidate(stack);
    }

    /**
     * Resolves the {@link Expression} from the left and right {@link Expression}s.
     *
     * @return the resolved {@link Expression}
     * @throws RuntimeException if the {@link NodeResolver} has not been consolidated prior to resolving
     */
    @Override
    public Expression resolve() {
        if (!this.isConsolidated) {
            throw new RuntimeException("The NodeResolver has not been consolidated.");
        }
        return this.createNodeRunnable.apply(this.left.resolve(), this.right.resolve());
    }
}
