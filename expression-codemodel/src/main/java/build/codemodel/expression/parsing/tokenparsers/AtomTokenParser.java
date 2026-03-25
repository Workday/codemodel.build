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

import build.codemodel.expression.parsing.Token;
import build.codemodel.expression.parsing.resolvers.NodeResolver;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A {@link TokenParser} that defines an expression Atom, which is matched based on a regular expression pattern.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class AtomTokenParser
        extends PatternTokenParser {

    /**
     * Constructs a new {@link AtomTokenParser}.
     *
     * @param pattern the regular expression pattern to match, expressed as a {@link String}
     * @param priority the priority to use when processing {@link Token}s that match this {@link TokenParser}
     * @param runnable the {@link Function} that will create the {@link NodeResolver} for a token defined by this {@link TokenParser}
     */
    public AtomTokenParser(final String pattern, final int priority, final Function<Token, NodeResolver> runnable) {
        super(Pattern.compile(pattern), priority, runnable);
    }

    /**
     * Determines if the token is allowed after the preceding list of tokens.
     *
     * @param tokens the list of preceding tokens
     * @return {@code true} if the token is allowed after the list of tokens, {@code false} otherwise
     */
    @Override
    public boolean isAllowedAfter(final List<Token> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        final var lastToken = tokens.getLast();
        return !(lastToken.tokenParser() instanceof AtomTokenParser) && !(lastToken.tokenParser() instanceof SectionEndTokenParser);
    }
}
