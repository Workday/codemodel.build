package build.codemodel.foundation;

import build.base.foundation.stream.Streamable;
import build.base.marshalling.Marshalling;
import build.base.query.Indexable;
import build.base.telemetry.Location;
import build.codemodel.foundation.descriptor.AbstractTypeDescriptor;
import build.codemodel.foundation.descriptor.ModuleDescriptor;
import build.codemodel.foundation.descriptor.NamespaceDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.TypeName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CodeModel} compatibility tests.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public interface CodeModelCompatibilityTests
    extends TraitableCompatibilityTests {

    /**
     * Creates an empty {@link CodeModel} for compatibility testing.
     *
     * @return a new empty {@link CodeModel}.
     */
    CodeModel createEmptyCodeModel();

    @Override
    default Traitable getTraitable() {
        return createEmptyCodeModel();
    }

    /**
     * Ensure an empty {@link CodeModel} is created.
     */
    @Test
    default void shouldCreateEmptyCodeModel() {
        final var codeModel = createEmptyCodeModel();

        assertThat(codeModel)
            .isNotNull();

        final var nameProvider = codeModel.getNameProvider();

        assertThat(codeModel.getNameProvider())
            .isSameAs(nameProvider);

        assertThat(codeModel.hasTraits())
            .isFalse();

        assertThat(codeModel.traits())
            .isEmpty();
    }

    /**
     * Ensure a single {@link TypeDescriptor} can be created.
     */
    @Test
    default void shouldCreateSingleTypeDescriptor() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var aTypeName = nameProvider.getTypeName("A");
        final var aTypeDescriptor = codeModel.createTypeDescriptor(aTypeName);

        assertThat(aTypeDescriptor)
            .isNotNull();

        assertThat(aTypeDescriptor.codeModel())
            .isSameAs(codeModel);

        assertThat(aTypeDescriptor.typeName())
            .isEqualTo(aTypeName);

        assertThat(aTypeDescriptor.hasTraits())
            .isFalse();

        assertThat(aTypeDescriptor.traits())
            .isEmpty();

        assertThat(aTypeDescriptor.dependencies())
            .isEmpty();

        assertThat(codeModel.typeDescriptors())
            .hasSize(1);

        assertThat(codeModel.typeDescriptors()
            .findFirst()
            .orElseThrow())
            .isSameAs(aTypeDescriptor);

        assertThat(codeModel.getTypeDescriptor(aTypeName)
            .orElseThrow())
            .isSameAs(aTypeDescriptor);
    }

    /**
     * Ensure a {@link CodeModel} prevents duplicate {@link TypeDescriptor}s being created with the same
     * {@link TypeName}.
     */
    @Test
    default void shouldPreventDuplicateTypeDescriptorsWithSameTypeName() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var aTypeName = nameProvider.getTypeName("A");
        final var aTypeDescriptor = codeModel.createTypeDescriptor(aTypeName);

        // now attempt to do it again (it must return the existing TypeDescriptor)
        assertThat(codeModel
            .createTypeDescriptor(nameProvider.getTypeName("A")))
            .isSameAs(aTypeDescriptor);

        assertThat(codeModel.getTypeDescriptor(nameProvider.getTypeName("A"))
            .orElseThrow())
            .isSameAs(aTypeDescriptor);
    }

    /**
     * Ensure multiple {@link TypeDescriptor}s can be created.
     */
    @Test
    default void shouldCreateMultipleTypeDescriptors() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeNames = Streamable.of(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

        // create a bunch of TypeDescriptors
        typeNames.stream()
            .map(nameProvider::getTypeName)
            .forEach(codeModel::createTypeDescriptor);

        assertThat(codeModel.typeDescriptors())
            .hasSize((int) typeNames.count());

        typeNames.stream()
            .map(nameProvider::getTypeName)
            .forEach(typeName -> {
                assertThat(codeModel.getTypeDescriptor(typeName))
                    .isPresent();

                assertThat(codeModel.getTypeDescriptor(typeName)
                    .map(TypeDescriptor::codeModel)
                    .orElseThrow())
                    .isSameAs(codeModel);

                assertThat(codeModel.getTypeDescriptor(typeName)
                    .map(TypeDescriptor::typeName)
                    .orElseThrow())
                    .isEqualTo(typeName);
            });
    }

    /**
     * Ensure a {@link CodeModel} can be queried for a {@link TypeDescriptor} using its {@link TypeName}.
     */
    @Test
    default void shouldQueryForTypeDescriptorUsingTypeName() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("A");
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        final var optional = codeModel.match(TypeDescriptor.class)
            .where(TypeDescriptor::typeName)
            .isEqualTo(typeName)
            .findFirst();

        assertThat(optional)
            .containsSame(typeDescriptor);

        assertThat(codeModel.match(TypeDescriptor.class)
            .where(Traitable::hasTraits)
            .isEqualTo(false)
            .findFirst())
            .isPresent();
    }

    /**
     * Ensure a {@link CodeModel} can be queried for a {@link Enum} {@link Trait} using an {@link Indexable}
     * {@link Function}.
     */
    @Test
    default void shouldQueryForEnumTraitByClassAndIndexableByMethod() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeNameA = nameProvider.getTypeName("A");
        final var typeDescriptorA = codeModel.createTypeDescriptor(typeNameA);
        typeDescriptorA.addTrait(Color.RED);

        assertThat(codeModel.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("RED")
            .findFirst())
            .contains(Color.RED);

        final var typeNameB = nameProvider.getTypeName("B");
        final var typeDescriptorB = codeModel.createTypeDescriptor(typeNameB);
        typeDescriptorB.addTrait(Color.BLUE);

        assertThat(codeModel.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("BLUE")
            .findFirst())
            .contains(Color.BLUE);

        assertThat(codeModel.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("GREEN")
            .noneMatch())
            .isTrue();

        assertThat(codeModel.match(Color.class)
            .where(Color.NAME)
            .isNotEqualTo("GREEN")
            .findAll())
            .containsExactlyInAnyOrder(Color.RED, Color.BLUE);

        assertThat(codeModel.match(Color.class)
            .where(Color.NAME)
            .matches(name -> name.endsWith("E"))
            .findAll())
            .containsExactlyInAnyOrder(Color.BLUE);
    }

    /**
     * Ensure a {@link CodeModel} can be queried for a {@link Record} {@link Trait} using an {@link Indexable}
     * {@link Function}.
     */
    @Test
    default void shouldQueryForRecordTraitByClassByIndexableFunction() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeNameA = nameProvider.getTypeName("A");
        final var typeDescriptorA = codeModel.createTypeDescriptor(typeNameA);
        typeDescriptorA.addTrait(new Colorful(Color.RED));

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .isPresent();

        final var typeNameB = nameProvider.getTypeName("B");
        final var typeDescriptorB = codeModel.createTypeDescriptor(typeNameB);
        typeDescriptorB.addTrait(new Colorful(Color.BLUE));

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.BLUE)
            .findFirst())
            .isPresent();

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.GREEN)
            .noneMatch())
            .isTrue();

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isNotEqualTo(Color.GREEN)
            .findAll())
            .hasSize(2)
            .extracting(Colorful::getColor)
            .containsExactlyInAnyOrder(Color.RED, Color.BLUE);

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .matches(color -> color.name().endsWith("E"))
            .findAll())
            .hasSize(1)
            .extracting(Colorful::getColor)
            .containsExactlyInAnyOrder(Color.BLUE);
    }

    /**
     * Ensure a {@link CodeModel} can be queried for {@code null} values.
     */
    @Test
    default void shouldQueryForNullValuesInTypeDescriptors() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeNameA = nameProvider.getTypeName("A");
        final var typeDescriptorA = codeModel.createTypeDescriptor(typeNameA);
        typeDescriptorA.addTrait(new Colorful(null));

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(null)
            .findFirst())
            .isPresent();

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isNotEqualTo(null)
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure a {@link CodeModel} can be queried when a {@link Trait} has been removed.
     */
    @Test
    default void shouldNotQueryForRemovedValuesFromTraits() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeNameA = nameProvider.getTypeName("A");
        final var typeDescriptorA = codeModel.createTypeDescriptor(typeNameA);

        final var colourful = new Colorful(Color.RED);
        typeDescriptorA.addTrait(colourful);

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .isPresent();

        typeDescriptorA.removeTrait(colourful);

        assertThat(codeModel.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure a {@link CodeModel} can be queried for a {@link ModuleDescriptor} using its {@link ModuleName}.
     */
    @Test
    default void shouldQueryForModuleDescriptorUsingTypeName() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var moduleName = nameProvider.getModuleName("ModuleA")
            .orElseThrow();

        final var moduleDescriptor = codeModel.createModuleDescriptor(moduleName);

        final var optional = codeModel.match(ModuleDescriptor.class)
            .where(ModuleDescriptor::moduleName)
            .isEqualTo(moduleName)
            .findFirst();

        assertThat(optional)
            .containsSame(moduleDescriptor);

        assertThat(codeModel.match(ModuleDescriptor.class)
            .where(Traitable::hasTraits)
            .isEqualTo(false)
            .findFirst())
            .isPresent();
    }

    /**
     * Ensure a {@link CodeModel} can be queried for a {@link NamespaceDescriptor} using its {@link Namespace}.
     */
    @Test
    default void shouldQueryForNamespaceDescriptorUsingTypeName() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var namespace = nameProvider.getNamespace("java.lang")
            .orElseThrow();

        final var namespaceDescriptor = codeModel.createNamespaceDescriptor(namespace);

        final var optional = codeModel.match(NamespaceDescriptor.class)
            .where(NamespaceDescriptor::namespace)
            .isEqualTo(namespace)
            .findFirst();

        assertThat(optional)
            .containsSame(namespaceDescriptor);

        assertThat(codeModel.match(NamespaceDescriptor.class)
            .where(Traitable::hasTraits)
            .isEqualTo(false)
            .findFirst())
            .isPresent();
    }

    /**
     * Ensure a {@link CodeModel} can be queried for a {@link TypeDescriptor} using an {@link Indexable} method that
     * is based on the presence of a {@link Trait}.
     */
    @Test
    default void shouldQueryForTypeDescriptorWithIndexableMethodThatIndexesTrait() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();
        final var typeName = nameProvider.getTypeName("Colorable");

        final var typeDescriptor = codeModel.createTypeDescriptor(
            typeName,
            (_, _) -> new ColorableTypeDescriptor(codeModel, typeName));

        // initially the ColorableTypeDescriptor does not have a Color
        assertThat(codeModel.match(ColorableTypeDescriptor.class)
            .where(ColorableTypeDescriptor.HAS_COLOR)
            .isEqualTo(false)
            .findFirst())
            .isPresent();

        typeDescriptor.addTrait(Color.RED);

        // after the ColorableTypeDescriptor has a Color, the index should be updated
        assertThat(codeModel.match(ColorableTypeDescriptor.class)
            .where(ColorableTypeDescriptor.HAS_COLOR)
            .isEqualTo(true)
            .findFirst())
            .isPresent();

        typeDescriptor.removeTrait(Color.RED);

        // after the ColorableTypeDescriptor no longer has a Color, the index should be updated
        assertThat(codeModel.match(ColorableTypeDescriptor.class)
            .where(ColorableTypeDescriptor.HAS_COLOR)
            .isEqualTo(false)
            .findFirst())
            .isPresent();
    }

    /**
     * Ensure a {@link TypeDescriptor} can be created from a {@link CodeModel}.
     */
    @Test
    default void shouldCreateTypeDescriptor() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName(String.class);
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        assertThat(typeDescriptor.typeName())
            .isEqualTo(typeName);

        assertThat(typeDescriptor.traits(Location.class))
            .isEmpty();

        assertThat(typeDescriptor.traits())
            .isEmpty();
    }

    /**
     * Ensure a single {@link Trait} can be added to a {@link TypeDescriptor}.
     */
    @Test
    default void shouldAddSingleTraitForTypeDescriptor() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName(String.class);
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        final var trait = new ValueTrait<>("Hello");
        typeDescriptor.addTrait(trait);

        assertThat(typeDescriptor.traits())
            .hasSize(1);

        assertThat(typeDescriptor.getTrait(ValueTrait.class))
            .hasValue(trait);

        assertThat(typeDescriptor.traits(ValueTrait.class)
            .findFirst())
            .hasValue(trait);
    }

    /**
     * Ensure a multiple {@link Trait}s of the same type can be added to a {@link TypeDescriptor}.
     */
    @Test
    default void shouldAddMultipleTraitsOfTheSameTypeForTypeDescriptor() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName(String.class);
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        typeDescriptor.addTrait(new ValueTrait<>("Hello"));
        typeDescriptor.addTrait(new ValueTrait<>("World"));

        assertThat(typeDescriptor.traits())
            .hasSize(2);

        assertThatThrownBy(() -> typeDescriptor.getTrait(ValueTrait.class))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(typeDescriptor.traits(ValueTrait.class)
            .findFirst())
            .hasValue(new ValueTrait<>("Hello"));

        assertThat(typeDescriptor.traits(ValueTrait.class)
            .skip(1)
            .findFirst())
            .hasValue(new ValueTrait<>("World"));
    }

    @Test
    default void shouldAddTraitsOfDifferentTypesForTypeDescriptor() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName(String.class);
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        typeDescriptor.addTrait(new ValueTrait<>("Hello"));
        typeDescriptor.addTrait(new RecordTrait<>("Servus"));
        typeDescriptor.addTrait(new OtherTrait<>("World"));

        assertThat(typeDescriptor.traits())
            .hasSize(3);

        assertThat(typeDescriptor.traits(ValueTrait.class, RecordTrait.class))
            .hasSize(2);
    }

    /**
     * A {@link Trait} to hold an immutable value for testing.
     *
     * @param <T> the type of value
     */
    final class ValueTrait<T>
        implements Trait {

        /**
         * The value being held
         */
        private final T value;

        /**
         * Constructs a {@link ValueTrait}.
         *
         * @param value the value to hold
         */
        public ValueTrait(final T value) {
            this.value = Objects.requireNonNull(value, "The Value must not be null");
        }

        /**
         * Obtains the value being held.
         *
         * @return the value
         */
        public T value() {
            return this.value;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof final ValueTrait<?> that)) {
                return false;
            }
            return Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }
    }

    /**
     * A {@code record}-based {@link Trait}.
     *
     * @param value the value
     * @param <T>   the type of value
     */
    record RecordTrait<T>(T value)
        implements Trait {

    }

    /**
     * Another {@code record}-based {@link Trait}.
     *
     * @param value the value
     * @param <T>   the type of value
     */
    record OtherTrait<T>(T value)
        implements Trait {

    }

    /**
     * A simple {@link Trait} for testing.
     */
    @Indexable
    enum Color
        implements Trait {

        RED,
        GREEN,
        BLUE;

        /**
         * Defines an {@link Indexable} method to retrieve the name of the color.
         */
        @Indexable
        public static final Function<Color, String> NAME = Color::name;

        static {
            Marshalling.registerEnum(CodeModelCompatibilityTests.Color.class);
        }
    }

    /**
     * A simple {@link Trait} for testing that uses an {@link Indexable} method.
     */
    record Colorful(Color color)
        implements Trait {

        /**
         * Defines an {@link Indexable} method to retrieve the {@link Color}.
         */
        @Indexable
        public static final Function<Colorful, Color> COLOR = Colorful::getColor;

        public Color getColor() {
            return this.color;
        }
    }

    /**
     * A custom {@link TypeDescriptor} for testing {@link CodeModel} queries when {@link Trait}s are added and removed.
     */
    class ColorableTypeDescriptor
        extends AbstractTypeDescriptor {

        /**
         * Constructs a {@link ColorableTypeDescriptor}.
         *
         * @param codeModel the {@link CodeModel}
         * @param typeName   the {@link TypeName}
         */
        public ColorableTypeDescriptor(final CodeModel codeModel,
                                       final TypeName typeName) {
            super(codeModel, typeName);
        }

        /**
         * Determines if the {@link ColorableTypeDescriptor} has a {@link Color} {@link Trait}.
         *
         * @return {@code true} if the {@link ColorableTypeDescriptor} has a {@link Color} {@link Trait}, otherwise {@code false}
         */
        public boolean hasColor() {
            return this.traits(Color.class)
                .findFirst()
                .isPresent();
        }

        /**
         * An {@link Indexable} method to determine if the {@link ColorableTypeDescriptor} has a {@link Color}
         * {@link Trait}.
         */
        @Indexable
        public static final Function<ColorableTypeDescriptor, Boolean> HAS_COLOR = ColorableTypeDescriptor::hasColor;
    }
}
