package build.codemodel.jdk.populator.descriptor;

/*-
 * #%L
 * JDK Code Model
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

import build.base.foundation.UniformResource;
import build.base.telemetry.Location;
import build.codemodel.foundation.descriptor.Trait;

import java.net.URI;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * A {@link Trait} and {@link Location} recording where in source a descriptor originated.
 *
 * <p>Three flavors cover the contexts that arise during JDK-backed code model construction:
 * <ul>
 *   <li>{@link FilePosition} — a URI plus character offsets, produced when the full source tree is
 *       available (e.g. from {@code JdkInitializer}).</li>
 *   <li>{@link ElementRef} — a reference to the {@link Element} itself, produced when only the
 *       annotation-processing {@code Element} API is available.</li>
 *   <li>{@link AnnotationRef} — additionally records the {@link AnnotationMirror}.</li>
 *   <li>{@link AnnotationValueRef} — additionally records the {@link AnnotationValue}.</li>
 * </ul>
 */
public sealed interface SourceLocation extends Location, Trait {

    // --- Variants ---

    record FilePosition(URI uri, long startPosition, long endPosition) implements SourceLocation {
        public static FilePosition of(final URI uri, final long startPosition, final long endPosition) {
            return new FilePosition(uri, startPosition, endPosition);
        }
    }

    record ElementRef(Element element) implements SourceLocation {
        @Override
        public URI uri() {
            return UniformResource.createURI("element", element);
        }

        @Override
        public <T> Optional<T> as(final Class<T> c) {
            if (c == null) {
                return Optional.empty();
            }
            return c.isInstance(element) ? Optional.of(c.cast(element)) : Optional.empty();
        }

        public AnnotationRef withAnnotation(final AnnotationMirror mirror) {
            return new AnnotationRef(element, mirror);
        }
    }

    record AnnotationRef(Element element, AnnotationMirror mirror) implements SourceLocation {
        @Override
        public URI uri() {
            return UniformResource.createURI("element", element);
        }

        @Override
        public <T> Optional<T> as(final Class<T> c) {
            if (c == null) {
                return Optional.empty();
            }
            if (c.isInstance(mirror)) {
                return Optional.of(c.cast(mirror));
            }
            return c.isInstance(element) ? Optional.of(c.cast(element)) : Optional.empty();
        }

        public AnnotationValueRef withValue(final AnnotationValue value) {
            return new AnnotationValueRef(element, mirror, value);
        }
    }

    record AnnotationValueRef(Element element, AnnotationMirror mirror, AnnotationValue value)
        implements SourceLocation {

        @Override
        public URI uri() {
            return UniformResource.createURI("element", element);
        }

        @Override
        public <T> Optional<T> as(final Class<T> c) {
            if (c == null) {
                return Optional.empty();
            }
            if (c.isInstance(value)) {
                return Optional.of(c.cast(value));
            }
            if (c.isInstance(mirror)) {
                return Optional.of(c.cast(mirror));
            }
            return c.isInstance(element) ? Optional.of(c.cast(element)) : Optional.empty();
        }
    }

    // --- Factories ---

    static FilePosition filePosition(final URI uri, final long startPosition, final long endPosition) {
        return new FilePosition(uri, startPosition, endPosition);
    }

    static ElementRef elementRef(final Element element) {
        return new ElementRef(element);
    }

    static Optional<ElementRef> elementRefOrEmpty(final Element element) {
        return element == null ? Optional.empty() : Optional.of(new ElementRef(element));
    }
}
