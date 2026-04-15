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
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BindingGraphTrait}, {@link GraphBuildingContributor}, and
 * {@link Context#snapshot}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class BindingGraphTests
    implements ContextualTesting {

    // ---- fixture types ----

    @Singleton
    static class Engine {
        @Inject
        FuelTank fuelTank;
        @Inject
        Transmission transmission;
    }

    @Singleton
    static class FuelTank {
    }

    static class Transmission {
    }

    @Singleton
    static class Car {
        @Inject
        Engine engine;
    }

    // ---- tests ----

    @Test
    void withoutBindingGraph_snapshotIsNoop(@TempDir final Path tempDir) throws Exception {
        final var framework = createInjectionFramework(); // no withBindingGraph()
        final var context = framework.newContext();
        context.bind(Car.class).to(Car.class);

        final var out = tempDir.resolve("WIRING_MAP.md");
        context.snapshot(out);

        assertThat(out).doesNotExist();
    }

    @Test
    void traitAttachedToCodeModel(@TempDir final Path tempDir) {
        final var framework = createInjectionFramework().withBindingGraph();
        final var context = framework.newContext();
        context.bind(Car.class).to(Car.class);
        context.bind(Engine.class).to(Engine.class);
        context.bind(FuelTank.class).to(FuelTank.class);

        context.snapshot(tempDir.resolve("WIRING_MAP.md"));

        assertThat(framework.codeModel().getTrait(BindingGraphTrait.class)).isPresent();
    }

    @Test
    void graphContainsRegisteredBindings(@TempDir final Path tempDir) {
        final var framework = createInjectionFramework().withBindingGraph();
        final var context = framework.newContext();
        context.bind(Car.class).to(Car.class);
        context.bind(Engine.class).to(Engine.class);
        context.bind(FuelTank.class).to(FuelTank.class);

        context.snapshot(tempDir.resolve("WIRING_MAP.md"));

        final var trait = framework.codeModel().getTrait(BindingGraphTrait.class).orElseThrow();
        final var names = trait.bindings()
            .map(WiringReportCompiler::displayName)
            .toList();

        // The code model uses '$' for inner-class separators, not '.'
        assertThat(names).anySatisfy(n -> assertThat(n).contains("Car"));
        assertThat(names).anySatisfy(n -> assertThat(n).contains("Engine"));
        assertThat(names).anySatisfy(n -> assertThat(n).contains("FuelTank"));
    }

    @Test
    void findCycleReturnEmptyWhenNoCycle(@TempDir final Path tempDir) {
        final var framework = createInjectionFramework().withBindingGraph();
        final var context = framework.newContext();
        context.bind(Car.class).to(Car.class);
        context.bind(Engine.class).to(Engine.class);
        context.bind(FuelTank.class).to(FuelTank.class);
        context.bind(Transmission.class).to(Transmission.class);

        // resolve dependencies so edges are recorded
        context.create(Car.class);
        context.snapshot(tempDir.resolve("WIRING_MAP.md"));

        final var trait = framework.codeModel().getTrait(BindingGraphTrait.class).orElseThrow();
        assertThat(trait.findCycle()).isEmpty();
    }

    @Test
    void snapshotWritesMarkdownFile(@TempDir final Path tempDir) throws Exception {
        final var framework = createInjectionFramework().withBindingGraph();
        final var context = framework.newContext();
        context.bind(Car.class).to(Car.class);
        context.bind(Engine.class).to(Engine.class);
        context.bind(FuelTank.class).to(FuelTank.class);
        context.bind(Transmission.class).to(Transmission.class);

        context.create(Car.class);

        final var out = tempDir.resolve("WIRING_MAP.md");
        context.snapshot(out);

        assertThat(out).exists();
        final var content = Files.readString(out);
        assertThat(content).contains("# Wiring Map");
        assertThat(content).contains("Engine");
        assertThat(content).contains("FuelTank");
        assertThat(content).contains("Singletons");
    }

    @Test
    void unsatisfiedDependencyDetected_unitTest() {
        // GraphBuildingContributor unit test: exercises the null-to path directly.
        // In Track 1, InjectionContext never produces a null 'to' (auto-binding eagerly registers
        // the satisfying type before contributeDependency is called). This path is infrastructure
        // for Track 2, where injection-point threading may produce edges with unresolved targets.
        final var contributor = new GraphBuildingContributor();

        final var engineNode = new BindingNode(null, Singleton.class, null, BindingKind.CLASS);
        contributor.contributeBinding(engineNode);

        final Dependency unregistered = new Dependency() {
            @Override public TypeUsage typeUsage() { return null; }
            @Override public String signature() { return "com.example.Unregistered"; }
        };
        contributor.contributeDependency(engineNode, null, new DependencyEdge(null, unregistered));

        final var trait = contributor.buildTrait().orElseThrow();
        assertThat(trait.unsatisfiedDependencies()).hasSize(1);
        assertThat(trait.unsatisfiedDependencies()
            .map(u -> u.edge().dependency().signature()))
            .containsExactly("com.example.Unregistered");
    }

    @Test
    void scopeViolationAppearsInReport(@TempDir final Path tempDir) throws Exception {
        final var framework = createInjectionFramework().withBindingGraph();
        final var context = framework.newContext();
        context.bind(Engine.class).to(Engine.class);      // @Singleton
        context.bind(Transmission.class).to(Transmission.class); // prototype

        context.create(Engine.class);

        final var out = tempDir.resolve("WIRING_MAP.md");
        context.snapshot(out);

        final var content = Files.readString(out);
        assertThat(content).contains("Scope Violations");
    }
}
