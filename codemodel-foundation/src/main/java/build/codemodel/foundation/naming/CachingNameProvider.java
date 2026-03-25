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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link NameProvider} adapter that caches previously requested names for another {@link NameProvider}.
 *
 * @author brian.oliver
 * @since Jun-2024
 */
public class CachingNameProvider
    implements NameProvider {

    /**
     * The underlying {@link NameProvider} on which to delegate requests for uncached values.
     */
    private final NameProvider delegate;

    /**
     * The cache of {@link IrreducibleName}s by name.
     */
    private final ConcurrentHashMap<String, IrreducibleName> irreducibleNames;

    /**
     * The cache of {@link Namespace}s by name.
     */
    private final ConcurrentHashMap<String, Optional<Namespace>> namespaces;

    /**
     * The cache of {@link ModuleName}s by name.
     */
    private final ConcurrentHashMap<String, Optional<ModuleName>> moduleNames;

    /**
     * Constructs a {@link CachingNameProvider}.
     *
     * @param delegate the {@link NameProvider} onto which requests will be delegated for non-cached values
     */
    public CachingNameProvider(final NameProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate, "The NameProvider must not be null");
        this.irreducibleNames = new ConcurrentHashMap<>();
        this.namespaces = new ConcurrentHashMap<>();
        this.moduleNames = new ConcurrentHashMap<>();
    }

    @Override
    public IrreducibleName getIrreducibleName(final String string) {
        if (string == null) {
            return delegate.getIrreducibleName(string);
        }

        var name = this.irreducibleNames.get(string);

        if (name == null) {
            name = this.delegate.getIrreducibleName(string);
            this.irreducibleNames.put(string, name);
        }

        return name;
    }

    @Override
    public Optional<Namespace> getNamespace(final String string) {
        if (string == null) {
            return Optional.empty();
        }

        var namespace = this.namespaces.get(string);

        if (namespace == null) {
            namespace = this.delegate.getNamespace(string);
            this.namespaces.put(string, namespace);
        }

        return namespace;
    }

    @Override
    public Optional<ModuleName> getModuleName(final String string) {
        if (string == null) {
            return Optional.empty();
        }

        var moduleName = this.moduleNames.get(string);

        if (moduleName == null) {
            moduleName = this.delegate.getModuleName(string);
            this.moduleNames.put(string, moduleName);
        }

        return moduleName;
    }

    @Override
    public TypeName getTypeName(final Optional<ModuleName> moduleName,
                                final Optional<Namespace> namespace,
                                final Optional<TypeName> enclosingTypeName,
                                final IrreducibleName irreducibleName) {

        return this.delegate.getTypeName(moduleName, namespace, enclosingTypeName, irreducibleName);
    }
}
