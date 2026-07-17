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

import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;

import java.util.Objects;
import java.util.Optional;

/**
 * A single structural reference from one type to another.
 *
 * @param owner  the type that contains the reference
 * @param kind   the structural role in which the target type is referenced
 * @param member the specific {@link Trait} (field, method, constructor…) carrying the reference,
 *               or empty for type-level references ({@link ReferenceKind#EXTENDS} /
 *               {@link ReferenceKind#IMPLEMENTS})
 */
public record TypeReference(TypeDescriptor owner,
                            ReferenceKind kind,
                            Optional<Trait> member) {

    public TypeReference {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(member, "member");
    }

    /**
     * Factory for type-level references (extends, implements) that have no associated member.
     */
    public static TypeReference of(final TypeDescriptor owner,
                                   final ReferenceKind kind) {
        return new TypeReference(owner, kind, Optional.empty());
    }

    /**
     * Factory for member-level references (field, method, constructor).
     */
    public static TypeReference of(final TypeDescriptor owner,
                                   final ReferenceKind kind,
                                   final Trait member) {
        return new TypeReference(owner, kind, Optional.of(member));
    }
}
