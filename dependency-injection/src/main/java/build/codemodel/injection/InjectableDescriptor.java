package build.codemodel.injection;

/*-
 * #%L
 * Dependency Injection
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

import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines information concerning the inject-ability of a {@link JDKTypeDescriptor}
 * with the {@link InjectionFramework}.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
public class InjectableDescriptor
    implements Trait {

    /**
     * The {@link JDKTypeDescriptor} for which the {@link InjectableDescriptor} was established.
     */
    private final JDKTypeDescriptor typeDescriptor;

    /**
     * The {@link InjectionPoint}s defined by the {@link JDKTypeDescriptor}.
     */
    private final ArrayList<InjectionPoint> injectionPoints;

    /**
     * The {@link PostInject} {@link MethodDescriptor}s.
     */
    private final ArrayList<MethodDescriptor> postInjectionMethods;

    /**
     * Constructs a new {@link InjectableDescriptor} for the specified {@link JDKTypeDescriptor}.
     *
     * @param typeDescriptor       the {@link JDKTypeDescriptor}
     * @param injectionPoints      the {@link InjectionPoint}s
     * @param postInjectionMethods the {@link PostInject} annotated {@link MethodDescriptor}s
     */
    private InjectableDescriptor(final JDKTypeDescriptor typeDescriptor,
                                 final Stream<InjectionPoint> injectionPoints,
                                 final Stream<MethodDescriptor> postInjectionMethods) {

        this.typeDescriptor = Objects.requireNonNull(typeDescriptor, "The TypeDescriptor must not be null");
        this.injectionPoints = injectionPoints == null
            ? new ArrayList<>()
            : injectionPoints.collect(Collectors.toCollection(ArrayList::new));
        this.postInjectionMethods = postInjectionMethods == null
            ? new ArrayList<>()
            : postInjectionMethods.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Determines if there are {@link InjectionPoint}s defined for the {@link JDKTypeDescriptor}
     *
     * @return {@code true} if there are {@link InjectionPoint}, {@code false} otherwise
     */
    public boolean isInjectable() {
        return !this.injectionPoints.isEmpty();
    }

    /**
     * Obtains the {@link JDKTypeDescriptor} for which the {@link InjectableDescriptor} was established.
     *
     * @return the {@link JDKTypeDescriptor}
     */
    public JDKTypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    /**
     * Obtains the {@link InjectionPoint}s defined for the {@link JDKTypeDescriptor}.
     *
     * @return the {@link InjectionPoint}s
     */
    public Stream<InjectionPoint> injectionPoints() {
        return this.injectionPoints.stream();
    }

    /**
     * Obtains the {@link PostInject} annotated {@link MethodDescriptor}s defined for the {@link JDKTypeDescriptor}.
     *
     * @return the {@link Stream} of {@link PostInject} {@link MethodDescriptor}s
     */
    public Stream<MethodDescriptor> postInjectionMethods() {
        return this.postInjectionMethods.stream();
    }

    /**
     * Creates a new {@link InjectableDescriptor} for the specified {@link JDKTypeDescriptor}.
     *
     * @param typeDescriptor       the {@link JDKTypeDescriptor}
     * @param injectionPoints      the {@link InjectionPoint}s
     * @param postInjectionMethods the {@link PostInject} annotated {@link MethodDescriptor}s
     */
    public static InjectableDescriptor of(final JDKTypeDescriptor typeDescriptor,
                                          final Stream<InjectionPoint> injectionPoints,
                                          final Stream<MethodDescriptor> postInjectionMethods) {

        return new InjectableDescriptor(typeDescriptor, injectionPoints, postInjectionMethods);
    }
}
