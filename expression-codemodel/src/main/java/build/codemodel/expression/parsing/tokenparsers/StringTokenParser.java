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

import java.util.function.Function;

/**
 * An abstract {@link TokenParser} which is matched based on a {@link String} template.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public abstract class StringTokenParser
        extends AbstractTokenParser {

    private final String template;

    /**
     * Constructs a new {@link StringTokenParser}.
     *
     * @param template the {@link String} template to match
     * @param priority the priority to use when processing {@link Token}s that match this {@link TokenParser}
     * @param runnable the {@link Function} that will create the {@link NodeResolver} for a token defined by this {@link TokenParser}
     */
    public StringTokenParser(final String template,
                             final int priority,
                             final Function<Token, NodeResolver> runnable) {
        super(priority, runnable);
        this.template = template;
    }

    /**
     * Determines if the expression {@link String} contains a token matching the declared pattern.
     *
     * @param scanner the {@link Scanner} to check for the {@link Token}
     * @return {@code true} if the token is the next one in the {@link Scanner}, {@code false} otherwise
     */
    @Override
    public boolean isNextToken(final Scanner scanner) {
        return scanner.follows(this.template);
    }

    /**
     * Extracts the token from the expression {@link String}.
     *
     * @param scanner the {@link Scanner} to extract the {@link Token} from
     * @return the extracted token characters
     */
    @Override
    public String extractToken(final Scanner scanner) {
        return scanner.consume(this.template);
    }

}
