package build.codemodel.foundation.naming;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NameProvider}s.
 *
 * @author brian.oliver
 * @since Jan-2024
 */
interface NameProviderTests {

    /**
     * Obtains the {@link NameProvider} under test.
     *
     * @return the {@link NameProvider}
     */
    NameProvider getNameProvider();

    /**
     * Ensure an empty {@link IrreducibleName} can be constructed from an empty {@link String}.
     */
    @Test
    default void shouldCreateEmptyIrreducibleNameFromEmptyString() {
        final var nameProvider = getNameProvider();
        final var name = nameProvider.getIrreducibleName("");

        assertThat(name.isEmpty())
            .isTrue();
    }

    /**
     * Ensure an empty {@link IrreducibleName} can be constructed from a {@code null}.
     */
    @Test
    default void shouldCreateEmptyIrreducibleNameFromNull() {
        final var nameProvider = getNameProvider();
        final var name = nameProvider.getIrreducibleName(null);

        assertThat(name.isEmpty())
            .isTrue();
    }

    /**
     * Ensure an {@link IrreducibleName} can be created when it contains a {@code $}.
     */
    @Test
    default void shouldCreateIrreducibleNameContainingDollarSeparator() {
        final var nameProvider = getNameProvider();

        assertThat(nameProvider.getIrreducibleName("A$B").toString())
            .isEqualTo("A$B");
    }

    /**
     * Ensure an {@link IrreducibleName} can be created when it contains a {@code _}.
     */
    @Test
    default void shouldCreateIrreducibleNameContainingUnderscore() {
        final var nameProvider = getNameProvider();

        assertThat(nameProvider.getIrreducibleName("A_B").toString())
            .isEqualTo("A_B");
    }

    /**
     * Ensure a {@link Namespace} can't be created using an empty {@link String}.
     */
    @Test
    default void shouldNotCreateNamespaceFromEmptyString() {
        final var nameProvider = getNameProvider();

        assertThat(nameProvider.getNamespace(""))
            .isEmpty();
    }

    /**
     * Ensure a {@link Namespace} can't be created using a {@code null}.
     */
    @Test
    default void shouldNotCreateNamespaceFromNullString() {
        final var nameProvider = getNameProvider();

        assertThat(nameProvider.getNamespace(null))
            .isEmpty();
    }

    /**
     * Ensure a {@link Namespace} can be created from a single name (root package).
     */
    @Test
    default void shouldCreateNamespaceForRootPackage() {
        final var nameProvider = getNameProvider();

        final var name = nameProvider.getNamespace("codemodel")
            .orElseThrow();

        assertThat(name.isEmpty())
            .isFalse();

        assertThat(name.parts())
            .hasSize(1);

        assertThat(name.parts().findFirst())
            .isPresent()
            .map(Object::toString)
            .contains("codemodel");
    }

    /**
     * Ensure a {@link ModuleName} can be created from dot-separated package name.
     */
    @Test
    default void shouldCreateModuleNameForFullyQualifiedSubPackage() {
        final var nameProvider = getNameProvider();

        final var name = nameProvider.getModuleName("build.codemodel")
            .orElseThrow();

        assertThat(name.isEmpty())
            .isFalse();

        assertThat(name.parts())
            .hasSize(2);

        assertThat(name.parts().findFirst())
            .isPresent()
            .map(Object::toString)
            .contains("build");

        assertThat(name.parts().skip(1).findFirst())
            .isPresent()
            .map(Object::toString)
            .contains("codemodel");
    }

    /**
     * Ensure a {@link ModuleName} can't be created using an empty {@link String}.
     */
    @Test
    default void shouldNotCreateModuleNameFromEmptyString() {
        final var nameProvider = getNameProvider();

        assertThat(nameProvider.getModuleName(""))
            .isEmpty();
    }

    /**
     * Ensure a {@link ModuleName} can't be created using a {@code null}.
     */
    @Test
    default void shouldNotCreateModuleNameFromNullString() {
        final var nameProvider = getNameProvider();

        assertThat(nameProvider.getModuleName(null))
            .isEmpty();
    }

    /**
     * Ensure a {@link ModuleName} can be created from a single name (root package).
     */
    @Test
    default void shouldCreateModuleNameForSimpleModule() {
        final var nameProvider = getNameProvider();

        final var name = nameProvider.getModuleName("codemodel")
            .orElseThrow();

        assertThat(name.isEmpty())
            .isFalse();

        assertThat(name.parts())
            .hasSize(1);

        assertThat(name.parts().findFirst())
            .isPresent()
            .map(Object::toString)
            .contains("codemodel");
    }

    /**
     * Ensure a {@link ModuleName} can be created from dot-separated package name.
     */
    @Test
    default void shouldCreateModuleNameForFullyQualifiedSubModule() {
        final var nameProvider = getNameProvider();

        final var name = nameProvider.getModuleName("build.codemodel")
            .orElseThrow();

        assertThat(name.isEmpty())
            .isFalse();

        assertThat(name.parts())
            .hasSize(2);

        assertThat(name.parts().findFirst())
            .isPresent()
            .map(Object::toString)
            .contains("build");

        assertThat(name.parts().skip(1).findFirst())
            .isPresent()
            .map(Object::toString)
            .contains("codemodel");
    }

    /**
     * Ensure a {@link TypeName} can be created from an {@link IrreducibleName}.
     */
    @Test
    default void shouldCreateTypeNameForIrreducibleName() {
        final var nameProvider = getNameProvider();

        final var name = nameProvider.getIrreducibleName("Integer");
        final var typeName = nameProvider.getTypeName(Optional.empty(), name);

        assertThat(typeName.moduleName())
            .isEmpty();

        assertThat(typeName.namespace())
            .isEmpty();

        assertThat(typeName.enclosingTypeName())
            .isEmpty();

        assertThat(name)
            .isEqualTo(typeName.name());
    }

    /**
     * Ensure a {@link TypeName} can be created from an {@link IrreducibleName}.
     */
    @Test
    default void shouldCompareDifferentTypeNamesAsNotEquals() {
        final var nameProvider = getNameProvider();

        final var integer = nameProvider.getTypeName(
            Optional.empty(),
            nameProvider.getIrreducibleName("Integer"));

        final var bigdecimal = nameProvider.getTypeName(
            Optional.empty(),
            nameProvider.getIrreducibleName("BigDecimal"));

        assertThat(integer)
            .isNotEqualTo(bigdecimal);
    }

    /**
     * Ensure a {@link TypeName} can be created from a {@link Class}.
     */
    @Test
    default void shouldCreateTypeNameFromClass() {
        final var nameProvider = getNameProvider();
        final var typeName = nameProvider.getTypeName(Optional.class);

        assertThat(typeName.name().toString())
            .isEqualTo("Optional");

        assertThat(typeName.namespace())
            .isPresent()
            .map(Object::toString)
            .contains("java.util");

        assertThat(typeName.moduleName())
            .isPresent()
            .map(Object::toString)
            .contains("java.base");

        assertThat(typeName.enclosingTypeName()).isEmpty();
    }

    /**
     * Ensure a {@link TypeName} can be created from a primitive type.
     */
    @Test
    default void shouldCreateTypeNameFromPrimitive() {
        final var nameProvider = getNameProvider();
        final var typeName = nameProvider.getTypeName(int.class);

        assertThat(typeName.name().toString())
            .isEqualTo("int");

        assertThat(typeName.namespace())
            .isPresent()
            .map(Object::toString)
            .contains("java.lang");

        assertThat(typeName.moduleName())
            .isPresent()
            .map(Object::toString)
            .contains("java.base");

        assertThat(typeName.enclosingTypeName()).isEmpty();
    }

    @Test
    default void getTypeNameFromFqnShouldParseNamespaceAndSimpleName() {
        final var nameProvider = getNameProvider();
        final var typeName = nameProvider.getEmptyModuleTypeName("com.example.Foo");

        assertThat(typeName.namespace()).isPresent().map(Object::toString).contains("com.example");
        assertThat(typeName.name().toString()).isEqualTo("Foo");
        assertThat(typeName.enclosingTypeName()).isEmpty();
        assertThat(typeName.moduleName()).isEmpty();
    }

    @Test
    default void getTypeNameFromFqnWithDollarShouldParseEnclosingType() {
        final var nameProvider = getNameProvider();
        final var typeName = nameProvider.getTypeNameFromBinary(Optional.empty(), "com.example.Outer$Inner");

        assertThat(typeName.name().toString()).isEqualTo("Inner");
        assertThat(typeName.namespace()).isPresent().map(Object::toString).contains("com.example");
        assertThat(typeName.enclosingTypeName())
            .isPresent()
            .hasValueSatisfying(enclosing -> {
                assertThat(enclosing.name().toString()).isEqualTo("Outer");
                assertThat(enclosing.namespace()).isPresent().map(Object::toString).contains("com.example");
            });
        assertThat(typeName.moduleName()).isEmpty();
    }

    @Test
    default void getTypeNameFromFqnWithDollarShouldCarryModule() {
        final var nameProvider = getNameProvider();
        final var module = nameProvider.getModuleName("com.example").orElseThrow();
        final var typeName = nameProvider.getTypeNameFromBinary(Optional.of(module), "com.example.Outer$Inner");

        assertThat(typeName.moduleName()).isPresent().map(Object::toString).contains("com.example");
        assertThat(typeName.enclosingTypeName())
            .isPresent()
            .hasValueSatisfying(enclosing ->
                assertThat(enclosing.moduleName()).isPresent().map(Object::toString).contains("com.example"));
    }

    @Test
    default void getTypeNameFromFqnWithDeeplyNestedDollarShouldResolveFullChain() {
        final var nameProvider = getNameProvider();
        final var typeName = nameProvider.getTypeNameFromBinary(Optional.empty(), "com.example.Outer$Middle$Inner");

        assertThat(typeName.name().toString()).isEqualTo("Inner");
        assertThat(typeName.enclosingTypeName())
            .isPresent()
            .hasValueSatisfying(middle -> {
                assertThat(middle.name().toString()).isEqualTo("Middle");
                assertThat(middle.enclosingTypeName())
                    .isPresent()
                    .hasValueSatisfying(outer -> assertThat(outer.name().toString()).isEqualTo("Outer"));
            });
    }

    @Test
    default void getTypeNameFromFqnWithNoPackageShouldHaveEmptyNamespace() {
        final var nameProvider = getNameProvider();
        final var typeName = nameProvider.getEmptyModuleTypeName("Foo");

        assertThat(typeName.name().toString()).isEqualTo("Foo");
        assertThat(typeName.namespace()).isEmpty();
        assertThat(typeName.enclosingTypeName()).isEmpty();
    }

    @Test
    default void shouldCreateDistinctTypeNamesForDistinctAnonymousClasses() {
        final var nameProvider = getNameProvider();

        final Runnable first = new Runnable() {
            @Override
            public void run() {
            }
        };
        final Runnable second = new Runnable() {
            @Override
            public void run() {
            }
        };

        final var firstTypeName = nameProvider.getTypeName(first.getClass());
        final var secondTypeName = nameProvider.getTypeName(second.getClass());

        assertThat(firstTypeName).isNotEqualTo(secondTypeName);
        assertThat(firstTypeName.name().isEmpty()).isFalse();
        assertThat(secondTypeName.name().isEmpty()).isFalse();
    }

    @Test
    default void shouldRetainEnclosingTypeNameForAnonymousClass() {
        final var nameProvider = getNameProvider();

        final Runnable anonymous = new Runnable() {
            @Override
            public void run() {
            }
        };

        final var typeName = nameProvider.getTypeName(anonymous.getClass());
        assertThat(typeName.enclosingTypeName())
            .isPresent()
            .hasValueSatisfying(enclosing ->
                assertThat(enclosing.name().toString()).isEqualTo("NameProviderTests"));
    }

    @Test
    default void shouldCreateDistinctTypeNamesForNestedAnonymousClasses() {
        final var nameProvider = getNameProvider();

        final Class<?>[] innerClasses = new Class<?>[1];
        final Runnable outer = new Runnable() {
            @Override
            public void run() {
                final Runnable inner = new Runnable() {
                    @Override
                    public void run() {
                    }
                };
                innerClasses[0] = inner.getClass();
            }
        };
        outer.run();

        final var outerTypeName = nameProvider.getTypeName(outer.getClass());
        final var innerTypeName = nameProvider.getTypeName(innerClasses[0]);

        assertThat(outerTypeName).isNotEqualTo(innerTypeName);
        assertThat(innerTypeName.enclosingTypeName())
            .isPresent()
            .hasValueSatisfying(enclosing -> assertThat(enclosing).isEqualTo(outerTypeName));
    }

    /**
     * Declares a local class named {@code Foo} and returns its {@link Class}; used to check
     * that two identically-named local classes declared in different methods of the same
     * enclosing type are still given distinct {@link TypeName}s.
     */
    private Class<?> declareFirstLocalFoo() {
        class Foo {
        }
        return Foo.class;
    }

    /**
     * @see #declareFirstLocalFoo()
     */
    private Class<?> declareSecondLocalFoo() {
        class Foo {
        }
        return Foo.class;
    }

    @Test
    default void shouldCreateDistinctTypeNamesForSameNamedLambdaClassesFromDifferentSites() {
        final var nameProvider = getNameProvider();

        final Runnable first = () -> {
        };
        final Runnable second = () -> {
        };

        final var firstTypeName = nameProvider.getTypeName(first.getClass());
        final var secondTypeName = nameProvider.getTypeName(second.getClass());

        assertThat(firstTypeName).isNotEqualTo(secondTypeName);
        assertThat(firstTypeName.name().isEmpty()).isFalse();
        assertThat(secondTypeName.name().isEmpty()).isFalse();
    }

    @Test
    default void shouldRoundTripEncodedTypeNameForLambdaClass() {
        final var nameProvider = getNameProvider();

        final Runnable lambda = () -> {
        };

        final var typeName = nameProvider.getTypeName(lambda.getClass());
        final var roundTripped = nameProvider.getTypeName(typeName.toString());

        assertThat(roundTripped).isEqualTo(typeName);
    }

    @Test
    default void shouldCreateDistinctTypeNamesForSameNamedLocalClassesInDifferentMethods() {
        final var nameProvider = getNameProvider();

        final var firstTypeName = nameProvider.getTypeName(declareFirstLocalFoo());
        final var secondTypeName = nameProvider.getTypeName(declareSecondLocalFoo());

        assertThat(firstTypeName).isNotEqualTo(secondTypeName);
    }

    @Test
    default void shouldProduceAValidJavaIdentifierForAnonymousClass() {
        final var nameProvider = getNameProvider();

        final Runnable anonymous = new Runnable() {
            @Override
            public void run() {
            }
        };

        final var typeName = nameProvider.getTypeName(anonymous.getClass());

        assertThat(isValidJavaIdentifier(typeName.name().toString())).isTrue();
    }

    @Test
    default void shouldProduceAValidJavaIdentifierForLocalClass() {
        final var nameProvider = getNameProvider();

        final var typeName = nameProvider.getTypeName(declareFirstLocalFoo());

        assertThat(isValidJavaIdentifier(typeName.name().toString())).isTrue();
    }

    /**
     * Determines whether the given {@link String} is a valid Java identifier (ignoring
     * reserved keywords, which compiler-generated names never collide with).
     */
    private static boolean isValidJavaIdentifier(final String string) {
        if (string.isEmpty() || !Character.isJavaIdentifierStart(string.charAt(0))) {
            return false;
        }

        return string.chars().allMatch(Character::isJavaIdentifierPart);
    }

    @Test
    default void shouldRoundTripEncodedTypeNameForAnonymousClass() {
        final var nameProvider = getNameProvider();

        final Runnable anonymous = new Runnable() {
            @Override
            public void run() {
            }
        };

        final var typeName = nameProvider.getTypeName(anonymous.getClass());
        final var roundTripped = nameProvider.getTypeName(typeName.toString());

        assertThat(roundTripped).isEqualTo(typeName);
    }

    @Test
    default void shouldRoundTripEncodedTypeNameForLocalClass() {
        final var nameProvider = getNameProvider();

        final var typeName = nameProvider.getTypeName(declareFirstLocalFoo());
        final var roundTripped = nameProvider.getTypeName(typeName.toString());

        assertThat(roundTripped).isEqualTo(typeName);
    }

    /**
     * Ensure an empty {@link TypeName} can be created.
     */
    @Test
    default void shouldCreateAnEmptyTypeName() {
        final var empty = TypeName.empty();

        assertThat(empty.isEmpty())
            .isTrue();

        assertThat(empty.namespace())
            .isEmpty();

        assertThat(empty.moduleName())
            .isEmpty();

        assertThat(empty.enclosingTypeName())
            .isEmpty();

        assertThat(empty.name().isEmpty())
            .isTrue();
    }
}
