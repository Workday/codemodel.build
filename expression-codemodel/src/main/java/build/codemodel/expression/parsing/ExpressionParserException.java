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

/**
 * Represents an exception that occurs during expression parsing.
 *
 * @author tim.berston
 * @since Nov-2024
 */
public class ExpressionParserException extends RuntimeException {

    /**
     * Constructs a new {@link ExpressionParserException} with the specified message.
     *
     * @param message the exception message
     */
    public ExpressionParserException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@link ExpressionParserException} with the specified message and cause.
     *
     * @param message the exception message
     * @param cause the exception that caused this exception
     */
    public ExpressionParserException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
