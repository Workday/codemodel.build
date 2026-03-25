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
 * A {@link Location} of an {@link AnnotationValue}.
 *
 * @author brian.oliver
 * @since Feb-2024
 */
public class AnnotationValueLocation
    extends AnnotationMirrorLocation {

    /**
     * The {@link AnnotationValue}.
     */
    private final AnnotationValue annotationValue;

    /**
     * Constructs an {@link AnnotationValueLocation} given an {@link Element}.
     *
     * @param element          the {@link Element}
     * @param annotationMirror the {@link AnnotationMirror}
     * @param annotationValue  the {@link AnnotationValue}
     */
    public AnnotationValueLocation(final Element element,
                                   final AnnotationMirror annotationMirror,
                                   final AnnotationValue annotationValue) {

        super(element, annotationMirror);
        this.annotationValue = Objects.requireNonNull(annotationValue, "The AnnotationValue must not be null");
    }

    /**
     * Obtains the {@link AnnotationValue}.
     *
     * @return the {@link AnnotationValue}
     */
    public AnnotationValue annotationValue() {
        return this.annotationValue;
    }

    @Override
    public <T> Optional<T> as(final Class<T> c) {
        return c == null
            ? Optional.empty()
            : (c.isInstance(this.annotationValue)
                ? Optional.of(c.cast(this.annotationValue))
                : super.as(c));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        return object instanceof AnnotationValueLocation other
            && Objects.equals(this.annotationValue, other.annotationValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.annotationValue);
    }
}
