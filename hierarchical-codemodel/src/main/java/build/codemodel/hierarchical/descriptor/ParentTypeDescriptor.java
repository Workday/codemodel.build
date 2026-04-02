package build.codemodel.hierarchical.descriptor;

/*-
 * #%L
 * Hierarchical Code Model
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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.Dependent;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.stream.Stream;

/**
 * A {@link Trait} defining the <i>parent</i> {@link TypeDescriptor} of a {@link TypeDescriptor} in a
 * <a href="https://en.wikipedia.org/wiki/Nominal_type_system">Nominal or Normative</a> {@link CodeModel}, thus
 * forming a <a href="https://en.wikipedia.org/wiki/Class_hierarchy">Type Hierarchy</a>.
 *
 * @author brian.oliver
 * @see HierarchicalTypeDescriptor
 * @since May-2024
 */
public interface ParentTypeDescriptor
    extends Trait, Dependent, Traitable {

    /**
     * Obtains the {@link NamedTypeUsage} defining the <i>parent</i> type.
     *
     * @return the {@link NamedTypeUsage}
     */
    NamedTypeUsage parentTypeUsage();

    /**
     * Obtains the {@link TypeName} of the <i>parent</i> type.
     *
     * @return the <i>parent</i> {@link TypeName}
     */
    default TypeName parentTypeName() {
        return parentTypeUsage().typeName();
    }

    /**
     * Obtains the {@link HierarchicalTypeDescriptor} of the <i>parent</i> type.
     *
     * @return the {@link HierarchicalTypeDescriptor} of the <i>parent</i> type
     */
    default HierarchicalTypeDescriptor parent() {
        return codeModel()
            .getTypeDescriptor(parentTypeName(), HierarchicalTypeDescriptor.class)
            .orElseThrow(
                () -> new IllegalStateException("Failed to find the parent TypeDescriptor [" + parentTypeName() + "]"));
    }

    @Override
    default Stream<TypeUsage> dependencies() {
        return Stream.of(parentTypeUsage());
    }
}
