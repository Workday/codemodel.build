package build.codemodel.objectoriented.descriptor;

/*-
 * #%L
 * Object-Oriented Code Model
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

import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;

/**
 * A {@link Trait} recording the zero-based position of a member (a field, method, or constructor) among
 * <i>all</i> members of its enclosing type, in declaration order — fields, constructors, and methods share a
 * single counter rather than each kind restarting at zero, so a field declared between two methods still
 * sorts between them.
 *
 * <p>{@code Traitable} member storage does not itself preserve insertion order, so any consumer that needs to
 * render members in source-declaration order (e.g. {@code type-members}, LSP {@code documentSymbol}) must sort
 * explicitly using this trait rather than relying on iteration order.
 *
 * @param order the zero-based declaration order of the member among all members of its enclosing type
 * @author reed.vonredwitz
 * @since Jul-2026
 */
@Singular
public record DeclarationOrder(int order) implements Trait {
}
