package build.codemodel.foundation;

import build.base.foundation.stream.Streamable;
import build.base.marshalling.Marshalling;
import build.base.mereology.Strategy;
import build.base.query.Indexable;
import build.codemodel.foundation.descriptor.AbstractTraitable;
import build.codemodel.foundation.descriptor.PolymorphicModuleDescriptor;
import build.codemodel.foundation.descriptor.PolymorphicNamespaceDescriptor;
import build.codemodel.foundation.descriptor.PolymorphicTypeDescriptor;
import build.codemodel.foundation.descriptor.Singular;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CodeModelCompatibilityTests} for {@link ConceptualCodeModel}s.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
class ConceptualCodeModelTests
    implements CodeModelCompatibilityTests {

    @Override
    public CodeModel createEmptyCodeModel() {
        final var nameProvider = new NonCachingNameProvider();
        return new ConceptualCodeModel(nameProvider);
    }

    @Test
    void shouldCreateNewTypeName() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("Car");

        assertThat(typeName.toString())
            .isEqualTo("Car");
    }

    @Test
    void shouldCreateNewTypeDescriptor() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("Car");
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        assertThat(typeDescriptor.typeName())
            .isEqualTo(typeName);

        assertThat(typeDescriptor.codeModel())
            .isSameAs(codeModel);

        assertThat(typeDescriptor.traits())
            .isEmpty();
    }

    @Test
    void shouldCreateNewTypeDescriptorWithATrait() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("Car");
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        typeDescriptor.addTrait(Color.RED);

        assertThat(typeDescriptor.traits())
            .hasSize(1);

        assertThat(typeDescriptor.traits(Color.class))
            .hasSize(1);

        assertThat(typeDescriptor.getTrait(Color.class))
            .contains(Color.RED);
    }

    @Test
    void shouldCreateNewTypeDescriptorWithMultipleTraits() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("Car");
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        typeDescriptor.addTrait(Color.RED);
        typeDescriptor.addTrait(Color.BLUE);

        assertThat(typeDescriptor.traits())
            .hasSize(2);

        assertThat(typeDescriptor.traits(Color.class))
            .hasSize(2);

        assertThat(typeDescriptor.traits(Color.class))
            .contains(Color.RED, Color.BLUE);
    }

    @Test
    void shouldCreateNewTypeDescriptorWithMultipleTraitables() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("Car");
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        typeDescriptor.addTrait(Speed.FAST);

        final var wheel1 = typeDescriptor.createTrait(Wheel::new);
        final var wheel2 = typeDescriptor.createTrait(Wheel::new);
        final var wheel3 = typeDescriptor.createTrait(Wheel::new);
        final var wheel4 = typeDescriptor.createTrait(Wheel::new);

        assertThat(typeDescriptor.traits())
            .hasSize(5);

        assertThat(typeDescriptor.traits(Wheel.class))
            .hasSize(4);

        assertThat(typeDescriptor.traits(Wheel.class))
            .contains(wheel1, wheel2, wheel3, wheel4);
    }

    @Test
    void shouldCreateNewTypeDescriptorWithMultipleTraitablesAndTraits() {
        final var codeModel = createEmptyCodeModel();
        final var nameProvider = codeModel.getNameProvider();

        final var typeName = nameProvider.getTypeName("Car");
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName);

        typeDescriptor.addTrait(Color.RED);
        typeDescriptor.addTrait(Speed.FAST);

        final var wheel1 = typeDescriptor.createTrait(Wheel::new);
        wheel1.addTrait(Color.BLUE);

        final var wheel2 = typeDescriptor.createTrait(Wheel::new);
        wheel2.addTrait(Color.GREEN);

        final var wheel3 = typeDescriptor.createTrait(Wheel::new);
        wheel3.addTrait(Color.RED);

        assertThat(typeDescriptor.traits())
            .hasSize(5);

        assertThat(typeDescriptor.traits(Color.class))
            .hasSize(1);

        assertThat(typeDescriptor.traits(Speed.class))
            .hasSize(1);

        assertThat(typeDescriptor.traits(Wheel.class))
            .hasSize(3);

        assertThat(typeDescriptor.traverse(Color.class)
            .strategy(Strategy.DepthFirst))
            .hasSize(4);

        assertThat(codeModel.match(Wheel.class)
            .where(Wheel.COLOR)
            .isEqualTo(Color.RED)
            .findAll())
            .hasSize(1);
    }

    @Test
    void shouldCreateTypeDescriptorWithTraitSuppliers() {
        final var codeModel = createEmptyCodeModel();
        final var typeName = codeModel.getNameProvider().getTypeName("Car");

        final var typeDescriptor = codeModel.createTypeDescriptor(
            typeName,
            PolymorphicTypeDescriptor::of,
            Streamable.of(_ -> Color.RED, _ -> Speed.FAST));

        assertThat(typeDescriptor.getTrait(Color.class))
            .contains(Color.RED);

        assertThat(typeDescriptor.getTrait(Speed.class))
            .contains(Speed.FAST);
    }

    @Test
    void shouldCreateTypeDescriptorOnceWithTraitSuppliers() {
        final var codeModel = createEmptyCodeModel();
        final var typeName = codeModel.getNameProvider().getTypeName("Car");
        final var counter = new AtomicInteger();

        codeModel.createTypeDescriptor(
            typeName,
            PolymorphicTypeDescriptor::of,
            Streamable.of(_ -> { counter.incrementAndGet(); return Color.RED; }));

        codeModel.createTypeDescriptor(
            typeName,
            PolymorphicTypeDescriptor::of,
            Streamable.of(_ -> { counter.incrementAndGet(); return Color.RED; }));

        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void shouldCreateModuleDescriptorWithTraitSuppliers() {
        final var codeModel = createEmptyCodeModel();
        final var moduleName = codeModel.getNameProvider().getModuleName("ModuleA").orElseThrow();

        final var moduleDescriptor = codeModel.createModuleDescriptor(
            moduleName,
            PolymorphicModuleDescriptor::of,
            Streamable.of(_ -> Color.RED, _ -> Speed.FAST));

        assertThat(moduleDescriptor.getTrait(Color.class))
            .contains(Color.RED);

        assertThat(moduleDescriptor.getTrait(Speed.class))
            .contains(Speed.FAST);
    }

    @Test
    void shouldCreateModuleDescriptorOnceWithTraitSuppliers() {
        final var codeModel = createEmptyCodeModel();
        final var moduleName = codeModel.getNameProvider().getModuleName("ModuleA").orElseThrow();
        final var counter = new AtomicInteger();

        codeModel.createModuleDescriptor(
            moduleName,
            PolymorphicModuleDescriptor::of,
            Streamable.of(_ -> { counter.incrementAndGet(); return Color.RED; }));

        codeModel.createModuleDescriptor(
            moduleName,
            PolymorphicModuleDescriptor::of,
            Streamable.of(_ -> { counter.incrementAndGet(); return Color.RED; }));

        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void shouldCreateNamespaceDescriptorWithTraitSuppliers() {
        final var codeModel = createEmptyCodeModel();
        final var namespace = codeModel.getNameProvider().getNamespace("java.lang").orElseThrow();

        final var namespaceDescriptor = codeModel.createNamespaceDescriptor(
            namespace,
            PolymorphicNamespaceDescriptor::of,
            Streamable.of(_ -> Color.RED, _ -> Speed.FAST));

        assertThat(namespaceDescriptor.getTrait(Color.class))
            .contains(Color.RED);

        assertThat(namespaceDescriptor.getTrait(Speed.class))
            .contains(Speed.FAST);
    }

    @Test
    void shouldCreateNamespaceDescriptorOnceWithTraitSuppliers() {
        final var codeModel = createEmptyCodeModel();
        final var namespace = codeModel.getNameProvider().getNamespace("java.lang").orElseThrow();
        final var counter = new AtomicInteger();

        codeModel.createNamespaceDescriptor(
            namespace,
            PolymorphicNamespaceDescriptor::of,
            Streamable.of(_ -> { counter.incrementAndGet(); return Color.RED; }));

        codeModel.createNamespaceDescriptor(
            namespace,
            PolymorphicNamespaceDescriptor::of,
            Streamable.of(_ -> { counter.incrementAndGet(); return Color.RED; }));

        assertThat(counter.get()).isEqualTo(1);
    }

    public enum Color
        implements Trait {
        RED,
        GREEN,
        BLUE;

        static {
            Marshalling.registerEnum(ConceptualCodeModelTests.Color.class);
        }
    }

    @Singular
    public enum Speed
        implements Trait {

        FAST,
        SLOW,
        MEDIUM;

        static {
            Marshalling.registerEnum(ConceptualCodeModelTests.Speed.class);
        }
    }

    public static class Wheel
        extends AbstractTraitable
        implements Trait {

        public Wheel(final TypeDescriptor typeDescriptor) {
            super(typeDescriptor);
        }

        public Color color() {
            return getTrait(Color.class)
                .orElse(null);
        }

        @Indexable
        public static final Function<Wheel, Color> COLOR = Wheel::color;
    }
}
