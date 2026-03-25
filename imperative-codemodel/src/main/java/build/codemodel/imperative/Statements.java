package build.codemodel.imperative;

/*-
 * #%L
 * Imperative Code Model
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
import build.codemodel.expression.VariableUsage;
import build.codemodel.expression.naming.VariableName;
import build.codemodel.foundation.CodeModel;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link CodeModel} wrapper for working with various {@link Statement}s.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public class Statements {

    /**
     * The {@link CodeModel} for which {@link Statement}s will be created.
     */
    private final CodeModel codeModel;

    /**
     * Constructs a {@link Statements} helper for the specified {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel}
     */
    public Statements(final CodeModel codeModel) {
        this.codeModel = Objects.requireNonNull(codeModel, "The CodeModel must not be null");
    }

    /**
     * Obtains the {@link CodeModel} for which {@link Statement}s will be created.
     *
     * @return the {@link CodeModel}
     */
    public CodeModel codeModel() {
        return codeModel;
    }

    /**
     * Creates an {@link Assignment} to the specified {@link VariableUsage} with the provided {@link Expression}.
     *
     * @param variableUsage the {@link VariableUsage}
     * @param expression    the {@link Expression}
     * @return the new {@link Assignment}
     */
    public Assignment assign(final VariableUsage variableUsage,
                             final Expression expression) {

        return Assignment.of(variableUsage, expression);
    }

    /**
     * Creates an {@link Assignment} to the specified {@link VariableName} with the provided {@link Expression}.
     *
     * @param variableName the {@link VariableName}
     * @param expression   the {@link Expression}
     * @return the new {@link Assignment}
     */
    public Assignment assign(final VariableName variableName,
                             final Expression expression) {

        return assign(VariableUsage.of(this.codeModel, variableName), expression);
    }

    /**
     * Creates a {@link Block} given the {@link Statement}s.
     *
     * @param statements the {@link Statement}s
     * @return a new {@link Block}
     */
    public Block blockOf(final Statement... statements) {
        return statements == null || statements.length == 0
            ? Block.empty(this.codeModel)
            : Block.of(statements);
    }

    /**
     * Creates an empty {@link Block}.
     *
     * @return a new empty {@link Block}
     */
    public Block emptyBlock() {
        return Block.empty(this.codeModel);
    }

    /**
     * Creates an {@link If} {@link Statement}.
     *
     * @param condition     the condition {@link Expression}
     * @param thenStatement the 'then' {@link Statement}
     * @param elseStatement the {@link Optional} 'else' {@link Statement}
     * @return the new {@link If} {@link Statement}
     */
    public If ifStatement(final Expression condition,
                          final Statement thenStatement,
                          final Optional<Statement> elseStatement) {

        return If.of(condition, thenStatement, elseStatement);
    }

    /**
     * Creates an {@link If} {@link Statement}.
     *
     * @param condition     the condition {@link Expression}
     * @param thenStatement the 'then' {@link Statement}
     * @return the new {@link If} {@link Statement}
     */
    public If ifStatement(final Expression condition,
                          final Statement thenStatement) {

        return If.of(condition, thenStatement);
    }
}
