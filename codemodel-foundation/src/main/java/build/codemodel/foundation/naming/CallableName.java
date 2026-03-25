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

import build.codemodel.foundation.CodeModel;

import java.util.Optional;

/**
 * A {@link Name} of a <i>Callable Unit</i> with in {@link CodeModel}, typically for an executable
 * <a href="https://en.wikipedia.org/wiki/Function_(computer_programming)">Function</a>,
 * <a href="https://en.wikipedia.org/wiki/Method_(computer_programming)">Method</a>, operation,
 * subroutine, procedure, or analogous structure that appears within a module.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public interface CallableName
    extends Name {

    /**
     * The {@link Optional} {@link ModuleName} in which the {@link CallableName} is defined.
     *
     * @return the {@link Optional} {@link ModuleName}
     */
    Optional<ModuleName> moduleName();

    /**
     * The {@link Optional} {@link Namespace} in which the {@link CallableName} is defined.
     *
     * @return the {@link Optional} {@link Namespace}
     */
    Optional<Namespace> namespace();

    /**
     * Obtains the {@link Optional} {@link TypeName} in which this {@link CallableName} is defined.
     *
     * @return the {@link Optional} {@link CallableName}
     */
    Optional<TypeName> typeName();

    /**
     * The {@link IrreducibleName} name of the {@link CallableName}.
     *
     * @return the {@link IrreducibleName}
     */
    IrreducibleName name();

    /**
     * Obtains the <i>canonical-name</i>.
     *
     * @return the canonical name
     */
    default String canonicalName() {
        return namespace()
            .map(p -> p + ".")
            .orElse("")
            + typeName()
            .map(e -> e.name() + ".")
            .orElse("")
            + name().toString();
    }
}
