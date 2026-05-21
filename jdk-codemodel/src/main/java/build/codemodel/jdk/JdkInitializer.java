package build.codemodel.jdk;

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

import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.framework.initialization.Initializer;
import build.codemodel.jdk.descriptor.EnumConstantDescriptor;
import build.codemodel.jdk.descriptor.FieldInitializerDescriptor;
import build.codemodel.jdk.descriptor.ImportDeclaration;
import build.codemodel.jdk.descriptor.InitializerBlockDescriptor;
import build.codemodel.jdk.descriptor.JDKModuleDescriptor;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.descriptor.SourceLocation;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Initializes a {@link build.codemodel.foundation.CodeModel} by parsing Java source files using
 * {@code javac} and building {@link build.codemodel.jdk.descriptor.JDKTypeDescriptor}s.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
public class JdkInitializer
    implements Initializer {

    private final List<File> sourceFiles;
    private final List<Path> sourceDirectories;
    private final List<JavaFileObject> javaFileObjects;
    private final List<Path> classpath;
    private final List<Path> modulePath;

    /**
     * When non-null, only compilation units whose source URI satisfies this predicate have their
     * descriptors registered. All compilation units are still compiled together, so types from
     * filtered-out units remain resolvable by javac. Used by rescan to include sibling sources
     * for type resolution without re-registering their already-present descriptors.
     */
    private Predicate<URI> registrationFilter = null;

    private boolean initialized = false;

    // Set at the start of initialize() for use by helper methods
    private CodeModel codeModel;
    private NameProvider nameProvider;
    private TypeMirrorResolver resolver;
    private Trees trees;
    private JdkExpressionConverter exprConverter;
    private JdkStatementConverter stmtConverter;

    /**
     * Creates a {@link JdkInitializer} with explicit source inputs.
     *
     * @param sourceFiles       individual {@link File} source files to parse
     * @param sourceDirectories directories to walk recursively for {@code .java} files
     * @param javaFileObjects   in-memory or pre-built {@link JavaFileObject} sources
     */
    public JdkInitializer(final List<File> sourceFiles,
                          final List<Path> sourceDirectories,
                          final List<JavaFileObject> javaFileObjects) {
        this(sourceFiles, sourceDirectories, javaFileObjects, List.of(), List.of());
    }

    /**
     * Creates a {@link JdkInitializer} with explicit source inputs and dependency paths.
     * Callers are responsible for assembling the classpath and module-path lists; this class
     * does no jar classification or module dependency resolution.
     *
     * @param sourceFiles       individual {@link File} source files to parse
     * @param sourceDirectories directories to walk recursively for {@code .java} files
     * @param javaFileObjects   in-memory or pre-built {@link JavaFileObject} sources
     * @param classpath         jars or directories to pass as {@code --class-path}
     * @param modulePath        jars or directories to pass as {@code --module-path}
     */
    public JdkInitializer(final List<File> sourceFiles,
                          final List<Path> sourceDirectories,
                          final List<JavaFileObject> javaFileObjects,
                          final List<Path> classpath,
                          final List<Path> modulePath) {
        this.sourceFiles = sourceFiles;
        this.sourceDirectories = sourceDirectories;
        this.javaFileObjects = javaFileObjects;
        this.classpath = classpath;
        this.modulePath = modulePath;
    }

    /**
     * Creates a {@link JdkInitializer} for a list of individual source files.
     *
     * @param files the {@link File}s to parse
     * @return a new {@link JdkInitializer}
     */
    public static JdkInitializer ofFiles(final List<File> files) {
        return new JdkInitializer(files, List.of(), List.of());
    }

    /**
     * Creates a {@link JdkInitializer} for a source directory, walked recursively for {@code .java} files.
     *
     * @param directory the root source {@link Path}
     * @return a new {@link JdkInitializer}
     */
    public static JdkInitializer ofDirectory(final Path directory) {
        return new JdkInitializer(List.of(), List.of(directory), List.of());
    }

    /**
     * Restricts descriptor registration to compilation units whose source URI satisfies the given
     * predicate. Units that do not match are still compiled (providing type resolution context)
     * but produce no descriptors.
     *
     * @param filter predicate over source URIs; {@code null} means register all units
     * @return this initializer, for chaining
     */
    public JdkInitializer withRegistrationFilter(final Predicate<URI> filter) {
        this.registrationFilter = filter;
        return this;
    }

    /**
     * Parses all configured sources and registers their {@link build.codemodel.jdk.descriptor.JDKTypeDescriptor}s
     * into the given {@link CodeModel}.
     * May only be called once; throws {@link IllegalStateException} on repeated invocations.
     *
     * @param codeModel the {@link CodeModel} to populate
     */
    @Override
    public void initialize(final CodeModel codeModel) {
        if (initialized) {
            throw new IllegalStateException("JdkInitializer.initialize() may only be called once");
        }
        initialized = true;
        this.codeModel = codeModel;
        this.nameProvider = codeModel.getNameProvider();
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {

            final List<JavaFileObject> combined = collectSources(fileManager);
            if (combined.isEmpty()) {
                return;
            }

            final var task = compiler.getTask(null, fileManager, diagnostic -> {
            }, buildOptions(), null, combined);
            final var javacTask = (JavacTask) task;
            final var compilationUnits = javacTask.parse();
            javacTask.analyze();
            this.resolver = new TypeMirrorResolver(codeModel, javacTask.getElements(), null);
            this.trees = Trees.instance(javacTask);
            this.exprConverter = new JdkExpressionConverter(codeModel);
            this.stmtConverter = new JdkStatementConverter(codeModel, this.exprConverter);
            this.exprConverter.setStmtConverter(this.stmtConverter);

            for (final var cut : compilationUnits) {
                final var sourceUri = cut.getSourceFile().toUri();
                final boolean register = registrationFilter == null || registrationFilter.test(sourceUri);
                cut.accept(new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(final ClassTree classTree, final Void unused) {
                        if (!register) {
                            return null;
                        }
                        exprConverter.setTypeContext(trees, cut,
                            mirror -> resolver.resolve(mirror, null));
                        final TreePath classPath = trees.getPath(cut, classTree);
                        final var typeElement = (TypeElement) trees.getElement(classPath);
                        if (typeElement != null
                            && !typeElement.getQualifiedName().toString().isEmpty()) {
                            exprConverter.setEnclosingType(resolver.resolve(typeElement.asType(), null));
                            processTypeElement(typeElement, classTree, cut);
                        }
                        return super.visitClass(classTree, unused);
                    }

                    @Override
                    public Void visitModule(final ModuleTree moduleTree, final Void unused) {
                        if (!register) {
                            return null;
                        }
                        nameProvider.getModuleName(moduleTree.getName().toString()).ifPresent(moduleName -> {
                            final var descriptor = codeModel.createModuleDescriptor(
                                moduleName, JDKModuleDescriptor::of);
                            descriptor.populateFrom(moduleTree);
                            addSourceLocation(trees.getSourcePositions(), cut, moduleTree, descriptor);
                        });
                        return null;
                    }
                }, null);
            }

        } catch (final IOException e) {
            initialized = false;
            throw new RuntimeException("Failed to initialize CodeModel from source files", e);
        }
    }

    // --- Compiler options ---

    private List<String> buildOptions() {
        final var options = new ArrayList<String>();
        if (!classpath.isEmpty()) {
            options.add("--class-path");
            options.add(classpath.stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator)));
        }
        if (!modulePath.isEmpty()) {
            options.add("--module-path");
            options.add(modulePath.stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator)));
        }
        return options.isEmpty() ? null : options;
    }

    // --- Source collection ---

    private List<JavaFileObject> collectSources(final StandardJavaFileManager fileManager) throws IOException {
        final List<File> allFiles = new ArrayList<>(sourceFiles);

        for (final Path directory : sourceDirectories) {
            try (var stream = Files.walk(directory)) {
                stream.filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .forEach(allFiles::add);
            }
        }

        final List<JavaFileObject> combined = new ArrayList<>(javaFileObjects);
        if (!allFiles.isEmpty()) {
            for (final var jfo : fileManager.getJavaFileObjectsFromFiles(allFiles)) {
                combined.add(jfo);
            }
        }
        return combined;
    }

    // --- Type processing ---

    private void processTypeElement(final TypeElement typeElement,
                                    final ClassTree classTree,
                                    final CompilationUnitTree cut) {
        final var typeName = resolver.resolveTypeName(typeElement);

        // Skip if already registered (can happen with inner types visited by the scanner)
        if (codeModel.getTypeDescriptor(typeName).isPresent()) {
            return;
        }

        final var typeDescriptor = resolver.buildTypeDescriptor(typeName, typeElement);

        if (classTree != null && cut != null) {
            final var srcPositions = trees.getSourcePositions();
            addSourceLocation(srcPositions, cut, classTree, typeDescriptor);
        }

        if (!(typeElement.getEnclosingElement() instanceof TypeElement)) {
            addImports(typeDescriptor, cut);
        }
        processMembers(typeDescriptor, typeElement, classTree, cut);
    }

    private void addImports(final JDKTypeDescriptor typeDescriptor, final CompilationUnitTree cut) {
        if (cut == null) {
            return;
        }
        final var imports = cut.getImports();
        for (int i = 0; i < imports.size(); i++) {
            final var importTree = imports.get(i);
            final var qualifiedId = importTree.getQualifiedIdentifier().toString();
            final boolean isStatic = importTree.isStatic();
            final boolean isOnDemand = qualifiedId.endsWith(".*");
            final var name = isOnDemand ? qualifiedId.substring(0, qualifiedId.length() - 2) : qualifiedId;
            if (isStatic && isOnDemand) {
                typeDescriptor.addTrait(ImportDeclaration.ofStaticOnDemand(name, i));
            } else if (isStatic) {
                typeDescriptor.addTrait(ImportDeclaration.ofStatic(name, i));
            } else if (isOnDemand) {
                typeDescriptor.addTrait(ImportDeclaration.ofOnDemand(name, i));
            } else {
                typeDescriptor.addTrait(ImportDeclaration.of(name, i));
            }
        }
    }

    private void processMembers(final JDKTypeDescriptor typeDescriptor,
                                final TypeElement typeElement,
                                final ClassTree classTree,
                                final CompilationUnitTree cut) {
        if (classTree == null || cut == null) {
            return;
        }
        final var srcPositions = trees.getSourcePositions();
        final var sortedMembers = classTree.getMembers().stream()
            .sorted(java.util.Comparator.comparingLong(m -> srcPositions.getStartPosition(cut, m)))
            .toList();
        int enumConstantOrder = 0;
        for (final var member : sortedMembers) {
            final var path = TreePath.getPath(cut, member);
            if (path == null) {
                continue;
            }
            final var elem = trees.getElement(path);
            if (member instanceof VariableTree vt && elem instanceof VariableElement ve) {
                if (ve.getKind() == ElementKind.FIELD) {
                    processField(typeDescriptor, ve, vt, cut, srcPositions);
                } else if (ve.getKind() == ElementKind.ENUM_CONSTANT) {
                    final var name = nameProvider.getIrreducibleName(ve.getSimpleName());
                    typeDescriptor.addTrait(EnumConstantDescriptor.of(name, enumConstantOrder++));
                }
            } else if (member instanceof MethodTree mt && elem instanceof ExecutableElement ee) {
                if (ee.getKind() == ElementKind.CONSTRUCTOR) {
                    processConstructor(typeDescriptor, ee, mt, cut, srcPositions);
                } else if (ee.getKind() == ElementKind.METHOD) {
                    processMethod(typeDescriptor, ee, mt, cut, srcPositions);
                }
            } else if (member instanceof BlockTree bt) {
                typeDescriptor.addTrait(
                    new InitializerBlockDescriptor(bt.isStatic(), stmtConverter.convertStatements(bt.getStatements())));
            }
        }
    }

    private void processField(final JDKTypeDescriptor typeDescriptor,
                              final VariableElement fieldElement,
                              final VariableTree varTree,
                              final CompilationUnitTree cut,
                              final SourcePositions srcPositions) {
        final var fieldDescriptor = resolver.createFieldDescriptor(fieldElement);
        addSourceLocation(srcPositions, cut, varTree, fieldDescriptor);

        typeDescriptor.addTrait(fieldDescriptor);

        if (varTree.getInitializer() != null) {
            fieldDescriptor.addTrait(
                new FieldInitializerDescriptor(exprConverter.convert(varTree.getInitializer())));
        }
    }

    private void processConstructor(final JDKTypeDescriptor typeDescriptor,
                                    final ExecutableElement methodElement,
                                    final MethodTree ctorTree,
                                    final CompilationUnitTree cut,
                                    final SourcePositions srcPositions) {
        final var formalParameters = getFormalParameters(methodElement);

        final var constructorDescriptor = ConstructorDescriptor.of(typeDescriptor, formalParameters);
        TypeMirrorResolver.getAccessModifier(methodElement.getModifiers()).ifPresent(constructorDescriptor::addTrait);
        resolver.addTypeAnnotations(constructorDescriptor, methodElement);

        addSourceLocation(srcPositions, cut, ctorTree, constructorDescriptor);

        typeDescriptor.addTrait(constructorDescriptor);

        if (ctorTree.getBody() != null) {
            final var bodyStart = srcPositions.getStartPosition(cut, ctorTree.getBody());
            final var realStmts = ctorTree.getBody().getStatements().stream()
                .filter(stmt -> {
                    final var pos = srcPositions.getStartPosition(cut, stmt);
                    return pos != Diagnostic.NOPOS && pos > bodyStart;
                })
                .toList();
            constructorDescriptor.addTrait(new MethodBodyDescriptor(stmtConverter.convertStatements(realStmts)));
        }
    }

    private Stream<FormalParameterDescriptor> getFormalParameters(final ExecutableElement methodElement) {
        return resolver.getFormalParameters(methodElement, (_, _) -> {
        });
    }

    private void processMethod(final JDKTypeDescriptor typeDescriptor,
                               final ExecutableElement methodElement,
                               final MethodTree methodTree,
                               final CompilationUnitTree cut,
                               final SourcePositions srcPositions) {
        final var returnType = resolver.resolve(methodElement.getReturnType(), methodElement);
        final var methodName = resolver.methodName(typeDescriptor, methodElement);

        final var formalParameters = getFormalParameters(methodElement);

        final var methodDescriptor = MethodDescriptor.of(typeDescriptor, methodName, returnType, formalParameters);
        resolver.modifyMethod(methodDescriptor, methodElement);

        addSourceLocation(srcPositions, cut, methodTree, methodDescriptor);

        typeDescriptor.addTrait(methodDescriptor);

        if (methodTree.getBody() != null) {
            methodDescriptor.addTrait(new MethodBodyDescriptor(stmtConverter.convertBlock(methodTree.getBody())));
        }
    }

    private void addSourceLocation(final SourcePositions srcPositions,
                                   final CompilationUnitTree cut,
                                   final Tree tree,
                                   final Traitable traitable) {
        final var start = srcPositions.getStartPosition(cut, tree);
        final var end = srcPositions.getEndPosition(cut, tree);
        if (start != Diagnostic.NOPOS) {
            traitable.addTrait(SourceLocation.filePosition(cut.getSourceFile().toUri(), start, end));
        }
    }
}
