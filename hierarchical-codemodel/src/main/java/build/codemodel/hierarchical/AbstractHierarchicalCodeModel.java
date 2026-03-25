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

import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Unmarshal;
import build.codemodel.foundation.AbstractCodeModel;
import build.codemodel.foundation.descriptor.ModuleDescriptor;
import build.codemodel.foundation.descriptor.NamespaceDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.hierarchical.descriptor.HierarchicalTypeDescriptor;
import build.codemodel.hierarchical.descriptor.ParentTypeDescriptor;

import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * An {@code abstract} {@link HierarchicalCodeModel}.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public abstract class AbstractHierarchicalCodeModel
    extends AbstractCodeModel
    implements HierarchicalCodeModel {

    /**
     * The <i>parent</i> {@link TypeName}s for which one or more <i>child</i> {@link HierarchicalTypeDescriptor}s
     * require them, but as yet the {@link HierarchicalTypeDescriptor} for the said <i>parent</i> is unknown.
     */
    private ConcurrentHashMap<TypeName, LinkedHashSet<HierarchicalTypeDescriptor>> orphanedChildren;

    /**
     * The <i>parent</i> {@link HierarchicalTypeDescriptor}s by <i>child</i> {@link HierarchicalTypeDescriptor}.
     */
    private ConcurrentHashMap<HierarchicalTypeDescriptor, LinkedHashSet<HierarchicalTypeDescriptor>> parents;

    /**
     * The <i>child</i> {@link HierarchicalTypeDescriptor}s by <i>parents</i> {@link HierarchicalTypeDescriptor}.
     */
    private ConcurrentHashMap<HierarchicalTypeDescriptor, LinkedHashSet<HierarchicalTypeDescriptor>> children;

    /**
     * Constructs an empty {@link AbstractHierarchicalCodeModel}.
     *
     * @param nameProvider the {@link NameProvider}
     */
    protected AbstractHierarchicalCodeModel(final NameProvider nameProvider) {
        super(nameProvider);

        this.orphanedChildren = new ConcurrentHashMap<>();
        this.parents = new ConcurrentHashMap<>();
        this.children = new ConcurrentHashMap<>();
    }

    /**
     * {@link Unmarshal}s an {@link AbstractHierarchicalCodeModel} using a {@link Marshaller}.
     *
     * @param nameProvider         the {@link NameProvider}
     * @param marshaller           the {@link Marshaller}
     * @param traits               the {@link Traitable}
     * @param typeDescriptors      the {@link Stream} of {@link Marshalled} {@link TypeDescriptor}s
     * @param moduleDescriptors    the {@link Stream} of {@link Marshalled} {@link ModuleDescriptor}s
     * @param namespaceDescriptors the {@link Stream} of {@link Marshalled} {@link NamespaceDescriptor}s
     */
    protected AbstractHierarchicalCodeModel(final NameProvider nameProvider,
                                             final Marshaller marshaller,
                                             final Stream<Marshalled<Trait>> traits,
                                             final Stream<Marshalled<TypeDescriptor>> typeDescriptors,
                                             final Stream<Marshalled<ModuleDescriptor>> moduleDescriptors,
                                             final Stream<Marshalled<NamespaceDescriptor>> namespaceDescriptors) {

        super(nameProvider, marshaller, traits, typeDescriptors, moduleDescriptors, namespaceDescriptors);
    }

    @Override
    protected void prepareForUnmarshalling() {
        super.prepareForUnmarshalling();

        this.orphanedChildren = new ConcurrentHashMap<>();
        this.parents = new ConcurrentHashMap<>();
        this.children = new ConcurrentHashMap<>();
    }

    @Override
    protected void onCreatedTypeDescriptor(final TypeDescriptor typeDescriptor) {

        if (typeDescriptor instanceof HierarchicalTypeDescriptor hierarchicalTypeDescriptor) {

            // establish child and parent references
            // (we search all Traits that are ParentTypeDescriptors as we don't know if there are subclasses)
            typeDescriptor.traits()
                .filter(ParentTypeDescriptor.class::isInstance)
                .map(ParentTypeDescriptor.class::cast)
                .map(ParentTypeDescriptor::parentTypeName)
                .forEach(parentTypeName ->
                    onCreatedParentTypeDescriptor(parentTypeName, hierarchicalTypeDescriptor));

            // handle if the TypeDescriptor being added is the parent of some orphaned children
            this.orphanedChildren.compute(typeDescriptor.typeName(), (_, existing) -> {
                if (existing == null) {
                    return null;
                }

                existing.forEach(childTypeDescriptor -> {
                    onCreatedParentTypeDescriptor(hierarchicalTypeDescriptor, childTypeDescriptor);
                });

                return null;
            });
        }

        super.onCreatedTypeDescriptor(typeDescriptor);
    }

    /**
     * Invoked when a {@link ParentTypeDescriptor} for the specified <i>parent</i> {@link TypeName} is added to the
     * specified <i>child</i> {@link HierarchicalTypeDescriptor}.
     *
     * @param parentTypeName      the <i>parent</i> {@link TypeName}
     * @param childTypeDescriptor the <i>child</i> {@link HierarchicalTypeDescriptor}
     */
    public void onCreatedParentTypeDescriptor(final TypeName parentTypeName,
                                              final HierarchicalTypeDescriptor childTypeDescriptor) {

        getTypeDescriptor(parentTypeName)
            .filter(HierarchicalTypeDescriptor.class::isInstance)
            .map(HierarchicalTypeDescriptor.class::cast)
            .ifPresentOrElse(parentTypeDescriptor ->
                    onCreatedParentTypeDescriptor(parentTypeDescriptor, childTypeDescriptor),
                () -> onOrphanedChildTypeDescriptor(parentTypeName, childTypeDescriptor));
    }

    /**
     * Invoked when a {@link HierarchicalTypeDescriptor} for the specified <i>parent</i>] is added to the
     * specified <i>child</i> {@link HierarchicalTypeDescriptor}.
     *
     * @param parentTypeDescriptor the <i>parent</i> {@link HierarchicalTypeDescriptor}
     * @param childTypeDescriptor  the <i>child</i> {@link HierarchicalTypeDescriptor}
     */
    void onCreatedParentTypeDescriptor(final HierarchicalTypeDescriptor parentTypeDescriptor,
                                       final HierarchicalTypeDescriptor childTypeDescriptor) {

        this.children.compute(parentTypeDescriptor, (_, existing) -> {
            final var children = existing == null
                ? new LinkedHashSet<HierarchicalTypeDescriptor>()
                : existing;

            children.add(childTypeDescriptor);
            return children;
        });

        this.parents.compute(childTypeDescriptor, (_, existing) -> {
            final var parents = existing == null
                ? new LinkedHashSet<HierarchicalTypeDescriptor>()
                : existing;

            parents.add(parentTypeDescriptor);
            return parents;
        });
    }

    /**
     * Invoked when a {@link ParentTypeDescriptor} for the specified <i>parent</i> {@link TypeName} is unknown
     * for to the specified <i>child</i> {@link HierarchicalTypeDescriptor}.
     *
     * @param parentTypeName      the <i>parent</i> {@link TypeName}
     * @param childTypeDescriptor the <i>child</i> {@link HierarchicalTypeDescriptor}
     */
    void onOrphanedChildTypeDescriptor(final TypeName parentTypeName,
                                       final HierarchicalTypeDescriptor childTypeDescriptor) {

        this.orphanedChildren.compute(parentTypeName, (_, existing) -> {
            final var children = existing == null
                ? new LinkedHashSet<HierarchicalTypeDescriptor>()
                : existing;

            children.add(childTypeDescriptor);

            return children;
        });
    }

    /**
     * Invoked when a {@link ParentTypeDescriptor} for the specified <i>parent</i> {@link TypeName} is removed from the
     * specified <i>child</i> {@link HierarchicalTypeDescriptor}.
     *
     * @param parentTypeName      the <i>parent</i> {@link TypeName}
     * @param childTypeDescriptor the <i>child</i> {@link HierarchicalTypeDescriptor}
     */
    public void onRemovedParentTypeDescriptor(final TypeName parentTypeName,
                                              final HierarchicalTypeDescriptor childTypeDescriptor) {

        getTypeDescriptor(parentTypeName)
            .filter(HierarchicalTypeDescriptor.class::isInstance)
            .map(HierarchicalTypeDescriptor.class::cast)
            .ifPresentOrElse(parentTypeDescriptor -> {
                    // remove the child TypeDescriptor from the Parent TypeDescriptor
                    this.children.compute(parentTypeDescriptor, (_, existing) -> {
                        if (existing == null) {
                            return null;
                        }

                        existing.remove(childTypeDescriptor);

                        return existing.isEmpty() ? null : existing;
                    });

                    // remove the parent TypeDescriptor from child TypeDescriptor (parents)
                    this.parents.compute(childTypeDescriptor, (_, existing) -> {
                        if (existing == null) {
                            return null;
                        }

                        existing.remove(parentTypeDescriptor);

                        return existing.isEmpty() ? null : existing;
                    });
                },
                () -> {
                    // attempt to remove the child from orphaned
                    this.orphanedChildren.compute(parentTypeName, (_, existing) -> {
                        if (existing == null) {
                            return null;
                        }

                        existing.remove(childTypeDescriptor);

                        return existing.isEmpty() ? null : existing;
                    });
                });
    }

    /**
     * Obtains the <i>children</i> for the specified {@link HierarchicalTypeDescriptor}.
     *
     * @param typeDescriptor the {@link HierarchicalTypeDescriptor}
     * @return a {@link Stream} of <i>child</i> {@link HierarchicalTypeDescriptor}s
     */
    public Stream<HierarchicalTypeDescriptor> children(final HierarchicalTypeDescriptor typeDescriptor) {
        final var children = this.children.get(typeDescriptor);
        return children == null ? Stream.empty() : children.stream();
    }

    /**
     * Obtains the <i>parents</i> for the specified {@link HierarchicalTypeDescriptor}.
     *
     * @param typeDescriptor the {@link HierarchicalTypeDescriptor}
     * @return a {@link Stream} of <i>parents</i> {@link HierarchicalTypeDescriptor}s
     */
    public Stream<HierarchicalTypeDescriptor> parents(final HierarchicalTypeDescriptor typeDescriptor) {
        final var parents = this.parents.get(typeDescriptor);
        return parents == null ? Stream.empty() : parents.stream();
    }
}
