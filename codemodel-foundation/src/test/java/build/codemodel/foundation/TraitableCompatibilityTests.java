package build.codemodel.foundation;

import build.base.mereology.Strategy;
import build.codemodel.foundation.descriptor.NonSingular;
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.Traitable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Compatibility tests of {@link Traitable}s.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
public interface TraitableCompatibilityTests {

    /**
     * Obtains the {@link Traitable} under test.
     *
     * @return the {@link Traitable}
     */
    Traitable getTraitable();

    /**
     * Ensures the empty {@link Traitable} has no {@link Trait}s.
     */
    @Test
    default void shouldNotHaveTraits() {
        final var traitable = getTraitable();

        assertThat(traitable.hasTraits())
            .isFalse();

        assertThat(traitable.traits())
            .isEmpty();

        assertThat(traitable.traverse()
            .stream())
            .isEmpty();

        assertThat(traitable.traverse(Trait.class)
            .stream())
            .isEmpty();

        assertThat(traitable.traverse()
            .strategy(Strategy.DepthFirst)
            .stream())
            .isEmpty();

        assertThat(traitable.traverse(Trait.class)
            .strategy(Strategy.BreadthFirst)
            .stream())
            .isEmpty();
    }

    /**
     * Should create, obtain and remove concrete {@link Trait}s without {@link Singular} and {@link NonSingular}
     * annotations.
     */
    @Test
    default void shouldAddCreateObtainAndRemoveTraits() {
        final var traitable = getTraitable();

        traitable.addTrait(Color.RED);

        assertThat(traitable.hasTraits())
            .isTrue();

        assertThat(traitable.traits())
            .hasSize(1);

        assertThat(traitable.getTrait(Color.class))
            .contains(Color.RED);

        assertThat(traitable.traits(Color.class))
            .hasSize(1);

        assertThat(traitable.traverse(Color.class))
            .containsExactly(Color.RED);

        assertThat(traitable.traits())
            .containsExactly(Color.RED);

        traitable.addTrait(Color.GREEN);
        traitable.addTrait(Color.BLUE);

        assertThat(traitable.traits())
            .hasSize(3);

        assertThat(traitable.traits())
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traverse())
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traverse()
            .strategy(Strategy.DepthFirst))
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traverse()
            .strategy(Strategy.BreadthFirst))
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traits(Color.class))
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traverse(Color.class))
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traverse(Color.class)
            .strategy(Strategy.DepthFirst))
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThat(traitable.traverse(Color.class)
            .strategy(Strategy.BreadthFirst))
            .contains(Color.RED, Color.GREEN, Color.BLUE);

        assertThatThrownBy(() -> traitable.getTrait(Color.class))
            .isInstanceOf(IllegalArgumentException.class);

        traitable.createTrait(_ -> Color.YELLOW);
        traitable.createTrait(_ -> Color.PURPLE);

        assertThat(traitable.traits())
            .hasSize(5);

        traitable.removeTrait(Color.PURPLE);

        assertThat(traitable.traits())
            .hasSize(4);

        assertThat(traitable.traverse())
            .hasSize(4);

        assertThat(traitable.traits(Color.class)
            .filter(color -> color == Color.PURPLE)
            .findFirst())
            .isEmpty();

        assertThat(traitable.traverse(Color.class)
            .stream()
            .filter(color -> color == Color.PURPLE)
            .findFirst())
            .isEmpty();

        traitable.removeTrait(Color.YELLOW);

        assertThat(traitable.traits())
            .hasSize(3);

        assertThat(traitable.traverse())
            .hasSize(3);

        assertThat(traitable.traits(Color.class)
            .filter(color -> color == Color.YELLOW)
            .findFirst())
            .isEmpty();
    }

    /**
     * Should create, obtain and remove concrete {@link NonSingular} {@link Trait}s.
     */
    @Test
    default void shouldAddCreateObtainAndRemoveNonSingularTraits() {
        final var traitable = getTraitable();

        final var firstDog = new Dog();
        traitable.addTrait(firstDog);

        assertThat(traitable.hasTraits())
            .isTrue();

        assertThat(traitable.getTrait(Dog.class))
            .containsSame(firstDog);

        assertThat(traitable.getTrait(Animal.class))
            .containsSame(firstDog);

        assertThat(traitable.getTrait(Cat.class))
            .isEmpty();

        final var secondDog = new Dog();
        traitable.addTrait(secondDog);

        final var firstCat = new Cat();
        traitable.addTrait(firstCat);

        final var secondCat = new Cat();
        traitable.addTrait(secondCat);

        assertThat(traitable.traits())
            .hasSize(4);

        assertThat(traitable.traits())
            .contains(firstDog, secondDog, firstCat, secondCat);

        assertThat(traitable.traits(Dog.class))
            .contains(firstDog, secondDog);

        assertThat(traitable.traverse(Dog.class))
            .contains(firstDog, secondDog);

        assertThat(traitable.traits(Cat.class))
            .contains(firstCat, secondCat);

        assertThat(traitable.traverse(Cat.class))
            .contains(firstCat, secondCat);

        assertThat(traitable.traits(Animal.class))
            .contains(firstDog, secondDog, firstCat, secondCat);

        assertThat(traitable.traverse(Animal.class))
            .contains(firstDog, secondDog, firstCat, secondCat);

        traitable.removeTrait(firstDog);
        traitable.removeTrait(firstCat);

        assertThat(traitable.traits())
            .contains(secondDog, secondCat);

        assertThat(traitable.traverse())
            .contains(secondDog, secondCat);
    }

    /**
     * Ensure a {@link Trait} can be removed from a {@link Traitable} while streaming without concurrency exceptions.
     */
    @Test
    default void shouldRemoveTraitWhileStreaming() {

        final var traitable = getTraitable();

        final var firstDog = new Dog();
        traitable.addTrait(firstDog);

        final var secondDog = new Dog();
        traitable.addTrait(secondDog);

        final var thirdDog = new Dog();
        traitable.addTrait(thirdDog);

        traitable.traits()
            .forEach(trait -> {
                if (trait == secondDog) {
                    traitable.removeTrait(secondDog);
                }
            });

        assertThat(traitable.traits())
            .containsOnly(firstDog, thirdDog);

        traitable.traverse()
            .forEach(trait -> {
                if (trait == thirdDog) {
                    traitable.removeTrait(thirdDog);
                }
            });

        assertThat(traitable.traverse())
            .containsOnly(firstDog);
    }

    /**
     * Ensure lots of non-singular {@link Trait}s can be added.
     */
    @Test
    default void shouldCreateLotsOfTraits() {
        final var traitable = getTraitable();

        final var start = System.nanoTime();

        final var count = 100000;
        for (int i = 0; i < count; i++) {
            traitable.addTrait(new Dog());
        }

        final var end = System.nanoTime();

        final var elapsed = (end - start) / 1000;

        System.out.println("Duration (us): " + elapsed);
    }

    /**
     * A simple concrete un-{@link Traitable} {@link Trait}.
     */
    enum Color
        implements Trait {

        RED,
        GREEN,
        BLUE,
        YELLOW,
        PURPLE;
    }

    @Singular
    interface Vehicle
        extends Trait {

    }

    class Car
        implements Vehicle {

    }

    class Boat
        implements Vehicle {

    }

    @NonSingular
    interface Animal
        extends Trait {

    }

    class Dog
        implements Animal {

    }

    class Cat
        implements Animal {

    }
}
