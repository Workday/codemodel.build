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
 * Locks down the contract of {@link MethodDescriptor#signature()}, the human-readable Java-like rendering of a
 * method (e.g. {@code "void doWork(java.lang.String)"}).
 *
 * <p>Unlike {@link MethodDescriptor#overrideKey()} (see {@link MethodDescriptorOverrideKeyTests}),
 * {@code signature()} never includes the declaring type's namespace and ignores the {@link AccessModifier}
 * trait entirely — it exists purely for display.
 *
 * <ul>
 *   <li>format — {@code "<returnType> <name>(<paramTypes>)"}</li>
 *   <li>all return types — always present via {@link TypeUsage#canonicalName()}, module qualifier stripped,
 *       and the synthetic {@code java.lang} namespace stripped from primitives (e.g. {@code void}, not
 *       {@code java.lang.void})</li>
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

    @BeforeEach
    void setUp() {
        naming = new NonCachingNameProvider();
        codeModel = new ObjectOrientedCodeModel(naming);
        declaringType = ClassTypeDescriptor.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.MyService"));
    }

    private TypeUsage specific(final String qualifiedName) {
        return SpecificTypeUsage.of(codeModel, naming.getEmptyModuleTypeName(qualifiedName));
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

    // --- Access modifier and namespace are irrelevant to signature() ---

    @Test
    void namedReturn_noParams() {
        final var descriptor = method("getValue", specific("java.lang.String"), Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void namespaceIsIgnored() {
        // namespace on the MethodName only affects overrideKey(), never signature()
        final var classNamespace = Namespace.of(IrreducibleName.of("com.example.MyService"));
        final var descriptor = method("getValue", specific("java.lang.String"), classNamespace);

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    // --- Return type variations ---

    @Test
    void voidTypeUsage_stripsSyntheticJavaLangNamespace() {
        // VoidTypeUsage IS a NamedTypeUsage; its canonical name is "java.lang.void" internally, but
        // primitives don't actually live in java.lang, so the synthetic namespace is stripped for display.
        final var descriptor = method("init", VoidTypeUsage.create(codeModel), Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("void init()");
    }

    @Test
    void intReturnType_stripsSyntheticJavaLangNamespace() {
        // Non-void primitives are also (mis)represented internally under the synthetic java.lang
        // namespace (e.g. "java.lang.int"); confirm the stripping isn't void-specific.
        final var descriptor = method("size", specific("java.lang.int"), Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("int size()");
    }

    @Test
    void booleanParamType_stripsSyntheticJavaLangNamespace() {
        final var descriptor = method("setEnabled", VoidTypeUsage.create(codeModel), Optional.empty(),
            param(specific("java.lang.boolean")));

        assertThat(descriptor.signature()).isEqualTo("void setEnabled(boolean)");
    }

    @Test
    void genericReturnType_includesTypeParameters() {
        final var listName = naming.getEmptyModuleTypeName("java.util.List");
        final var stringUsage = specific("java.lang.String");
        final var listOfString = GenericTypeUsage.of(codeModel, listName, (TypeUsage) stringUsage);

        final var descriptor = method("items", listOfString, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("java.util.List<java.lang.String> items()");
    }

    @Test
    void moduleQualifiedReturnType_canonicalNameStripsModule() {
        final var descriptor = method("getValue", specificWithModule("java.base", "java.lang.String"), Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("java.lang.String getValue()");
    }

    @Test
    void arrayReturnType_noModule_appearsInSignature() {
        // ArrayTypeUsage is NOT a NamedTypeUsage. With the old instanceof-NamedTypeUsage guard
        // the return type was silently omitted from the signature entirely. This test locks down
        // that canonicalName() is called unconditionally so array returns are never dropped.
        final var stringArray = ArrayTypeUsage.of(codeModel, Lazy.of(specific("java.lang.String")));
        final var descriptor = method("getItems", stringArray, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("java.lang.String[] getItems()");
    }

    @Test
    void arrayReturnType_withModule_canonicalNameStripsModule() {
        // This is the critical regression case: if signature() ever reverts to toString()
        // or uses TypeName#toString() instead of TypeUsage#canonicalName(), the module
        // qualifier bleeds in and the super/sub signatures diverge, silently breaking DI
        // override detection (see MethodDescriptorOverrideKeyTests).
        final var stringArray = ArrayTypeUsage.of(codeModel,
            Lazy.of(specificWithModule("java.base", "java.lang.String")));
        final var descriptor = method("getItems", stringArray, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("java.lang.String[] getItems()");
    }

    @Test
    void annotationReturnType_noValues_noModule() {
        final var annotation = AnnotationTypeUsage.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.Qualifier"));
        final var descriptor = method("getQualifier", annotation, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("@com.example.Qualifier() getQualifier()");
    }

    @Test
    void annotationReturnType_withModule_canonicalNameStripsModule() {
        final var annotation = AnnotationTypeUsage.of(codeModel,
            naming.getTypeName(naming.getModuleName("java.base"), "java.lang.annotation.Retention"));
        final var descriptor = method("getRetention", annotation, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("@java.lang.annotation.Retention() getRetention()");
    }

    @Test
    void annotationReturnType_withValues_valuesAppearInSignature() {
        final var annotation = AnnotationTypeUsage.of(codeModel,
            naming.getEmptyModuleTypeName("com.example.Named"),
            AnnotationValue.of(codeModel, "value", "foo"));
        final var descriptor = method("getNamed", annotation, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("@com.example.Named(foo) getNamed()");
    }

    @Test
    void typeVariableReturnType_unbounded() {
        final var tName = naming.getEmptyModuleTypeName("T");
        final var tUsage = TypeVariableUsage.of(codeModel, tName, Optional.empty(), Optional.empty());
        final var descriptor = method("getItem", tUsage, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("T getItem()");
    }

    @Test
    void typeVariableReturnType_withBound_moduleStrippedFromBound() {
        final var tName = naming.getEmptyModuleTypeName("T");
        final var numberUsage = specificWithModule("java.base", "java.lang.Number");
        final var tUsage = TypeVariableUsage.of(codeModel, tName, Optional.empty(),
            Optional.of(Lazy.of(numberUsage)));
        final var descriptor = method("getNumber", tUsage, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo("T extends java.lang.Number getNumber()");
    }

    @Test
    void unknownTypeReturnType_appearsAsNull() {
        final var descriptor = method("getUnresolved", UnknownTypeUsage.create(codeModel), Optional.empty());

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
        final var descriptor = method("doThrow", union, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo(" java.lang.Exception | java.io.IOException doThrow()");
    }

    @Test
    void unionTypeReturnType_withModule_canonicalNameStripsModule() {
        final var union = UnionTypeUsage.of(codeModel,
            specificWithModule("java.base", "java.lang.Exception"),
            specificWithModule("java.base", "java.io.IOException"));
        final var descriptor = method("doThrow", union, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo(" java.lang.Exception | java.io.IOException doThrow()");
    }

    @Test
    void intersectionTypeReturnType_membersJoinedWithAmpersand() {
        final var intersection = IntersectionTypeUsage.of(codeModel,
            specific("java.io.Serializable"),
            specific("java.lang.Comparable"));
        final var descriptor = method("get", intersection, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo(" java.io.Serializable & java.lang.Comparable get()");
    }

    @Test
    void intersectionTypeReturnType_withModule_canonicalNameStripsModule() {
        final var intersection = IntersectionTypeUsage.of(codeModel,
            specificWithModule("java.base", "java.io.Serializable"),
            specificWithModule("java.base", "java.lang.Comparable"));
        final var descriptor = method("get", intersection, Optional.empty());

        assertThat(descriptor.signature()).isEqualTo(" java.io.Serializable & java.lang.Comparable get()");
    }

    // --- Parameter variations ---

    @Test
    void oneNamedParam() {
        final var descriptor = method("transform", specific("java.lang.String"), Optional.empty(),
            param(specific("java.lang.String")));

        assertThat(descriptor.signature()).isEqualTo("java.lang.String transform(java.lang.String)");
    }

    @Test
    void twoParams_commaSeparated() {
        final var descriptor = method("set", VoidTypeUsage.create(codeModel), Optional.empty(),
            param(specific("java.lang.String")),
            param(specific("java.lang.Integer")));

        assertThat(descriptor.signature()).isEqualTo("void set(java.lang.String, java.lang.Integer)");
    }

    @Test
    void moduleQualifiedParamType_canonicalNameStripsModule() {
        final var descriptor = method("set", VoidTypeUsage.create(codeModel), Optional.empty(),
            param(specificWithModule("java.base", "java.lang.String")));

        assertThat(descriptor.signature()).isEqualTo("void set(java.lang.String)");
    }

    @Test
    void nonNamedParamType_usesToString() {
        // ArrayTypeUsage is NOT a NamedTypeUsage, so the signature falls back to type.toString()
        // for the parameter — unlike SpecificTypeUsage, which uses typeName().canonicalName().
        final var stringArray = ArrayTypeUsage.of(codeModel, Lazy.of(specific("java.lang.String")));
        final var descriptor = method("setItems", specific("java.lang.String"), Optional.empty(),
            param(stringArray));

        assertThat(descriptor.signature()).isEqualTo("java.lang.String setItems(" + stringArray + ")");
    }
}
