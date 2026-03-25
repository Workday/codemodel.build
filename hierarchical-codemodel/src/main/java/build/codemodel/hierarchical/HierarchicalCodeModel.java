package build.codemodel.hierarchical;

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
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.hierarchical.descriptor.HierarchicalTypeDescriptor;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link CodeModel} whereby one or more {@link TypeDescriptor}s may be arranged in a
 * <a href="https://en.wikipedia.org/wiki/Hierarchy">Hierarchy</a>.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public interface HierarchicalCodeModel
    extends CodeModel {

    /**
     * Obtains the <i>root</i> {@link HierarchicalTypeDescriptor}s in the {@link HierarchicalCodeModel}, those
     * which they have no <i>parents</i>.
     *
     * @return {@link Stream} <i>root</i> {@link HierarchicalTypeDescriptor}s
     */
    default Stream<HierarchicalTypeDescriptor> roots() {

        return typeDescriptors()
            .filter(HierarchicalTypeDescriptor.class::isInstance)
            .map(HierarchicalTypeDescriptor.class::cast)
            .filter(HierarchicalTypeDescriptor::isRoot);
    }

    /**
     * Determines if the specified {@link TypeName} is <i>assignable to</i> (is the same as or an <i>ancestor</i> of)
     * another {@link TypeName}.
     *
     * @param fromTypeName the from {@link TypeName}
     * @param toTypeName   the to {@link TypeName}
     * @return {@code true} if the {@link TypeName} is assignable, {@code false} otherwise
     */
    default boolean isAssignable(final TypeName fromTypeName,
                                 final TypeName toTypeName) {

        Objects.requireNonNull(fromTypeName, "The from TypeName must not be null");
        Objects.requireNonNull(toTypeName, "The to TypeName must not be null");

        final var fromTypeDescriptor = getTypeDescriptor(fromTypeName, HierarchicalTypeDescriptor.class)
            .orElseThrow(
                () -> new IllegalArgumentException("The fromTypeName does not exist as a HierarchicalTypeDescriptor"));

        final var toTypeDescriptor = getTypeDescriptor(toTypeName, HierarchicalTypeDescriptor.class)
            .orElseThrow(
                () -> new IllegalArgumentException("The toTypeName does not exist as a HierarchicalTypeDescriptor"));

        return fromTypeDescriptor.isAssignableTo(toTypeDescriptor);
    }

    /**
     * Determines if the specified {@link HierarchicalTypeDescriptor} is <i>assignable to</i>
     * (is the same as or an <i>ancestor</i> of) another {@link HierarchicalTypeDescriptor}.
     *
     * @param fromTypeDescriptor the from {@link HierarchicalTypeDescriptor}
     * @param toTypeDescriptor   the to {@link HierarchicalTypeDescriptor}
     * @return {@code true} if the {@link HierarchicalTypeDescriptor} is assignable, {@code false} otherwise
     */
    static boolean isAssignable(final HierarchicalTypeDescriptor fromTypeDescriptor,
                                final HierarchicalTypeDescriptor toTypeDescriptor) {

        Objects.requireNonNull(fromTypeDescriptor, "The from TypeDescriptor must not be null");
        Objects.requireNonNull(toTypeDescriptor, "The to TypeDescriptor must not be null");

        return fromTypeDescriptor.isAssignableTo(toTypeDescriptor);
    }

    /**
     * Obtains the {@link HierarchicalTypeDescriptor}s in the {@link HierarchicalCodeModel} that are
     * assignable to every specified {@link HierarchicalTypeDescriptor}.
     *
     * @param stream the {@link HierarchicalTypeDescriptor}s to which the resulting {@link HierarchicalTypeDescriptor}
     *               must be assignable
     * @return the {@link Stream} of assignable {@link HierarchicalTypeDescriptor}
     * @see HierarchicalTypeDescriptor#isAssignableTo(HierarchicalTypeDescriptor)
     */
    static Stream<HierarchicalTypeDescriptor> getAssignableTypeDescriptors(final Stream<? extends HierarchicalTypeDescriptor> stream) {

        if (stream == null) {
            return Stream.empty();
        }

        // create the Set of HierarchicalTypeDescriptors for which we need to determine assignable types
        final var typeDescriptors = stream
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (typeDescriptors.isEmpty()) {
            return Stream.empty();
        }

        if (typeDescriptors.size() == 1) {
            return Stream.of(typeDescriptors.getFirst());
        }

        // determine the initial types that are assignable based on the first TypeDescriptor
        final var assignableTypes = new LinkedHashSet<HierarchicalTypeDescriptor>();
        assignableTypes.addFirst(typeDescriptors.getFirst());
        typeDescriptors.getFirst()
            .descendants()
            .forEach(assignableTypes::add);

        // remove the intersecting descendants, excluding those of the first TypeDescriptor
        typeDescriptors.stream()
            .skip(1)
            .forEach(typeDescriptor -> {

                if (!assignableTypes.isEmpty()) {
                    final var descendants = typeDescriptor
                        .descendants()
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                    descendants.add(typeDescriptor);

                    assignableTypes.retainAll(descendants);
                }
            });

        return assignableTypes.stream();
    }

    /**
     * Obtains the {@link HierarchicalTypeDescriptor}s in the {@link HierarchicalCodeModel} that are
     * assignable to every specified {@link HierarchicalTypeDescriptor}.
     *
     * @param hierarchicalTypeDescriptors the {@link HierarchicalTypeDescriptor}s to which the resulting
     *                                    {@link HierarchicalTypeDescriptor} must be assignable
     * @return the {@link Stream} of assignable {@link HierarchicalTypeDescriptor}
     * @see HierarchicalTypeDescriptor#isAssignableTo(HierarchicalTypeDescriptor)
     */
    static Stream<HierarchicalTypeDescriptor> getAssignableTypeDescriptors(final HierarchicalTypeDescriptor... hierarchicalTypeDescriptors) {

        return hierarchicalTypeDescriptors == null
            ? Stream.empty()
            : getAssignableTypeDescriptors(Stream.of(hierarchicalTypeDescriptors));
    }
}
