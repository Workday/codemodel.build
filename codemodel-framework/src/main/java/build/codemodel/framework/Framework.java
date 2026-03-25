package build.codemodel.framework;

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
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.framework.compiler.Compilation;
import build.codemodel.framework.compiler.Compiler;
import build.codemodel.framework.compiler.TypeChecker;
import build.codemodel.framework.completer.Completer;
import build.codemodel.framework.completer.Completion;
import build.codemodel.framework.initialization.Enricher;

import java.nio.file.FileSystem;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides <i>pluggable</i> infrastructure to support prototyping, type-checking, building and compiling
 * {@link CodeModel}s.
 *
 * @author brian.oliver
 * @see Plugin
 * @since Apr-2024
 */
public interface Framework {

    /**
     * Obtains the {@link FileSystem} to be used by {@link Plugin}s for accessing the file system.
     *
     * @return the {@link FileSystem}
     */
    FileSystem fileSystem();

    /**
     * Obtains the {@link Plugin}s.
     *
     * @return the {@link Stream} of {@link Plugin}s
     */
    Stream<Plugin> plugins();

    /**
     * Obtains the {@link Plugin}s that are assignable to the specified {@link Class}.
     *
     * @param requiredClass the required {@link Class}
     * @param <T>           the required type of {@link Class}
     * @return a {@link Stream} of {@link Plugin}s assignable to the specified {@link Class}
     */
    <T> Stream<T> plugins(Class<T> requiredClass);

    /**
     * Obtains the {@link NameProvider}.
     *
     * @return the {@link NameProvider}
     */
    NameProvider nameProvider();

    /**
     * Creates a new {@link CodeModel} that has been initialized by the {@link Framework}.
     *
     * @return a new {@link CodeModel}
     */
    CodeModel newCodeModel();

    /**
     * Creates a new {@link CodeModel} of the specified {@link Class} that has been initialized
     * through Dependency Injection by the {@link Framework}.
     *
     * @param <T> the type of {@link CodeModel}
     * @return a new {@link CodeModel}
     */
    <T extends CodeModel> T newCodeModel(Class<T> codeModelClass);

    /**
     * Enriches the provided {@link CodeModel} using the {@link Enricher}s known to the {@link Framework},
     * reporting any issues with the specified {@link TelemetryRecorder}.
     *
     * @param codeModel        the {@link CodeModel} to enrich
     * @param telemetryRecorder the {@link TelemetryRecorder}
     * @return the {@link CodeModel} to permit fluent-style method invocation
     */
    <T extends CodeModel> T enrich(T codeModel, TelemetryRecorder telemetryRecorder);

    /**
     * Performs type-checking on the provided {@link CodeModel} using the {@link TypeChecker}s known to the
     * {@link Framework}, reporting any issues with the specified {@link TelemetryRecorder}, returning
     * the {@link CodeModel} if successful.
     *
     * @param codeModel        the {@link CodeModel} to type-check
     * @param telemetryRecorder the {@link TelemetryRecorder}
     * @return the {@link Optional} {@link CodeModel} if type-checking was successful, otherwise {@link Optional#empty()}
     */

    <T extends CodeModel> Optional<T> typeCheck(T codeModel,
                                                 TelemetryRecorder telemetryRecorder);

    /**
     * Attempts to compile the specified {@link CodeModel} using the {@link Compiler}s known to the {@link Framework},
     * returning the {@link Optional} {@link Compilation} when successful, otherwise returning an {@link Optional#empty()},
     * with errors, warnings and other compilation information recorded in the provided {@link TelemetryRecorder}.
     *
     * @param codeModel        the {@link CodeModel}
     * @param telemetryRecorder the {@link TelemetryRecorder}
     * @return the {@link Optional} {@link Compilation} when compilation was successful, otherwise {@link Optional#empty()}
     */
    Optional<Compilation> compile(CodeModel codeModel,
                                  TelemetryRecorder telemetryRecorder);

    /**
     * Completes {@link Compilation} using the {@link Completer}s known to the {@link Framework}, returning the
     * the {@link Completion} when successful, otherwise returning {@link Optional#empty()},
     * with errors, warnings and other completion information recorded in the provided {@link TelemetryRecorder}.
     *
     * @param compilation       the {@link Compilation} to complete
     * @param telemetryRecorder the {@link TelemetryRecorder}
     * @return the {@link Optional} {@link Completion} when the completion was successful, otherwise {@link Optional#empty()}
     */
    Optional<Completion> complete(Compilation compilation,
                                  TelemetryRecorder telemetryRecorder);
}
