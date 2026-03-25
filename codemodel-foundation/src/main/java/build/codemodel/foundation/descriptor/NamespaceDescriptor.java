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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.Namespace;

import java.util.Optional;

/**
 * Provides information concerning the <i>definition</i> of a {@link Namespace}.
 *
 * @author brian.oliver
 * @see Namespace
 * @since Jun-2025
 */
public interface NamespaceDescriptor
    extends Traitable {

    /**
     * The {@link Namespace} of the {@link NamespaceDescriptor}.
     *
     * @return the {@link Namespace}
     */
    Namespace namespace();

    /**
     * Determines if this {@link NamespaceDescriptor} is a <i>root</i> {@link Namespace}.
     *
     * @return {@code true} if this {@link NamespaceDescriptor} is a root {@link Namespace}, otherwise {@code false}
     */
    default boolean isRoot() {
        return namespace().isRoot();
    }

    /**
     * Attempts to obtain the parent {@link NamespaceDescriptor} of this {@link NamespaceDescriptor}.
     *
     * @return the {@link Optional} parent {@link NamespaceDescriptor}, or an empty {@link Optional} if this is a
     * root {@link NamespaceDescriptor}
     * @throws IllegalStateException should the parent {@link NamespaceDescriptor} not be defined in the {@link CodeModel}
     */
    default Optional<NamespaceDescriptor> parent() {
        return namespace().parent()
            .map(parent -> codeModel()
                .getNamespaceDescriptor(parent)
                .orElseThrow(() -> new IllegalStateException(
                    "Parent NamespaceDescriptor not defined in CodeModel for: " + namespace())));
    }
}
