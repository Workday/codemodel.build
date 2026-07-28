package build.codemodel.jdk.example;

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

/**
 * Fixture with several fields, constructors, and methods, used to exercise
 * {@code DeclarationOrder} stamping on the reflection-based population path in {@link JDKCodeModelTests}.
 */
public class OrderedExample {

    public int alpha;
    public int beta;
    public int gamma;

    public OrderedExample() {
    }

    public OrderedExample(final int alpha) {
        this.alpha = alpha;
    }

    public void first() {
    }

    public void second() {
    }

    public void third() {
    }
}
