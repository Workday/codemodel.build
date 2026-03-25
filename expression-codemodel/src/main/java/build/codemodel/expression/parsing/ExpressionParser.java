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
import build.codemodel.expression.parsing.resolvers.AtomNodeResolver;
import build.codemodel.expression.parsing.resolvers.BinaryNodeResolver;
import build.codemodel.expression.parsing.resolvers.SectionBeginNodeResolver;
import build.codemodel.expression.parsing.resolvers.SectionEndNodeResolver;
import build.codemodel.expression.parsing.resolvers.UnaryNodeResolver;
import build.codemodel.expression.parsing.tokenparsers.AtomTokenParser;
import build.codemodel.expression.parsing.tokenparsers.BinaryTokenParser;
import build.codemodel.expression.parsing.tokenparsers.SectionBeginTokenParser;
import build.codemodel.expression.parsing.tokenparsers.SectionEndTokenParser;
import build.codemodel.expression.parsing.tokenparsers.TokenParser;
import build.codemodel.expression.parsing.tokenparsers.UnaryTokenParser;
import build.codemodel.foundation.CodeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Parses an {@link Expression} from an expression string.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class ExpressionParser {

    private final CodeModel codeModel;
    private final List<TokenParser> tokenParsers;
    private final ExpressionStackManager stackManager;

    /**
     * Constructs a new {@link ExpressionParser}.
     */
    public ExpressionParser(final CodeModel codeModel) {
        this.codeModel = codeModel;
        this.tokenParsers = new ArrayList<>();
        this.stackManager = new ExpressionStackManager();
    }

    /**
     * Defines an Atom {@link TokenParser}.
     *
     * @param pattern        the RegEx pattern of the Atom {@link TokenParser}
     * @param implementation the {@link Function} that will create the {@link Expression} for the {@link TokenParser}
     */
    public void defineAtom(final String pattern, final Function<Token, Expression> implementation) {
        final var tokenDef = new AtomTokenParser(pattern, 0, token -> new AtomNodeResolver(token, implementation));
        this.tokenParsers.add(tokenDef);
    }

    /**
     * Defines a Unary {@link TokenParser}.
     *
     * @param template       the string template of the Unary {@link TokenParser}
     * @param priority       the priority to use when testing the TokenDef against the expression string
     * @param implementation the {@link Function} that will create the {@link Expression} for the {@link TokenParser}
     */
    public void defineUnary(final String template, final int priority, final Function<Expression, Expression> implementation) {
        final var tokenDef = new UnaryTokenParser(template, priority, token -> new UnaryNodeResolver(token, implementation));
        this.tokenParsers.add(tokenDef);
    }

    /**
     * Defines a Binary {@link TokenParser}.
     *
     * @param template       the string template of the Binary {@link TokenParser}
     * @param priority       the priority to use when testing the TokenDef against the expression string
     * @param implementation the {@link BiFunction} that will create the {@link Expression} for the {@link TokenParser}
     */
    public void defineBinary(final String template, final int priority, final BiFunction<Expression, Expression, Expression> implementation) {
        final var tokenDef = new BinaryTokenParser(template, priority, token -> new BinaryNodeResolver(token, implementation));
        this.tokenParsers.add(tokenDef);
    }

    /**
     * Defines an Open and Close Section {@link TokenParser}.
     *
     * @param openTemplate  the string template of the Section Begin {@link TokenParser}
     * @param closeTemplate the string template of the Section End {@link TokenParser}
     */
    public void defineSection(final String openTemplate, final String closeTemplate) {
        final var openDef = new SectionBeginTokenParser(openTemplate, 0, SectionBeginNodeResolver::new);
        final var closeDef = new SectionEndTokenParser(closeTemplate, 9999, SectionEndNodeResolver::new);
        this.tokenParsers.add(openDef);
        this.tokenParsers.add(closeDef);
    }

    /**
     * Parses the expression.
     *
     * @param expression the expression to parse
     * @return the parsed {@link Expression}
     */
    public Expression parse(final String expression) {
        if (expression == null || expression.isBlank()) {
            return new EmptyExpression(this.codeModel);
        }

        final var tokenizer = new Tokenizer(this.tokenParsers);
        final var tokens = tokenizer.tokenize(expression);
        try {
            for (final var token : tokens) {
                this.stackManager.push(token.tokenParser().createNodeResolver(token));
            }
            final var result = this.stackManager.getExpressionRoot();
            return result.resolve();
        }
        catch (final ExpressionParserException e) {
            throw e;
        }
        catch (final RuntimeException e) {
            throw new ExpressionParserException("There was an error parsing the expression", e);
        }
    }
}
