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

import java.util.Objects;

/**
 * A {@link Trait} representing a single import declaration from the source file that declared a type.
 * Traits of this kind are ordered as they appear in source.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
public final class ImportDeclaration
    implements Trait {

    private final String qualifiedName;
    private final boolean isStatic;
    private final boolean isOnDemand;
    private final int order;

    private ImportDeclaration(final String qualifiedName, final boolean isStatic, final boolean isOnDemand, final int order) {
        this.qualifiedName = Objects.requireNonNull(qualifiedName, "qualifiedName");
        this.isStatic = isStatic;
        this.isOnDemand = isOnDemand;
        this.order = order;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isOnDemand() {
        return isOnDemand;
    }

    public int order() {
        return order;
    }

    public static ImportDeclaration of(final String qualifiedName, final int order) {
        return new ImportDeclaration(qualifiedName, false, false, order);
    }

    public static ImportDeclaration ofStatic(final String qualifiedName, final int order) {
        return new ImportDeclaration(qualifiedName, true, false, order);
    }

    public static ImportDeclaration ofOnDemand(final String qualifiedName, final int order) {
        return new ImportDeclaration(qualifiedName, false, true, order);
    }

    public static ImportDeclaration ofStaticOnDemand(final String qualifiedName, final int order) {
        return new ImportDeclaration(qualifiedName, true, true, order);
    }
}
