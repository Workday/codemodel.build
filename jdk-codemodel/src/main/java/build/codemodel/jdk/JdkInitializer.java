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

import build.base.foundation.Lazy;
import build.codemodel.foundation.CodeModel;
import build.codemodel.foundation.descriptor.FormalParameterDescriptor;
import build.codemodel.foundation.descriptor.ThrowableDescriptor;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.usage.AnnotationTypeUsage;
import build.codemodel.foundation.usage.AnnotationValue;
import build.codemodel.foundation.usage.ArrayTypeUsage;
import build.codemodel.foundation.usage.GenericTypeUsage;
import build.codemodel.foundation.usage.IntersectionTypeUsage;
import build.codemodel.foundation.usage.NamedTypeUsage;
import build.codemodel.foundation.usage.SpecificTypeUsage;
import build.codemodel.foundation.usage.TypeUsage;
import build.codemodel.foundation.usage.TypeVariableUsage;
import build.codemodel.foundation.usage.UnionTypeUsage;
import build.codemodel.foundation.usage.UnknownTypeUsage;
import build.codemodel.foundation.usage.VoidTypeUsage;
import build.codemodel.foundation.usage.WildcardTypeUsage;
import build.codemodel.framework.initialization.Initializer;
import build.codemodel.jdk.descriptor.AnnotationMemberDefaultValue;
import build.codemodel.jdk.descriptor.AnnotationType;
import build.codemodel.jdk.descriptor.EnclosingTypeDescriptor;
import build.codemodel.jdk.descriptor.EnumConstantDescriptor;
import build.codemodel.jdk.descriptor.EnumType;
import build.codemodel.jdk.descriptor.FieldInitializerDescriptor;
import build.codemodel.jdk.descriptor.Final;
import build.codemodel.jdk.descriptor.JDKClassTypeDescriptor;
import build.codemodel.jdk.descriptor.JDKInterfaceTypeDescriptor;
import build.codemodel.jdk.descriptor.JDKModuleDescriptor;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.jdk.descriptor.MethodBodyDescriptor;
import build.codemodel.jdk.descriptor.MethodImplementationDescriptor;
import build.codemodel.jdk.descriptor.RecordComponentDescriptor;
import build.codemodel.jdk.descriptor.RecordType;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ConstructorDescriptor;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.ImplementsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import build.codemodel.objectoriented.naming.MethodName;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
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

    private boolean initialized = false;

    // Set at the start of initialize() for use by helper methods
    private CodeModel codeModel;
    private NameProvider nameProvider;
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
            this.trees = Trees.instance(javacTask);
            this.exprConverter = new JdkExpressionConverter(codeModel);
            this.stmtConverter = new JdkStatementConverter(codeModel, this.exprConverter);
            this.exprConverter.setStmtConverter(this.stmtConverter);

            for (final var cut : compilationUnits) {
                cut.accept(new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(final ClassTree classTree, final Void unused) {
                        exprConverter.setTypeContext(trees, cut,
                            mirror -> resolveTypeUsage(mirror, null));
                        final TreePath classPath = trees.getPath(cut, classTree);
                        final var typeElement = (TypeElement) trees.getElement(classPath);
                        if (typeElement != null
                            && !typeElement.getQualifiedName().toString().isEmpty()) {
                            exprConverter.setEnclosingType(resolveTypeUsage(typeElement.asType(), null));
                            processTypeElement(typeElement, classTree, cut);
                        }
                        return super.visitClass(classTree, unused);
                    }

                    @Override
                    public Void visitModule(final ModuleTree moduleTree, final Void unused) {
                        nameProvider.getModuleName(moduleTree.getName().toString()).ifPresent(moduleName -> {
                            final var descriptor = codeModel.createModuleDescriptor(
                                moduleName, JDKModuleDescriptor::of);
                            descriptor.populateFrom(moduleTree);
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
        final var typeName = resolveTypeName(typeElement);

        // Skip if already registered (can happen with inner types visited by the scanner)
        if (codeModel.getTypeDescriptor(typeName).isPresent()) {
            return;
        }

        final boolean isInterface = typeElement.getKind().isInterface();
        final JDKTypeDescriptor typeDescriptor = codeModel.createTypeDescriptor(
            typeName, isInterface ? JDKInterfaceTypeDescriptor::of : JDKClassTypeDescriptor::of);

        if (typeElement.getKind() == ElementKind.ENUM) {
            typeDescriptor.addTrait(EnumType.ENUM);
        } else if (typeElement.getKind() == ElementKind.RECORD) {
            typeDescriptor.addTrait(RecordType.RECORD);
        } else if (typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
            typeDescriptor.addTrait(AnnotationType.ANNOTATION_TYPE);
        }

        if (typeElement.getEnclosingElement() instanceof TypeElement enclosingElement) {
            typeDescriptor.addTrait(new EnclosingTypeDescriptor(resolveTypeName(enclosingElement)));
        }

        addTypeParameters(typeDescriptor, typeElement);
        addModifiers(typeDescriptor, typeElement);
        addSuperclass(typeDescriptor, typeElement);
        addInterfaces(typeDescriptor, typeElement);
        addFields(typeDescriptor, typeElement, classTree, cut);
        if (typeElement.getKind() == ElementKind.ENUM) {
            addEnumConstants(typeDescriptor, typeElement);
        }
        if (typeElement.getKind() == ElementKind.RECORD) {
            addRecordComponents(typeDescriptor, typeElement);
        }
        addMethods(typeDescriptor, typeElement, classTree, cut);
        addTypeAnnotations(typeDescriptor, typeElement);
    }

    private void addTypeParameters(final JDKTypeDescriptor typeDescriptor, final TypeElement typeElement) {
        if (typeElement.getTypeParameters().isEmpty()) {
            return;
        }
        final var typeVariableUsages = typeElement.getTypeParameters().stream()
            .map(tp -> resolveTypeParameter(tp, typeElement))
            .toList();
        typeDescriptor.addTrait(ParameterizedTypeDescriptor.of(codeModel, typeVariableUsages.stream()));
    }

    private TypeVariableUsage resolveTypeParameter(final TypeParameterElement tp,
                                                   final Element enclosingElement) {
        final var name = nameProvider.getTypeName(tp.getSimpleName().toString());
        final var typeVar = (TypeVariable) tp.asType();

        // Upper bound: skip the implicit java.lang.Object (every type parameter extends it)
        final var upperBound = typeVar.getUpperBound();
        final Optional<Lazy<TypeUsage>> optUpper;
        if (upperBound.getKind() == TypeKind.DECLARED
            && ((TypeElement) ((DeclaredType) upperBound).asElement())
            .getQualifiedName().toString().equals("java.lang.Object")) {
            optUpper = Optional.empty();
        } else {
            optUpper = Optional.of(Lazy.of(resolveTypeUsage(upperBound, enclosingElement)));
        }

        // Type parameters never have lower bounds (only wildcards do)
        return TypeVariableUsage.of(codeModel, name, Optional.empty(), optUpper);
    }

    private void addModifiers(final JDKTypeDescriptor typeDescriptor, final TypeElement typeElement) {
        final var modifiers = typeElement.getModifiers();
        if (modifiers.contains(Modifier.STATIC)) {
            typeDescriptor.addTrait(Static.STATIC);
        }
        getAccessModifier(modifiers).ifPresent(typeDescriptor::addTrait);
        typeDescriptor.addTrait(getClassification(modifiers));
    }

    private void addSuperclass(final JDKTypeDescriptor typeDescriptor, final TypeElement typeElement) {
        final var superMirror = typeElement.getSuperclass();
        if (superMirror.getKind() == TypeKind.NONE || superMirror.getKind() == TypeKind.ERROR) {
            return;
        }
        // java.lang.Object is the implicit superclass of every class; omit it per plan invariant
        if (superMirror instanceof DeclaredType dt
            && ((TypeElement) dt.asElement()).getQualifiedName().toString().equals("java.lang.Object")) {
            return;
        }
        final var superUsage = resolveTypeUsage(superMirror, typeElement);
        if (superUsage instanceof NamedTypeUsage named) {
            typeDescriptor.addTrait(ExtendsTypeDescriptor.of(named));
        }
    }

    private void addInterfaces(final JDKTypeDescriptor typeDescriptor, final TypeElement typeElement) {
        for (final var interfaceMirror : typeElement.getInterfaces()) {
            final var usage = resolveTypeUsage(interfaceMirror, typeElement);
            if (usage instanceof NamedTypeUsage named) {
                typeDescriptor.addTrait(ImplementsTypeDescriptor.of(named));
            }
        }
    }

    private void addFields(final JDKTypeDescriptor typeDescriptor,
                           final TypeElement typeElement,
                           final ClassTree classTree,
                           final CompilationUnitTree cut) {
        final var fieldTrees = buildFieldTreeMap(classTree, cut);
        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(VariableElement.class::cast)
            .forEach(fieldElement -> {
                final var fieldName = nameProvider.getIrreducibleName(fieldElement.getSimpleName());
                final var fieldType = resolveTypeUsage(fieldElement.asType(), fieldElement);
                final var fieldDescriptor = FieldDescriptor.of(codeModel, fieldName, fieldType);

                final var fieldModifiers = fieldElement.getModifiers();
                if (fieldModifiers.contains(Modifier.STATIC)) {
                    fieldDescriptor.addTrait(Static.STATIC);
                }
                getAccessModifier(fieldModifiers).ifPresent(fieldDescriptor::addTrait);

                fieldElement.getAnnotationMirrors().stream()
                    .map(mirror -> createAnnotationTypeUsage(fieldElement, mirror))
                    .forEach(fieldDescriptor::addTrait);

                typeDescriptor.addTrait(fieldDescriptor);

                final var varTree = fieldTrees.get(fieldElement);
                if (varTree != null && varTree.getInitializer() != null) {
                    fieldDescriptor.addTrait(
                        new FieldInitializerDescriptor(exprConverter.convert(varTree.getInitializer())));
                }
            });
    }

    private void addEnumConstants(final JDKTypeDescriptor typeDescriptor,
                                  final TypeElement typeElement) {
        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
            .map(VariableElement.class::cast)
            .forEach(element -> {
                final var name = nameProvider.getIrreducibleName(element.getSimpleName());
                typeDescriptor.addTrait(EnumConstantDescriptor.of(name));
            });
    }

    private void addRecordComponents(final JDKTypeDescriptor typeDescriptor,
                                     final TypeElement typeElement) {
        for (final RecordComponentElement component : typeElement.getRecordComponents()) {
            final var name = nameProvider.getIrreducibleName(component.getSimpleName());
            final var type = resolveTypeUsage(component.asType(), component);
            typeDescriptor.addTrait(RecordComponentDescriptor.of(name, type));
        }
    }

    private void addMethods(final JDKTypeDescriptor typeDescriptor,
                            final TypeElement typeElement,
                            final ClassTree classTree,
                            final CompilationUnitTree cut) {
        final var methodTrees = buildMethodTreeMap(classTree, cut);

        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
            .map(ExecutableElement.class::cast)
            .forEach(methodElement -> {
                final var formalParameters = methodElement.getParameters().stream()
                    .map(param -> {
                        final var pd = FormalParameterDescriptor.of(
                            codeModel,
                            Optional.of(nameProvider.getIrreducibleName(param.getSimpleName())),
                            resolveTypeUsage(param.asType(), param));
                        if (param.getModifiers().contains(Modifier.FINAL)) {
                            pd.addTrait(Final.FINAL);
                        }
                        param.getAnnotationMirrors().stream()
                            .map(mirror -> createAnnotationTypeUsage(param, mirror))
                            .forEach(pd::addTrait);
                        return pd;
                    });

                final var constructorDescriptor = ConstructorDescriptor.of(typeDescriptor, formalParameters);

                final var methodModifiers = methodElement.getModifiers();
                getAccessModifier(methodModifiers).ifPresent(constructorDescriptor::addTrait);

                methodElement.getAnnotationMirrors().stream()
                    .map(mirror -> createAnnotationTypeUsage(methodElement, mirror))
                    .forEach(constructorDescriptor::addTrait);

                typeDescriptor.addTrait(constructorDescriptor);

                final var ctorTree = methodTrees.get(methodElement);
                if (ctorTree != null && ctorTree.getBody() != null) {
                    constructorDescriptor.addTrait(
                        new MethodBodyDescriptor(stmtConverter.convertBlock(ctorTree.getBody())));
                }
            });

        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .map(ExecutableElement.class::cast)
            .forEach(methodElement -> {
                final var methodSimpleName = nameProvider.getIrreducibleName(methodElement.getSimpleName());
                final var returnType = resolveTypeUsage(methodElement.getReturnType(), methodElement);
                final var methodName = MethodName.of(
                    typeDescriptor.typeName().moduleName(),
                    typeDescriptor.typeName().namespace(),
                    Optional.of(typeDescriptor.typeName()),
                    methodSimpleName);

                final var formalParameters = methodElement.getParameters().stream()
                    .map(param -> {
                        final var pd = FormalParameterDescriptor.of(
                            codeModel,
                            Optional.of(nameProvider.getIrreducibleName(param.getSimpleName())),
                            resolveTypeUsage(param.asType(), param));
                        if (param.getModifiers().contains(Modifier.FINAL)) {
                            pd.addTrait(Final.FINAL);
                        }
                        param.getAnnotationMirrors().stream()
                            .map(mirror -> createAnnotationTypeUsage(param, mirror))
                            .forEach(pd::addTrait);
                        return pd;
                    });

                final var methodDescriptor = MethodDescriptor.of(
                    typeDescriptor, methodName, returnType, formalParameters);

                final var jdkDefault = methodElement.getDefaultValue();
                if (jdkDefault != null) {
                    methodDescriptor.addTrait(new AnnotationMemberDefaultValue(jdkDefault.getValue()));
                }

                if (!methodElement.getTypeParameters().isEmpty()) {
                    final var typeVars = methodElement.getTypeParameters().stream()
                        .map(tp -> resolveTypeParameter(tp, methodElement))
                        .toList();
                    methodDescriptor.addTrait(ParameterizedTypeDescriptor.of(codeModel, typeVars.stream()));
                }

                methodElement.getThrownTypes().stream()
                    .map(t -> resolveTypeUsage(t, methodElement))
                    .map(ThrowableDescriptor::of)
                    .forEach(methodDescriptor::addTrait);

                if (methodElement.isDefault()) {
                    methodDescriptor.addTrait(new MethodImplementationDescriptor(methodDescriptor));
                }

                final var methodModifiers = methodElement.getModifiers();
                if (methodModifiers.contains(Modifier.STATIC)) {
                    methodDescriptor.addTrait(Static.STATIC);
                }
                getAccessModifier(methodModifiers).ifPresent(methodDescriptor::addTrait);
                methodDescriptor.addTrait(getClassification(methodModifiers));

                methodElement.getAnnotationMirrors().stream()
                    .map(mirror -> createAnnotationTypeUsage(methodElement, mirror))
                    .forEach(methodDescriptor::addTrait);

                typeDescriptor.addTrait(methodDescriptor);

                final var methodTree = methodTrees.get(methodElement);
                if (methodTree != null && methodTree.getBody() != null) {
                    methodDescriptor.addTrait(
                        new MethodBodyDescriptor(stmtConverter.convertBlock(methodTree.getBody())));
                }
            });
    }

    /**
     * Builds an element→MethodTree map from the class tree using tree-side lookup.
     * This is reliable even when element symbols were loaded from class files, because
     * {@code trees.getElement(TreePath)} always resolves from the source tree.
     */
    private HashMap<Element, MethodTree> buildMethodTreeMap(final ClassTree classTree,
                                                            final CompilationUnitTree cut) {
        final var map = new HashMap<Element, MethodTree>();
        if (classTree == null || cut == null) {
            return map;
        }
        for (final var member : classTree.getMembers()) {
            if (member instanceof MethodTree mt) {
                final var path = TreePath.getPath(cut, mt);
                if (path != null) {
                    final var elem = trees.getElement(path);
                    if (elem != null) {
                        map.put(elem, mt);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Builds an element→VariableTree map for fields from the class tree using tree-side lookup.
     */
    private HashMap<Element, VariableTree> buildFieldTreeMap(final ClassTree classTree,
                                                             final CompilationUnitTree cut) {
        final var map = new HashMap<Element, VariableTree>();
        if (classTree == null || cut == null) {
            return map;
        }
        for (final var member : classTree.getMembers()) {
            if (member instanceof VariableTree vt) {
                final var path = TreePath.getPath(cut, vt);
                if (path != null) {
                    final var elem = trees.getElement(path);
                    if (elem != null) {
                        map.put(elem, vt);
                    }
                }
            }
        }
        return map;
    }

    private void addTypeAnnotations(final JDKTypeDescriptor typeDescriptor, final TypeElement typeElement) {
        typeElement.getAnnotationMirrors().stream()
            .map(mirror -> createAnnotationTypeUsage(typeElement, mirror))
            .forEach(typeDescriptor::addTrait);
    }

    // --- Naming ---

    private build.codemodel.foundation.naming.TypeName resolveTypeName(final TypeElement typeElement) {
        return nameProvider.getTypeName(Optional.empty(), typeElement.getQualifiedName().toString());
    }

    // --- Annotation creation ---

    private AnnotationTypeUsage createAnnotationTypeUsage(final Element enclosing,
                                                          final AnnotationMirror mirror) {
        final var annotationTypeName = resolveElementTypeName(mirror.getAnnotationType().asElement());
        final var values = new ArrayList<AnnotationValue>();
        mirror.getElementValues().forEach((exec, val) -> {
            final var valueName = nameProvider.getIrreducibleName(exec.getSimpleName());
            values.add(AnnotationValue.of(codeModel, valueName, val.getValue()));
        });
        return AnnotationTypeUsage.of(codeModel, annotationTypeName, values.stream());
    }

    private build.codemodel.foundation.naming.TypeName resolveElementTypeName(final Element element) {
        if (element instanceof TypeElement typeElement) {
            final var fqn = typeElement.getQualifiedName().toString();
            if (!fqn.isEmpty()) {
                return nameProvider.getTypeName(Optional.empty(), fqn);
            }
        }
        return nameProvider.getTypeName(element.getSimpleName().toString());
    }

    // --- Modifier helpers ---

    private static Optional<AccessModifier> getAccessModifier(final Collection<? extends Modifier> modifiers) {
        for (final var m : modifiers) {
            switch (m) {
                case PUBLIC:
                    return Optional.of(AccessModifier.PUBLIC);
                case PROTECTED:
                    return Optional.of(AccessModifier.PROTECTED);
                case PRIVATE:
                    return Optional.of(AccessModifier.PRIVATE);
                default:
                    break;
            }
        }
        return Optional.empty();
    }

    private static Classification getClassification(final Collection<? extends Modifier> modifiers) {
        if (modifiers.contains(Modifier.ABSTRACT)) {
            return Classification.ABSTRACT;
        } else if (modifiers.contains(Modifier.FINAL)) {
            return Classification.FINAL;
        }
        return Classification.CONCRETE;
    }

    // --- Type resolution ---

    /**
     * Returns the shared {@link Lazy} placeholder for {@code mirror}, inserting one into
     * {@code pending} if neither {@code pending} nor {@code resolved} contains it yet.
     *
     * <p>This is the single primitive behind the lazy-queue pattern. Every visitor method that
     * encounters a sub-type calls this to obtain a {@link Lazy} whose value will be set when the
     * queue loop eventually visits that sub-type. Because all callers share the same {@link Lazy}
     * instance by reference, the resolved value is automatically visible to every composite built
     * around it — no re-wiring needed.
     */
    private static Lazy<TypeUsage> enqueueIfAbsent(
        final TypeMirror mirror,
        final LinkedHashMap<TypeMirror, Lazy<TypeUsage>> pending,
        final HashMap<TypeMirror, Element> enclosing,
        final LinkedHashMap<TypeMirror, TypeUsage> resolved,
        final Element currentEnclosing) {

        var lazy = Lazy.ofNullable(resolved.get(mirror)).or(() -> pending.get(mirror));
        if (lazy.isEmpty()) {
            pending.putIfAbsent(mirror, lazy);
            lazy = pending.get(mirror);
            enclosing.putIfAbsent(mirror, currentEnclosing);
        }
        return lazy;
    }

    /**
     * Resolves an optional bound {@link TypeMirror} into the lazy-queue.
     * Returns {@link Optional#empty()} when {@code bound} is null (no bound declared in source)
     * or has kind {@link TypeKind#NULL} (the null-type lower-bound javac uses for unbounded type
     * variables).
     */
    private static Optional<Lazy<TypeUsage>> resolveOptionalBound(
        final TypeMirror bound,
        final LinkedHashMap<TypeMirror, Lazy<TypeUsage>> pending,
        final HashMap<TypeMirror, Element> enclosing,
        final LinkedHashMap<TypeMirror, TypeUsage> resolved,
        final Element currentEnclosing) {

        if (bound == null || bound.getKind() == TypeKind.NULL) {
            return Optional.empty();
        }
        return Optional.of(enqueueIfAbsent(bound, pending, enclosing, resolved, currentEnclosing));
    }

    /**
     * Resolves a {@link TypeMirror} to a {@link TypeUsage} using a lazy-queue visitor to handle
     * recursive and mutually-referential generic types without stack overflow.
     *
     * <p><b>Algorithm — depth-first lazy queue:</b>
     * <ol>
     *   <li>The root {@link TypeMirror} is placed in {@code pending} with an empty {@link Lazy}
     *       placeholder.</li>
     *   <li>Each iteration takes the <em>last</em> entry ({@link java.util.LinkedHashMap#lastEntry})
     *       — i.e. the most-recently-added — implementing a LIFO / depth-first traversal.</li>
     *   <li>If the entry's {@link Lazy} is already populated it is moved to {@code resolved} and
     *       removed from {@code pending}.</li>
     *   <li>Otherwise the {@link TypeMirror} is visited. Visitor methods fill the entry's
     *       {@link Lazy} directly ({@link Lazy#set}) for leaf types (primitives, void, …) or
     *       insert new empty-{@link Lazy} placeholders into {@code pending} for any
     *       sub-types they depend on, then construct a composite usage that holds references to
     *       those placeholders.</li>
     *   <li>On the next iteration the newly-inserted sub-type placeholder is at the tail and is
     *       processed first, ensuring dependencies are resolved before the composites that
     *       reference them.</li>
     * </ol>
     *
     * <p>Annotations for each {@link TypeMirror} are computed and cached in {@code annotations}
     * <em>before</em> the visitor fires, so every {@code visitXxx} method can safely call
     * {@code annotations.get(currentMirror)} without a null-check.
     */
    TypeUsage resolveTypeUsage(final TypeMirror typeMirror, final Element enclosingElement) {
        final var pending = new LinkedHashMap<TypeMirror, Lazy<TypeUsage>>();
        final var enclosing = new HashMap<TypeMirror, Element>();
        final var resolved = new LinkedHashMap<TypeMirror, TypeUsage>();
        final var annotations = new HashMap<TypeMirror, ArrayList<AnnotationTypeUsage>>();

        pending.put(typeMirror, Lazy.empty());
        enclosing.put(typeMirror, enclosingElement);

        while (!pending.isEmpty()) {
            // LIFO: most-recently-added entry first → depth-first traversal.
            // Dependencies are pushed to the tail by visitor methods and therefore
            // resolved before the composite types that depend on them.
            final var entry = pending.lastEntry();
            final var pendingMirror = entry.getKey();
            final var pendingLazy = entry.getValue();
            final var pendingEnclosing = enclosing.get(pendingMirror);

            if (pendingLazy.isPresent()) {
                resolved.put(pendingMirror, pendingLazy.get());
                pending.remove(pendingMirror);
            } else if (resolved.containsKey(pendingMirror)) {
                pending.remove(pendingMirror);
            } else {
                // Pre-populate annotations before the visitor fires so that every visitXxx
                // method can safely call annotations.get(currentMirror) without a null-check.
                annotations.computeIfAbsent(pendingMirror, __ ->
                    pendingMirror.getAnnotationMirrors().stream()
                        .map(m -> createAnnotationTypeUsage(pendingEnclosing, m))
                        .collect(Collectors.toCollection(ArrayList::new)));

                pendingMirror.accept(buildTypeVisitor(pending, enclosing, resolved, annotations,
                    pendingMirror, pendingEnclosing), pendingLazy);
            }
        }

        final var result = resolved.get(typeMirror);
        if (result == null) {
            throw new IllegalStateException("Failed to resolve TypeUsage for: " + typeMirror);
        }
        return result;
    }

    private TypeVisitor<Lazy<TypeUsage>, Lazy<TypeUsage>> buildTypeVisitor(
        final LinkedHashMap<TypeMirror, Lazy<TypeUsage>> pending,
        final HashMap<TypeMirror, Element> enclosing,
        final LinkedHashMap<TypeMirror, TypeUsage> resolved,
        final HashMap<TypeMirror, ArrayList<AnnotationTypeUsage>> annotations,
        final TypeMirror currentMirror,
        final Element currentEnclosing) {

        return new TypeVisitor<>() {
            @Override
            public Lazy<TypeUsage> visit(final TypeMirror t, final Lazy<TypeUsage> lazy) {
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitPrimitive(final PrimitiveType t, final Lazy<TypeUsage> lazy) {
                final var moduleName = nameProvider.getModuleName("java.base");
                final var namespace = nameProvider.getNamespace("java.lang");
                final var typeName = nameProvider.getTypeName(
                    moduleName, namespace, Optional.empty(),
                    nameProvider.getIrreducibleName(t.toString()));
                final var usage = SpecificTypeUsage.of(codeModel, typeName);
                annotations.get(currentMirror).forEach(usage::addTrait);
                lazy.set(usage);
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitNull(final NullType t, final Lazy<TypeUsage> lazy) {
                lazy.set(UnknownTypeUsage.create(codeModel));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitArray(final ArrayType t, final Lazy<TypeUsage> lazy) {
                final var lazyComponent = enqueueIfAbsent(
                    t.getComponentType(), pending, enclosing, resolved, currentEnclosing);
                final var usage = ArrayTypeUsage.of(codeModel, lazyComponent);
                annotations.get(currentMirror).forEach(usage::addTrait);
                lazy.set(usage);
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitDeclared(final DeclaredType t, final Lazy<TypeUsage> lazy) {
                final var typeElement = (TypeElement) t.asElement();
                final var fqn = typeElement.getQualifiedName().toString();
                final var typeName = nameProvider.getTypeName(Optional.empty(), fqn);

                if (typeElement.getTypeParameters().isEmpty()) {
                    final var usage = SpecificTypeUsage.of(codeModel, typeName);
                    annotations.get(currentMirror).forEach(usage::addTrait);
                    lazy.set(usage);
                } else {
                    final var lazyArgs = t.getTypeArguments().stream()
                        .map(arg -> enqueueIfAbsent(arg, pending, enclosing, resolved, currentEnclosing))
                        .toList();
                    final var usage = GenericTypeUsage.of(codeModel, typeName, lazyArgs.stream());
                    annotations.get(currentMirror).forEach(usage::addTrait);
                    lazy.set(usage);
                }
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitError(final ErrorType t, final Lazy<TypeUsage> lazy) {
                lazy.set(UnknownTypeUsage.create(codeModel));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitTypeVariable(final TypeVariable t, final Lazy<TypeUsage> lazy) {
                final var typeName = nameProvider.getTypeName(
                    Optional.empty(), t.asElement().getSimpleName().toString());

                // Lower bound: TypeKind.NULL means "no super bound" — resolveOptionalBound handles this.
                final Optional<Lazy<TypeUsage>> optLower = resolveOptionalBound(
                    t.getLowerBound(), pending, enclosing, resolved, t.asElement());

                // Upper bound: skip the implicit java.lang.Object (every type variable extends it).
                final var upperBound = t.getUpperBound();
                final var isObjectUpper = upperBound.getKind() == TypeKind.DECLARED
                    && ((TypeElement) ((DeclaredType) upperBound).asElement()).getQualifiedName().toString()
                    .equals("java.lang.Object");
                final Optional<Lazy<TypeUsage>> optUpper = isObjectUpper
                    ? Optional.empty()
                    : Optional.of(enqueueIfAbsent(upperBound, pending, enclosing, resolved, t.asElement()));

                lazy.set(TypeVariableUsage.of(codeModel, typeName, optLower, optUpper));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitWildcard(final WildcardType t, final Lazy<TypeUsage> lazy) {
                // extends bound → upper bound; super bound → lower bound
                final Optional<Lazy<TypeUsage>> optUpper = resolveOptionalBound(
                    t.getExtendsBound(), pending, enclosing, resolved, currentEnclosing);
                final Optional<Lazy<TypeUsage>> optLower = resolveOptionalBound(
                    t.getSuperBound(), pending, enclosing, resolved, currentEnclosing);
                final var usage = WildcardTypeUsage.of(codeModel, optLower, optUpper);
                annotations.get(currentMirror).forEach(usage::addTrait);
                lazy.set(usage);
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitExecutable(final ExecutableType t, final Lazy<TypeUsage> lazy) {
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitNoType(final NoType t, final Lazy<TypeUsage> lazy) {
                lazy.set(VoidTypeUsage.create(codeModel));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitUnknown(final TypeMirror t, final Lazy<TypeUsage> lazy) {
                lazy.set(UnknownTypeUsage.create(codeModel));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitUnion(final UnionType t, final Lazy<TypeUsage> lazy) {
                final var lazyAlts = t.getAlternatives().stream()
                    .map(alt -> enqueueIfAbsent(alt, pending, enclosing, resolved, currentEnclosing))
                    .toList();
                lazy.set(UnionTypeUsage.of(codeModel, lazyAlts.stream()));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitIntersection(final IntersectionType t, final Lazy<TypeUsage> lazy) {
                final var lazyBounds = t.getBounds().stream()
                    .map(bound -> enqueueIfAbsent(bound, pending, enclosing, resolved, currentEnclosing))
                    .toList();
                lazy.set(IntersectionTypeUsage.of(codeModel, lazyBounds.stream()));
                return lazy;
            }
        };
    }
}
