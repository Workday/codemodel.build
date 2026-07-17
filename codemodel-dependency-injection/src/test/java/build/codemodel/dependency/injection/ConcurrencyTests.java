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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency regression tests for {@link InjectionContext}.
 *
 * @author reed.vonredwitz
 */
class ConcurrencyTests
    implements ContextualTesting {

    // ---- bindSet race ----

    static class StringSetHolder {
        @Inject
        Set<String> values;
    }

    /**
     * Concurrent calls to {@link Binder#bindSet} for the same type must not lose contributions
     * or call the binding-graph contributor more than once.
     */
    @Test
    void concurrentBindSetShouldMergeAllValues() throws InterruptedException {
        final var context = createInjectionFramework().newContext();
        final int threadCount = 16;
        final var barrier = new CyclicBarrier(threadCount);
        final var errors = new CopyOnWriteArrayList<Throwable>();

        final var threads = IntStream.range(0, threadCount)
            .mapToObj(i -> new Thread(() -> {
                try {
                    barrier.await();
                    context.bindSet(String.class).add("value-" + i);
                } catch (final BrokenBarrierException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (final Throwable t) {
                    errors.add(t);
                }
            }))
            .peek(Thread::start)
            .toList();

        for (final var thread : threads) {
            thread.join();
        }

        assertThat(errors).isEmpty();
        assertThat(context.inject(new StringSetHolder()).values).hasSize(threadCount);
    }

    // ---- auto-singleton registration race ----

    @Singleton
    static class AutoSingleton {
    }

    /**
     * Concurrent first-touch creation of an unbound {@link Singleton} must not throw
     * {@link BindingAlreadyExistsException} and must return the same instance to all callers.
     */
    @Test
    void concurrentAutoSingletonCreationShouldReturnSameInstance() throws InterruptedException {
        final var context = createInjectionFramework().newContext();
        // AutoSingleton is intentionally NOT pre-bound — exercises the auto-registration path in getValue()
        final int threadCount = 20;
        final var barrier = new CyclicBarrier(threadCount);
        final var instances = new CopyOnWriteArrayList<AutoSingleton>();
        final var errors = new CopyOnWriteArrayList<Throwable>();

        final var threads = IntStream.range(0, threadCount)
            .mapToObj(_ -> new Thread(() -> {
                try {
                    barrier.await();
                    instances.add(context.create(AutoSingleton.class));
                } catch (final BrokenBarrierException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (final Throwable t) {
                    errors.add(t);
                }
            }))
            .peek(Thread::start)
            .toList();

        for (final var thread : threads) {
            thread.join();
        }

        assertThat(errors).isEmpty();
        assertThat(instances).hasSize(threadCount);
        final var first = instances.getFirst();
        assertThat(instances).allSatisfy(instance -> assertThat(instance).isSameAs(first));
    }
}
