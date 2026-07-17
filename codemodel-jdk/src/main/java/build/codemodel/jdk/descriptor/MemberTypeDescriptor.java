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

import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.TypeName;

import java.util.Objects;

/**
 * A {@link Trait} on an enclosing type that identifies one of its directly declared member (nested/inner) types.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
public final class MemberTypeDescriptor
    implements Trait {

    private final TypeName memberTypeName;

    private MemberTypeDescriptor(final TypeName memberTypeName) {
        this.memberTypeName = Objects.requireNonNull(memberTypeName, "memberTypeName");
    }

    public static MemberTypeDescriptor of(final TypeName typeName) {
        return new MemberTypeDescriptor(typeName);
    }

    public TypeName memberTypeName() {
        return memberTypeName;
    }
}
