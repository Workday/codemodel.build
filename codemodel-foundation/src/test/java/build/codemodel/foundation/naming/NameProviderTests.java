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
