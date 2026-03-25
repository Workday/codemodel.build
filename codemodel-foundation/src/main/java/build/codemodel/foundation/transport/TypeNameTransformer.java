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
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.TypeName;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A {@link Transformer} for {@link TypeName}s to and from {@link String}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
public class TypeNameTransformer
    implements Transformer<TypeName, String> {

    /**
     * The {@link NameProvider} to use for obtaining {@link TypeName}s
     */
    private final NameProvider nameProvider;

    /**
     * Constructs a {@link TypeNameTransformer} based on the specified {@link NameProvider}.
     *
     * @param nameProvider the {@link NameProvider}
     */
    public TypeNameTransformer(final NameProvider nameProvider) {
        this.nameProvider = Objects.requireNonNull(nameProvider, "The NameProvider must not be null");
    }

    @Override
    public Class<? extends TypeName> sourceClass() {
        return TypeName.class;
    }

    @Override
    public Class<? extends String> targetClass() {
        return String.class;
    }

    @Override
    public String transform(final Marshaller marshaller,
                            final TypeName typeName) {

        return typeName.toString();
    }

    @Override
    public TypeName reform(final Marshaller marshaller,
                           final Type type,
                           final String string) {

        return this.nameProvider.getTypeName(string);
    }
}
