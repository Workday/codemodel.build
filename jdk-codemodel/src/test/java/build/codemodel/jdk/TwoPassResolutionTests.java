package build.codemodel.jdk;

import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.expression.MethodInvocation;
import build.codemodel.jdk.expression.ResolvedMethod;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link JdkInitializer} resolves calls to types regardless of source-list order.
 * Type descriptor registration and method/constructor/field-initializer body conversion happen in
 * two separate passes: all compilation units are registered first, and body conversion is deferred
 * until every type's descriptor exists, so a call to a type processed later in source order still
 * resolves to a {@link ResolvedMethod} trait.
 *
 * <p>{@code Caller#outer()} calls {@code Callee#helper()}. Listing {@code caller} before
 * {@code callee} reproduces the bug; swapping the order makes it pass.
 */
class TwoPassResolutionTests {

    private static final String CALLEE_SOURCE = """
        package com.example;
        public class Callee {
            public static String helper() {
                return "helper";
            }
        }
        """;

    private static final String CALLER_SOURCE = """
        package com.example;
        public class Caller {
            public String outer() {
                return Callee.helper();
            }
        }
        """;

    private static JavaFileObject inMemorySource(final String qualifiedName, final String source) {
        final var uri = URI.create("mem:///" + qualifiedName.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension);
        return new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
    }

    private static boolean helperCallResolves(final List<JavaFileObject> sourcesInProcessingOrder) {
        final var nameProvider = new NonCachingNameProvider();
        final var codeModel = new JDKCodeModel(nameProvider);
        new JdkInitializer(List.of(), List.of(), sourcesInProcessingOrder).initialize(codeModel);

        final var callerType = codeModel.getTypeDescriptor(
            nameProvider.getTypeName(java.util.Optional.empty(), "com.example.Caller")).orElseThrow();

        final var outer = callerType.traits(MethodDescriptor.class)
            .filter(md -> md.methodName().name().toString().equals("outer"))
            .findFirst().orElseThrow();

        final var body = outer.getTrait(MethodBodyDescriptor.class).orElseThrow().body();

        final var helperCall = body.composition(MethodInvocation.class)
            .filter(mi -> mi.methodName().equals("helper"))
            .findFirst().orElseThrow();

        return helperCall.getTrait(ResolvedMethod.class).isPresent();
    }

    @Test
    void helperCallResolvesToResolveWhenCallerIsProcessedBeforeCallee() {
        final var caller = inMemorySource("com.example.Caller", CALLER_SOURCE);
        final var callee = inMemorySource("com.example.Callee", CALLEE_SOURCE);

        assertThat(helperCallResolves(List.of(caller, callee))).isTrue();
    }

    @Test
    void helperCallResolvesWhenCalleeIsProcessedBeforeCaller() {
        final var caller = inMemorySource("com.example.Caller", CALLER_SOURCE);
        final var callee = inMemorySource("com.example.Callee", CALLEE_SOURCE);

        assertThat(helperCallResolves(List.of(callee, caller))).isTrue();
    }
}
