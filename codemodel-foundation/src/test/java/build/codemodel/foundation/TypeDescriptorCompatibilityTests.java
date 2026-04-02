package build.codemodel.foundation;

import build.base.foundation.predicate.Predicates;
import build.base.query.Indexable;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TypeDescriptor} compatibility tests.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public interface TypeDescriptorCompatibilityTests
    extends TraitableCompatibilityTests {

    /**
     * Creates a {@link TypeDescriptor} to use for testing.
     *
     * @return the {@link TypeDescriptor}
     */
    TypeDescriptor createTypeDescriptor();

    @Override
    default Traitable getTraitable() {
        return createTypeDescriptor();
    }

    /**
     * Ensure a {@link TypeDescriptor} can be queried for an {@link Indexable} custom {@link Traitable}.
     */
    @Test
    default void shouldMatchIndexableTrait() {
        final var typeDescriptor = createTypeDescriptor();
        final var codeModel = typeDescriptor.codeModel();
        final var customTrait = new CustomTrait(typeDescriptor.codeModel(), "Hello");

        typeDescriptor.addTrait(customTrait);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Hello")
            .findFirst())
            .contains(customTrait);
    }

    /**
     * Ensure a {@link TypeDescriptor} can be queried for an {@link Indexable} custom {@link Traitable} part of a
     * {@link Traitable}.
     */
    @Test
    default void shouldMatchIndexableNestedTrait() {
        final var typeDescriptor = createTypeDescriptor();
        final var codeModel = typeDescriptor.codeModel();

        final var outerTrait = new CustomTrait(typeDescriptor.codeModel(), "Outer");
        typeDescriptor.addTrait(outerTrait);

        final var innerTrait = new CustomTrait(typeDescriptor.codeModel(), "Inner");
        outerTrait.addTrait(innerTrait);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Outer")
            .findAll())
            .hasSize(1);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Outer")
            .findFirst())
            .contains(outerTrait);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Inner")
            .findAll())
            .hasSize(1);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Inner")
            .findFirst())
            .contains(innerTrait);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .matches(Predicates.always())
            .findAll())
            .hasSize(2);

        // the following returns 0 as the CustomTrait is not @Indexable itself
        assertThat(codeModel.match(CustomTrait.class)
            .findAll())
            .hasSize(0);
    }

    /**
     * Ensure a {@link TypeDescriptor} won't be queried for an {@link Indexable} custom {@link Traitable} that has
     * been removed.
     */
    @Test
    default void shouldNotMatchRemovedIndexableTrait() {
        final var typeDescriptor = createTypeDescriptor();
        final var codeModel = typeDescriptor.codeModel();
        final var customTrait = new CustomTrait(typeDescriptor.codeModel(), "Hello");

        typeDescriptor.addTrait(customTrait);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Hello")
            .findFirst())
            .contains(customTrait);

        typeDescriptor.removeTrait(customTrait);

        assertThat(codeModel.match(CustomTrait.class)
            .where(CustomTrait.NAME)
            .isEqualTo("Hello")
            .findFirst())
            .isEmpty();
    }

    /**
     * A custom {@link Trait} for testing.
     */
    class CustomTrait
        extends AbstractTraitable
        implements Trait {

        private final String name;

        public CustomTrait(final CodeModel codeModel,
                           final String name) {
            super(codeModel);
            this.name = name;
        }

        @Indexable
        public static final Function<CustomTrait, String> NAME = customTrait -> customTrait.name;
    }

}
