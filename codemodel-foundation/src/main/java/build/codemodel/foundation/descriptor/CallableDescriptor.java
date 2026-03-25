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

import build.base.foundation.stream.Streams;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.CallableName;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.stream.Stream;

/**
 * Describes a <i>Callable Unit</i> with in {@link CodeModel}, defined for a {@link TypeDescriptor}, typically for an
 * executable <a href="https://en.wikipedia.org/wiki/Function_(computer_programming)">Function</a>,
 * <a href="https://en.wikipedia.org/wiki/Method_(computer_programming)">Method</a>, operation,
 * subroutine, procedure, or analogous structure that appears within a module.
 *
 * @author brian.oliver
 * @see TypeDescriptor
 * @since Jan-2024
 */
public interface CallableDescriptor
    extends Trait, Dependent, Traitable {

    /**
     * Obtains the {@link TypeDescriptor} in which the {@link CallableDescriptor} is defined.
     *
     * @return the {@link TypeDescriptor}
     */
    TypeDescriptor typeDescriptor();

    /**
     * Obtains the {@link CallableName}.
     *
     * @return the {@link CallableName}
     */
    CallableName callableName();

    /**
     * Obtains the return {@link TypeUsage} of the <i>Callable</i>.
     *
     * @return the return {@link TypeUsage}
     */
    TypeUsage returnType();

    /**
     * Obtains the number of {@link FormalParameterDescriptor}s.
     *
     * @return the number of {@link FormalParameterDescriptor}
     */
    int getFormalParameterCount();

    /**
     * Obtains the {@link FormalParameterDescriptor} at the specified index.
     *
     * @param index the zero-based index
     * @return the {@link FormalParameterDescriptor}
     * @throws IndexOutOfBoundsException
     */
    FormalParameterDescriptor getFormalParameter(int index)
        throws IndexOutOfBoundsException;

    /**
     * Determines if the <i>Callable</i> has any {@link FormalParameterDescriptor}s.
     *
     * @return {@code true} if there are {@link FormalParameterDescriptor}s, {@code false} otherwise
     */
    default boolean hasFormalParameters() {
        return getFormalParameterCount() > 0;
    }

    /**
     * Obtains the {@link FormalParameterDescriptor}s for the <i>Callable</i>.
     *
     * @return a {@link Stream} of {@link FormalParameterDescriptor}s
     */
    Stream<FormalParameterDescriptor> formalParameters();

    /**
     * Obtains the {@link TypeUsage}s of the {@link ThrowableDescriptor} {@link Trait}s on the
     * {@link CallableDescriptor}.
     *
     * @return a {@link Stream} of {@link TypeUsage}s
     */
    default Stream<TypeUsage> throwables() {
        return hasTraits()
            ? traits(ThrowableDescriptor.class)
            .map(ThrowableDescriptor::throwable)
            : Stream.empty();
    }

    @Override
    default Stream<TypeUsage> dependencies() {
        return Streams.concat(
            Stream.of(returnType()),
            formalParameters()
                .map(FormalParameterDescriptor::type),
            throwables(),
            traits(Dependent.class)
                .flatMap(Dependent::dependencies));
    }
}
