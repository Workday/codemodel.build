package build.codemodel.framework.compiler;

/*-
 * #%L
 * Code Model Framework
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

import build.codemodel.framework.Framework;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.Targetable;
import build.base.telemetry.TelemetryRecorder;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;

import java.util.Optional;

/**
 * A {@link Plugin} to perform <a href="https://en.wikipedia.org/wiki/Type_system">type checking</a> of a target type
 * when building a {@link CodeModel}.
 * <p>
 * The target type may be a {@link CodeModel} or any part of a {@link CodeModel}, including
 * {@link Trait}s and {@link Traitable}s.
 * <p>
 * {@link TypeChecker} implementations are typically located using the {@link java.util.ServiceLoader}
 * as a {@link Plugin} <i>service</i>, or they are provided by a {@link Framework}.
 *
 * @param <T> the target type to be checked
 * @author brian.oliver
 * @see Compiler
 * @see Framework
 * @since Feb-2024
 */
public interface TypeChecker<T>
    extends Plugin, Targetable<T> {

    @Override
    default Optional<? extends Class<T>> getTargetClass() {
        return Targetable.super.getTargetClass(TypeChecker.class);
    }

    /**
     * Performs type checking of the target.
     * <p>
     * Errors, Warnings, Notes and problems should be reported using the {@link TelemetryRecorder}.  No exceptions
     * should be thrown, unless they are to report unrecoverable catastrophic {@link TypeChecker} implementation
     * failures.
     *
     * @param target            the target to be checked
     * @param codeModel        the {@link CodeModel}
     * @param telemetryRecorder the {@link TelemetryRecorder}
     */
    void check(T target,
               CodeModel codeModel,
               TelemetryRecorder telemetryRecorder);
}
