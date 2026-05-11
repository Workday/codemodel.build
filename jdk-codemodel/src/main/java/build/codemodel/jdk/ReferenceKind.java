package build.codemodel.jdk;

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
 * The structural role in which one type references another.
 */
public enum ReferenceKind {
    /**
     * The type appears in an {@code extends} clause.
     */
    EXTENDS,
    /**
     * The type appears in an {@code implements} clause.
     */
    IMPLEMENTS,
    /**
     * The type is the declared type of a field.
     */
    FIELD_TYPE,
    /**
     * The type is the return type of a method.
     */
    RETURN_TYPE,
    /**
     * The type appears as a formal parameter type.
     */
    PARAMETER_TYPE,
    /**
     * The type is referenced inside a method or constructor body.
     */
    METHOD_BODY,
}
