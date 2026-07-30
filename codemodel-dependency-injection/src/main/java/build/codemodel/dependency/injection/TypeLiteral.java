package build.codemodel.dependency.injection;

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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Captures a fully-parameterized generic {@link Type} (e.g. {@code List<Person>}) that would otherwise be lost
 * to erasure, allowing it to be used as a {@link Binder#bind(TypeLiteral) binding} key.
 * <p>
 * Create a {@link TypeLiteral} using an anonymous subclass, so the compiler retains the type argument in the
 * generated class's {@link Class#getGenericSuperclass()}:
 * <pre>{@code
 * List<Person> people = new ArrayList<>();
 * // ... adding some people ...
 *
 * binder.bind(new TypeLiteral<List<Person>>() {}).to(people);
 * }</pre>
 *
 * @param <T> the type being captured
 * @author brian.oliver
 * @see Binder#bind(TypeLiteral)
 * @since Apr-2026
 */
public abstract class TypeLiteral<T> {

    /**
     * The captured generic {@link Type}, extracted from the anonymous subclass's parameterized superclass.
     */
    private final Type type;

    /**
     * Constructs a {@link TypeLiteral}, capturing the type argument supplied by an anonymous subclass.
     *
     * @throws IllegalArgumentException if constructed directly (not as an anonymous subclass) or without a
     *                                  concrete type argument
     */
    protected TypeLiteral() {
        if (!(getClass().getGenericSuperclass() instanceof ParameterizedType parameterizedSuperclass)) {
            throw new IllegalArgumentException(
                "TypeLiteral must be created as an anonymous subclass with a concrete type argument, "
                    + "e.g. new TypeLiteral<List<Person>>() {}");
        }

        this.type = parameterizedSuperclass.getActualTypeArguments()[0];
    }

    /**
     * Obtains the captured generic {@link Type}.
     *
     * @return the {@link Type}
     */
    public Type type() {
        return this.type;
    }
}
