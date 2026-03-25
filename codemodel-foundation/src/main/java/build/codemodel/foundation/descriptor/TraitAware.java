package build.codemodel.foundation.descriptor;

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
 * A {@link Traitable} that is aware of {@link Trait}s being dynamically added and removed.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public interface TraitAware
    extends Traitable {

    /**
     * Invoked when a {@link Trait} is added to the {@link Traitable}.
     *
     * @param trait the {@link Trait}
     */
    default void onAddedTrait(final Trait trait) {
        // by default, nothing happens
    }

    /**
     * Invoked when a {@link Trait} is removed from the {@link Traitable}.
     *
     * @param trait the {@link Trait}
     */
    default void onRemovedTrait(final Trait trait) {
        // by default, nothing happens
    }
}
