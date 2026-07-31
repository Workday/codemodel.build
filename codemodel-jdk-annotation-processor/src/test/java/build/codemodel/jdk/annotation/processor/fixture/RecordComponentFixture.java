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
 * A record fixture used to assert that {@code RecordComponentDescriptor} name/type agree across
 * all three member-population paths.
 *
 * @see build.codemodel.jdk.annotation.processor.MemberPopulationParityTests
 */
@Discoverable
public record RecordComponentFixture(int x, String label) {
}
