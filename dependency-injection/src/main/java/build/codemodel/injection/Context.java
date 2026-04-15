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

import build.codemodel.foundation.usage.TypeUsage;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An {@link Injector} that manages zero or more {@link Binding}s to be used for injection into {@link InjectionPoint}s.
 * <p>
 * {@link Context}s are the preferred means by which applications perform
 * <a href="https://en.wikipedia.org/wiki/Dependency_injection">Dependency Injection</a>.  Once established,
 * applications typically use the following steps with a {@link Context} to perform injection:
 * <ol>
 *     <li>Invoke the {@link Binder#bind(Class)} method to create and register the {@link Binding}s,
 *         representing the {@link Object}s to be injected for specific {@link Class}es</li>
 *     <li>Invoke the {@link Injector#inject(Object)} method to perform injection into existing {@link Object}s</li>
 *     <li>Invoke the {@link #create(Class)} method to instantiate an instance of {@link Class}, using
 *         constructor-based injection, <strong>and</strong> afterwards using field or setter-based injection</li>
 * </ol>
 *
 * @author brian.oliver
 * @see Binding
 * @see BindingBuilder
 * @see Binder
 * @see Injector
 * @see InjectionPoint
 * @since Oct-2024
 */
public interface Context
    extends Injector, Binder, AutoCloseable {

    /**
     * Creates a {@link BindingBuilder} pre-loaded with the specified instance value, enabling
     * {@link BindingBuilder#asAllInterfaces()} and {@link BindingBuilder#asAllInterfaces(Predicate)} to
     * register the value against every interface in its type hierarchy in a single call.
     *
     * <p>Example:
     * <pre>{@code
     * context.bind(myService).asAllInterfaces();
     * }</pre>
     *
     * @param <T>   the type of the instance
     * @param value the instance to bind
     * @return a {@link BindingBuilder} loaded with the value
     */
    <T> BindingBuilder<T> bind(T value);

    /**
     * Creates an instance of the specified class by locating and injecting a constructor annotated
     * with {@link Inject} with values resolved from the {@link Context}.  Should an injectable
     * constructor not be found, an attempt will be made to use the {@code default} constructor.
     * <p>
     * Should constructor injection succeed, the further injection of {@link Inject}-annotated
     * fields and methods will be attempted.
     *
     * @param <T>           the type of the {@link Class}
     * @param requiredClass the required {@link Class} to create
     * @return a new instance of the required {@link Class}
     * @throws NullPointerException when the required class was {@code null}
     * @throws InjectionException   when injection fails
     */
    <T> T create(Class<T> requiredClass)
        throws InjectionException;

    /**
     * Creates an instance of the type represented by a {@link TypeUsage} with values resolved from the
     * {@link Context}.
     *
     * @param <T>       the type to create
     * @param typeUsage the {@link TypeUsage}
     * @return an instance of the type represented by the {@link TypeUsage}
     * @throws NullPointerException when the {@link TypeUsage} was {@code null}
     * @throws InjectionException   when injection fails
     */
    <T> T create(TypeUsage typeUsage)
        throws InjectionException;

    /**
     * Creates an instance of the type represented by the specified {@link Dependency} with values resolved
     * from the {@link Context}.
     *
     * @param <T>        the type to create
     * @param dependency the {@link Dependency}
     * @return an instance of the type represented by the {@link Dependency}
     * @throws NullPointerException when the {@link Dependency} was {@code null}
     * @throws InjectionException   when injection fails
     */
    <T> T create(Dependency dependency)
        throws InjectionException;

    /**
     * Builds a {@link BindingGraphTrait} from the current contributor state, attaches it to the
     * framework's {@link build.codemodel.jdk.JDKCodeModel}, and writes a human-readable wiring
     * report to {@code outputPath}.
     *
     * <p>No-ops immediately if no real {@link BindingGraphContributor} is installed (i.e. the
     * {@link BindingGraphContributor#NOOP} is still in place). May be called at any point after
     * bindings are registered.
     *
     * @param outputPath the path to write the wiring report to
     * @return this {@link Context} for fluent chaining
     * @throws java.io.UncheckedIOException if writing the report fails
     */
    Context snapshot(Path outputPath);

    /**
     * Validates the current set of registered bindings before any objects are created. Performs three checks:
     * <ol>
     *   <li><strong>Cycle detection</strong> — throws {@link CyclicDependencyException} with the full cycle
     *       path if a dependency cycle is found among class bindings.</li>
     *   <li><strong>Unsatisfied dependency detection</strong> — collects every class binding whose injected
     *       dependencies have no corresponding binding and throws {@link ValidationException} listing them
     *       all at once.</li>
     *   <li><strong>Scope violation detection</strong> — flags edges where a wider-scoped binding (e.g.
     *       {@link jakarta.inject.Singleton}) depends on a narrower-scoped one (e.g. prototype).</li>
     * </ol>
     *
     * @return this {@link Context} for fluent chaining (e.g. {@code context.validate().initializeEagerSingletons()})
     * @throws CyclicDependencyException if a dependency cycle is detected
     * @throws ValidationException       if unsatisfied dependencies or scope violations are found
     */
    Context validate();

    /**
     * Pre-creates all {@link jakarta.inject.Singleton}-scoped class bindings in dependency order, using
     * {@link build.base.graph.Graphs#parallelizableGroups} to initialize independent groups in parallel.
     *
     * <p>Typically called immediately after {@link #validate()}:
     * <pre>{@code
     * context.validate().initializeEagerSingletons();
     * }</pre>
     *
     * @return this {@link Context} for fluent chaining
     */
    Context initializeEagerSingletons();

    /**
     * Closes this {@link Context}, invoking any {@link PreDestroy} lifecycle methods on
     * instantiated singleton instances in reverse dependency order (dependents are destroyed
     * before their dependencies).
     */
    @Override
    void close();

    /**
     * Creates a new {@link Context} with the this {@link Context} as a parent solver.
     *
     * @return a new {@link Context}
     */
    Context newContext();

    /**
     * Obtains a {@link Resolver} that can be used to resolve {@link Dependency}s based on this {@link Context}.
     *
     * @return a {@link Resolver}
     */
    Resolver<Object> resolver();

    /**
     * Adds the specified {@link Resolver}
     *
     * @param resolver the {@link Resolver}
     * @return this {@link Context} to permit fluent-style method invocation
     */
    Context addResolver(Resolver<?> resolver);

    /**
     * Adds a {@link Resolver} produced by the specified {@link Function} based on this {@link Context}.
     *
     * @param supplier the {@link BiFunction} to supply a {@link Resolver} based on the {@link InjectionFramework} and {@link Context}
     * @return this {@link Context} to permit fluent-style method invocation
     */
    Context addResolver(BiFunction<? super InjectionFramework, ? super Context, Resolver<?>> supplier);
}
