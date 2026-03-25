package build.codemodel.foundation.descriptor;

/*-
 * #%L
 * Code Model Foundation
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that zero or more instances of a {@link Class} of {@link Trait} may be present in a {@link Traitable}.
 * <p>
 * This annotation is provided for <i>symmetry</i> with the {@link Singular} annotation.  The {@link NonSingular}
 * annotation is not required to be specified for {@link Trait}s as {@link NonSingular} is the default behavior
 * for {@link Trait}s that aren't {@link Singular}.  While not required, it is often helpful for documentation
 * purposes.
 *
 * @author brian.oliver
 * @since Mar-2025
 * @see Singular
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NonSingular {

}
