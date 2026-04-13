package build.codemodel.injection;

/*-
 * #%L
 * Dependency Injection
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown by {@link Context#validate()} when one or more validation problems are found. Unlike exceptions
 * thrown during injection, this collects <em>all</em> detected problems and reports them together so the
 * caller can fix multiple issues in a single iteration.
 *
 * @author reed.vonredwitz
 * @see Context#validate()
 * @since Apr-2026
 */
public class ValidationException
    extends InjectionException {

    /**
     * The list of human-readable problem descriptions detected during validation.
     */
    private final List<String> problems;

    /**
     * Constructs a {@link ValidationException} with the specified list of problems.
     *
     * @param problems the list of problem descriptions; must not be empty
     */
    public ValidationException(final List<String> problems) {
        this.problems = List.copyOf(problems);
    }

    /**
     * Returns the list of problems detected during validation.
     *
     * @return an unmodifiable list of problem descriptions
     */
    public List<String> problems() {
        return this.problems;
    }

    @Override
    public String getMessage() {
        return "Validation failed with " + this.problems.size() + " problem(s):\n"
            + this.problems.stream()
            .map(p -> "  - " + p)
            .collect(Collectors.joining("\n"));
    }
}
