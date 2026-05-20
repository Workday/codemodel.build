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
import build.codemodel.foundation.descriptor.Traitable;
import build.codemodel.foundation.naming.IrreducibleName;
import build.codemodel.foundation.naming.ModuleName;
import build.codemodel.foundation.naming.NameProvider;
import build.codemodel.foundation.naming.Namespace;
import build.codemodel.foundation.naming.TypeName;
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
import build.codemodel.jdk.descriptor.AnnotationMemberDefaultValue;
import build.codemodel.jdk.descriptor.AnnotationType;
import build.codemodel.jdk.descriptor.EnclosingTypeDescriptor;
import build.codemodel.jdk.descriptor.EnumType;
import build.codemodel.jdk.descriptor.Final;
import build.codemodel.jdk.descriptor.JDKClassTypeDescriptor;
import build.codemodel.jdk.descriptor.JDKInterfaceTypeDescriptor;
import build.codemodel.jdk.descriptor.JDKTypeDescriptor;
import build.codemodel.jdk.descriptor.MemberTypeDescriptor;
import build.codemodel.jdk.descriptor.MethodImplementationDescriptor;
import build.codemodel.jdk.descriptor.RecordComponentDescriptor;
import build.codemodel.jdk.descriptor.RecordType;
import build.codemodel.jdk.descriptor.SourceLocation;
import build.codemodel.jdk.descriptor.Static;
import build.codemodel.jdk.descriptor.Varargs;
import build.codemodel.objectoriented.descriptor.AccessModifier;
import build.codemodel.objectoriented.descriptor.Classification;
import build.codemodel.objectoriented.descriptor.ExtendsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.FieldDescriptor;
import build.codemodel.objectoriented.descriptor.ImplementsTypeDescriptor;
import build.codemodel.objectoriented.descriptor.MethodDescriptor;
import build.codemodel.objectoriented.descriptor.ParameterizedTypeDescriptor;
import build.codemodel.objectoriented.naming.MethodName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Parameterizable;
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
import javax.lang.model.util.Elements;

/**
 * Resolves a {@link TypeMirror} to a {@link TypeUsage} using a depth-first lazy-queue visitor.
 *
 * <p>Both {@link JdkInitializer} and the annotation processor need this algorithm; this class
 * owns it once. Callers supply three strategies that differ between the two contexts:
 * <ul>
 *   <li>{@code errorHandler} — optional; called when an {@link ErrorType} is encountered before
 *       the type is silently mapped to {@link UnknownTypeUsage}</li>
 * </ul>
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
public final class TypeMirrorResolver {

    private final CodeModel codeModel;
    private final NameProvider nameProvider;
    private final Elements elements;
    private final Consumer<ErrorType> errorHandler;
    private final HashMap<TypeElement, TypeName> typeNameCache = new HashMap<>();

    public TypeMirrorResolver(
        final CodeModel codeModel,
        final Elements elements,
        final Consumer<ErrorType> errorHandler) {

        this.codeModel = codeModel;
        this.nameProvider = codeModel.getNameProvider();
        this.elements = elements;
        this.errorHandler = errorHandler;
    }

    /**
     * Resolves {@code typeMirror} to a {@link TypeUsage}.
     *
     * <p><b>Algorithm — depth-first lazy queue:</b>
     * <ol>
     *   <li>The root {@link TypeMirror} is placed in {@code pending} with an empty {@link Lazy} placeholder.</li>
     *   <li>Each iteration takes the last entry (LIFO), implementing depth-first traversal.</li>
     *   <li>Leaf visitor methods fill the {@link Lazy} directly; composite visitors enqueue sub-types
     *       and build composites that hold references to those placeholders.</li>
     *   <li>Annotations for each mirror are pre-computed before the visitor fires.</li>
     * </ol>
     */
    public TypeUsage resolve(final TypeMirror typeMirror, final Element enclosingElement) {
        final var pending = new LinkedHashMap<TypeMirror, Lazy<TypeUsage>>();
        final var enclosing = new HashMap<TypeMirror, Element>();
        final var resolved = new LinkedHashMap<TypeMirror, TypeUsage>();
        final var annotations = new HashMap<TypeMirror, ArrayList<AnnotationTypeUsage>>();

        pending.put(typeMirror, Lazy.empty());
        enclosing.put(typeMirror, enclosingElement);

        while (!pending.isEmpty()) {
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
                annotations.computeIfAbsent(pendingMirror, __ ->
                    pendingMirror.getAnnotationMirrors().stream()
                        .map(m -> createAnnotationTypeUsage(pendingEnclosing, m))
                        .collect(Collectors.toCollection(ArrayList::new)));

                pendingMirror.accept(
                    buildVisitor(pending, enclosing, resolved, annotations, pendingMirror, pendingEnclosing),
                    pendingLazy);
            }
        }

        final var result = resolved.get(typeMirror);
        if (result == null) {
            throw new IllegalStateException("Failed to resolve TypeUsage for: " + typeMirror);
        }
        return result;
    }

    // --- Static utilities ---

    public static Optional<AccessModifier> getAccessModifier(final Collection<? extends Modifier> modifiers) {
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

    public static Classification getClassification(final Collection<? extends Modifier> modifiers) {
        if (modifiers.contains(Modifier.ABSTRACT)) {
            return Classification.ABSTRACT;
        } else if (modifiers.contains(Modifier.FINAL)) {
            return Classification.FINAL;
        }
        return Classification.CONCRETE;
    }

    public static void applyModifiers(final Traitable descriptor,
                                      final Collection<? extends Modifier> modifiers) {
        if (modifiers.contains(Modifier.STATIC)) {
            descriptor.addTrait(Static.STATIC);
        }
        getAccessModifier(modifiers).ifPresent(descriptor::addTrait);
        descriptor.addTrait(getClassification(modifiers));
    }

    // --- Formal parameters ---

    /**
     * Resolves the {@link FormalParameterDescriptor}s for the parameters of {@code methodElement}.
     * Handles name, type, {@link Final}, {@link Varargs}, and annotation traits.
     *
     * @param methodElement the method or constructor whose parameters to resolve
     * @param postProcessor called for each (parameter element, descriptor) pair after the shared
     *                      traits are applied — use this to attach context-specific traits such as
     *                      source location; pass {@code null} if not needed
     */
    public Stream<FormalParameterDescriptor> getFormalParameters(
        final ExecutableElement methodElement,
        final BiConsumer<VariableElement, FormalParameterDescriptor> postProcessor) {

        final var params = methodElement.getParameters();
        final int lastIdx = params.size() - 1;
        return IntStream.range(0, params.size()).mapToObj(i -> {
            final var param = params.get(i);
            final var name = param.getSimpleName().isEmpty()
                ? Optional.<IrreducibleName>empty()
                : Optional.of(nameProvider.getIrreducibleName(param.getSimpleName()));
            final var pd = FormalParameterDescriptor.of(codeModel, name, resolve(param.asType(), param));
            if (param.getModifiers().contains(Modifier.FINAL)) {
                pd.addTrait(Final.FINAL);
            }
            if (methodElement.isVarArgs() && i == lastIdx) {
                pd.addTrait(Varargs.VARARGS);
            }
            param.getAnnotationMirrors().stream()
                .map(mirror -> createAnnotationTypeUsage(param, mirror))
                .forEach(pd::addTrait);

            postProcessor.accept(param, pd);
            return pd;
        });
    }

    // --- Annotation value resolution ---

    public AnnotationTypeUsage createAnnotationTypeUsage(final Element enclosing,
                                                         final AnnotationMirror mirror) {
        final var annotationLocation = SourceLocation.elementRefOrEmpty(enclosing)
            .map(ref -> ref.withAnnotation(mirror));

        final var annotationTypeName = resolveElementTypeName(mirror.getAnnotationType().asElement());
        final var values = new ArrayList<AnnotationValue>();
        mirror.getElementValues().forEach((exec, val) -> {
            final var valueName = nameProvider.getIrreducibleName(exec.getSimpleName());
            final var annotationValue = AnnotationValue.of(codeModel, valueName, resolveAnnotationValue(enclosing, val.getValue()));
            annotationLocation.map(ref -> ref.withValue(val)).ifPresent(annotationValue::addTrait);
            values.add(annotationValue);
        });

        final var annotationTypeUsage = AnnotationTypeUsage.of(codeModel, annotationTypeName, values.stream());
        annotationLocation.ifPresent(annotationTypeUsage::addTrait);
        return annotationTypeUsage;
    }

    // --- Type name resolution ---

    public TypeName resolveTypeName(final TypeElement typeElement) {
        return typeNameCache.computeIfAbsent(typeElement, e -> {
            final var moduleName = resolveModuleName(e);
            final var namespace = resolveNamespace(e);
            final var enclosingTypeName = resolveEnclosingTypeName(e);
            final var irreducibleName = nameProvider.getIrreducibleName(e.getSimpleName().toString());
            return nameProvider.getTypeName(moduleName, namespace, enclosingTypeName, irreducibleName);
        });
    }

    private Optional<ModuleName> resolveModuleName(final TypeElement typeElement) {
        return Optional.ofNullable(elements.getModuleOf(typeElement))
            .filter(me -> !me.isUnnamed())
            .flatMap(me -> nameProvider.getModuleName(me.getQualifiedName().toString()));
    }

    private Optional<Namespace> resolveNamespace(final TypeElement typeElement) {
        return Optional.ofNullable(elements.getPackageOf(typeElement))
            .map(p -> p.getQualifiedName().toString())
            .filter(name -> !name.isEmpty())
            .flatMap(nameProvider::getNamespace);
    }

    private Optional<TypeName> resolveEnclosingTypeName(final TypeElement typeElement) {
        return switch (typeElement.getEnclosingElement()) {
            case TypeElement enclosing -> Optional.of(resolveTypeName(enclosing));
            default -> Optional.empty();
        };
    }

    private TypeName resolveElementTypeName(final Element element) {
        if (element instanceof TypeElement typeElement && !typeElement.getQualifiedName().toString().isEmpty()) {
            return resolveTypeName(typeElement);
        }
        return nameProvider.getTypeName(element.getSimpleName().toString());
    }

    // --- Fields ---

    public FieldDescriptor createFieldDescriptor(final VariableElement fieldElement) {
        final var fieldName = nameProvider.getIrreducibleName(fieldElement.getSimpleName());
        final var fieldType = this.resolve(fieldElement.asType(), fieldElement);
        final var fieldDescriptor = FieldDescriptor.of(codeModel, fieldName, fieldType);
        this.addFieldModifiers(fieldDescriptor, fieldElement);
        this.addTypeAnnotations(fieldDescriptor, fieldElement);
        return fieldDescriptor;
    }

    private void addFieldModifiers(final FieldDescriptor fieldDescriptor,
                                   final VariableElement fieldElement) {
        final var fieldModifiers = fieldElement.getModifiers();
        if (fieldModifiers.contains(Modifier.STATIC)) {
            fieldDescriptor.addTrait(Static.STATIC);
        }
        TypeMirrorResolver.getAccessModifier(fieldModifiers).ifPresent(fieldDescriptor::addTrait);
    }

    // --- Annotations ---

    public void addTypeAnnotations(final Traitable traitable,
                                   final Element element) {
        element.getAnnotationMirrors().stream()
            .map(mirror -> createAnnotationTypeUsage(element, mirror))
            .forEach(traitable::addTrait);
    }

    public AnnotationValue.Value resolveAnnotationValue(final Element enclosing, final Object raw) {
        return switch (raw) {
            case AnnotationMirror nestedMirror ->
                new AnnotationValue.Value.Nested(createAnnotationTypeUsage(enclosing, nestedMirror));
            case javax.lang.model.element.AnnotationValue av -> resolveAnnotationValue(enclosing, av.getValue());
            case List<?> list -> new AnnotationValue.Value.Array(list.stream()
                .map(item -> resolveAnnotationValue(enclosing, item))
                .toList());
            case DeclaredType declaredType -> new AnnotationValue.Value.ClassRef(
                resolveTypeName((TypeElement) declaredType.asElement()));
            case TypeMirror typeMirror -> new AnnotationValue.Value.ClassRef(
                nameProvider.getTypeName(Optional.empty(), typeMirror.toString()));
            case VariableElement varElement -> new AnnotationValue.Value.EnumConstant(
                resolveTypeName((TypeElement) varElement.getEnclosingElement()),
                varElement.getSimpleName().toString());
            default -> new AnnotationValue.Value.Literal(raw);
        };
    }

    // --- Queue helpers ---

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

    // --- Classes ---

    public JDKTypeDescriptor buildTypeDescriptor(final TypeName typeName,
                                                 final TypeElement typeElement) {
        final boolean isInterface = typeElement.getKind().isInterface();
        final var typeDescriptor = codeModel.createTypeDescriptor(typeName, isInterface ? JDKInterfaceTypeDescriptor::of : JDKClassTypeDescriptor::of);

        final var kindTrait = switch (typeElement.getKind()) {
            case ElementKind.ANNOTATION_TYPE -> AnnotationType.ANNOTATION_TYPE;
            case ElementKind.ENUM -> EnumType.ENUM;
            case ElementKind.RECORD -> RecordType.RECORD;
            default -> null;
        };
        Optional.ofNullable(kindTrait).ifPresent(typeDescriptor::addTrait);

        if (typeElement.getEnclosingElement() instanceof TypeElement enclosingElement) {
            typeDescriptor.addTrait(new EnclosingTypeDescriptor(this.resolveTypeName(enclosingElement)));
        }

        addTypeAnnotations(typeDescriptor, typeElement);
        if (typeElement.getKind() == ElementKind.RECORD) {
            addRecordComponents(typeDescriptor, typeElement);
        }

        this.addTypeParameters(typeDescriptor, typeElement);
        TypeMirrorResolver.applyModifiers(typeDescriptor, typeElement.getModifiers());

        this.addSuperclass(typeDescriptor, typeElement);
        this.addInterfaces(typeDescriptor, typeElement);
        this.addedInnerTypes(typeDescriptor, typeElement);

        return typeDescriptor;
    }

    private void addRecordComponents(final JDKTypeDescriptor typeDescriptor,
                                     final TypeElement typeElement) {
        for (final RecordComponentElement component : typeElement.getRecordComponents()) {
            final var name = nameProvider.getIrreducibleName(component.getSimpleName());
            final var type = this.resolve(component.asType(), component);
            typeDescriptor.addTrait(RecordComponentDescriptor.of(name, type));
        }
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
        final var superUsage = this.resolve(superMirror, typeElement);
        if (superUsage instanceof NamedTypeUsage named) {
            typeDescriptor.addTrait(ExtendsTypeDescriptor.of(named));
        }
    }

    private void addInterfaces(final JDKTypeDescriptor typeDescriptor,
                               final TypeElement typeElement) {
        for (final var interfaceMirror : typeElement.getInterfaces()) {
            final var usage = this.resolve(interfaceMirror, typeElement);
            if (usage instanceof NamedTypeUsage named) {
                typeDescriptor.addTrait(ImplementsTypeDescriptor.of(named));
            }
        }
    }

    private void addedInnerTypes(final JDKTypeDescriptor typeDescriptor, final TypeElement typeElement) {
        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
            .map(TypeElement.class::cast)
            .map(this::resolveTypeName)
            .map(MemberTypeDescriptor::of)
            .forEach(typeDescriptor::addTrait);
    }

    // --- Methods ---

    public void modifyMethod(final MethodDescriptor methodDescriptor,
                             final ExecutableElement methodElement) {
        final var jdkDefault = methodElement.getDefaultValue();
        if (jdkDefault != null) {
            methodDescriptor.addTrait(new AnnotationMemberDefaultValue(jdkDefault.getValue()));
        }
        this.addTypeParameters(methodDescriptor, methodElement);
        this.addThrowables(methodElement, methodDescriptor);
        if (methodElement.isDefault()) {
            methodDescriptor.addTrait(new MethodImplementationDescriptor(methodDescriptor));
        }
        TypeMirrorResolver.applyModifiers(methodDescriptor, methodElement.getModifiers());
        this.addTypeAnnotations(methodDescriptor, methodElement);
    }

    public MethodName methodName(final JDKTypeDescriptor typeDescriptor,
                                 final ExecutableElement methodElement) {
        final var methodSimpleName = nameProvider.getIrreducibleName(methodElement.getSimpleName());
        return MethodName.of(
            typeDescriptor.typeName().moduleName(),
            typeDescriptor.typeName().namespace(),
            Optional.of(typeDescriptor.typeName()),
            methodSimpleName);
    }

    private void addThrowables(final ExecutableElement methodElement,
                               final MethodDescriptor methodDescriptor) {
        methodElement.getThrownTypes().stream()
            .map(t -> this.resolve(t, methodElement))
            .map(ThrowableDescriptor::of)
            .forEach(methodDescriptor::addTrait);
    }

    private void addTypeParameters(final Traitable traitable,
                                   final Parameterizable parameterizableElement) {
        if (parameterizableElement.getTypeParameters().isEmpty()) {
            return;
        }
        final var typeVariableUsages = parameterizableElement.getTypeParameters().stream()
            .map(tp -> this.resolveTypeParameter(tp, parameterizableElement))
            .toList();
        traitable.addTrait(ParameterizedTypeDescriptor.of(codeModel, typeVariableUsages.stream()));
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
            optUpper = Optional.of(Lazy.of(this.resolve(upperBound, enclosingElement)));
        }

        // Type parameters never have lower bounds (only wildcards do)
        return TypeVariableUsage.of(codeModel, name, Optional.empty(), optUpper);
    }

    // --- Visitor ---

    private TypeVisitor<Lazy<TypeUsage>, Lazy<TypeUsage>> buildVisitor(
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
                final var typeName = resolveTypeName(typeElement);

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
                if (errorHandler != null) {
                    errorHandler.accept(t);
                }
                lazy.set(UnknownTypeUsage.create(codeModel));
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitTypeVariable(final TypeVariable t, final Lazy<TypeUsage> lazy) {
                final var typeName = nameProvider.getTypeName(Optional.empty(), t.asElement().getSimpleName().toString());

                final Optional<Lazy<TypeUsage>> optLower = resolveOptionalBound(
                    t.getLowerBound(), pending, enclosing, resolved, t.asElement());

                final var upperBound = t.getUpperBound();
                final var isObjectUpper = upperBound.getKind() == TypeKind.DECLARED
                    && ((TypeElement) ((DeclaredType) upperBound).asElement())
                    .getQualifiedName().toString().equals("java.lang.Object");
                final Optional<Lazy<TypeUsage>> optUpper = isObjectUpper
                    ? Optional.empty()
                    : Optional.of(enqueueIfAbsent(upperBound, pending, enclosing, resolved, t.asElement()));

                final var usage = TypeVariableUsage.of(codeModel, typeName, optLower, optUpper);
                annotations.get(currentMirror).forEach(usage::addTrait);
                lazy.set(usage);
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitWildcard(final WildcardType t, final Lazy<TypeUsage> lazy) {
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
                final var usage = UnionTypeUsage.of(codeModel, lazyAlts.stream());
                annotations.get(currentMirror).forEach(usage::addTrait);
                lazy.set(usage);
                return lazy;
            }

            @Override
            public Lazy<TypeUsage> visitIntersection(final IntersectionType t, final Lazy<TypeUsage> lazy) {
                final var lazyBounds = t.getBounds().stream()
                    .map(bound -> enqueueIfAbsent(bound, pending, enclosing, resolved, currentEnclosing))
                    .toList();
                final var usage = IntersectionTypeUsage.of(codeModel, lazyBounds.stream());
                annotations.get(currentMirror).forEach(usage::addTrait);
                lazy.set(usage);
                return lazy;
            }
        };
    }
}
