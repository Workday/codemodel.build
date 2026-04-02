/*-
 * #%L
 * JDK Annotation Processor
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

import build.codemodel.annotation.discovery.AnnotationDiscovery;
import build.codemodel.framework.Plugin;
import build.codemodel.framework.compiler.Compiler;
import build.codemodel.framework.compiler.TypeChecker;
import build.codemodel.framework.completer.Completer;
import build.codemodel.framework.initialization.Enricher;

import java.lang.reflect.Type;
import javax.annotation.processing.Processor;

/**
 * Defines an <i>Annotation Processor</i> to reverse-engineer the definition of JDK-based {@link Type}s into a
 * {@code CodeModel} during compilation.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
module build.codemodel.jdk.annotation.processor {
    requires java.compiler;

    requires com.google.auto.service;

    requires jakarta.inject;

    requires build.codemodel.jdk.annotation.discovery;

    requires build.base.foundation;
    requires build.base.mereology;
    requires build.base.query;
    requires build.base.marshalling;
    requires build.base.telemetry;
    requires build.base.telemetry.foundation;

    requires build.codemodel.foundation;
    requires build.codemodel.imperative;
    requires build.codemodel.objectoriented;
    requires build.codemodel.jdk;

    requires build.codemodel.injection;

    requires build.codemodel.framework;
    requires build.codemodel.framework.builder;

    provides Processor with build.codemodel.annotation.processing.AnnotationProcessor;

    exports build.codemodel.annotation.processing;

    uses AnnotationDiscovery;

    uses Plugin;

    uses Compiler;
    uses TypeChecker;
    uses Completer;
    uses Enricher;
}
