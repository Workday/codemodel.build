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
 * A {@link NodeResolver} that represents the beginning of a 'section'.
 * <p>
 * A {@link SectionBeginNodeResolver} should never be resolved, and will instead be removed from the stack when a {@link SectionEndNodeResolver} is 'collapsed'.
 *
 * @author tim.berston
 * @since Mar-2021
 */
public class SectionBeginNodeResolver extends AbstractNodeResolver {

    /**
     * Constructs a new {@link SectionBeginNodeResolver}.
     *
     * @param token the {@link Token} to resolve
     */
    public SectionBeginNodeResolver(final Token token) {
        super(token);
    }

    /**
     * Resolves the {@link Expression}. This should never be called on a {@link SectionBeginNodeResolver}.
     *
     * @return the resolved {@link Expression}
     * @throws RuntimeException always
     */
    @Override
    public Expression resolve() {
        throw new RuntimeException("'resolve' should never called on SectionBeginNodeResolver.");
    }
}
