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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Scope} backed by a {@link ScopedValue}. Within a single {@link #run} or {@link #call}
 * invocation, every resolution of a scoped binding returns the same cached instance. The per-scope
 * instance cache is automatically discarded when the invocation completes — no manual cleanup
 * required.
 *
 * <p>Example usage:
 * <pre>{@code
 * final var requestScope = new ScopedValueScope();
 * framework.bindScope(RequestScoped.class, requestScope);
 *
 * requestScope.run(() -> {
 *     // every resolution of a @RequestScoped type returns the same instance here
 *     var service = context.create(MyService.class);
 * });
 * }</pre>
 *
 * <p>Compared to a {@link ThreadLocal}-backed scope, this scope:
 * <ul>
 *   <li>works safely with virtual threads — no per-thread storage overhead</li>
 *   <li>cleans up automatically when the scope block exits — no memory leaks</li>
 *   <li>requires an explicit scope boundary via {@link #run} or {@link #call}</li>
 * </ul>
 *
 * @author reed.vonredwitz
 * @see ScopeAnnotation
 * @since Apr-2026
 */
public class ScopedValueScope
    implements Scope {

    /**
     * Holds the per-invocation instance cache. Bound for the duration of each {@link #run} or
     * {@link #call} invocation and automatically released on exit.
     */
    private final ScopedValue<Map<Dependency, Object>> context = ScopedValue.newInstance();

    /**
     * Enters the scope and executes {@code action}. Within {@code action}, every resolution of a
     * binding registered under this scope returns the same cached instance. The cache is discarded
     * when {@code action} completes.
     *
     * @param action the action to run within this scope
     */
    public void run(final Runnable action) {
        ScopedValue.where(this.context, new ConcurrentHashMap<>()).run(action);
    }

    /**
     * Enters the scope, executes {@code action}, and returns its result. Behaves identically to
     * {@link #run} but allows a value to be returned from the scope.
     *
     * @param <T>    the return type
     * @param <X>    the exception type that {@code action} may throw
     * @param action the action to call within this scope
     * @return the result produced by {@code action}
     * @throws X if {@code action} throws
     */
    public <T, X extends Throwable> T call(final ScopedValue.CallableOp<T, X> action) throws X {
        return ScopedValue.where(this.context, new ConcurrentHashMap<>()).call(action);
    }

    @Override
    public Binding<?> scope(final ValueBinding<?> binding) {
        return new SupplierBinding<>(binding.dependency(), () -> {
            if (!this.context.isBound()) {
                throw new InjectionException(
                    "Binding [" + binding.dependency() + "] requires an active scope — "
                        + "call ScopedValueScope.run() before resolving scoped bindings");
            }
            return this.context.get()
                .computeIfAbsent(binding.dependency(), _ -> (Object) binding.value());
        });
    }
}
