package build.codemodel.objectoriented.descriptor;

/*-
 * #%L
 * Object-Oriented Code Model
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

import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.VoidTypeUsage;
import build.codemodel.objectoriented.ObjectOrientedCodeModel;
import build.codemodel.objectoriented.naming.MethodName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks down the contract of {@link MethodDescriptor#overrideKey()}, the key used by {@code InjectionFramework}
 * to detect method overrides and deduplicate injection points as it walks the type hierarchy from leaf to root.
 *
 * <p>Unlike {@link MethodDescriptor#signature()} (see {@link MethodDescriptorSignatureTests}), this is not a
 * display string — it deliberately folds in the declaring type's namespace so that {@link AccessModifier#PRIVATE}
 * methods (which are not polymorphic) never collide across declaring classes.
 *
 * <p>The key format depends on the {@link AccessModifier} trait and the method's namespace:
 * <ul>
 *   <li>PUBLIC / PROTECTED — {@code "<returnType> <name>(<paramTypes>)"}</li>
 *   <li>package-private (no modifier) with namespace — {@code "<namespace> <returnType> <name>(<paramTypes>)"}</li>
 *   <li>PRIVATE with namespace — {@code "<namespace>.<returnType> <name>(<paramTypes>)"}</li>
 * </ul>
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class MethodDescriptorOverrideKeyTests {

    private NonCachingNameProvider naming;
    private ObjectOrientedCodeModel codeModel;
    private ClassTypeDescriptor declaringType;
    private Optional<Namespace> classNamespace;

    @BeforeEach
    void setUp() {
        naming = new NonCachingNameProvider();
        codeModel = new ObjectOrientedCodeModel(naming);
        declaringType = ClassTypeDescriptor.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.MyService"));
        classNamespace = Namespace.of(IrreducibleName.of("com.example.MyService"));
    }

    private TypeUsage specific(final String qualifiedName) {
        return SpecificTypeUsage.of(codeModel, naming.getEmptyModuleTypeName(qualifiedName));
    }

    private FormalParameterDescriptor param(final TypeUsage type) {
        return FormalParameterDescriptor.of(codeModel, Optional.empty(), type);
    }

    private MethodDescriptor method(final String name,
                                    final TypeUsage returnType,
                                    final Optional<Namespace> namespace,
                                    final FormalParameterDescriptor... params) {
        final var methodName = MethodName.of(Optional.empty(), namespace, Optional.empty(),
            IrreducibleName.of(name));
        return MethodDescriptor.of(declaringType, methodName, returnType, Stream.of(params));
    }

    private MethodDescriptor withModifier(final MethodDescriptor descriptor, final AccessModifier modifier) {
        descriptor.addTrait(modifier);
        return descriptor;
    }

    // --- PUBLIC and PROTECTED: no namespace prefix ---

    @Test
    void publicMethod_namedReturn_noParams() {
        final var descriptor = withModifier(
            method("getValue", specific("java.lang.String"), Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.overrideKey()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void protectedMethod_producesIdenticalFormatToPublic() {
        final var descriptor = withModifier(
            method("getValue", specific("java.lang.String"), Optional.empty()),
            AccessModifier.PROTECTED);

        assertThat(descriptor.overrideKey()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void publicMethod_namespaceIsIgnored() {
        // namespace on the MethodName is only used for private / package-private methods
        final var descriptor = withModifier(
            method("getValue", specific("java.lang.String"), classNamespace),
            AccessModifier.PUBLIC);

        assertThat(descriptor.overrideKey()).isEqualTo("java.lang.String getValue()");
    }

    // --- Package-private (null modifier): namespace prepended with space ---

    @Test
    void packagePrivate_withNamespace_prependsNamespaceWithSpace() {
        final var descriptor = method("doWork", specific("java.lang.String"), classNamespace);
        // no addTrait call → AccessModifier stays null

        assertThat(descriptor.overrideKey()).isEqualTo("com.example.MyService java.lang.String doWork()");
    }

    @Test
    void packagePrivate_noNamespace_noPrefix() {
        final var descriptor = method("doWork", specific("java.lang.String"), Optional.empty());

        assertThat(descriptor.overrideKey()).isEqualTo("java.lang.String doWork()");
    }

    @Test
    void packagePrivate_voidReturn_withNamespace() {
        final var descriptor = method("doWork", VoidTypeUsage.create(codeModel), classNamespace);

        assertThat(descriptor.overrideKey()).isEqualTo("com.example.MyService void doWork()");
    }

    // --- PRIVATE: namespace prepended with dot ---

    @Test
    void private_withNamespace_prependsNamespaceWithDot() {
        final var descriptor = withModifier(
            method("doSecret", specific("java.lang.String"), classNamespace),
            AccessModifier.PRIVATE);

        assertThat(descriptor.overrideKey()).isEqualTo("com.example.MyService.java.lang.String doSecret()");
    }

    @Test
    void private_noNamespace_noPrefix() {
        final var descriptor = withModifier(
            method("doSecret", specific("java.lang.String"), Optional.empty()),
            AccessModifier.PRIVATE);

        assertThat(descriptor.overrideKey()).isEqualTo("java.lang.String doSecret()");
    }

    // --- DI-critical: override detection ---

    @Test
    void overridingMethod_producesIdenticalOverrideKey_enablingInjectionPointDeduplication() {
        // InjectionFramework walks the hierarchy from leaf → root. For each method it calls
        // injectionPoints.remove(overrideKey) then conditionally puts the current one back.
        // Override detection only works if super and sub produce the same key.

        final var superType = ClassTypeDescriptor.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.SuperService"));
        final var subType = ClassTypeDescriptor.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.ConcreteService"));

        final var methodName = IrreducibleName.of("setFoo");
        final var fooType = specific("com.example.Foo");

        final var superMethod = withModifier(
            MethodDescriptor.of(superType,
                MethodName.of(Optional.empty(), Optional.empty(), Optional.empty(), methodName),
                VoidTypeUsage.create(codeModel),
                Stream.of(param(fooType))),
            AccessModifier.PUBLIC);

        final var subMethod = withModifier(
            MethodDescriptor.of(subType,
                MethodName.of(Optional.empty(), Optional.empty(), Optional.empty(), methodName),
                VoidTypeUsage.create(codeModel),
                Stream.of(param(fooType))),
            AccessModifier.PUBLIC);

        assertThat(superMethod.overrideKey())
            .isEqualTo(subMethod.overrideKey())
            .isEqualTo("void setFoo(com.example.Foo)");
    }

    @Test
    void privateOverride_differentOverrideKeys_notDeduplicatedByDI() {
        // PRIVATE methods are not polymorphic — each class keeps its own copy.
        // InjectionFramework relies on them having distinct keys so that
        // the subclass's private @Inject method doesn't accidentally remove the
        // superclass's private @Inject method from the injection point map.

        final var superNamespace = Namespace.of(IrreducibleName.of("com.example.SuperService"));
        final var subNamespace = Namespace.of(IrreducibleName.of("com.example.ConcreteService"));

        final var superType = ClassTypeDescriptor.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.SuperService"));
        final var subType = ClassTypeDescriptor.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.ConcreteService"));

        final var methodName = IrreducibleName.of("injectInternal");
        final var fooType = specific("com.example.Foo");

        final var superMethod = withModifier(
            MethodDescriptor.of(superType,
                MethodName.of(Optional.empty(), superNamespace, Optional.empty(), methodName),
                VoidTypeUsage.create(codeModel),
                Stream.of(param(fooType))),
            AccessModifier.PRIVATE);

        final var subMethod = withModifier(
            MethodDescriptor.of(subType,
                MethodName.of(Optional.empty(), subNamespace, Optional.empty(), methodName),
                VoidTypeUsage.create(codeModel),
                Stream.of(param(fooType))),
            AccessModifier.PRIVATE);

        assertThat(superMethod.overrideKey()).isEqualTo("com.example.SuperService.void injectInternal(com.example.Foo)");
        assertThat(subMethod.overrideKey()).isEqualTo("com.example.ConcreteService.void injectInternal(com.example.Foo)");
        assertThat(superMethod.overrideKey()).isNotEqualTo(subMethod.overrideKey());
    }
}
