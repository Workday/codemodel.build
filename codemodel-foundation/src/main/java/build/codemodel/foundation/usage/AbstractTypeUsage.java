package build.codemodel.foundation.usage;

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

import build.base.foundation.Capture;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Out;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;

import java.util.stream.Stream;

/**
 * An abstract {@link TypeUsage} implementation.
 *
 * @author brian.oliver
 * @since Apr-2024
 */
public abstract class AbstractTypeUsage
    extends AbstractTraitable
    implements TypeUsage {

    /**
     * Constructs an {@link AbstractTypeUsage} with in a {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel}
     */
    public AbstractTypeUsage(final CodeModel codeModel) {
        super(codeModel);
    }

    /**
     * {@link Unmarshal} an {@link AbstractTypeUsage}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractTypeUsage(final CodeModel codeModel,
                                final Marshaller marshaller,
                                final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);
    }

    /**
     * {@link Marshal} an {@link AbstractTypeUsage}.
     *
     * @param marshaller the {@link Marshaller}
     * @param traits     the {@link Capture}d {@link Marshalled} {@link Trait}s
     */
    protected void destructor(final Marshaller marshaller,
                              final Out<Stream<Marshalled<Trait>>> traits) {

        super.destructor(marshaller, traits);
    }
}
