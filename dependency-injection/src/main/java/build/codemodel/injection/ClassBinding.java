package build.codemodel.injection;

/*-
 * #%L
 * Dependency Injection
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

/**
 * A {@link Binding} that requires an instance of a concrete {@link Class} to be created.
 *
 * @param <T> the type of {@link Class}.
 * @author brian.oliver
 * @since Oct-2024
 */
public interface ClassBinding<T>
    extends Binding<T> {

    /**
     * The concrete {@link Class}.
     *
     * @return the concrete {@link Class}
     */
    Class<? extends T> concreteClass();
}
