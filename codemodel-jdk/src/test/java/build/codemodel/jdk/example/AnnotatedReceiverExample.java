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
 * A type whose instance method declares an explicit, annotated receiver parameter
 * ({@code @NonNull AnnotatedReceiverExample this}), used for reflection-path receiver-annotation
 * discovery tests. Also declares an inner class whose constructor's outer-instance receiver
 * ({@code @NonNull AnnotatedReceiverExample AnnotatedReceiverExample.this}) is likewise annotated.
 */
public class AnnotatedReceiverExample {

    public void run(@NonNull AnnotatedReceiverExample this) {
    }

    public class Inner {

        public Inner(@NonNull AnnotatedReceiverExample AnnotatedReceiverExample.this) {
        }
    }
}
