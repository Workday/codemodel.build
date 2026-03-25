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

/**
 * A {@link NodeResolver} that represents the end of a 'section'.
 * <p>
 * A {@link SectionEndNodeResolver} should never be resolved, and will instead be removed from the stack when it is 'collapsed'.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class SectionEndNodeResolver extends AbstractNodeResolver {

    /**
     * Constructs a new {@link SectionEndNodeResolver}.
     *
     * @param token the {@link Token} to resolve
     */
    public SectionEndNodeResolver(final Token token) {
        super(token);
    }

    /**
     * Resolves the {@link Expression}. This should never be called on a {@link SectionEndNodeResolver}.
     *
     * @return the resolved {@link Expression}
     * @throws RuntimeException always
     */
    @Override
    public Expression resolve() {
        throw new RuntimeException("'resolve' should never called on SectionEndNodeResolver.");
    }
}
