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

import build.base.graph.WeightedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The real {@link BindingGraphContributor} implementation. Uses an event-log approach:
 * accumulates {@link #contributeBinding} and {@link #contributeDependency} calls into
 * synchronized lists, then constructs the {@link WeightedGraph} on demand when
 * {@link #buildTrait()} is called.
 *
 * <p>Installed via {@link InjectionFramework#withBindingGraph()}.
 *
 * @author reed.vonredwitz
 * @see BindingGraphContributor
 * @see BindingGraphTrait
 * @since Apr-2026
 */
final class GraphBuildingContributor
    implements BindingGraphContributor {

    /**
     * Accumulated binding nodes, in registration order.
     */
    private final List<BindingNode> nodes = new ArrayList<>();

    /**
     * Accumulated dependency edges. {@code from} and {@code to} may be {@code null} for
     * auto-resolved or multibinding dependencies not registered as explicit class bindings.
     */
    private final List<ContributedEdge> edges = new ArrayList<>();

    @Override
    public synchronized void contributeBinding(final BindingNode node) {
        if (node != null) {
            this.nodes.add(node);
        }
    }

    @Override
    public synchronized void contributeDependency(final BindingNode from,
                                                  final BindingNode to,
                                                  final DependencyEdge edge) {
        this.edges.add(new ContributedEdge(from, to, edge));
    }

    /**
     * Builds a {@link BindingGraphTrait} from the accumulated event log. Constructs an immutable
     * {@link WeightedGraph} from the recorded nodes and edges; edges where {@code from} or
     * {@code to} is {@code null} are omitted from the graph. Edges where {@code from} is non-null
     * but {@code to} is {@code null} are captured as
     * {@link BindingGraphTrait.UnsatisfiedDependency} records instead.
     *
     * @return an {@link Optional} containing the built trait
     */
    @Override
    public synchronized Optional<BindingGraphTrait> buildTrait() {
        final var builder = WeightedGraph.<BindingNode, DependencyEdge>directed();
        this.nodes.forEach(builder::addVertex);
        this.edges.stream()
            .filter(e -> e.from() != null && e.to() != null)
            .forEach(e -> builder.addEdge(e.from(), e.to(), e.edge()));
        final var unsatisfied = this.edges.stream()
            .filter(e -> e.from() != null && e.to() == null)
            .map(e -> new BindingGraphTrait.UnsatisfiedDependency(e.from(), e.edge()))
            .toList();
        return Optional.of(new BindingGraphTrait(builder.build(), unsatisfied));
    }

    /**
     * A recorded dependency contribution: the from-node, to-node, and the edge description.
     */
    private record ContributedEdge(BindingNode from, BindingNode to, DependencyEdge edge) {
    }
}
