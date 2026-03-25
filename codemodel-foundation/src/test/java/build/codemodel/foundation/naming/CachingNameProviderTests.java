package build.codemodel.foundation.naming;

import org.junit.jupiter.api.BeforeEach;

/**
 * Unit tests for {@link CachingNameProvider}s.
 *
 * @author brian.oliver
 * @since Jun-2024
 */
class CachingNameProviderTests
    implements NameProviderTests {

    /**
     * The {@link NameProvider} to use for testing.
     */
    private NameProvider provider;

    /**
     * Establish the {@link NameProvider} for each test.
     */
    @BeforeEach
    void onBeforeEach() {
        this.provider = new CachingNameProvider(new NonCachingNameProvider());
    }

    @Override
    public NameProvider getNameProvider() {
        return this.provider;
    }
}
