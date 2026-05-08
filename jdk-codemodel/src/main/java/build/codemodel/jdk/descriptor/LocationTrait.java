package build.codemodel.jdk.descriptor;

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

import build.base.telemetry.Location;
import build.codemodel.foundation.descriptor.Trait;

import java.net.URI;

/**
 * A {@link Trait} and {@link Location} recording the source position of a declared member
 * (field, method, constructor, etc.). {@code startPosition} and {@code endPosition} are character
 * offsets from the start of the source file identified by {@code uri}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
public record LocationTrait(URI uri, long startPosition, long endPosition)
    implements Trait, Location {

    public static LocationTrait of(final URI uri, final long startPosition, final long endPosition) {
        return new LocationTrait(uri, startPosition, endPosition);
    }
}
