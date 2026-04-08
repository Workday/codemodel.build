package build.codemodel.jdk.expression;

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
 * The operator of a {@link CompoundAssignment} expression.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public enum AssignmentOperator {
    /** Simple assignment: {@code x = y} */
    ASSIGN,
    /** Addition assignment: {@code x += y} */
    PLUS,
    /** Subtraction assignment: {@code x -= y} */
    MINUS,
    /** Multiplication assignment: {@code x *= y} */
    MULTIPLY,
    /** Division assignment: {@code x /= y} */
    DIVIDE,
    /** Remainder assignment: {@code x %= y} */
    REMAINDER,
    /** Bitwise AND assignment: {@code x &= y} */
    AND,
    /** Bitwise OR assignment: {@code x |= y} */
    OR,
    /** Bitwise XOR assignment: {@code x ^= y} */
    XOR,
    /** Left shift assignment: {@code x <<= y} */
    LEFT_SHIFT,
    /** Signed right shift assignment: {@code x >>= y} */
    RIGHT_SHIFT,
    /** Unsigned right shift assignment: {@code x >>>= y} */
    UNSIGNED_RIGHT_SHIFT
}
