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
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import com.sun.source.tree.AnnotationTree;
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
import java.util.Set;
import java.util.function.BiFunction;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

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

    /**
     * The filename of a JPMS module declaration source file.
     */
    public static final String SOURCE_FILENAME = "module-info.java";

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

        for (final AnnotationTree ann : moduleTree.getAnnotations()) {
            final String name = ann.getAnnotationType().toString();
            addTrait(AnnotationTypeUsage.of(codeModel(),
                codeModel().getNameProvider().getTypeName(Optional.empty(), name),
                Stream.empty()));
        }

        for (final var directive : moduleTree.getDirectives()) {
            switch (directive.getKind()) {
                case REQUIRES -> {
                    final var req = (RequiresTree) directive;
                    addRequires(req.getModuleName().toString(), req.isTransitive(), req.isStatic(),
                        false, false, Optional.empty());
                }
                case EXPORTS -> {
                    final var exp = (ExportsTree) directive;
                    addExports(exp.getPackageName().toString(),
                        exp.getModuleNames() == null
                            ? Stream.empty()
                            : exp.getModuleNames().stream().map(Object::toString),
                        Optional.empty());
                }
                case OPENS -> {
                    final var op = (OpensTree) directive;
                    addOpens(op.getPackageName().toString(),
                        op.getModuleNames() == null
                            ? Stream.empty()
                            : op.getModuleNames().stream().map(Object::toString),
                        Optional.empty());
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
        if (mod.moduleFlags().contains(AccessFlag.SYNTHETIC)) {
            addTrait(ModuleModifier.SYNTHETIC);
        }
        if (mod.moduleFlags().contains(AccessFlag.MANDATED)) {
            addTrait(ModuleModifier.MANDATED);
        }

        mod.moduleVersion()
            .map(v -> v.stringValue())
            .filter(v -> !v.isBlank())
            .flatMap(Version::tryParse)
            .ifPresent(v -> addTrait(VersionTrait.of(v)));

        mod.requires().forEach(req -> {
            final Optional<Version> version = req.requiresVersion()
                .map(v -> v.stringValue())
                .filter(v -> !v.isBlank())
                .flatMap(Version::tryParse);
            addRequires(req.requires().name().stringValue(),
                req.requiresFlags().contains(AccessFlag.TRANSITIVE),
                req.requiresFlags().contains(AccessFlag.STATIC_PHASE),
                req.requiresFlags().contains(AccessFlag.SYNTHETIC),
                req.requiresFlags().contains(AccessFlag.MANDATED),
                version);
        });

        mod.exports().forEach(exp -> {
            final Optional<PackageDirectiveModifier> modifier;
            if (exp.exportsFlags().contains(AccessFlag.MANDATED)) {
                modifier = Optional.of(PackageDirectiveModifier.MANDATED);
            } else if (exp.exportsFlags().contains(AccessFlag.SYNTHETIC)) {
                modifier = Optional.of(PackageDirectiveModifier.SYNTHETIC);
            } else {
                modifier = Optional.empty();
            }
            addExports(exp.exportedPackage().name().stringValue(),
                exp.exportsTo().stream().map(me -> me.name().stringValue()),
                modifier);
        });

        mod.opens().forEach(op -> {
            final Optional<PackageDirectiveModifier> modifier;
            if (op.opensFlags().contains(AccessFlag.MANDATED)) {
                modifier = Optional.of(PackageDirectiveModifier.MANDATED);
            } else if (op.opensFlags().contains(AccessFlag.SYNTHETIC)) {
                modifier = Optional.of(PackageDirectiveModifier.SYNTHETIC);
            } else {
                modifier = Optional.empty();
            }
            addOpens(op.openedPackage().name().stringValue(),
                op.opensTo().stream().map(me -> me.name().stringValue()),
                modifier);
        });

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

        // capture annotations that legally precede the module declaration (e.g. @SomeAnnotation or @Some.Annotation(...))
        final var annotationNames = new ArrayList<String>();
        while (scanner.follows(Pattern.compile("@[\\w.]+"))) {
            final String token = scanner.consume(Pattern.compile("@[\\w.]+"));
            annotationNames.add(token.substring(1)); // strip leading @
            if (scanner.follows(Pattern.compile("\\("))) {
                scanner.consume(Pattern.compile("\\([^)]*\\)"));
            }
        }

        final boolean open = scanner.optionallyConsume(OPEN).isPresent();
        scanner.consume(MODULE);
        final String rawName = scanner.consume(NAME_PATTERN);

        final ModuleName moduleName = codeModel.getNameProvider().getModuleName(rawName)
            .orElseThrow(() -> new ParseException(null, "Invalid module name", rawName));

        final JDKModuleDescriptor descriptor =
            codeModel.createModuleDescriptor(moduleName, JDKModuleDescriptor::of);

        if (open) {
            descriptor.computeIfAbsent(OpenModule.class, _ -> OpenModule.OPEN);
        }

        annotationNames.forEach(name ->
            descriptor.addTrait(AnnotationTypeUsage.of(codeModel,
                codeModel.getNameProvider().getTypeName(Optional.empty(), name),
                Stream.empty())));

        scanner.consume(OPEN_BRACE);

        while (scanner.hasNext() && !scanner.follows(CLOSE_BRACE)) {
            if (scanner.optionallyConsume(REQUIRES).isPresent()) {
                final boolean isStatic = scanner.optionallyConsume(STATIC).isPresent();
                final boolean isTransitive = !isStatic && scanner.optionallyConsume(TRANSITIVE).isPresent();
                descriptor.addRequires(scanner.consume(NAME_PATTERN), isTransitive, isStatic,
                    false, false, Optional.empty());
                scanner.consume(SEMICOLON);
            } else if (scanner.optionallyConsume(EXPORTS).isPresent()) {
                descriptor.addExports(scanner.consume(NAME_PATTERN),
                    parsePackageTargets(scanner, TO, NAME_PATTERN, COMMA).stream(),
                    Optional.empty());
                scanner.consume(SEMICOLON);
            } else if (scanner.optionallyConsume(OPENS).isPresent()) {
                descriptor.addOpens(scanner.consume(NAME_PATTERN),
                    parsePackageTargets(scanner, TO, NAME_PATTERN, COMMA).stream(),
                    Optional.empty());
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
        return extractWith(codeModel, path,
            (cm, mn) -> cm.createModuleDescriptor(mn, JDKModuleDescriptor::of));
    }

    /**
     * Extracts a {@link JDKModuleDescriptor} from the {@code module-info.class} inside the JAR at
     * the given {@link Path} <em>without</em> registering it in the {@link CodeModel}.
     * Falls back to the {@code Automatic-Module-Name} manifest attribute for automatic modules.
     * <p>
     * Use this when the caller maintains its own descriptor cache keyed by artifact coordinates
     * (groupId:artifactId:version) so that two JARs sharing the same JPMS module name but carrying
     * different versions receive independent descriptor objects rather than sharing the single entry
     * that {@link CodeModel#createModuleDescriptor} would return.
     *
     * @param codeModel the {@link CodeModel} used for name resolution and child-descriptor creation
     * @param path      the {@link Path} of a JAR file
     * @return the {@link Optional} {@link JDKModuleDescriptor}, or {@link Optional#empty()} if the
     * JAR carries no recognisable JPMS module information
     * @throws IllegalArgumentException if the path does not exist
     * @throws UncheckedIOException     if the JAR cannot be read
     */
    public static Optional<JDKModuleDescriptor> extractFresh(final CodeModel codeModel,
                                                             final Path path) {
        return extractWith(codeModel, path, JDKModuleDescriptor::of);
    }

    private static Optional<JDKModuleDescriptor> extractWith(final CodeModel codeModel,
                                                             final Path path,
                                                             final BiFunction<CodeModel, ModuleName, JDKModuleDescriptor> factory) {

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        try (JarFile jarFile = new JarFile(path.toFile(), true, ZipFile.OPEN_READ, Runtime.version())) {

            final var entry = Optional.ofNullable(jarFile.getJarEntry("module-info.class"));

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
                    final var descriptor = factory.apply(codeModel, moduleName);
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
                        final var descriptor = factory.apply(codeModel, moduleName);
                        descriptor.addTrait(ModuleModifier.AUTOMATIC);
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

    // ---- Convenience accessors -------------------------------------------

    /**
     * Returns the module version if one was recorded.
     *
     * @return the {@link Optional} {@link Version}
     */
    public Optional<Version> version() {
        return getTrait(VersionTrait.class).map(VersionTrait::version);
    }

    /**
     * Returns {@code true} if this is an open module ({@code open module ...}).
     *
     * @return {@code true} if the module is open
     */
    public boolean isOpen() {
        return hasTrait(OpenModule.class);
    }

    /**
     * Returns {@code true} if this is an automatic module (derived from a plain JAR via
     * {@code Automatic-Module-Name}). Never {@code true} for source-parsed descriptors.
     *
     * @return {@code true} if the module is automatic
     */
    public boolean isAutomatic() {
        return traits(ModuleModifier.class).anyMatch(m -> m == ModuleModifier.AUTOMATIC);
    }

    /**
     * Returns {@code true} if this module is marked {@code SYNTHETIC} in bytecode.
     * Never {@code true} for source-parsed descriptors.
     *
     * @return {@code true} if the module is synthetic
     */
    public boolean isSynthetic() {
        return traits(ModuleModifier.class).anyMatch(m -> m == ModuleModifier.SYNTHETIC);
    }

    /**
     * Returns {@code true} if this module is marked {@code MANDATED} in bytecode.
     * Never {@code true} for source-parsed descriptors.
     *
     * @return {@code true} if the module is mandated
     */
    public boolean isMandated() {
        return traits(ModuleModifier.class).anyMatch(m -> m == ModuleModifier.MANDATED);
    }

    /**
     * Returns the version declared on a {@code requires} clause, if any.
     * Only present in bytecode-extracted descriptors; always empty for source-parsed descriptors.
     *
     * @param req the {@link RequiresModuleDescriptor}
     * @return the {@link Optional} {@link Version}
     */
    public static Optional<Version> requiresVersion(final RequiresModuleDescriptor req) {
        return req.getTrait(RequiresVersionTrait.class).map(RequiresVersionTrait::version);
    }

    /**
     * Copies all JPMS directives from {@code other} into this descriptor, deduplicating
     * each directive type: {@code requires} by module name, {@code exports} and {@code opens}
     * by package name, {@code provides} and {@code uses} by service type.
     *
     * @param other the {@link JDKModuleDescriptor} whose directives should be merged in
     */
    public void include(final JDKModuleDescriptor other) {
        final Set<ModuleName> existingReqs = requiresClauses()
            .map(RequiresModuleDescriptor::requiresModuleName)
            .collect(Collectors.toSet());
        other.requiresClauses()
            .filter(r -> !existingReqs.contains(r.requiresModuleName()))
            .forEach(r -> {
                final var copy = RequiresModuleDescriptor.of(codeModel(), r.requiresModuleName());
                r.traits(RequiresModifier.class).forEach(copy::addTrait);
                requiresVersion(r).ifPresent(v -> copy.addTrait(RequiresVersionTrait.of(v)));
                addTrait(copy);
            });
        final Set<Namespace> existingExports = exportsClauses()
            .map(ExportsDescriptor::packageName)
            .collect(Collectors.toSet());
        other.exportsClauses()
            .filter(e -> !existingExports.contains(e.packageName()))
            .forEach(this::addTrait);
        final Set<Namespace> existingOpens = opensClauses()
            .map(OpensDescriptor::packageName)
            .collect(Collectors.toSet());
        other.opensClauses()
            .filter(o -> !existingOpens.contains(o.packageName()))
            .forEach(this::addTrait);
        final Set<TypeUsage> existingProvides = providesClauses()
            .map(ProvidesDescriptor::serviceType)
            .collect(Collectors.toSet());
        other.providesClauses()
            .filter(p -> !existingProvides.contains(p.serviceType()))
            .forEach(this::addTrait);
        final Set<TypeUsage> existingUses = usesClauses()
            .map(UsesDescriptor::serviceType)
            .collect(Collectors.toSet());
        other.usesClauses()
            .filter(u -> !existingUses.contains(u.serviceType()))
            .forEach(this::addTrait);
    }

    /**
     * Returns all {@code requires} clauses as a stream of {@link RequiresModuleDescriptor}s.
     * Named {@code requiresClauses} to avoid shadowing the foundation method
     * {@link build.codemodel.foundation.descriptor.ModuleDescriptor#requires()}.
     *
     * @return a {@link Stream} of {@link RequiresModuleDescriptor}
     */
    public Stream<RequiresModuleDescriptor> requiresClauses() {
        return traits(RequiresModuleDescriptor.class);
    }

    /**
     * Returns all {@code exports} clauses as a stream of {@link ExportsDescriptor}s.
     *
     * @return a {@link Stream} of {@link ExportsDescriptor}
     */
    public Stream<ExportsDescriptor> exportsClauses() {
        return traits(ExportsDescriptor.class);
    }

    /**
     * Returns all {@code opens} clauses as a stream of {@link OpensDescriptor}s.
     *
     * @return a {@link Stream} of {@link OpensDescriptor}
     */
    public Stream<OpensDescriptor> opensClauses() {
        return traits(OpensDescriptor.class);
    }

    /**
     * Returns all {@code provides} clauses as a stream of {@link ProvidesDescriptor}s.
     *
     * @return a {@link Stream} of {@link ProvidesDescriptor}
     */
    public Stream<ProvidesDescriptor> providesClauses() {
        return traits(ProvidesDescriptor.class);
    }

    /**
     * Returns all {@code uses} clauses as a stream of {@link UsesDescriptor}s.
     *
     * @return a {@link Stream} of {@link UsesDescriptor}
     */
    public Stream<UsesDescriptor> usesClauses() {
        return traits(UsesDescriptor.class);
    }

    /**
     * Returns all annotations declared on the {@code module} declaration as a stream of
     * {@link AnnotationTypeUsage}s. Only populated for source-parsed and javac-tree-based descriptors;
     * bytecode-extracted descriptors return an empty stream.
     *
     * @return a {@link Stream} of {@link AnnotationTypeUsage}
     */
    public Stream<AnnotationTypeUsage> annotationClauses() {
        return traits(AnnotationTypeUsage.class);
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
                             final boolean isStatic,
                             final boolean isSynthetic,
                             final boolean isMandated,
                             final Optional<Version> version) {

        codeModel().getNameProvider().getModuleName(rawName).ifPresent(reqName -> {
            // idempotent: skip if this module is already in the requires list
            if (requiresClauses().anyMatch(r -> r.requiresModuleName().equals(reqName))) {
                return;
            }
            final var reqDescriptor = RequiresModuleDescriptor.of(codeModel(), reqName);
            if (isTransitive) {
                reqDescriptor.addTrait(RequiresModifier.TRANSITIVE);
            }
            if (isStatic) {
                reqDescriptor.addTrait(RequiresModifier.STATIC);
            }
            if (isSynthetic) {
                reqDescriptor.addTrait(RequiresModifier.SYNTHETIC);
            }
            if (isMandated) {
                reqDescriptor.addTrait(RequiresModifier.MANDATED);
            }
            version.ifPresent(v -> reqDescriptor.addTrait(RequiresVersionTrait.of(v)));
            addTrait(reqDescriptor);
        });
    }

    private void addExports(final String rawPkg,
                            final Stream<String> rawTargets,
                            final Optional<PackageDirectiveModifier> modifier) {
        final String pkg = rawPkg.replace('/', '.');
        codeModel().getNameProvider().getNamespace(pkg).ifPresent(ns -> {
            if (exportsClauses().anyMatch(e -> e.packageName().equals(ns))) {
                return;
            }
            addTrait(modifier
                .map(m -> ExportsDescriptor.of(ns, resolveModuleNames(rawTargets), m))
                .orElseGet(() -> ExportsDescriptor.of(ns, resolveModuleNames(rawTargets))));
        });
    }

    private void addOpens(final String rawPkg,
                          final Stream<String> rawTargets,
                          final Optional<PackageDirectiveModifier> modifier) {
        final String pkg = rawPkg.replace('/', '.');
        codeModel().getNameProvider().getNamespace(pkg).ifPresent(ns -> {
            if (opensClauses().anyMatch(o -> o.packageName().equals(ns))) {
                return;
            }
            addTrait(modifier
                .map(m -> OpensDescriptor.of(ns, resolveModuleNames(rawTargets), m))
                .orElseGet(() -> OpensDescriptor.of(ns, resolveModuleNames(rawTargets))));
        });
    }

    private void addProvides(final String rawService,
                             final Stream<String> rawProviders) {
        final var service = typeUsage(rawService);
        if (providesClauses().anyMatch(p -> p.serviceType().equals(service))) {
            return;
        }
        final var impls = rawProviders.map(this::typeUsage);
        addTrait(ProvidesDescriptor.of(service, impls));
    }

    private void addUses(final String rawService) {
        final var service = typeUsage(rawService);
        if (usesClauses().anyMatch(u -> u.serviceType().equals(service))) {
            return;
        }
        addTrait(UsesDescriptor.of(service));
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

