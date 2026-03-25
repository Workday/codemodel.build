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

import build.base.parsing.Filter;
import build.base.parsing.Scanner;
import build.codemodel.expression.parsing.tokenparsers.TokenParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizes an expression string (see <a href="https://en.wikipedia.org/wiki/Lexical_analysis#Tokenization">Tokenization</a>).
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class Tokenizer {

    private final List<TokenParser> tokenParsers;
    private final boolean ignoreWhitespace;

    /**
     * Constructs a {@link Tokenizer} with the specified list of {@link TokenParser}s that will ignore whitespace.
     *
     * @param tokenParsers the {@link List} of {@link TokenParser}s to use when tokenizing
     */
    public Tokenizer(final List<TokenParser> tokenParsers) {
        this(tokenParsers, true);
    }

    /**
     * Constructs a {@link Tokenizer} with the specified list of {@link TokenParser}s.
     *
     * @param tokenParsers the {@link List} of {@link TokenParser}s to use when tokenizing
     * @param ignoreWhitespace {@code true} if whitespace should be ignored, otherwise {@code false}
     */
    public Tokenizer(final List<TokenParser> tokenParsers, final boolean ignoreWhitespace) {
        this.tokenParsers = tokenParsers;
        this.ignoreWhitespace = ignoreWhitespace;
    }

    /**
     * Tokenizes the specified expression string.
     *
     * @param expression the expression string to tokenize
     * @return the {@link List} of {@link Token}s extracted from the expression string
     * @throws ExpressionParserException if the expression contains an unknown or unexpected token
     */
    public List<Token> tokenize(final String expression) {
        final var scanner = new Scanner(expression);
        if (this.ignoreWhitespace) {
            scanner.register(Filter.WHITESPACE);
        }

        final var tokens = new ArrayList<Token>();

        while (scanner.hasNext()) {
            var tokenProcessed = false;
            for (TokenParser tokenParser : this.tokenParsers) {
                final var token = tokenParser.attemptExtractToken(scanner, tokens);
                if (token.isPresent()) {
                    tokens.add(token.get());
                    tokenProcessed = true;
                    break;
                }
            }
            if (!tokenProcessed) {
                throw new ExpressionParserException("The expression contains an unknown or unexpected token at " + scanner.getLocation());
            }
        }

        return tokens;
    }
}
