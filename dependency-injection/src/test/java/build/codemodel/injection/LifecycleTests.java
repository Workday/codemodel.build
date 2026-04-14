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
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for lifecycle hooks ({@link PreDestroy}) and custom scopes ({@link ScopedValueScope},
 * {@link InjectionFramework#bindScope}).
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class LifecycleTests
    implements ContextualTesting {

    // ---- @PreDestroy fixtures ----

    static final List<String> events = new CopyOnWriteArrayList<>();

    @Singleton
    static class LifecycleRoot {
        @Inject
        LifecycleLeaf leaf;

        @PreDestroy
        void destroy() {
            events.add("root");
        }
    }

    @Singleton
    static class LifecycleLeaf {
        @PreDestroy
        void destroy() {
            events.add("leaf");
        }
    }

    @Singleton
    static class NoDestroyService {
    }

    // ---- ScopedValueScope fixtures ----

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @ScopeAnnotation
    @interface ContextScoped {
    }

    @ContextScoped
    static class ContextScopedService {
    }

    // ---- custom scope fixture ----

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @ScopeAnnotation
    @interface AlwaysSame {
    }

    @AlwaysSame
    static class CustomScopedService {
    }

    // ---- @PreDestroy: reverse topological order ----

    /**
     * Ensures {@link Context#close()} invokes {@link PreDestroy} methods on instantiated singletons in
     * reverse topological order: dependents before their dependencies.
     */
    @Test
    void shouldInvokePreDestroyInReverseTopologicalOrder() throws Exception {
        events.clear();
        final var context = createInjectionFramework().newContext();
        context.bind(LifecycleRoot.class).to(LifecycleRoot.class);
        context.bind(LifecycleLeaf.class).to(LifecycleLeaf.class);

        context.create(LifecycleRoot.class); // instantiates both
        context.close();

        // Root depends on Leaf, so Root is destroyed first
        assertThat(events).containsExactly("root", "leaf");
    }

    /**
     * Ensures {@link Context#close()} does NOT invoke {@link PreDestroy} for singletons that were
     * never instantiated.
     */
    @Test
    void shouldNotInvokePreDestroyForUninstantiatedSingletons() throws Exception {
        events.clear();
        final var context = createInjectionFramework().newContext();
        context.bind(LifecycleRoot.class).to(LifecycleRoot.class);
        context.bind(LifecycleLeaf.class).to(LifecycleLeaf.class);
        // neither created

        context.close();

        assertThat(events).isEmpty();
    }

    /**
     * Ensures {@link Context#close()} is a no-op for bindings that have no {@link PreDestroy} methods.
     */
    @Test
    void shouldCloseWithoutErrorWhenNoPreDestroyMethods() {
        final var context = createInjectionFramework().newContext();
        context.bind(NoDestroyService.class).to(NoDestroyService.class);
        context.create(NoDestroyService.class);

        assertThatCode(context::close).doesNotThrowAnyException();
    }

    // ---- ScopedValueScope ----

    /**
     * Ensures that within a single {@link ScopedValueScope#run} invocation, every resolution
     * returns the same cached instance.
     */
    @Test
    void shouldReturnSameInstanceWithinScope() {
        final var framework = createInjectionFramework();
        final var scope = new ScopedValueScope();
        framework.bindScope(ContextScoped.class, scope);

        final var context = framework.newContext();
        context.bind(ContextScopedService.class).to(ContextScopedService.class);

        final var results = new AtomicReference<ContextScopedService[]>();
        scope.run(() -> results.set(new ContextScopedService[]{
            context.create(ContextScopedService.class),
            context.create(ContextScopedService.class)
        }));

        assertThat(results.get()[0]).isSameAs(results.get()[1]);
    }

    /**
     * Ensures that separate {@link ScopedValueScope#run} invocations each produce a fresh instance.
     */
    @Test
    void shouldReturnDifferentInstancesBetweenScopes() {
        final var framework = createInjectionFramework();
        final var scope = new ScopedValueScope();
        framework.bindScope(ContextScoped.class, scope);

        final var context = framework.newContext();
        context.bind(ContextScopedService.class).to(ContextScopedService.class);

        final var first = new AtomicReference<ContextScopedService>();
        final var second = new AtomicReference<ContextScopedService>();

        scope.run(() -> first.set(context.create(ContextScopedService.class)));
        scope.run(() -> second.set(context.create(ContextScopedService.class)));

        assertThat(first.get()).isNotSameAs(second.get());
    }

    /**
     * Ensures that resolving a scoped binding outside of a {@link ScopedValueScope#run} block
     * throws an {@link InjectionException}.
     */
    @Test
    void shouldThrowWhenResolvingScopedBindingOutsideScope() {
        final var framework = createInjectionFramework();
        final var scope = new ScopedValueScope();
        framework.bindScope(ContextScoped.class, scope);

        final var context = framework.newContext();
        context.bind(ContextScopedService.class).to(ContextScopedService.class);

        assertThatThrownBy(() -> context.create(ContextScopedService.class))
            .isInstanceOf(InjectionException.class);
    }

    // ---- custom scope via bindScope ----

    /**
     * Ensures {@link InjectionFramework#bindScope} registers a custom scope that is used when binding
     * a class annotated with the corresponding scope annotation.
     */
    @Test
    void shouldSupportCustomScopeViaBindScope() {
        final var framework = createInjectionFramework();
        final var fixedInstance = new CustomScopedService();

        // scope that always hands back the same pre-built instance
        framework.bindScope(AlwaysSame.class, binding -> ValueBinding.of(binding.dependency(), fixedInstance));

        final var context = framework.newContext();
        context.bind(CustomScopedService.class).to(CustomScopedService.class);

        assertThat(context.create(CustomScopedService.class)).isSameAs(fixedInstance);
        assertThat(context.create(CustomScopedService.class)).isSameAs(fixedInstance);
    }
}
