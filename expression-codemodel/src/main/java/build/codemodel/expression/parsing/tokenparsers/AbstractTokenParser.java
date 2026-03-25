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
import java.util.function.Function;

/**
 * An abstract {@link TokenParser}.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public abstract class AbstractTokenParser
        implements TokenParser {

    private final int precedence;
    private final Function<Token, NodeResolver> runnable;

    /**
     * Constructs a new {@link AbstractTokenParser}.
     *
     * @param priority the priority to use when processing {@link Token}s that match this {@link TokenParser}
     * @param runnable the {@link Function} that will create the {@link NodeResolver} for a token defined by this {@link TokenParser}
     */
    protected AbstractTokenParser(final int priority, final Function<Token, NodeResolver> runnable) {
        this.precedence = priority;
        this.runnable = runnable;
    }

    /**
     * Determines if the next token in the {@link Scanner} matches this {@link TokenParser}, and
     * if so builds a {@link Token} to represent it.
     *
     * @param scanner      the {@link Scanner} to check for the {@link Token}
     * @param parsedTokens the list of tokens parsed so far, which may be used to determine if the token is allowed
     * @return {@link Optional<Token>} if the token is the next one in the {@link Scanner}, otherwise {@link Optional#empty()}
     */
    public Optional<Token> attemptExtractToken(final Scanner scanner, final List<Token> parsedTokens) {
        if (isNextToken(scanner) && isAllowedAfter(parsedTokens)) {
            final var token = new Token(this, scanner.getLocation(), extractToken(scanner));
            return Optional.of(token);
        }
        return Optional.empty();
    }

    /**
     * Gets the precedence of this {@link TokenParser}.
     *
     * @return the precedence
     */
    public int getPrecedence() {
        return this.precedence;
    }

    /**
     * Creates a new {@link NodeResolver} for the given {@link Token}.
     *
     * @param token the {@link Token} to create a {@link NodeResolver} for
     * @return the created {@link NodeResolver}
     */
    public NodeResolver createNodeResolver(final Token token) {
        return this.runnable.apply(token);
    }

    /**
     * Determines if the next token in the {@link Scanner} matches this {@link TokenParser}.
     *
     * @param scanner the {@link Scanner} to check
     * @return {@code true} if the next token is this {@link TokenParser}, {@code false} otherwise
     */
    protected abstract boolean isNextToken(Scanner scanner);

    /**
     * Extracts the token from the expression {@link String}.
     *
     * @param scanner the {@link Scanner} to extract the {@link Token} from
     * @return the extracted token characters
     */
    protected abstract String extractToken(Scanner scanner);

    /**
     * Determines if the token is allowed after the preceding list of tokens.
     *
     * @param tokens the list of preceding tokens
     * @return {@code true} if the token is allowed after the list of tokens, {@code false} otherwise
     */
    protected abstract boolean isAllowedAfter(List<Token> tokens);
}
