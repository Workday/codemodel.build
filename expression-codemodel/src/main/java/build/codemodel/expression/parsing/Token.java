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

import build.base.io.LookaheadReader;
import build.codemodel.expression.parsing.tokenparsers.TokenParser;

/**
 * Represents a Token in an expression.
 *
 * @param tokenParser the {@link TokenParser} that parsed this token
 * @param location the {@link LookaheadReader.Location} of the token in the expression
 * @param value    the value of the token extracted from the expression
 *
 * @author tim.berston
 * @since Nov-2024
 */
public record Token(TokenParser tokenParser, LookaheadReader.Location location, String value) {

    /**
     * Trims the first and last character from the token value.
     *
     * @return the value of the token with the leading and trailing characters removed
     */
    public String trimFirstAndLast() {
        return this.value.substring(1, this.value.length() - 1);
    }

}
