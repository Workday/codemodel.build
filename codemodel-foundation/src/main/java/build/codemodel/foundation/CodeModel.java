package build.codemodel.foundation;

/*-
 * #%L
 * Code Model Foundation
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

import build.base.foundation.stream.Streamable;
import build.base.query.Queryable;
import build.codemodel.foundation.descriptor.ModuleDescriptor;
import build.codemodel.foundation.descriptor.NamespaceDescriptor;
import build.codemodel.foundation.descriptor.PolymorphicModuleDescriptor;
import build.codemodel.foundation.descriptor.PolymorphicNamespaceDescriptor;
import build.codemodel.foundation.descriptor.PolymorphicTypeDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * {@link CodeModel}s provide a simplified <i>modular</i> representation of the <i>types</i> defined, required and/or
 * used by a model, domain, application, program or system, in the form of {@link TypeDescriptor}s.
 * <p>
 * The <i>types</i> represented by {@link TypeDescriptor}s in a {@link CodeModel} are programming language and paradigm
 * agnostic.  {@link CodeModel}s neither require, warrant nor demand a <i>textual representation</i> in order to be
 * established or used.  While many <i>textual representations</i> are theoretically possible for {@link CodeModel}s,
 * such representations are always independent of the {@link CodeModel}s themselves.  {@link CodeModel}s should
 * neither constrain nor dictate such <i>textual representations</i> and likewise, <i>textual representations</i> should
 * not influence, constrain nor dictate the design and use of {@link CodeModel}s.
 * <p>
 * {@link CodeModel}s may represent in an incomplete, ambiguous or contradictory set of <i>types</i> .  This is
 * important as it allows {@link CodeModel}s to represent partial information, that may later be completed or
 * corrected. Therefore, no assumptions should be made about the correctness of a {@link CodeModel}.  It's only when a
 * {@link CodeModel} has been through some <i>validation</i> or <i>verification</i> process, may they be considered
 * complete and correct for the context in which they are being used.
 * <p>
 * {@link CodeModel}s are always immutable.  While thread-safe, care should be taken when mutating them across
 * multiple threads, especially if those threads are creating contradictory changes.  It is the responsibility of
 * applications using {@link CodeModel}s to prevent such changes.
 *
 * @author brian.oliver
 * @since Mar-2024
 */
public interface CodeModel
    extends Traitable, Queryable {

    /**
     * Obtains the {@link NameProvider} that is used by the {@link CodeModel} for naming.
     *
     * @return the {@link NameProvider}
     */
    NameProvider getNameProvider();

    /**
     * Creates a {@link TypeDescriptor} for the specified {@link TypeName}.
     * <p>
     * Should a {@link TypeDescriptor} for the specified {@link TypeName} already exist, the
     * existing {@link TypeDescriptor} is returned.
     *
     * @param typeName the {@link TypeName}
     * @return the {@link TypeDescriptor}
     */
    default TypeDescriptor createTypeDescriptor(final TypeName typeName) {
        return createTypeDescriptor(typeName, PolymorphicTypeDescriptor::of);
    }

    /**
     * Creates a {@link TypeDescriptor} for the specified {@link TypeName} using the provided
     * {@link java.util.function.BiFunction} to supply the {@link TypeDescriptor} implementation.
     * <p>
     * Should a {@link TypeDescriptor} for the specified {@link TypeName} already exist, the
     * existing {@link TypeDescriptor} is returned.
     *
     * @param typeName the {@link TypeName}
     * @param supplier the {@link BiFunction} to supply the {@link TypeDescriptor} implementation
     * @return the {@link TypeDescriptor}
     */
    default <T extends TypeDescriptor> T createTypeDescriptor(
        final TypeName typeName,
        final BiFunction<? super CodeModel, ? super TypeName, T> supplier) {
        return createTypeDescriptor(typeName, supplier, Streamable.empty());
    }

    /**
     * Creates a {@link TypeDescriptor} for the specified {@link TypeName} using the provided
     * {@link java.util.function.BiFunction} to supply the {@link TypeDescriptor} implementation, with
     * additional {@link Trait}s applied atomically during creation.
     * <p>
     * Should a {@link TypeDescriptor} for the specified {@link TypeName} already exist, the
     * existing {@link TypeDescriptor} is returned.
     *
     * @param typeName       the {@link TypeName}
     * @param supplier       the {@link BiFunction} to supply the {@link TypeDescriptor} implementation
     * @param traitSuppliers the {@link Streamable} of {@link Function}s to supply {@link Trait}s to apply to the
     *                       {@link TypeDescriptor}
     * @return the {@link TypeDescriptor}
     */
    <T extends TypeDescriptor> T createTypeDescriptor(
        TypeName typeName,
        BiFunction<? super CodeModel, ? super TypeName, T> supplier,
        Streamable<? extends Function<? super T, ? extends Trait>> traitSuppliers);

    /**
     * Attempts to obtain the {@link TypeDescriptor} with the specified {@link TypeName}.
     *
     * @param typeName the {@link TypeName}
     * @return the {@link Optional} {@link TypeDescriptor}
     */
    Optional<TypeDescriptor> getTypeDescriptor(TypeName typeName);

    /**
     * Attempts to obtain the {@link TypeDescriptor} with the specified {@link TypeName} of the specified {@link Class}.
     *
     * @param <T>                 the type of {@link TypeDescriptor}
     * @param typeName            the {@link TypeName}
     * @param typeDescriptorClass the {@link Class} of {@link TypeDescriptor}
     * @return the {@link Optional} {@link TypeDescriptor}
     */
    default <T> Optional<T> getTypeDescriptor(final TypeName typeName,
                                              final Class<T> typeDescriptorClass) {

        return getTypeDescriptor(typeName)
            .filter(typeDescriptorClass::isInstance)
            .map(typeDescriptorClass::cast);
    }

    /**
     * Attempts to obtain the {@link TypeDescriptor} with the specified {@link TypeUsage}.
     *
     * @param typeUsage the {@link TypeUsage}
     * @return the {@link Optional} {@link TypeDescriptor}
     */
    default Optional<TypeDescriptor> getTypeDescriptor(final TypeUsage typeUsage) {
        return typeUsage instanceof NamedTypeUsage namedTypeUsage
            ? getTypeDescriptor(namedTypeUsage.typeName())
            : Optional.empty();
    }

    /**
     * Attempts to obtain the {@link TypeDescriptor} with the specified {@link TypeUsage} of the specified {@link Class}.
     *
     * @param <T>                 the type of the {@link TypeDescriptor}
     * @param typeUsage           the {@link TypeUsage}
     * @param typeDescriptorClass the {@link Class} of the {@link TypeDescriptor}
     * @return the {@link Optional} {@link TypeDescriptor}
     */
    default <T> Optional<T> getTypeDescriptor(final TypeUsage typeUsage,
                                              final Class<T> typeDescriptorClass) {

        return typeUsage instanceof NamedTypeUsage namedTypeUsage
            ? getTypeDescriptor(namedTypeUsage.typeName(), typeDescriptorClass)
            : Optional.empty();
    }

    /**
     * Obtains a {@link Stream} of known {@link TypeDescriptor}s.
     *
     * @return a {@link Stream} of {@link TypeDescriptor}
     */
    Stream<TypeDescriptor> typeDescriptors();

    /**
     * Obtains a {@link Stream} of known {@link TypeDescriptor}s assignable to the specified {@link Class}.
     *
     * @param <T>                 the type of {@link TypeDescriptor}
     * @param typeDescriptorClass the {@link Class} of {@link TypeDescriptor}
     * @return a {@link Stream} of {@link TypeDescriptor}s of the specified {@link Class}
     */
    default <T> Stream<T> typeDescriptors(final Class<T> typeDescriptorClass) {
        return typeDescriptors()
            .filter(typeDescriptorClass::isInstance)
            .map(typeDescriptorClass::cast);
    }

    /**
     * Creates a {@link ModuleDescriptor} for the specified {@link ModuleName}.
     * <p>
     * Should a {@link ModuleDescriptor} for the specified {@link ModuleName} already exist, the
     * existing {@link ModuleDescriptor} is returned.
     *
     * @param moduleName the {@link ModuleName}
     * @return the {@link ModuleDescriptor}
     */
    default ModuleDescriptor createModuleDescriptor(final ModuleName moduleName) {
        return createModuleDescriptor(moduleName, PolymorphicModuleDescriptor::of);
    }

    /**
     * Creates a {@link ModuleDescriptor} for the specified {@link ModuleName} using the provided
     * {@link java.util.function.BiFunction} to supply the {@link ModuleDescriptor} implementation.
     * <p>
     * Should a {@link ModuleDescriptor} for the specified {@link ModuleName} already exist, the
     * existing {@link ModuleDescriptor} is returned.
     *
     * @param moduleName the {@link ModuleName}
     * @param supplier   the {@link BiFunction} to supply the {@link ModuleDescriptor} implementation
     * @return the {@link ModuleDescriptor}
     */
    default <M extends ModuleDescriptor> M createModuleDescriptor(
        final ModuleName moduleName,
        final BiFunction<? super CodeModel, ? super ModuleName, M> supplier) {
        return createModuleDescriptor(moduleName, supplier, Streamable.empty());
    }

    /**
     * Creates a {@link ModuleDescriptor} for the specified {@link ModuleName} using the provided
     * {@link java.util.function.BiFunction} to supply the {@link ModuleDescriptor} implementation, with
     * additional {@link Trait}s applied atomically during creation.
     * <p>
     * Should a {@link ModuleDescriptor} for the specified {@link ModuleName} already exist, the
     * existing {@link ModuleDescriptor} is returned.
     *
     * @param moduleName     the {@link ModuleName}
     * @param supplier       the {@link BiFunction} to supply the {@link ModuleDescriptor} implementation
     * @param traitSuppliers the {@link Streamable} of {@link Function}s to supply {@link Trait}s to apply to the
     *                       {@link ModuleDescriptor}
     * @return the {@link ModuleDescriptor}
     */
    <M extends ModuleDescriptor> M createModuleDescriptor(
        ModuleName moduleName,
        BiFunction<? super CodeModel, ? super ModuleName, M> supplier,
        Streamable<? extends Function<? super M, ? extends Trait>> traitSuppliers);

    /**
     * Obtains a {@link Stream} of known {@link ModuleDescriptor}s.
     *
     * @return a {@link Stream} of {@link ModuleDescriptor}
     */
    Stream<ModuleDescriptor> moduleDescriptors();

    /**
     * Attempts to obtain the {@link ModuleDescriptor} with the specified {@link ModuleName}.
     *
     * @param moduleName the {@link ModuleName}
     * @return the {@link Optional} {@link ModuleDescriptor}
     */
    Optional<ModuleDescriptor> getModuleDescriptor(ModuleName moduleName);

    /**
     * Obtains a {@link Stream} of known {@link NamespaceDescriptor}s.
     *
     * @return a {@link Stream} of {@link NamespaceDescriptor}
     */
    Stream<NamespaceDescriptor> namespaceDescriptors();

    /**
     * Attempts to obtain the {@link NamespaceDescriptor} with the specified {@link Namespace}.
     *
     * @param namespace the {@link Namespace}
     * @return the {@link Optional} {@link NamespaceDescriptor}
     */
    Optional<NamespaceDescriptor> getNamespaceDescriptor(Namespace namespace);

    /**
     * Creates a {@link NamespaceDescriptor} for the specified {@link Namespace}.
     * <p>
     * Should a {@link NamespaceDescriptor} for the specified {@link Namespace} already exist, the
     * existing {@link NamespaceDescriptor} is returned.
     *
     * @param namespace the {@link Namespace}
     * @return the {@link NamespaceDescriptor}
     */
    default NamespaceDescriptor createNamespaceDescriptor(final Namespace namespace) {
        return createNamespaceDescriptor(namespace, PolymorphicNamespaceDescriptor::of);
    }

    /**
     * Creates a {@link NamespaceDescriptor} for the specified {@link Namespace} using the provided
     * {@link java.util.function.BiFunction} to supply the {@link NamespaceDescriptor} implementation.
     * <p>
     * Should a {@link NamespaceDescriptor} for the specified {@link Namespace} already exist, the
     * existing {@link NamespaceDescriptor} is returned.
     *
     * @param namespace the {@link Namespace}
     * @param supplier  the {@link BiFunction} to supply the {@link NamespaceDescriptor} implementation
     * @return the {@link NamespaceDescriptor}
     */
    default <N extends NamespaceDescriptor> N createNamespaceDescriptor(
        final Namespace namespace,
        final BiFunction<? super CodeModel, ? super Namespace, N> supplier) {

        return createNamespaceDescriptor(namespace, supplier, Streamable.empty());
    }

    /**
     * Creates a {@link NamespaceDescriptor} for the specified {@link Namespace} using the provided
     * {@link java.util.function.BiFunction} to supply the {@link NamespaceDescriptor} implementation, with
     * additional {@link Trait}s applied atomically during creation.
     * <p>
     * Should a {@link NamespaceDescriptor} for the specified {@link Namespace} already exist, the
     * existing {@link NamespaceDescriptor} is returned.
     *
     * @param namespace      the {@link Namespace}
     * @param supplier       the {@link BiFunction} to supply the {@link NamespaceDescriptor} implementation
     * @param traitSuppliers the {@link Streamable} of {@link Function}s to supply {@link Trait}s to apply to the
     *                       {@link NamespaceDescriptor}
     * @return the {@link NamespaceDescriptor}
     */
    <N extends NamespaceDescriptor> N createNamespaceDescriptor(
        Namespace namespace,
        BiFunction<? super CodeModel, ? super Namespace, N> supplier,
        Streamable<? extends Function<? super N, ? extends Trait>> traitSuppliers);

    /**
     * Creates a new concrete {@link Traitable}, managed by the {@link CodeModel}, on behalf of the specified
     * {@link Traitable} {@link Object}.
     *
     * @param object the {@link Traitable} {@link Object}
     * @return the new {@link Traitable} for the {@link Object} in the {@link CodeModel}
     */
    default Traitable createTraitable(final Traitable object) {
        return createTraitable(object, Streamable.empty());
    }

    /**
     * Creates a new concrete {@link Traitable}, managed by the {@link CodeModel}, on behalf of the specified
     * {@link Traitable} {@link Object}, with additional {@link Trait}s applied atomically during creation.
     *
     * @param object         the {@link Traitable} {@link Object}
     * @param traitSuppliers the {@link Streamable} of {@link Function}s to supply {@link Trait}s to apply to the
     *                       {@link Traitable}
     * @return the new {@link Traitable} for the {@link Object} in the {@link CodeModel}
     */
    Traitable createTraitable(
        Traitable object,
        Streamable<? extends Function<? super Traitable, ? extends Trait>> traitSuppliers);
}
