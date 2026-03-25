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
 * A {@link Plugin} to perform <a href="https://en.wikipedia.org/wiki/Compiler">compilation</a> of a target within or
 * part of a type-safe {@link CodeModel}.
 * <p>
 * The target type may be a {@link CodeModel} or any part of a {@link CodeModel}, including
 * {@link Trait}s and {@link Traitable}s.
 * <p>
 * {@link Compiler}s are typically invoked as part of compiling a {@link CodeModel} with
 * {@link Framework#compile(CodeModel, TelemetryRecorder)}.
 * <p>
 * When compiling into {@code java} source files, an implementation should make use of an injectable {@code Filer}
 * to support automatic detection and compilation of generated sources.
 * <p>
 * {@link Compiler} implementations are typically located using the {@link java.util.ServiceLoader}
 * as a {@link Plugin} <i>service</i>.
 *
 * @param <T> the target type
 * @author brian.oliver
 * @see Compilation
 * @see TypeChecker
 * @see Framework
 * @see Framework#compile(CodeModel, TelemetryRecorder)
 * @since Mar-2024
 */
public interface Compiler<T>
    extends Plugin, Targetable<T> {

    @Override
    default Optional<? extends Class<T>> getTargetClass() {
        return Targetable.super.getTargetClass(Compiler.class);
    }

    /**
     * Attempts to compile the target, reporting any errors, warnings or information with the specified
     * {@link TelemetryRecorder}.
     *
     * @param target            the target to compile
     * @param codeModel        the {@link CodeModel} in which compilation is occurring
     * @param telemetryRecorder the {@link TelemetryRecorder}
     */
    void compile(T target,
                 CodeModel codeModel,
                 TelemetryRecorder telemetryRecorder);
}
