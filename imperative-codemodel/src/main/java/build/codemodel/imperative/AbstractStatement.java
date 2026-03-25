package build.codemodel.imperative;

/*-
 * #%L
 * Imperative Code Model
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

import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;

import java.util.stream.Stream;

/**
 * An abstract {@link Statement}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
public abstract class AbstractStatement
    extends AbstractTraitable
    implements Statement {

    /**
     * Constructs a {@link AbstractStatement}.
     *
     * @param codeModel the {@link CodeModel}
     */
    protected AbstractStatement(final CodeModel codeModel) {
        super(codeModel);
    }

    /**
     * {@link Unmarshal} an {@link AbstractStatement}.
     *
     * @param codeModel the {@link CodeModel}
     * @param marshaller the {@link Marshaller} for unmarshalling the {@link Marshalled} {@link Trait}s
     * @param traits     the {@link Marshalled} {@link Trait}s
     */
    protected AbstractStatement(final CodeModel codeModel,
                                final Marshaller marshaller,
                                final Stream<Marshalled<Trait>> traits) {

        super(codeModel, marshaller, traits);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return object instanceof AbstractStatement other
            && super.equals(other);
    }
}
