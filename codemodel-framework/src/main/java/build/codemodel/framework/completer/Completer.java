package build.codemodel.framework.completer;

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

import build.base.telemetry.TelemetryRecorder;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.Targetable;
import build.codemodel.framework.compiler.Compilation;
import build.codemodel.framework.compiler.Compiler;
import build.codemodel.foundation.CodeModel;

import java.util.Optional;

/**
 * Performs post-{@link Compilation} <i>completion</i> operations on a successfully compiled <i>target</i>
 * within a {@link CodeModel}.
 * <p>
 * While {@link Completer}s may have access to the file system and other resources, {@link Completer}s <b>should not</b>
 * create any further <i>new</i> content related to the {@link CodeModel} or {@link Compilation}.  Creation of content
 * based on a {@link CodeModel} is the task of a {@link Compiler}.
 * <p>
 * {@link Completer} implementations are typically located using the {@link java.util.ServiceLoader}
 * as a {@link Plugin} <i>service</i>.
 *
 * @author brian.oliver
 * @see Compilation
 * @since Mar-2024
 */
public interface Completer<T>
    extends Plugin, Targetable<T> {

    @Override
    default Optional<? extends Class<T>> getTargetClass() {
        return Targetable.super.getTargetClass(Completer.class);
    }

    /**
     * Attempts to complete the processing of the specified target that exists in the {@link CodeModel}
     * given the compilation {@link Compilation}, reporting any errors, warnings or information with the specified
     * {@link TelemetryRecorder}.
     *
     * @param target            the target to complete
     * @param compilation       the {@link Compilation}
     * @param telemetryRecorder the {@link TelemetryRecorder}
     */
    void complete(T target,
                  Compilation compilation,
                  TelemetryRecorder telemetryRecorder);
}
