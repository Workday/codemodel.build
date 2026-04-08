package build.codemodel.jdk;

import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.jdk.example.BoundedContainer;
import build.codemodel.jdk.example.WildcardContainer;
import build.codemodel.hierarchical.descriptor.HierarchicalTypeDescriptor;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.jdk.example.AbstractPerson;
import build.codemodel.jdk.example.Container;
import build.codemodel.jdk.example.Description;
import build.codemodel.jdk.example.NonAbstractPerson;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.ImplementsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JDKCodeModel}.
 *
 * @author brian.oliver
 * @since Sep-2024
 */
class JDKCodeModelTests {

    /**
     * Creates a new {@link JDKCodeModel}.
     *
     * @return a new {@link JDKCodeModel}
     */
    protected JDKCodeModel createCodeModel() {
        final var nameProvider = new NonCachingNameProvider();
        return new JDKCodeModel(nameProvider);
    }

    /**
     * Ensure a new {@link JDKCodeModel} can be created.
     */
    @Test
    void shouldCreateCodeModel() {
        final var codeModel = createCodeModel();

        assertThat(codeModel)
            .isNotNull();

        assertThat(codeModel.typeDescriptors())
            .isNotEmpty();

        final var objectTypeDescriptor = codeModel.getJDKTypeDescriptor(Object.class)
            .orElseThrow();

        assertThat(objectTypeDescriptor.typeName().canonicalName())
            .isEqualTo("java.lang.Object");
    }

    /**
     * Ensure a {@link TypeDescriptor} can be created from an {@link AbstractPerson} using the {@link JDKCodeModel}.
     */
    @Test
    void shouldCreateTypeDescriptorForAbstractPersonClass() {
        final var codeModel = createCodeModel();

        final var typeDescriptor = codeModel.getJDKTypeDescriptor(AbstractPerson.class)
            .orElseThrow();

        assertThat(typeDescriptor.typeName().canonicalName())
            .isEqualTo(AbstractPerson.class.getCanonicalName());

        assertThat(typeDescriptor.getTrait(AccessModifier.class))
            .contains(AccessModifier.PUBLIC);

        assertThat(typeDescriptor.getTrait(Classification.class))
            .contains(Classification.ABSTRACT);

        assertThat(typeDescriptor.getTrait(ExtendsTypeDescriptor.class)
            .orElseThrow()
            .parentTypeUsage()
            .typeName()
            .canonicalName())
            .isEqualTo(Object.class.getCanonicalName());

        assertThat(typeDescriptor.traits(ImplementsTypeDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(ConstructorDescriptor.class))
            .hasSize(1);

        final var constructorDescriptor = typeDescriptor.getTrait(ConstructorDescriptor.class)
            .orElseThrow();

        assertThat(constructorDescriptor.getTrait(AccessModifier.class))
            .contains(AccessModifier.PROTECTED);

        assertThat(constructorDescriptor.callableName()
            .typeName()
            .orElseThrow())
            .isEqualTo(typeDescriptor.typeName());

        assertThat(constructorDescriptor.formalParameters())
            .hasSize(2);

        assertThat(typeDescriptor.traits(MethodDescriptor.class))
            .hasSize(5);

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .hasSize(4);
    }

    /**
     * Ensure a {@link TypeDescriptor} can be created from a {@link NonAbstractPerson} using the {@link JDKCodeModel}.
     */
    @Test
    void shouldCreateTypeDescriptorForNonPersonClass() {
        final var codeModel = createCodeModel();

        final var typeDescriptor = codeModel.getJDKTypeDescriptor(NonAbstractPerson.class)
            .orElseThrow();

        assertThat(typeDescriptor.typeName().canonicalName())
            .isEqualTo(NonAbstractPerson.class.getCanonicalName());

        assertThat(typeDescriptor.getTrait(AccessModifier.class))
            .contains(AccessModifier.PUBLIC);

        assertThat(typeDescriptor.getTrait(Classification.class))
            .contains(Classification.CONCRETE);

        assertThat(typeDescriptor.getTrait(ExtendsTypeDescriptor.class)
            .orElseThrow()
            .parentTypeUsage()
            .typeName()
            .canonicalName())
            .isEqualTo(AbstractPerson.class.getCanonicalName());

        assertThat(typeDescriptor.traits(ImplementsTypeDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(ConstructorDescriptor.class))
            .hasSize(1);

        final var constructorDescriptor = typeDescriptor.getTrait(ConstructorDescriptor.class)
            .orElseThrow();

        assertThat(constructorDescriptor.getTrait(AccessModifier.class))
            .contains(AccessModifier.PUBLIC);

        assertThat(constructorDescriptor.callableName()
            .typeName()
            .orElseThrow())
            .isEqualTo(typeDescriptor.typeName());

        assertThat(constructorDescriptor.formalParameters())
            .hasSize(2);

        final var deprecatedMethodDescriptor = typeDescriptor.traits(MethodDescriptor.class)
            .findFirst()
            .orElseThrow();

        assertThat(deprecatedMethodDescriptor.signature())
            .isEqualTo("java.lang.String fullName()");

        assertThat(deprecatedMethodDescriptor.traits(AnnotationTypeUsage.class))
            .hasSize(2);

        assertThat(deprecatedMethodDescriptor.traits(AnnotationTypeUsage.class)
            .filter(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(Deprecated.class.getCanonicalName()))
            .findFirst())
            .isPresent();

        assertThat(deprecatedMethodDescriptor.traits(AnnotationTypeUsage.class)
            .filter(annotationTypeUsage -> annotationTypeUsage
                .typeName()
                .canonicalName()
                .equals(Description.class.getCanonicalName()))
            .findFirst()
            .orElseThrow()
            .values()
            .findFirst()
            .orElseThrow()
            .as(String.class))
            .contains("Calculates the full name");

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .isEmpty();

        // ensure we can find the MethodDescriptors
        final var methodDescriptors = codeModel.getTraitsInHierarchy(typeDescriptor, MethodDescriptor.class)
            .toList();

        assertThat(methodDescriptors)
            .hasSize(6);
    }

    /**
     * Ensure a {@link TypeDescriptor} can be created from a generically declared {@link Container} using the
     * {@link JDKCodeModel}.
     */
    @Test
    void shouldCreateTypeDescriptorForContainer() {
        final var codeModel = createCodeModel();

        final var typeDescriptor = codeModel.getJDKTypeDescriptor(Container.class)
            .orElseThrow();

        assertThat(typeDescriptor.typeName().canonicalName())
            .isEqualTo(Container.class.getCanonicalName());

        assertThat(typeDescriptor.getTrait(AccessModifier.class))
            .contains(AccessModifier.PUBLIC);

        assertThat(typeDescriptor.getTrait(Classification.class))
            .contains(Classification.CONCRETE);

        assertThat(typeDescriptor.getTrait(ExtendsTypeDescriptor.class)
            .orElseThrow()
            .parentTypeUsage()
            .typeName()
            .canonicalName())
            .isEqualTo(Object.class.getCanonicalName());

        assertThat(typeDescriptor.traits(ImplementsTypeDescriptor.class))
            .isEmpty();

        assertThat(typeDescriptor.traits(ConstructorDescriptor.class))
            .hasSize(1);

        final var constructorDescriptor = typeDescriptor.getTrait(ConstructorDescriptor.class)
            .orElseThrow();

        assertThat(constructorDescriptor.getTrait(AccessModifier.class))
            .contains(AccessModifier.PUBLIC);

        assertThat(constructorDescriptor.callableName()
            .typeName()
            .orElseThrow())
            .isEqualTo(typeDescriptor.typeName());

        assertThat(constructorDescriptor.formalParameters())
            .hasSize(0);

        assertThat(typeDescriptor.traits(MethodDescriptor.class))
            .hasSize(0);

        assertThat(typeDescriptor.traits(FieldDescriptor.class))
            .hasSize(1);

        final var fieldDescriptor = typeDescriptor.getTrait(FieldDescriptor.class)
            .orElseThrow();

        assertThat(fieldDescriptor.type())
            .isInstanceOf(GenericTypeUsage.class);
    }

    /**
     * Ensure {@link HierarchicalTypeDescriptor} navigation is possible.
     */
    @Test
    void shouldNavigateHierarchicalTypeDescriptors() {
        final var codeModel = createCodeModel();

        final var typeDescriptor = codeModel.getJDKTypeDescriptor(NonAbstractPerson.class)
            .orElseThrow();

        final var parentTypeDescriptor = typeDescriptor.parent()
            .orElseThrow();

        final var objectTypeDescriptor = parentTypeDescriptor.parent()
            .orElseThrow();

        // ensure isChild is as expected
        assertThat(parentTypeDescriptor.isChild(typeDescriptor))
            .isTrue();

        assertThat(parentTypeDescriptor.isChild(objectTypeDescriptor))
            .isFalse();

        assertThat(objectTypeDescriptor.isChild(parentTypeDescriptor))
            .isTrue();

        // ensure isRootType is as expected
        assertThat(typeDescriptor.isRoot())
            .isFalse();

        assertThat(parentTypeDescriptor.isRoot())
            .isFalse();

        assertThat(objectTypeDescriptor.isRoot())
            .isTrue();

        // ensure parents(...) is as expected
        assertThat(typeDescriptor.parents())
            .containsExactly(parentTypeDescriptor);

        assertThat(typeDescriptor.parents(JDKTypeDescriptor.class))
            .containsExactly(parentTypeDescriptor);

        // ensure ancestors(...) is as expected
        assertThat(typeDescriptor.ancestors())
            .containsExactly(parentTypeDescriptor, objectTypeDescriptor);

        assertThat(typeDescriptor.ancestors(JDKTypeDescriptor.class))
            .containsExactly(parentTypeDescriptor, objectTypeDescriptor);

        // ensure children(...) is as expected
        assertThat(objectTypeDescriptor.children()
            .filter(childTypeDescriptor -> childTypeDescriptor.typeName().moduleName()
                .equals(parentTypeDescriptor.typeName().moduleName())))
            .containsExactly(parentTypeDescriptor);

        assertThat(objectTypeDescriptor.children(JDKTypeDescriptor.class)
            .filter(childTypeDescriptor -> childTypeDescriptor.typeName().moduleName()
                .equals(parentTypeDescriptor.typeName().moduleName())))
            .containsExactly(parentTypeDescriptor);

        assertThat(parentTypeDescriptor.children())
            .containsExactly(typeDescriptor);

        assertThat(parentTypeDescriptor.children(JDKTypeDescriptor.class))
            .containsExactly(typeDescriptor);

        assertThat(typeDescriptor.children())
            .isEmpty();

        // ensure descendants(...) is as expected
        assertThat(parentTypeDescriptor.descendants())
            .containsExactly(typeDescriptor);

        assertThat(parentTypeDescriptor.descendants(JDKTypeDescriptor.class))
            .containsExactly(typeDescriptor);

        assertThat(objectTypeDescriptor.descendants()
            .filter(descendantTypeDescriptor -> descendantTypeDescriptor.typeName().moduleName()
                .equals(parentTypeDescriptor.typeName().moduleName())))
            .containsExactly(parentTypeDescriptor, typeDescriptor);

        assertThat(objectTypeDescriptor.descendants(JDKTypeDescriptor.class)
            .filter(descendantTypeDescriptor -> descendantTypeDescriptor.typeName().moduleName()
                .equals(parentTypeDescriptor.typeName().moduleName())))
            .containsExactly(parentTypeDescriptor, typeDescriptor);

        assertThat(codeModel.roots())
            .hasSize(9);
    }

    /**
     * Ensure that resolving a {@link TypeDescriptor} for a class with a self-referential type variable bound
     * (e.g. {@code E extends Enum<E>}) does not cause infinite recursion.
     */
    @Test
    void shouldResolveTypeDescriptorForSelfReferentialTypeVariable() {
        final var codeModel = createCodeModel();

        // Enum<E extends Enum<E>> is the canonical self-referential type variable;
        // if the recursion guard is absent this call will throw StackOverflowError
        final var typeDescriptor = codeModel.getJDKTypeDescriptor(Enum.class);

        assertThat(typeDescriptor)
            .isPresent();

        assertThat(typeDescriptor.orElseThrow().typeName().canonicalName())
            .isEqualTo("java.lang.Enum");
    }

    @Test
    void shouldDiscoverWildcardBoundsViaReflection() {
        final var codeModel = createCodeModel();
        final var descriptor = codeModel.getJDKTypeDescriptor(WildcardContainer.class).orElseThrow();

        final var upperField = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("upper"))
            .findFirst().orElseThrow();
        final var upperParam = ((GenericTypeUsage) upperField.type()).parameters().findFirst().orElseThrow();
        assertThat(upperParam).isInstanceOf(WildcardTypeUsage.class);
        final var upperWildcard = (WildcardTypeUsage) upperParam;
        assertThat(upperWildcard.upperBound()).isPresent();
        assertThat(upperWildcard.lowerBound()).isEmpty();
        assertThat(((NamedTypeUsage) upperWildcard.upperBound().orElseThrow()).typeName().toString())
            .contains("Number");

        final var lowerField = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("lower"))
            .findFirst().orElseThrow();
        final var lowerParam = ((GenericTypeUsage) lowerField.type()).parameters().findFirst().orElseThrow();
        assertThat(lowerParam).isInstanceOf(WildcardTypeUsage.class);
        final var lowerWildcard = (WildcardTypeUsage) lowerParam;
        assertThat(lowerWildcard.lowerBound()).isPresent();
        assertThat(lowerWildcard.upperBound()).isEmpty();
        assertThat(((NamedTypeUsage) lowerWildcard.lowerBound().orElseThrow()).typeName().toString())
            .contains("Integer");

        final var unboundedField = descriptor.traits(FieldDescriptor.class)
            .filter(f -> f.fieldName().toString().equals("unbounded"))
            .findFirst().orElseThrow();
        final var unboundedParam = ((GenericTypeUsage) unboundedField.type()).parameters().findFirst().orElseThrow();
        assertThat(unboundedParam).isInstanceOf(WildcardTypeUsage.class);
        final var unboundedWildcard = (WildcardTypeUsage) unboundedParam;
        assertThat(unboundedWildcard.upperBound()).isEmpty();
        assertThat(unboundedWildcard.lowerBound()).isEmpty();
    }

    @Test
    void shouldDiscoverTypeParameterDeclarationViaReflection() {
        final var codeModel = createCodeModel();
        final var descriptor = codeModel.getJDKTypeDescriptor(Container.class).orElseThrow();

        assertThat(descriptor.getTrait(ParameterizedTypeDescriptor.class)).isPresent();

        final var typeVars = descriptor.getTrait(ParameterizedTypeDescriptor.class)
            .orElseThrow()
            .typeVariables()
            .toList();
        assertThat(typeVars).hasSize(1);
        assertThat(typeVars.getFirst().typeName().name().toString()).isEqualTo("T");
    }

    @Test
    void shouldDiscoverBoundedTypeParameterDeclarationViaReflection() {
        final var codeModel = createCodeModel();
        final var descriptor = codeModel.getJDKTypeDescriptor(BoundedContainer.class).orElseThrow();

        assertThat(descriptor.getTrait(ParameterizedTypeDescriptor.class)).isPresent();

        final var typeVar = descriptor.getTrait(ParameterizedTypeDescriptor.class)
            .orElseThrow()
            .typeVariables()
            .findFirst()
            .orElseThrow();
        assertThat(typeVar).isInstanceOf(TypeVariableUsage.class);
        assertThat(typeVar.typeName().name().toString()).isEqualTo("T");
        assertThat(typeVar.upperBound()).isPresent();
        assertThat(((NamedTypeUsage) typeVar.upperBound().get()).typeName().toString()).contains("Number");
    }
}
