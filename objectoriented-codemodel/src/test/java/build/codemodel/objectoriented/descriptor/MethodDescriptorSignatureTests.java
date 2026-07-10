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

import build.base.foundation.Lazy;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.IntersectionTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnionTypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.VoidTypeUsage;
import build.codemodel.objectoriented.ObjectOrientedCodeModel;
import build.codemodel.objectoriented.naming.MethodName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks down the contract of {@link MethodDescriptor#signature()}, which is the key used by
 * {@code InjectionFramework} to detect method overrides and deduplicate injection points as it walks the
 * type hierarchy from leaf to root.
 *
 * <p>The signature format depends on the {@link AccessModifier} trait and the method's namespace:
 * <ul>
 *   <li>PUBLIC / PROTECTED — {@code "<returnType> <name>(<paramTypes>)"}</li>
 *   <li>package-private (no modifier) with namespace — {@code "<namespace> <returnType> <name>(<paramTypes>)"}</li>
 *   <li>PRIVATE with namespace — {@code "<namespace>.<returnType> <name>(<paramTypes>)"}</li>
 *   <li>all return types — always present via {@link TypeUsage#canonicalName()}, module qualifier stripped</li>
 *   <li>multiple parameters — comma-separated via {@link TypeUsage#canonicalName()}</li>
 * </ul>
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class MethodDescriptorSignatureTests {

    private NonCachingNameProvider naming;
    private ObjectOrientedCodeModel codeModel;
    private ClassTypeDescriptor declaringType;
    private Optional<Namespace> classNamespace;

    @BeforeEach
    void setUp() {
        naming = new NonCachingNameProvider();
        codeModel = new ObjectOrientedCodeModel(naming);
        declaringType = ClassTypeDescriptor.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.MyService"));
        classNamespace = Namespace.of(IrreducibleName.of("com.example.MyService"));
    }

    private TypeUsage specific(final String qualifiedName) {
        return SpecificTypeUsage.of(codeModel, naming.getTypeName(Optional.empty(), qualifiedName));
    }

    private TypeUsage specificWithModule(final String module, final String qualifiedName) {
        return SpecificTypeUsage.of(codeModel, naming.getTypeName(naming.getModuleName(module), qualifiedName));
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

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void protectedMethod_producesIdenticalFormatToPublic() {
        final var descriptor = withModifier(
            method("getValue", specific("java.lang.String"), Optional.empty()),
            AccessModifier.PROTECTED);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void publicMethod_namespaceIsIgnored() {
        // namespace on the MethodName is only used for private / package-private methods
        final var descriptor = withModifier(
            method("getValue", specific("java.lang.String"), classNamespace),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    // --- Return type variations ---

    @Test
    void voidTypeUsage_appearsAsJavaLangVoidInSignature() {
        // VoidTypeUsage IS a NamedTypeUsage; its canonical name is "java.lang.void".
        // The return type is therefore included in the signature — it is not omitted.
        final var descriptor = withModifier(
            method("init", VoidTypeUsage.create(codeModel), Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.void init()");
    }

    @Test
    void genericReturnType_includesTypeParameters() {
        final var listName = naming.getTypeName(Optional.empty(), "java.util.List");
        final var stringUsage = specific("java.lang.String");
        final var listOfString = GenericTypeUsage.of(codeModel, listName, (TypeUsage) stringUsage);

        final var descriptor = withModifier(
            method("items", listOfString, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.util.List<java.lang.String> items()");
    }

    @Test
    void moduleQualifiedReturnType_canonicalNameStripsModule() {
        final var descriptor = withModifier(
            method("getValue", specificWithModule("java.base", "java.lang.String"), Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void arrayReturnType_noModule_appearsInSignature() {
        // ArrayTypeUsage is NOT a NamedTypeUsage. With the old instanceof-NamedTypeUsage guard
        // the return type was silently omitted from the signature entirely. This test locks down
        // that canonicalName() is called unconditionally so array returns are never dropped.
        final var stringArray = ArrayTypeUsage.of(codeModel, Lazy.of(specific("java.lang.String")));
        final var descriptor = withModifier(
            method("getItems", stringArray, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String[] getItems()");
    }

    @Test
    void arrayReturnType_withModule_canonicalNameStripsModule() {
        // This is the critical regression case: if signature() ever reverts to toString()
        // or uses TypeName#toString() instead of TypeUsage#canonicalName(), the module
        // qualifier bleeds in and the super/sub signatures diverge, silently breaking DI
        // override detection.
        final var stringArray = ArrayTypeUsage.of(codeModel,
            Lazy.of(specificWithModule("java.base", "java.lang.String")));
        final var descriptor = withModifier(
            method("getItems", stringArray, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String[] getItems()");
    }

    @Test
    void annotationReturnType_noValues_noModule() {
        final var annotation = AnnotationTypeUsage.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.Qualifier"));
        final var descriptor = withModifier(
            method("getQualifier", annotation, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("@com.example.Qualifier() getQualifier()");
    }

    @Test
    void annotationReturnType_withModule_canonicalNameStripsModule() {
        final var annotation = AnnotationTypeUsage.of(codeModel,
            naming.getTypeName(naming.getModuleName("java.base"), "java.lang.annotation.Retention"));
        final var descriptor = withModifier(
            method("getRetention", annotation, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("@java.lang.annotation.Retention() getRetention()");
    }

    @Test
    void annotationReturnType_withValues_valuesAppearInSignature() {
        final var annotation = AnnotationTypeUsage.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.Named"),
            AnnotationValue.of(codeModel, "value", "foo"));
        final var descriptor = withModifier(
            method("getNamed", annotation, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("@com.example.Named(foo) getNamed()");
    }

    @Test
    void typeVariableReturnType_unbounded() {
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var tUsage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.empty());
        final var descriptor = withModifier(
            method("getItem", tUsage, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("T getItem()");
    }

    @Test
    void typeVariableReturnType_withBound_moduleStrippedFromBound() {
        final var tName = naming.getTypeName(Optional.empty(), "T");
        final var numberUsage = specificWithModule("java.base", "java.lang.Number");
        final var tUsage = TypeVariableUsage.of(codeModel, tName, Optional.empty(),
            Optional.of(Lazy.of(numberUsage)));
        final var descriptor = withModifier(
            method("getNumber", tUsage, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("T extends java.lang.Number getNumber()");
    }

    @Test
    void unknownTypeReturnType_appearsAsNull() {
        final var descriptor = withModifier(
            method("getUnresolved", UnknownTypeUsage.create(codeModel), Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("null getUnresolved()");
    }

    // Union and intersection types cannot appear as Java method return types, but the codemodel
    // is language-agnostic and these types can appear in other positions (catch clauses, bounds).
    // The tests below lock down canonicalName() for completeness and for non-Java language consumers.

    @Test
    void unionTypeReturnType_membersJoinedWithPipe() {
        final var union = UnionTypeUsage.of(codeModel,
            specific("java.lang.Exception"),
            specific("java.io.IOException"));
        final var descriptor = withModifier(
            method("doThrow", union, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo(" java.lang.Exception | java.io.IOException doThrow()");
    }

    @Test
    void unionTypeReturnType_withModule_canonicalNameStripsModule() {
        final var union = UnionTypeUsage.of(codeModel,
            specificWithModule("java.base", "java.lang.Exception"),
            specificWithModule("java.base", "java.io.IOException"));
        final var descriptor = withModifier(
            method("doThrow", union, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo(" java.lang.Exception | java.io.IOException doThrow()");
    }

    @Test
    void intersectionTypeReturnType_membersJoinedWithAmpersand() {
        final var intersection = IntersectionTypeUsage.of(codeModel,
            specific("java.io.Serializable"),
            specific("java.lang.Comparable"));
        final var descriptor = withModifier(
            method("get", intersection, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo(" java.io.Serializable & java.lang.Comparable get()");
    }

    @Test
    void intersectionTypeReturnType_withModule_canonicalNameStripsModule() {
        final var intersection = IntersectionTypeUsage.of(codeModel,
            specificWithModule("java.base", "java.io.Serializable"),
            specificWithModule("java.base", "java.lang.Comparable"));
        final var descriptor = withModifier(
            method("get", intersection, Optional.empty()),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo(" java.io.Serializable & java.lang.Comparable get()");
    }

    // --- Parameter variations ---

    @Test
    void publicMethod_oneNamedParam() {
        final var descriptor = withModifier(
            method("transform", specific("java.lang.String"), Optional.empty(),
                param(specific("java.lang.String"))),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String transform(java.lang.String)");
    }

    @Test
    void publicMethod_twoParams_commaSeparated() {
        final var descriptor = withModifier(
            method("set", VoidTypeUsage.create(codeModel), Optional.empty(),
                param(specific("java.lang.String")),
                param(specific("java.lang.Integer"))),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.void set(java.lang.String, java.lang.Integer)");
    }

    @Test
    void moduleQualifiedParamType_canonicalNameStripsModule() {
        final var descriptor = withModifier(
            method("set", VoidTypeUsage.create(codeModel), Optional.empty(),
                param(specificWithModule("java.base", "java.lang.String"))),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.void set(java.lang.String)");
    }

    @Test
    void nonNamedParamType_usesToString() {
        // ArrayTypeUsage is NOT a NamedTypeUsage, so the signature falls back to type.toString()
        // for the parameter — unlike SpecificTypeUsage, which uses typeName().canonicalName().
        final var stringArray = ArrayTypeUsage.of(codeModel, Lazy.of(specific("java.lang.String")));
        final var descriptor = withModifier(
            method("setItems", specific("java.lang.String"), Optional.empty(),
                param(stringArray)),
            AccessModifier.PUBLIC);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String setItems(" + stringArray + ")");
    }

    // --- Package-private (null modifier): namespace prepended with space ---

    @Test
    void packagePrivate_withNamespace_prependsNamespaceWithSpace() {
        final var descriptor = method("doWork", specific("java.lang.String"), classNamespace);
        // no addTrait call → AccessModifier stays null

        assertThat(descriptor.signature()).isEqualTo("com.example.MyService java.lang.String doWork()");
    }

    @Test
    void packagePrivate_noNamespace_noPrefix() {
        final var descriptor = method("doWork", specific("java.lang.String"), Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("java.lang.String doWork()");
    }

    @Test
    void packagePrivate_voidReturn_withNamespace() {
        final var descriptor = method("doWork", VoidTypeUsage.create(codeModel), classNamespace);

        assertThat(descriptor.signature()).isEqualTo("com.example.MyService java.lang.void doWork()");
    }

    // --- PRIVATE: namespace prepended with dot ---

    @Test
    void private_withNamespace_prependsNamespaceWithDot() {
        final var descriptor = withModifier(
            method("doSecret", specific("java.lang.String"), classNamespace),
            AccessModifier.PRIVATE);

        assertThat(descriptor.signature()).isEqualTo("com.example.MyService.java.lang.String doSecret()");
    }

    @Test
    void private_noNamespace_noPrefix() {
        final var descriptor = withModifier(
            method("doSecret", specific("java.lang.String"), Optional.empty()),
            AccessModifier.PRIVATE);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String doSecret()");
    }

    // --- DI-critical: override detection ---

    @Test
    void overridingMethod_producesIdenticalSignature_enablingInjectionPointDeduplication() {
        // InjectionFramework walks the hierarchy from leaf → root. For each method it calls
        // injectionPoints.remove(signature) then conditionally puts the current one back.
        // Override detection only works if super and sub produce the same signature string.

        final var superType = ClassTypeDescriptor.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.SuperService"));
        final var subType = ClassTypeDescriptor.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.ConcreteService"));

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

        assertThat(superMethod.signature())
            .isEqualTo(subMethod.signature())
            .isEqualTo("java.lang.void setFoo(com.example.Foo)");
    }

    @Test
    void privateOverride_differentSignatures_notDeduplicatedByDI() {
        // PRIVATE methods are not polymorphic — each class keeps its own copy.
        // InjectionFramework relies on them having distinct signatures so that
        // the subclass's private @Inject method doesn't accidentally remove the
        // superclass's private @Inject method from the injection point map.

        final var superNamespace = Namespace.of(IrreducibleName.of("com.example.SuperService"));
        final var subNamespace = Namespace.of(IrreducibleName.of("com.example.ConcreteService"));

        final var superType = ClassTypeDescriptor.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.SuperService"));
        final var subType = ClassTypeDescriptor.of(codeModel,
            naming.getTypeName(Optional.empty(), "com.example.ConcreteService"));

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

        assertThat(superMethod.signature()).isEqualTo("com.example.SuperService.java.lang.void injectInternal(com.example.Foo)");
        assertThat(subMethod.signature()).isEqualTo("com.example.ConcreteService.java.lang.void injectInternal(com.example.Foo)");
        assertThat(superMethod.signature()).isNotEqualTo(subMethod.signature());
    }
}
