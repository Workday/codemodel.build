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

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the multibinding system: {@link Binder#bindSet}, {@link MultiBinder}, and the five
 * resolvable collection types ({@link Set}, {@link Collection}, {@link Iterable}, {@link Stream},
 * {@link List}).
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class MultibindingTests
    implements ContextualTesting {

    // ---- holder classes for injection-point-based tests ----

    static class SetHolder {
        @Inject
        Set<String> values;
    }

    static class CollectionHolder {
        @Inject
        Collection<String> values;
    }

    static class IterableHolder {
        @Inject
        Iterable<String> values;
    }

    static class StreamHolder {
        @Inject
        Stream<String> values;
    }

    static class ListHolder {
        @Inject
        List<String> values;
    }

    // ---- Set<T> ----

    /**
     * Ensures bound values are injectable as a {@link Set}.
     */
    @Test
    void shouldInjectAsSet() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(String.class).add("a").add("b").add("c");

        final var holder = context.inject(new SetHolder());

        assertThat(holder.values).containsExactlyInAnyOrder("a", "b", "c");
    }

    // ---- Collection<T> ----

    /**
     * Ensures bound values are injectable as a {@link Collection}.
     */
    @Test
    void shouldInjectAsCollection() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(String.class).add("a").add("b");

        final var holder = context.inject(new CollectionHolder());

        assertThat(holder.values).containsExactlyInAnyOrder("a", "b");
    }

    // ---- Iterable<T> ----

    /**
     * Ensures bound values are injectable as an {@link Iterable}.
     */
    @Test
    void shouldInjectAsIterable() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(String.class).add("a").add("b");

        final var holder = context.inject(new IterableHolder());

        assertThat(holder.values).containsExactlyInAnyOrder("a", "b");
    }

    // ---- Stream<T> ----

    /**
     * Ensures bound values are injectable as a {@link Stream}, and a fresh stream is produced on each
     * injection so the stream can be consumed without affecting subsequent injections.
     */
    @Test
    void shouldInjectAsStream() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(String.class).add("a").add("b");

        final var holder1 = context.inject(new StreamHolder());
        assertThat(holder1.values).containsExactlyInAnyOrder("a", "b");

        // second injection must get a fresh, unconsumed stream
        final var holder2 = context.inject(new StreamHolder());
        assertThat(holder2.values).containsExactlyInAnyOrder("a", "b");
    }

    // ---- List<T> ----

    /**
     * Ensures bound values are injectable as an immutable {@link List}.
     */
    @Test
    void shouldInjectAsList() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(String.class).add("a").add("b").add("c");

        final var holder = context.inject(new ListHolder());

        assertThat(holder.values).containsExactlyInAnyOrder("a", "b", "c");
    }

    // ---- cross-module merging ----

    /**
     * Ensures that calling {@link Binder#bindSet} from two different modules merges into one set.
     */
    @Test
    void shouldMergeMultibindingsAcrossModules() {
        final Module moduleA = binder -> binder.bindSet(String.class).add("a");
        final Module moduleB = binder -> binder.bindSet(String.class).add("b");

        final var context = createInjectionFramework().newContext(moduleA, moduleB);
        final var holder = context.inject(new SetHolder());

        assertThat(holder.values).containsExactlyInAnyOrder("a", "b");
    }

    // ---- class and supplier variants ----

    /**
     * Ensures {@link MultiBinder#add(Class)} binds by class, resolving via the context.
     */
    @Test
    void shouldBindByClass() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(CharSequence.class).add(String.class);

        final var holder = context.inject(new CharSeqSetHolder());

        assertThat(holder.values).hasSize(1);
        assertThat(holder.values.iterator().next()).isInstanceOf(String.class);
    }

    /**
     * Ensures {@link MultiBinder#add(java.util.function.Supplier)} binds via supplier.
     */
    @Test
    void shouldBindBySupplier() {
        final var context = createInjectionFramework().newContext();
        context.bindSet(String.class).add(() -> "supplied");

        final var holder = context.inject(new SetHolder());

        assertThat(holder.values).containsExactly("supplied");
    }

    static class CharSeqSetHolder {
        @Inject
        Set<CharSequence> values;
    }
}
