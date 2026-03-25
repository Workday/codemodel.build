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

import java.util.function.Function;

/**
 * A {@link NodeResolver} that can resolve an {@link Expression} from an atom token.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class AtomNodeResolver extends AbstractNodeResolver {

    private final Function<Token, Expression> createNodeRunnable;

    /**
     * Constructs a new {@link AtomNodeResolver}.
     *
     * @param token the {@link Token} to resolve
     * @param createNodeRunnable the {@link Function} that will create the {@link Expression} for the token
     */
    public AtomNodeResolver(final Token token, final Function<Token, Expression> createNodeRunnable) {
        super(token);

        this.createNodeRunnable = createNodeRunnable;
        this.isConsolidated = true;
    }

    /**
     * Resolves the {@link Expression} from the {@link Token}.
     *
     * @return the resolved {@link Expression}
     */
    @Override
    public Expression resolve() {
        return this.createNodeRunnable.apply(this.token);
    }
}
