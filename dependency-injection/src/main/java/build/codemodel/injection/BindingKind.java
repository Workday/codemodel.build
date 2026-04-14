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
 * Classifies how a {@link Binding} produces its value.
 *
 * <p>Used by {@link BindingNode} to describe the binding's kind for the Track 2 binding graph.
 *
 * @author reed.vonredwitz
 * @see BindingNode
 * @see BindingGraphContributor
 * @since Apr-2026
 */
public enum BindingKind {

    /**
     * A pre-built value is returned directly (e.g. {@code bind(MyClass.class).to(instance)}).
     */
    VALUE,

    /**
     * An instance of a concrete class is created and injected on demand.
     */
    CLASS,

    /**
     * A {@link java.util.function.Supplier} produces the value.
     */
    SUPPLIER,

    /**
     * A multibinding set — produced by {@link Binder#bindSet}.
     */
    MULTI
}
