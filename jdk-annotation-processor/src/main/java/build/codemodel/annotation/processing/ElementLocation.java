package build.codemodel.annotation.processing;

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

import build.base.foundation.UniformResource;
import build.base.telemetry.Location;
import build.codemodel.foundation.descriptor.Trait;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/**
 * An {@link Element}-based {@link Location}.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public class ElementLocation
    implements Location, Trait {

    /**
     * The {@link Element} on which the {@link Location} is based.
     */
    private final Element element;

    /**
     * Constructs an {@link ElementLocation} given an {@link Element}.
     *
     * @param element the {@link Element}
     */
    protected ElementLocation(final Element element) {
        this.element = Objects.requireNonNull(element, "The Element must not be null");
    }

    /**
     * Obtains the {@link Element}.
     *
     * @return the {@link Element}
     */
    public Element element() {
        return this.element;
    }

    @Override
    public <T> Optional<T> as(final Class<T> c) {
        return c == null
            ? Optional.empty()
            : (c.isInstance(this.element) ? Optional.of(c.cast(this.element)) : Optional.empty());
    }

    @Override
    public URI uri() {
        return UniformResource.createURI("element", this.element);
    }

    /**
     * Creates an {@link AnnotationMirrorLocation} for the <i>Annotation</i> defined within the {@link Element}.
     *
     * @param annotationMirror the {@link AnnotationMirror}.
     * @return an {@link Location}
     */
    public AnnotationMirrorLocation createAnnotationMirrorLocation(final AnnotationMirror annotationMirror) {
        Objects.requireNonNull(annotationMirror, "The AnnotationMirror must not be null");
        return new AnnotationMirrorLocation(this.element, annotationMirror);
    }

    /**
     * Creates an {@link Optional} {@link ElementLocation} given a {@code nullable} {@link Element}.
     *
     * @param element the {@code nullable} {@link Element}
     * @return a {@link ElementLocation}
     */
    public static Optional<ElementLocation> ofNullable(final Element element) {
        return element == null ? Optional.empty() : Optional.of(new ElementLocation(element));
    }

    /**
     * Creates a {@code nullable} {@link ElementLocation} from an {@link Element}.
     *
     * @param element the {@code nullable} {@link Element}
     * @return a {@code nullable} {@link ElementLocation}
     */
    public static ElementLocation of(final Element element) {
        return element == null ? null : new ElementLocation(element);
    }
}
