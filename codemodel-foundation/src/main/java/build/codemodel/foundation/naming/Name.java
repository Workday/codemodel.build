package build.codemodel.foundation.naming;

/*-
 * #%L
 * Code Model Foundation
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
 * An immutable case-sensitive {@link Comparable} sequence of characters representing a name.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
public interface Name {

    /**
     * Obtains the number of characters in the {@link Name}.
     *
     * @return the number of characters
     */
    int length();

    /**
     * Determines if the {@link Name} is empty (contains no characters).
     *
     * @return {@code true} if the {@link Name} is empty, {@code false} otherwise
     */
    default boolean isEmpty() {
        return length() == 0;
    }

    /**
     * Determines if the {@link Name} matches the provided regular expression.
     *
     * @param regularExpression the regular expression
     * @return {@code true} if the name matches, {@code false} otherwise¬
     */
    boolean matches(String regularExpression);
}
