package build.codemodel.foundation;

import build.codemodel.foundation.descriptor.TypeDescriptor;
import build.codemodel.foundation.naming.NonCachingNameProvider;

/**
 * {@link TypeDescriptorCompatibilityTests} for {@link ConceptualCodeModel}s.
 *
 * @author brian.oliver
 * @since Mar-2025
 */
class ConceptualCodeModelTypeDescriptorTests
    implements TypeDescriptorCompatibilityTests {

    @Override
    public TypeDescriptor createTypeDescriptor() {
        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new ConceptualCodeModel(nameProvider);

        return codeModel.createTypeDescriptor(nameProvider.getTypeName("TestType"));
    }
}
