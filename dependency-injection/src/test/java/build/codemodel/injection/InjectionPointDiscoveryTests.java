package build.codemodel.injection;

import build.codemodel.injection.example.AbstractPerson;
import build.codemodel.injection.example.ConstructorInjectablePerson;
import build.codemodel.injection.example.FieldInjectablePerson;
import build.codemodel.injection.example.FieldInjectablePersonWithDefaultConstructor;
import build.codemodel.injection.example.FieldInjectablePersonWithInjectAnnotatedDefaultConstructor;
import build.codemodel.injection.example.MultipleConstructorPerson;
import build.codemodel.injection.example.NonAbstractPerson;
import build.codemodel.injection.example.SetterInjectablePerson;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Tests for discovering {@link InjectionPoint}s using the {@link InjectionFramework}.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
class InjectionPointDiscoveryTests
    implements ContextualTesting {

    /**
     * Ensure {@link InjectionPoint}s for the {@link AbstractPerson} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForAbstractPerson() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(AbstractPerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(2);

        assertThat(injectionPoints.get(0))
            .isInstanceOf(FieldInjectionPoint.class);

        assertThat(injectionPoints.get(1))
            .isInstanceOf(MethodInjectionPoint.class);
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link ConstructorInjectablePerson} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForConstructorInjectablePerson() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(ConstructorInjectablePerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(1);

        assertThat(injectionPoints.getFirst())
            .isInstanceOf(ConstructorInjectionPoint.class);
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link FieldInjectablePerson} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForFieldInjectablePerson() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(FieldInjectablePerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(4);

        assertThat(injectionPoints.stream())
            .allMatch(FieldInjectionPoint.class::isInstance);

        assertThat(injectionPoints.stream()
            .filter(FieldInjectionPoint.class::isInstance)
            .map(FieldInjectionPoint.class::cast)
            .flatMap(FieldInjectionPoint::dependencies)
            .map(Dependency::typeUsage)
            .flatMap(typeUsage -> typeUsage.traits(AnnotationTypeUsage.class))
            .filter(annotationTypeUsage -> annotationTypeUsage.typeName()
                .canonicalName().equals(Named.class.getCanonicalName()))
            .flatMap(AnnotationTypeUsage::values)
            .map(AnnotationValue::value))
            .containsOnly("FirstName", "LastName", "Age");
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link FieldInjectablePersonWithDefaultConstructor} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForFieldInjectablePersonWithDefaultConstructor() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(FieldInjectablePersonWithDefaultConstructor.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(4);

        assertThat(injectionPoints.stream())
            .allMatch(FieldInjectionPoint.class::isInstance);

        assertThat(injectionPoints.stream()
            .filter(FieldInjectionPoint.class::isInstance)
            .map(FieldInjectionPoint.class::cast)
            .flatMap(FieldInjectionPoint::dependencies)
            .map(Dependency::typeUsage)
            .flatMap(typeUsage -> typeUsage.traits(AnnotationTypeUsage.class))
            .filter(annotationTypeUsage -> annotationTypeUsage.typeName().
                canonicalName().equals(Named.class.getCanonicalName()))
            .flatMap(AnnotationTypeUsage::values)
            .map(AnnotationValue::value))
            .containsOnly("FirstName", "LastName", "Age");
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link FieldInjectablePersonWithInjectAnnotatedDefaultConstructor} can be
     * discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForFieldInjectablePersonWithInjectAnnotatedDefaultConstructor() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(FieldInjectablePersonWithInjectAnnotatedDefaultConstructor.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(5);

        assertThat(injectionPoints.getLast())
            .isInstanceOf(ConstructorInjectionPoint.class);

        assertThat(injectionPoints.stream()
            .takeWhile(FieldInjectionPoint.class::isInstance)
            .filter(FieldInjectionPoint.class::isInstance)
            .map(FieldInjectionPoint.class::cast)
            .flatMap(FieldInjectionPoint::dependencies)
            .map(Dependency::typeUsage)
            .flatMap(typeUsage -> typeUsage.traits(AnnotationTypeUsage.class))
            .filter(annotationTypeUsage -> annotationTypeUsage.typeName()
                .canonicalName().equals(Named.class.getCanonicalName()))
            .flatMap(AnnotationTypeUsage::values)
            .map(AnnotationValue::value))
            .containsOnly("FirstName", "LastName", "Age");
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link MultipleConstructorPerson} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForMultipleConstructorPerson() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(MultipleConstructorPerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(4);

        assertThat(injectionPoints.stream())
            .allMatch(FieldInjectionPoint.class::isInstance);

        assertThat(injectionPoints.stream()
            .filter(FieldInjectionPoint.class::isInstance)
            .map(FieldInjectionPoint.class::cast)
            .flatMap(FieldInjectionPoint::dependencies)
            .map(Dependency::typeUsage)
            .flatMap(typeUsage -> typeUsage.traits(AnnotationTypeUsage.class))
            .filter(annotationTypeUsage -> annotationTypeUsage.typeName().
                canonicalName().equals(Named.class.getCanonicalName()))
            .flatMap(AnnotationTypeUsage::values)
            .map(AnnotationValue::value))
            .containsOnly("FirstName", "LastName", "Age");
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link NonAbstractPerson} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForNonAbstractPerson() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(NonAbstractPerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(3);

        assertThat(injectionPoints.get(0))
            .isInstanceOf(FieldInjectionPoint.class);

        assertThat(injectionPoints.get(1))
            .isInstanceOf(MethodInjectionPoint.class);

        assertThat(injectionPoints.get(2))
            .isInstanceOf(ConstructorInjectionPoint.class);

        final var constructorInjectionPoint = (ConstructorInjectionPoint) injectionPoints.get(2);

        assertThat(constructorInjectionPoint.dependencies())
            .hasSize(2);
    }

    /**
     * Ensure {@link InjectionPoint}s for the {@link SetterInjectablePerson} can be discovered.
     */
    @Test
    void shouldCreateDiscoverInjectionPointsForSetterInjectablePerson() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(SetterInjectablePerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints)
            .hasSize(3);

        assertThat(injectionPoints.stream()
            .filter(MethodInjectionPoint.class::isInstance))
            .hasSize(3);

        assertThat(injectionPoints.stream()
            .filter(MethodInjectionPoint.class::isInstance)
            .map(MethodInjectionPoint.class::cast)
            .flatMap(MethodInjectionPoint::dependencies)
            .map(Dependency::typeUsage)
            .flatMap(typeUsage -> typeUsage.traits(AnnotationTypeUsage.class))
            .filter(annotationTypeUsage -> annotationTypeUsage.typeName().
                canonicalName().equals(Named.class.getCanonicalName()))
            .flatMap(AnnotationTypeUsage::values)
            .map(AnnotationValue::value))
            .containsOnly("FirstName", "LastName", "Age");
    }

    /**
     * Ensure the {@link Named} annotation is found to have a {@link Qualifier} meta-annotation.
     */
    @Test
    void shouldDiscoverQualifierMetaAnnotation() {
        final var injectionFramework = createInjectionFramework();
        final var codeModel = injectionFramework.codeModel();

        final var namedTypeDescriptor = codeModel.getJDKTypeDescriptor(Named.class)
            .orElseThrow();

        assertThat(namedTypeDescriptor.typeName().canonicalName())
            .isEqualTo(Named.class.getCanonicalName());

        assertThat(injectionFramework.hasQualifierAnnotation(namedTypeDescriptor))
            .isTrue();
    }

    /**
     * Ensure the {@link FieldInjectablePerson} {@link Qualifier}-based {@link Named} annotations can be discovered.
     */
    @Test
    void shouldDiscoverFieldBasedQualifierAnnotations() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(FieldInjectablePerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints.stream()
            .flatMap(InjectionPoint::dependencies)
            .map(Objects::toString)
            .filter(signature -> signature.contains("Named")))
            .hasSize(3);
    }

    /**
     * Ensure the {@link SetterInjectablePerson} {@link Qualifier}-based {@link Named} annotations can be discovered.
     */
    @Test
    void shouldDiscoverSetterBasedQualifierAnnotations() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(SetterInjectablePerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints.stream()
            .flatMap(InjectionPoint::dependencies)
            .map(Objects::toString)
            .filter(signature -> signature.contains("Named")))
            .hasSize(3);
    }

    /**
     * Ensure the {@link ConstructorInjectablePerson} {@link Qualifier}-based {@link Named} annotations can be discovered.
     */
    @Test
    void shouldDiscoverConstructorBasedQualifierAnnotations() {
        final var injectionFramework = createInjectionFramework();

        final var injectionPoints = injectionFramework
            .getInjectableDescriptor(ConstructorInjectablePerson.class)
            .injectionPoints()
            .toList();

        assertThat(injectionPoints.stream()
            .flatMap(InjectionPoint::dependencies)
            .map(Objects::toString)
            .filter(signature -> signature.contains("Named")))
            .hasSize(3);
    }
}
