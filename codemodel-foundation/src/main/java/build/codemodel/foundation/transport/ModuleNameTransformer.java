package build.codemodel.foundation.transport;

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

import build.base.marshalling.Marshaller;
import build.base.transport.Transformer;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A {@link Transformer} for {@link ModuleName}s to and from {@link String}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
public class ModuleNameTransformer
    implements Transformer<ModuleName, String> {

    /**
     * The {@link NameProvider} to use for obtaining {@link ModuleName}s
     */
    private final NameProvider nameProvider;

    /**
     * Constructs a {@link ModuleNameTransformer} based on the specified {@link NameProvider}.
     *
     * @param nameProvider the {@link NameProvider}
     */
    public ModuleNameTransformer(final NameProvider nameProvider) {
        this.nameProvider = Objects.requireNonNull(nameProvider, "The NameProvider must not be null");
    }

    @Override
    public Class<? extends ModuleName> sourceClass() {
        return ModuleName.class;
    }

    @Override
    public Class<? extends String> targetClass() {
        return String.class;
    }

    @Override
    public String transform(final Marshaller marshaller,
                            final ModuleName moduleName) {

        return moduleName.toString();
    }

    @Override
    public ModuleName reform(final Marshaller marshaller,
                             final Type type,
                             final String string) {

        return this.nameProvider.getModuleName(string)
            .orElseThrow(() -> new IllegalArgumentException("Failed to determine ModuleName for [" + string + "]"));
    }
}
