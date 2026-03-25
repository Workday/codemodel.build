package build.codemodel.expression.parsing.tokenparsers;

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

import build.base.parsing.Scanner;
import build.codemodel.expression.parsing.Token;
import build.codemodel.expression.parsing.resolvers.NodeResolver;

import java.util.List;
import java.util.Optional;

public interface TokenParser {

    /**
     * Gets the precedence of this {@link TokenParser}.
     *
     * @return the precedence
     */
    int getPrecedence();

    /**
     * Determines if the next token in the {@link Scanner} matches this {@link TokenParser}, and
     * if so builds a {@link Token} to represent it.
     *
     * @param scanner      the {@link Scanner} to check for the {@link Token}
     * @param parsedTokens the list of tokens parsed so far, which may be used to determine if the token is allowed
     * @return {@link Optional<Token>} if the token is the next one in the {@link Scanner}, otherwise {@link Optional#empty()}
     */
    Optional<Token> attemptExtractToken(Scanner scanner, List<Token> parsedTokens);

    /**
     * Creates a new {@link NodeResolver} for the given {@link Token}.
     *
     * @param token the {@link Token} to create a {@link NodeResolver} for
     * @return the created {@link NodeResolver}
     */
    NodeResolver createNodeResolver(Token token);
}
