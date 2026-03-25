package build.codemodel.jdk.descriptor;

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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.TypeName;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.hierarchical.descriptor.HierarchicalTypeDescriptor;
import build.codemodel.jdk.JDKCodeModel;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.ImplementsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A {@link JDKCodeModel} specific {@link TypeDescriptor} representing JDK {@link Type}s.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public interface JDKTypeDescriptor
    extends HierarchicalTypeDescriptor {

    /**
     * Obtains the {@link NamedTypeUsage} for the {@code super} type of the {@link JDKTypeDescriptor}.
     *
     * @return the {@link Optional} {@link NamedTypeUsage} for the {@code super} type or {@link Optional#empty()}
     * if there is no {@code super} type
     */
    default Optional<NamedTypeUsage> parentTypeUsage() {
        return getTrait(ExtendsTypeDescriptor.class)
            .map(ExtendsTypeDescriptor::parentTypeUsage);
    }

    /**
     * Obtains the {@link JDKTypeDescriptor} of the {@code super} type.
     *
     * @return the {@link Optional} {@link JDKTypeDescriptor} of the {@code super} type, or {@link Optional#empty()}
     * if there is no {@code super} type
     */
    default Optional<JDKTypeDescriptor> parent() {
        return parents()
            .findFirst()
            .map(JDKTypeDescriptor.class::cast);
    }

    /**
     * Obtains the {@link NamedTypeUsage}s for the {@code interface}s implemented by the {@link JDKTypeDescriptor}.
     *
     * @return the {@link Stream} of {@link NamedTypeUsage}
     */
    default Stream<NamedTypeUsage> interfaceTypeUsages() {
        return traits(ImplementsTypeDescriptor.class)
            .map(ImplementsTypeDescriptor::parentTypeUsage);
    }

    /**
     * Obtains the {@link ConstructorDescriptor}s for the <i>Constructors</i> declared by the {@link JDKTypeDescriptor}.
     *
     * @return the {@link Stream} of declared {@link ConstructorDescriptor}s
     */
    default Stream<ConstructorDescriptor> declaredConstructors() {
        return traits(ConstructorDescriptor.class);
    }

    /**
     * Obtains the {@link MethodDescriptor}s for the <i>Methods</i> declared by the {@link JDKTypeDescriptor}.
     *
     * @return the {@link Stream} of declared {@link MethodDescriptor}s
     */
    default Stream<MethodDescriptor> declaredMethods() {
        return traits(MethodDescriptor.class);
    }

    /**
     * Obtains the {@link FieldDescriptor}s for the <i>Fields</i> declared by the {@link JDKTypeDescriptor}.
     *
     * @return the {@link Stream} of declared {@link FieldDescriptor}s
     */
    default Stream<FieldDescriptor> declaredFields() {
        return traits(FieldDescriptor.class);
    }

    /**
     * Obtains a {@link BiFunction} that can supply a suitable {@link JDKTypeDescriptor} for a {@link CodeModel}
     * and {@link TypeName} when requested for the specified {@link Class}.
     *
     * @param javaClass the {@link Class}
     * @return a {@link BiFunction} supplier of {@link JDKTypeDescriptor}s
     */
    static BiFunction<? super CodeModel, ? super TypeName, JDKTypeDescriptor> supplier(final Class<?> javaClass) {

        Objects.requireNonNull(javaClass, "The Class must not be null");

        return javaClass.isInterface()
            ? JDKInterfaceTypeDescriptor::of
            : JDKClassTypeDescriptor::of;
    }
}
