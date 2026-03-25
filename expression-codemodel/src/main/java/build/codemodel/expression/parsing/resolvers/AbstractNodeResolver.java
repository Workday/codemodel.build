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

import build.base.io.LookaheadReader;
import build.codemodel.expression.Expression;
import build.codemodel.expression.parsing.Token;
import build.codemodel.expression.parsing.tokenparsers.TokenParser;

import java.util.Stack;

/**
 * Provides a facility to resolve an {@link Expression} given a {@link Token}.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public abstract class AbstractNodeResolver implements NodeResolver {

    protected final Token token;
    protected boolean isConsolidated;

    /**
     * Constructs a new {@link NodeResolver}.
     *
     * @param token the {@link Token} to resolve
     */
    public AbstractNodeResolver(final Token token) {
        this.token = token;
        this.isConsolidated = false;
    }

    /**
     * Gets the {@link TokenParser} that parsed the {@link Token} and that generated this {@link NodeResolver}.
     *
     * @return the {@link TokenParser}
     */
    @Override
    public TokenParser getTokenParser() {
        return this.token.tokenParser();
    }

    /**
     * Gets the {@link LookaheadReader.Location} of the {@link Token} that generated this {@link NodeResolver}.
     *
     * @return the {@link LookaheadReader.Location}
     */
    @Override
    public LookaheadReader.Location getTokenLocation() {
        return this.token.location();
    }

    /**
     * Gets whether the {@link NodeResolver} has been successfully consolidated, meaning that it now represents
     * a graph of {@link NodeResolver}s rather than a single {@link NodeResolver}.
     *
     * @return whether the {@link NodeResolver} has been consolidated
     */
    @Override
    public boolean getIsConsolidated() {
        return this.isConsolidated;
    }

    /**
     * Consolidates multiple adjacent {@link NodeResolver}s into this {@link NodeResolver}, turning
     * unrelated {@link NodeResolver}s on the stack into a graph of {@link NodeResolver}.
     *
     * @param stack the stack of {@link NodeResolver}s
     */
    @Override
    public void consolidate(final Stack<NodeResolver> stack) {
        this.isConsolidated = true;
    }
}

