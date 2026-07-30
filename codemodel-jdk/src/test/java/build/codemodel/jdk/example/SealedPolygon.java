package build.codemodel.jdk.example;

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

/**
 * A {@code non-sealed} permitted subtype of {@link SealedShape}, for testing {@code sealed}/
 * {@code non-sealed} discovery via reflection. {@code java.lang.reflect.Modifier} has no bit
 * for {@code non-sealed}, so the reflection path must infer it from the class hierarchy.
 *
 */
public non-sealed class SealedPolygon implements SealedShape {
}
