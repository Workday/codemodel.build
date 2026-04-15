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

import build.base.graph.Graphs;
import build.base.graph.WeightedGraph;
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.jdk.JDKCodeModel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link Trait} on {@link JDKCodeModel} that holds the binding graph for an
 * {@link InjectionContext}. Attached to the framework's {@link JDKCodeModel} by
 * {@link Context#snapshot} and readable by {@link WiringReportCompiler}.
 *
 * <p>Instances are produced by {@link BindingGraphContributor#buildTrait()} and are immutable
 * once constructed — the graph is a snapshot of the bindings registered at the time
 * {@link Context#snapshot} was called.
 *
 * @author reed.vonredwitz
 * @see BindingGraphContributor
 * @see WiringReportCompiler
 * @since Apr-2026
 */
@Singular
public final class BindingGraphTrait
    implements Trait {

    /**
     * Describes a dependency from a registered binding to a type that is not registered as an
     * explicit binding in the context. Produced when a dependency is satisfied at runtime by an
     * auto-bound or otherwise untracked type — the edge exists in the contributor event log but
     * cannot be represented as a graph edge because no {@link BindingNode} was produced for the
     * target type.
     *
     * @param from the binding that requires the dependency
     * @param edge the edge, carrying the unresolved {@link Dependency}
     */
    public record UnsatisfiedDependency(BindingNode from, DependencyEdge edge) {}

    /**
     * The underlying weighted binding graph. Vertices are {@link BindingNode}s; edge weights are
     * {@link DependencyEdge}s describing the injection point and resolved dependency for each edge.
     */
    private final WeightedGraph<BindingNode, DependencyEdge> graph;

    /**
     * Dependencies where the satisfying binding was not registered as an explicit binding —
     * auto-resolved or otherwise untracked at snapshot time.
     */
    private final List<UnsatisfiedDependency> unsatisfied;

    /**
     * Constructs a {@link BindingGraphTrait} wrapping the given graph and unsatisfied edges.
     *
     * @param graph       the immutable binding graph
     * @param unsatisfied edges where the target binding was not explicitly registered
     */
    BindingGraphTrait(final WeightedGraph<BindingNode, DependencyEdge> graph,
                      final List<UnsatisfiedDependency> unsatisfied) {
        this.graph = graph;
        this.unsatisfied = List.copyOf(unsatisfied);
    }

    /**
     * Returns all registered {@link BindingNode}s in this graph.
     *
     * @return a stream of all binding nodes
     */
    public Stream<BindingNode> bindings() {
        return this.graph.vertices().stream();
    }

    /**
     * Returns the {@link BindingNode}s that the given node directly depends on (its successors in
     * the directed graph — the dependencies it requires).
     *
     * @param node the node to query
     * @return a stream of direct dependencies
     */
    public Stream<BindingNode> dependenciesOf(final BindingNode node) {
        return this.graph.successors(node).stream();
    }

    /**
     * Returns the {@link BindingNode}s that directly depend on the given node (its predecessors in
     * the directed graph — the bindings that require it).
     *
     * @param node the node to query
     * @return a stream of direct dependents
     */
    public Stream<BindingNode> dependentsOf(final BindingNode node) {
        return this.graph.predecessors(node).stream();
    }

    /**
     * Returns the full cycle path if a dependency cycle exists among the registered bindings,
     * otherwise returns empty.
     *
     * @return an {@link Optional} containing the cycle path, or empty if no cycle exists
     */
    public Optional<List<BindingNode>> findCycle() {
        return Graphs.findCycle(this.graph.toUnweightedGraph());
    }

    /**
     * Returns dependency edges where the satisfying binding was not registered as an explicit
     * binding in the context (auto-resolved or untracked types). These are recorded by the
     * contributor but excluded from the graph because no {@link BindingNode} exists for the target.
     *
     * @return a stream of unsatisfied dependency records
     */
    public Stream<UnsatisfiedDependency> unsatisfiedDependencies() {
        return this.unsatisfied.stream();
    }

    /**
     * Returns the underlying {@link WeightedGraph} for consumers that need direct algorithm access.
     *
     * @return the binding graph
     */
    public WeightedGraph<BindingNode, DependencyEdge> graph() {
        return this.graph;
    }
}
