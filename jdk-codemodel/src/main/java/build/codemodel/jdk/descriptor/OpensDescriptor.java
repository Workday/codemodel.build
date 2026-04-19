package build.codemodel.jdk.descriptor;

/*-
 * #%L
 * JDK Code Model
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

import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link Trait} representing an {@code opens} directive in a {@code module-info.java}.
 * An empty {@code targetModuleNames} list means an unqualified open.
 *
 * @param packageName       the opened package
 * @param targetModuleNames the modules this package is opened to; empty means unqualified
 * @param modifier          an optional {@link PackageDirectiveModifier}; only present in bytecode
 * @author reed.vonredwitz
 * @since Apr-2026
 */
@NonSingular
public record OpensDescriptor(Namespace packageName,
                               List<ModuleName> targetModuleNames,
                               Optional<PackageDirectiveModifier> modifier)
    implements Trait {

    public OpensDescriptor {
        targetModuleNames = List.copyOf(targetModuleNames);
        Objects.requireNonNull(modifier, "modifier must not be null");
    }

    /**
     * Creates an {@link OpensDescriptor} with no modifier (source-parsed).
     */
    public static OpensDescriptor of(final Namespace packageName,
                                     final Stream<ModuleName> targetModuleNames) {
        return new OpensDescriptor(packageName, targetModuleNames.toList(), Optional.empty());
    }

    /**
     * Creates an {@link OpensDescriptor} with a {@link PackageDirectiveModifier} (bytecode-extracted).
     */
    public static OpensDescriptor of(final Namespace packageName,
                                     final Stream<ModuleName> targetModuleNames,
                                     final PackageDirectiveModifier modifier) {
        return new OpensDescriptor(packageName, targetModuleNames.toList(), Optional.of(modifier));
    }
}
