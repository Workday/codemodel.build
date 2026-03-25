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

import build.base.telemetry.Location;

import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * A {@link Location} of an {@link AnnotationMirror}.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public class AnnotationMirrorLocation
    extends ElementLocation {

    /**
     * The {@link AnnotationMirror} representing the <i>Annotation</i>.
     */
    private final AnnotationMirror annotationMirror;

    /**
     * Constructs an {@link AnnotationMirrorLocation} given an {@link Element}.
     *
     * @param element          the {@link Element}
     * @param annotationMirror the {@link AnnotationMirror}
     */
    public AnnotationMirrorLocation(final Element element,
                                    final AnnotationMirror annotationMirror) {

        super(element);
        this.annotationMirror = Objects.requireNonNull(annotationMirror, "The AnnotationMirror must not be null");
    }

    /**
     * Obtains the {@link AnnotationMirror}.
     *
     * @return the {@link AnnotationMirror}
     */
    public AnnotationMirror annotationMirror() {
        return this.annotationMirror;
    }

    @Override
    public <T> Optional<T> as(final Class<T> c) {
        return c == null
            ? Optional.empty()
            : (c.isInstance(this.annotationMirror)
                ? Optional.of(c.cast(this.annotationMirror))
                : super.as(c));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof AnnotationMirrorLocation other
            && Objects.equals(this.annotationMirror, other.annotationMirror);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.annotationMirror);
    }

    /**
     * Creates an {@link AnnotationValueLocation} for the {@link AnnotationValue} within the {@link AnnotationMirror}.
     *
     * @param annotationValue the {@link AnnotationValue}.
     * @return an {@link AnnotationValueLocation}
     */
    public AnnotationValueLocation createAnnotationValueLocation(final AnnotationValue annotationValue) {
        Objects.requireNonNull(annotationValue, "The AnnotationValue must not be null");
        return new AnnotationValueLocation(element(), annotationMirror(), annotationValue);
    }
}
