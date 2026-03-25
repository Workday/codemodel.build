package build.codemodel.expression;

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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;

/**
 * Defines an <a href="https://en.wikipedia.org/wiki/Expression_(mathematics)">Expression</a> based on the types
 * in a {@link CodeModel}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public interface Expression
    extends Traitable {

    /**
     * Obtains the {@link Optional}ly defined {@link TypeUsage} with in the {@link CodeModel}, representing the type
     * of value produced when the expression is evaluated.  When a {@link TypeUsage} can't be determined, or if it's
     * unknown when the expression is defined, {@link Optional#empty()} is returned.  The lack of a {@link TypeUsage}
     * does not indicate that a {@link TypeUsage} can't be inferred by some mechanism at some later point in time.  It
     * simply means that one isn't available for the expression when, where or how it was defined.
     *
     * @return the {@link Optional} {@link TypeUsage}
     */
    Optional<TypeUsage> type();
}
