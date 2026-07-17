package build.codemodel.jdk;

/*-
 * #%L
 * JDK Code Model
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

import build.base.compile.testing.JavaFileObjects;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.imperative.Return;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.Identifier;
import build.codemodel.jdk.expression.MethodInvocation;
import build.codemodel.jdk.expression.ResolvedMethod;
import build.codemodel.jdk.expression.Symbol;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduces a bug shared by {@link JdkExpressionConverter#resolveMethod}, {@code resolveField},
 * and {@code resolveParameter}: each rebuilds the declaring type's
 * {@link build.codemodel.foundation.naming.TypeName} via
 * {@code codeModel.getNameProvider().getTypeName(Optional.empty(), fqn)}, hardcoding "no module"
 * rather than resolving the type's actual module.
 *
 * <p>When the declaring type lives in a real named module, the registered {@code TypeName} carries
 * that module, but the lookup built by these helpers does not - so
 * {@code codeModel.getTypeDescriptor(typeName)} misses, and no {@code Symbol} or
 * {@link ResolvedMethod} trait gets attached even though the descriptor is present.
 *
 * <p>All three cases use small user-authored types compiled under an explicit
 * {@code module-info.java}, rather than a real JDK type such as {@code java.lang.String}:
 * {@link JDKCodeModel} pre-registers a fixed set of "foundation" JDK types (including
 * {@code String}) with a bare descriptor at construction time, and that registration permanently
 * blocks any later attempt to fully populate them (see {@code JDKCodeModel#initialize()}) - a
 * separate, pre-existing limitation that would make a {@code java.lang.String}-based method
 * resolution case fail for a reason unrelated to the module-name bug this test targets.
 */
class ModuleAwareSymbolResolutionTests {

    @Test
    void methodCallResolvesWhenDeclaringTypeIsInANamedModule() {
        final var moduleInfo = JavaFileObjects.forSourceString("module-info", """
            module com.example {
            }
            """);
        final var callee = JavaFileObjects.forSourceString("com.example.Callee", """
            package com.example;
            public class Callee {
                public static String helper() {
                    return "helper";
                }
            }
            """);
        final var caller = JavaFileObjects.forSourceString("com.example.Caller", """
            package com.example;
            public class Caller {
                public String outer() {
                    return Callee.helper();
                }
            }
            """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(moduleInfo, callee, caller)));

        final var callerType = codeModel.getJDKTypeDescriptor("com.example.Caller").orElseThrow();

        final var outer = callerType.traits(MethodDescriptor.class)
            .filter(md -> md.methodName().name().toString().equals("outer"))
            .findFirst().orElseThrow();

        final var body = outer.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var helperCall = body.composition(MethodInvocation.class)
            .filter(mi -> mi.methodName().equals("helper"))
            .findFirst().orElseThrow();

        assertThat(helperCall.getTrait(ResolvedMethod.class))
            .as("helper() is declared on com.example.Callee, registered in the com.example module; "
                + "resolution must account for that module rather than assuming the unnamed module")
            .isPresent();
    }

    @Test
    void mathPiFieldReferenceResolvesAgainstJavaBase() {
        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);

        // Register java.lang.Math's descriptor up front, keyed by its real (java.base-qualified)
        // TypeName - exactly as it would be if anything else in the pipeline had touched it first.
        codeModel.getJDKTypeDescriptor(Math.class);

        final var source = JavaFileObjects.forSourceString("com.example.Example", """
            package com.example;
            import static java.lang.Math.PI;
            public class Example {
                public double outer() {
                    return PI;
                }
            }
            """);
        new JdkInitializer(List.of(), List.of(), List.of(source)).initialize(codeModel);

        final var exampleType = codeModel.getJDKTypeDescriptor("com.example.Example").orElseThrow();

        final var outer = exampleType.traits(MethodDescriptor.class)
            .filter(md -> md.methodName().name().toString().equals("outer"))
            .findFirst().orElseThrow();

        final var body = outer.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) ret.expression().orElseThrow();
        assertThat(identifier.name()).isEqualTo("PI");

        assertThat(identifier.getTrait(Symbol.class))
            .as("PI is declared on java.lang.Math, registered in the java.base module; "
                + "resolution must account for that module rather than assuming the unnamed module")
            .isPresent();
    }

    @Test
    void parameterReferenceResolvesWhenDeclaringTypeIsInANamedModule() {
        final var moduleInfo = JavaFileObjects.forSourceString("module-info", """
            module com.example {
            }
            """);
        final var source = JavaFileObjects.forSourceString("com.example.Example", """
            package com.example;
            public class Example {
                public String outer(String value) {
                    return value;
                }
            }
            """);

        final var codeModel = JdkInitializerTests.runInternal(
            new JdkInitializer(List.of(), List.of(), List.of(moduleInfo, source)));

        final var exampleType = codeModel.getJDKTypeDescriptor("com.example.Example").orElseThrow();

        final var outer = exampleType.traits(MethodDescriptor.class)
            .filter(md -> md.methodName().name().toString().equals("outer"))
            .findFirst().orElseThrow();

        final var body = outer.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var ret = body.statements()
            .filter(s -> s instanceof Return)
            .map(s -> (Return) s)
            .findFirst().orElseThrow();

        final var identifier = (Identifier) ret.expression().orElseThrow();
        assertThat(identifier.name()).isEqualTo("value");

        assertThat(identifier.getTrait(Symbol.class))
            .as("value is a parameter of a method declared on com.example.Example, registered in "
                + "the com.example module; resolution must account for that module rather than "
                + "assuming the unnamed module")
            .isPresent();
    }
}
