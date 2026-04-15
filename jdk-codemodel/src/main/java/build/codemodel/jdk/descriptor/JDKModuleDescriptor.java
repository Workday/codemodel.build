package build.codemodel.jdk.descriptor;

/*-
 * #%L
 * JDK Code Model
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.parsing.Filter;
import build.base.parsing.ParseException;
import build.base.parsing.Scanner;
import build.base.version.Version;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.AbstractModuleDescriptor;
import build.codemodel.foundation.descriptor.RequiresModuleDescriptor;
import build.codemodel.foundation.descriptor.Trait;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.UsesTree;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A {@link build.codemodel.foundation.descriptor.ModuleDescriptor} implementation for the
 * <i>JDK Code Model</i>, representing a JPMS module declaration.
 * <p>
 * Instances are created via:
 * <ul>
 *   <li>{@link #parse(CodeModel, Reader)} / {@link #parse(CodeModel, String)} — Scanner-based
 *       {@code module-info.java} source parser</li>
 *   <li>{@link #extract(CodeModel, Path)} — ClassFile API extraction from a compiled JAR</li>
 *   <li>{@link CodeModel#createModuleDescriptor(ModuleName, java.util.function.BiFunction)
 *       codeModel.createModuleDescriptor(name, JDKModuleDescriptor::of)} — programmatic construction</li>
 * </ul>
 * <p>
 * JPMS information is stored as {@link build.codemodel.foundation.descriptor.Trait}s:
 * {@link OpenModule}, {@link RequiresModuleDescriptor} (with optional {@link RequiresModifier}),
 * {@link ExportsDescriptor}, {@link OpensDescriptor}, {@link ProvidesDescriptor},
 * {@link UsesDescriptor}, and {@link VersionTrait}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public final class JDKModuleDescriptor
    extends AbstractModuleDescriptor {

    private JDKModuleDescriptor(final CodeModel codeModel,
                                final ModuleName moduleName) {
        super(codeModel, moduleName);
    }

    /**
     * Factory method compatible with
     * {@link CodeModel#createModuleDescriptor(ModuleName, java.util.function.BiFunction)}.
     *
     * @param codeModel  the {@link CodeModel}
     * @param moduleName the {@link ModuleName}
     * @return a new {@link JDKModuleDescriptor}
     */
    public static JDKModuleDescriptor of(final CodeModel codeModel,
                                         final ModuleName moduleName) {
        return new JDKModuleDescriptor(codeModel, moduleName);
    }

    // ---- Populate from source (ModuleTree) --------------------------------

    /**
     * Populates this descriptor's traits from a javac {@link ModuleTree}.
     * Called by {@link build.codemodel.jdk.JdkInitializer} after creating the descriptor.
     *
     * @param moduleTree the {@link ModuleTree} produced by javac
     */
    public void populateFrom(final ModuleTree moduleTree) {

        if (moduleTree.getModuleType() == ModuleTree.ModuleKind.OPEN) {
            addTrait(OpenModule.OPEN);
        }

        for (final var directive : moduleTree.getDirectives()) {
            switch (directive.getKind()) {
                case REQUIRES -> {
                    final var req = (RequiresTree) directive;
                    addRequires(req.getModuleName().toString(), req.isTransitive(), req.isStatic());
                }
                case EXPORTS -> {
                    final var exp = (ExportsTree) directive;
                    addExports(exp.getPackageName().toString(),
                        exp.getModuleNames() == null
                            ? Stream.empty()
                            : exp.getModuleNames().stream().map(Object::toString));
                }
                case OPENS -> {
                    final var op = (OpensTree) directive;
                    addOpens(op.getPackageName().toString(),
                        op.getModuleNames() == null
                            ? Stream.empty()
                            : op.getModuleNames().stream().map(Object::toString));
                }
                case PROVIDES -> {
                    final var prov = (ProvidesTree) directive;
                    addProvides(prov.getServiceName().toString(),
                        prov.getImplementationNames().stream().map(Object::toString));
                }
                case USES -> {
                    final var uses = (UsesTree) directive;
                    addUses(uses.getServiceName().toString());
                }
                default -> { /* version directive — not modelled */ }
            }
        }
    }

    // ---- Populate from bytecode (ModuleAttribute) -------------------------

    /**
     * Populates this descriptor's traits from the ClassFile API's {@link ModuleAttribute}.
     * Called by {@link #extract(CodeModel, Path)} after creating the descriptor.
     *
     * @param mod the {@link ModuleAttribute} from a parsed {@code module-info.class}
     */
    public void populateFrom(final ModuleAttribute mod) {

        if (mod.moduleFlags().contains(AccessFlag.OPEN)) {
            addTrait(OpenModule.OPEN);
        }

        mod.moduleVersion()
            .map(v -> v.stringValue())
            .filter(v -> !v.isBlank())
            .flatMap(Version::tryParse)
            .ifPresent(v -> addTrait(VersionTrait.of(v)));

        mod.requires().forEach(req ->
            addRequires(req.requires().name().stringValue(),
                req.requiresFlags().contains(AccessFlag.TRANSITIVE),
                req.requiresFlags().contains(AccessFlag.STATIC_PHASE)));

        mod.exports().forEach(exp ->
            addExports(exp.exportedPackage().name().stringValue(),
                exp.exportsTo().stream().map(me -> me.name().stringValue())));

        mod.opens().forEach(op ->
            addOpens(op.openedPackage().name().stringValue(),
                op.opensTo().stream().map(me -> me.name().stringValue())));

        mod.provides().forEach(prov ->
            addProvides(prov.provides().asInternalName(),
                prov.providesWith().stream().map(ce -> ce.asInternalName())));

        mod.uses().forEach(use -> addUses(use.asInternalName()));
    }

    // ---- Static factories ------------------------------------------------

    /**
     * Parses a {@code module-info.java} source and registers the resulting
     * {@link JDKModuleDescriptor} in the given {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel} to register the descriptor in
     * @param reader    the source {@link Reader}
     * @return the created and populated {@link JDKModuleDescriptor}
     * @throws ParseException if the source is not valid {@code module-info.java} syntax
     */
    public static JDKModuleDescriptor parse(final CodeModel codeModel,
                                            final Reader reader) throws ParseException {

        final Pattern NAME_PATTERN =
            Pattern.compile("([a-zA-Z][a-zA-Z0-9_$]*)(\\.[a-zA-Z][a-zA-Z0-9_$]*)*");

        final String CLOSE_BRACE = "}";
        final String COMMA = ",";
        final String EXPORTS = "exports";
        final String IMPORT = "import";
        final String MODULE = "module";
        final String OPEN = "open";
        final String OPEN_BRACE = "{";
        final String OPENS = "opens";
        final String PROVIDES = "provides";
        final String REQUIRES = "requires";
        final String SEMICOLON = ";";
        final String STATIC = "static";
        final String TO = "to";
        final String TRANSITIVE = "transitive";
        final String USES = "uses";
        final String WITH = "with";

        final Scanner scanner = new Scanner(reader)
            .register(Filter.WHITESPACE)
            .register(Filter.JAVA_SINGLE_LINE_COMMENT)
            .register(Filter.JAVA_MULTILINE_COMMENT);

        // skip any import statements that legally precede the module declaration
        while (scanner.follows(IMPORT)) {
            scanner.consume(IMPORT);
            scanner.consume(Pattern.compile("[^;]+"));
            scanner.consume(SEMICOLON);
        }

        final boolean open = scanner.optionallyConsume(OPEN).isPresent();
        scanner.consume(MODULE);
        final String rawName = scanner.consume(NAME_PATTERN);

        final ModuleName moduleName = codeModel.getNameProvider().getModuleName(rawName)
            .orElseThrow(() -> new ParseException(null, "Invalid module name", rawName));

        final JDKModuleDescriptor descriptor =
            codeModel.createModuleDescriptor(moduleName, JDKModuleDescriptor::of);

        if (open) {
            descriptor.addTrait(OpenModule.OPEN);
        }

        scanner.consume(OPEN_BRACE);

        while (scanner.hasNext() && !scanner.follows(CLOSE_BRACE)) {
            if (scanner.optionallyConsume(REQUIRES).isPresent()) {
                final boolean isStatic = scanner.optionallyConsume(STATIC).isPresent();
                final boolean isTransitive = !isStatic && scanner.optionallyConsume(TRANSITIVE).isPresent();
                descriptor.addRequires(scanner.consume(NAME_PATTERN), isTransitive, isStatic);
                scanner.consume(SEMICOLON);
            } else if (scanner.optionallyConsume(EXPORTS).isPresent()) {
                descriptor.addExports(scanner.consume(NAME_PATTERN),
                    parsePackageTargets(scanner, TO, NAME_PATTERN, COMMA).stream());
                scanner.consume(SEMICOLON);
            } else if (scanner.optionallyConsume(OPENS).isPresent()) {
                descriptor.addOpens(scanner.consume(NAME_PATTERN),
                    parsePackageTargets(scanner, TO, NAME_PATTERN, COMMA).stream());
                scanner.consume(SEMICOLON);
            } else if (scanner.optionallyConsume(USES).isPresent()) {
                descriptor.addUses(scanner.consume(NAME_PATTERN));
                scanner.consume(SEMICOLON);
            } else if (scanner.optionallyConsume(PROVIDES).isPresent()) {
                final String svcName = scanner.consume(NAME_PATTERN);
                scanner.consume(WITH);
                final var providers = new ArrayList<String>();
                do {
                    providers.add(scanner.consume(NAME_PATTERN));
                    if (scanner.follows(COMMA)) {
                        scanner.consume(COMMA);
                    }
                } while (scanner.follows(NAME_PATTERN));
                descriptor.addProvides(svcName, providers.stream());
                scanner.consume(SEMICOLON);
            } else {
                throw new ParseException(
                    scanner.getLocation(),
                    "Invalid module-info.java syntax",
                    "exports, opens, uses, provides, or requires");
            }
        }

        scanner.consume(CLOSE_BRACE);

        return descriptor;
    }

    /**
     * Parses a {@code module-info.java} source string and registers the resulting
     * {@link JDKModuleDescriptor} in the given {@link CodeModel}.
     *
     * @param codeModel the {@link CodeModel} to register the descriptor in
     * @param source    the {@code module-info.java} source
     * @return the created and populated {@link JDKModuleDescriptor}
     * @throws ParseException if the source is not valid {@code module-info.java} syntax
     */
    public static JDKModuleDescriptor parse(final CodeModel codeModel,
                                            final String source) throws ParseException {

        return parse(codeModel, new StringReader(source));
    }

    /**
     * Extracts a {@link JDKModuleDescriptor} from the {@code module-info.class} inside the JAR at
     * the given {@link Path} and registers it in the given {@link CodeModel}.
     * Falls back to the {@code Automatic-Module-Name} manifest attribute for automatic modules.
     *
     * @param codeModel the {@link CodeModel} to register the descriptor in
     * @param path      the {@link Path} of a JAR file
     * @return the {@link Optional} {@link JDKModuleDescriptor}, or {@link Optional#empty()} if the
     * JAR carries no recognisable JPMS module information
     * @throws IllegalArgumentException if the path does not exist
     * @throws UncheckedIOException     if the JAR cannot be read
     */
    public static Optional<JDKModuleDescriptor> extract(final CodeModel codeModel,
                                                        final Path path) {

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        try (JarFile jarFile = new JarFile(path.toFile())) {

            final var entry = jarFile.stream()
                .filter(e -> e.getName().endsWith("module-info.class"))
                .findFirst();

            if (entry.isPresent()) {
                final byte[] bytes = jarFile.getInputStream(entry.get()).readAllBytes();
                final var classModel = ClassFile.of().parse(bytes);
                final Optional<ModuleAttribute> moduleAttr =
                    classModel.findAttribute(java.lang.classfile.Attributes.module());

                if (moduleAttr.isEmpty()) {
                    return Optional.empty();
                }

                final ModuleAttribute mod = moduleAttr.get();
                final String rawName = mod.moduleName().name().stringValue();

                return codeModel.getNameProvider().getModuleName(rawName).map(moduleName -> {
                    final var descriptor =
                        codeModel.createModuleDescriptor(moduleName, JDKModuleDescriptor::of);
                    descriptor.populateFrom(mod);
                    return descriptor;
                });
            }

            // fallback: automatic module via Automatic-Module-Name manifest attribute
            final var manifest = jarFile.getManifest();
            if (manifest != null) {
                final String autoName =
                    manifest.getMainAttributes().getValue("Automatic-Module-Name");
                if (autoName != null && !autoName.isBlank()) {
                    return codeModel.getNameProvider().getModuleName(autoName.trim()).map(moduleName -> {
                        final var descriptor =
                            codeModel.createModuleDescriptor(moduleName, JDKModuleDescriptor::of);
                        manifestVersion(manifest).ifPresent(v -> descriptor.addTrait(VersionTrait.of(v)));
                        return descriptor;
                    });
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read JAR: " + path, e);
        }

        return Optional.empty();
    }

    // ---- Private trait-adding helpers ------------------------------------
    // All three sources (ModuleTree, ModuleAttribute, Scanner) funnel through
    // these methods, which own the trait construction logic.

    private static ArrayList<String> parsePackageTargets(final Scanner scanner,
                                                         final String to,
                                                         final Pattern namePattern,
                                                         final String comma) {
        final var targets = new ArrayList<String>();
        if (scanner.optionallyConsume(to).isPresent()) {
            do {
                targets.add(scanner.consume(namePattern));
                if (scanner.follows(comma)) {
                    scanner.consume(comma);
                }
            } while (scanner.follows(namePattern));
        }
        return targets;
    }

    private void addRequires(final String rawName,
                             final boolean isTransitive,
                             final boolean isStatic) {

        codeModel().getNameProvider().getModuleName(rawName).ifPresent(reqName -> {
            final var reqDescriptor = RequiresModuleDescriptor.of(codeModel(), reqName);
            if (isTransitive) {
                reqDescriptor.addTrait(RequiresModifier.TRANSITIVE);
            }
            if (isStatic) {
                reqDescriptor.addTrait(RequiresModifier.STATIC);
            }
            addTrait(reqDescriptor);
        });
    }

    private void addExports(final String rawPkg,
                            final Stream<String> rawTargets) {
        addPackageDirective(rawPkg, rawTargets, ExportsDescriptor::of);
    }

    private void addOpens(final String rawPkg,
                          final Stream<String> rawTargets) {
        addPackageDirective(rawPkg, rawTargets, OpensDescriptor::of);
    }

    private void addPackageDirective(final String rawPkg,
                                     final Stream<String> rawTargets,
                                     final BiFunction<Namespace, Stream<ModuleName>, Trait> factory) {
        final String pkg = rawPkg.replace('/', '.');
        codeModel().getNameProvider().getNamespace(pkg).ifPresent(ns ->
            addTrait(factory.apply(ns, resolveModuleNames(rawTargets))));
    }

    private void addProvides(final String rawService,
                             final Stream<String> rawProviders) {
        final var service = typeUsage(rawService);
        final var impls = rawProviders.map(this::typeUsage);
        addTrait(ProvidesDescriptor.of(service, impls));
    }

    private void addUses(final String rawService) {
        addTrait(UsesDescriptor.of(typeUsage(rawService)));
    }

    // ---- Private utilities -----------------------------------------------

    private Stream<ModuleName> resolveModuleNames(final Stream<String> rawNames) {
        return rawNames
            .map(n -> codeModel().getNameProvider().getModuleName(n))
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    private TypeUsage typeUsage(final String rawName) {
        final String canonical = rawName.replace('/', '.');
        return SpecificTypeUsage.of(codeModel(),
            codeModel().getNameProvider().getTypeName(Optional.empty(), canonical));
    }

    private static Optional<Version> manifestVersion(final Manifest manifest) {
        if (manifest == null) {
            return Optional.empty();
        }
        final String raw = manifest.getMainAttributes()
            .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Version.tryParse(raw.split("\\s+")[0]);
    }
}

