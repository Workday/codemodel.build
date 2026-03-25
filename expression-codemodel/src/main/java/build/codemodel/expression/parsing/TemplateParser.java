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

import build.codemodel.expression.Expression;
import build.codemodel.expression.StringLiteral;
import build.codemodel.expression.TemplateExpression;
import build.codemodel.expression.parsing.resolvers.AtomNodeResolver;
import build.codemodel.expression.parsing.resolvers.NodeResolver;
import build.codemodel.expression.parsing.tokenparsers.TemplateAtomTokenParser;
import build.codemodel.expression.parsing.tokenparsers.TokenParser;
import build.codemodel.foundation.CodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Parses a {@link TemplateExpression} from a template string.
 *
 * @author tim.berston
 * @since Mar-2025
 */
public class TemplateParser {

    private final CodeModel codeModel;
    private final List<TokenParser> tokenParsers;

    /**
     * Constructs a new {@link TemplateParser}.
     */
    public TemplateParser(final CodeModel codeModel) {
        this.codeModel = codeModel;
        this.tokenParsers = new ArrayList<>();
    }

    /**
     * Defines an Atom {@link TokenParser}.
     *
     * @param pattern        the RegEx pattern of the Atom {@link TokenParser}
     * @param implementation the {@link Function} that will create the {@link Expression} for the {@link TokenParser}
     */
    public void defineAtom(final String pattern, final Function<Token, Expression> implementation) {
        final var tokenDef = new TemplateAtomTokenParser(pattern, 0, token -> new AtomNodeResolver(token, implementation));
        this.tokenParsers.add(tokenDef);
    }

    /**
     * Parses the template expression.
     *
     * @param expression the expression to parse
     * @return the parsed {@link TemplateExpression}
     */
    public TemplateExpression parse(final String expression) {
        if (expression == null || expression.isBlank()) {
            return TemplateExpression.empty(this.codeModel);
        }

        final var tokenizer = new Tokenizer(this.tokenParsers, false);
        final var tokens = tokenizer.tokenize(expression);
        final var resolvers = new ArrayList<NodeResolver>();
        try {
            for (final var token : tokens) {
                resolvers.add(token.tokenParser().createNodeResolver(token));
            }
            final var expressions = resolveAndConsolidate(resolvers);
            return TemplateExpression.of(expressions);
        }
        catch (final ExpressionParserException e) {
            throw e;
        }
        catch (final RuntimeException e) {
            throw new ExpressionParserException("There was an error parsing the expression", e);
        }
    }

    /**
     * Resolves the list of {@link NodeResolver}s into a list of {@link Expression}s, and then consolidates
     * any adjacent {@link StringLiteral}s into a single {@link StringLiteral}.
     *
     * @param resolvers the set of {@link NodeResolver}s to resolve
     * @return the resolved and consolidated {@link Expression}s
     */
    private List<Expression> resolveAndConsolidate(final List<NodeResolver> resolvers) {
        return resolvers
                .stream()
                .map(NodeResolver::resolve)
                .collect(Collector.of(
                    ArrayList::new,
                    (list, node) -> {
                        if (list.isEmpty()) {
                            list.add(node);
                        } else {
                            final var last = list.getLast();
                            if (last instanceof StringLiteral stringLiteral && node instanceof StringLiteral nextStringLiteral) {
                                list.set(list.size() - 1, StringLiteral.of(stringLiteral.codeModel(), stringLiteral.value() + nextStringLiteral.value()));
                            } else {
                                list.add(node);
                            }
                        }
                    },
                    // This is a sequential stream, so we shouldn't need to worry about the combiner
                    (list1, list2) -> {
                        list1.addAll(list2);
                        return list1;
                    }
                ));
    }
}
