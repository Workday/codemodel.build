package build.codemodel.jdk.annotation.processor.fixture;

/*-
 * #%L
 * JDK Annotation Processor
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

import build.codemodel.jdk.annotation.discovery.Discoverable;

/**
 * A fixture compiled/reflected/processed identically by all three member-population paths
 * ({@code JDKCodeModel}, {@code JdkInitializer}, {@code AnnotationProcessor}), used to assert
 * they agree on the traits ({@code Classification}, {@code Static}, {@code AccessModifier},
 * {@code Final}) attached to fields, constructors, and methods, and on the shape of resolved
 * {@code TypeUsage}s such as the self-referential type variable bound {@code E}.
 *
 * @see build.codemodel.jdk.annotation.processor.MemberPopulationParityTests
 */
@Discoverable
public abstract class ClassificationFixture<E extends ClassificationFixture<E>> {

    public static final int MAX = 10;

    private int count;

    protected ClassificationFixture(final int count) {
        this.count = count;
    }

    public static void staticMethod() {
    }

    public final void finalMethod() {
    }

    public void concreteMethod() {
    }

    public abstract void abstractMethod();
}
