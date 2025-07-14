package com.osmerion.atbuilder.apt;

import com.palantir.javapoet.*;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor14;
import java.util.List;
import java.util.Map;

final class AnnotatedTypeNameConverter {

    public static TypeName get(TypeMirror mirror) {
        return get(mirror, Map.of());
    }

    private static TypeName get(TypeMirror mirror, Map<TypeParameterElement, TypeVariableName> typeVariables) {
        return mirror.accept(new SimpleTypeVisitor14<TypeName, @Nullable Void>() {

            @Override
            public TypeName visitPrimitive(PrimitiveType t, Void unused) {
                return switch (t.getKind()) {
                    case BOOLEAN -> TypeName.BOOLEAN;
                    case BYTE -> TypeName.BYTE;
                    case SHORT -> TypeName.SHORT;
                    case INT -> TypeName.INT;
                    case LONG -> TypeName.LONG;
                    case CHAR -> TypeName.CHAR;
                    case FLOAT -> TypeName.FLOAT;
                    case DOUBLE -> TypeName.DOUBLE;
                    default -> throw new IllegalStateException();
                };
            }

            @Override
            public TypeName visitDeclared(DeclaredType t, @Nullable Void _p) {
                ClassName rawType = ClassName.get((TypeElement) t.asElement());
                TypeMirror enclosingType = t.getEnclosingType();

                TypeName enclosing = (enclosingType.getKind() != TypeKind.NONE)
                    && !t.asElement().getModifiers().contains(Modifier.STATIC)
                    ? enclosingType.accept(this, null)
                    : null;

                if (t.getTypeArguments().isEmpty() && !(enclosing instanceof ParameterizedTypeName)) {
                    return rawType;
                }

                List<TypeName> typeArgumentNames = t.getTypeArguments().stream()
                    .map(typeMirror -> get(typeMirror, typeVariables))
                    .toList();

                return enclosing instanceof ParameterizedTypeName
                    ? ((ParameterizedTypeName) enclosing)
                    .nestedClass(rawType.simpleName(), typeArgumentNames)
                    : ParameterizedTypeName.get(rawType, typeArgumentNames.toArray(TypeName[]::new));
            }

            @Override
            public TypeName visitError(ErrorType t, @Nullable Void _p) {
                return this.visitDeclared(t, null);
            }

            @Override
            public ArrayTypeName visitArray(ArrayType t, @Nullable Void _p) {
                return getArrayTypeName(t, typeVariables);
            }

            @Override
            public TypeName visitTypeVariable(javax.lang.model.type.TypeVariable t, @Nullable Void _p) {
                return getTypeVariableName(t, typeVariables);
            }

            @Override
            public TypeName visitWildcard(javax.lang.model.type.WildcardType t, @Nullable Void _p) {
                return getWildcardTypeName(t, typeVariables);
            }

            @Override
            public TypeName visitNoType(NoType t, Void _p) {
                if (t.getKind() == TypeKind.VOID) {
                    return TypeName.VOID;
                }
                return super.visitUnknown(t, null);
            }

            @Override
            protected TypeName defaultAction(TypeMirror e, Void _p) {
                throw new IllegalArgumentException("Unexpected type mirror: " + e);
            }

        }, null)
            .annotated(mirror.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList());
    }

    private static ArrayTypeName getArrayTypeName(ArrayType type, Map<TypeParameterElement, TypeVariableName> typeVariables) {
        TypeName componentType = get(type.getComponentType(), typeVariables);
        return ArrayTypeName.of(componentType)
            .annotated(type.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList());
    }

    private static TypeVariableName getTypeVariableName(TypeVariable typeVariable, Map<TypeParameterElement, TypeVariableName> typeVariables) {
        TypeParameterElement element = (TypeParameterElement) typeVariable.asElement();
        TypeVariableName typeVariableName = typeVariables.get(element);

        if (typeVariableName == null) {
            TypeName[] bounds = element.getBounds().stream()
                // TODO we might need to "cache" bounds in typeVariables here
                .map(typeMirror -> get(typeMirror, typeVariables))
                .filter(it -> !it.equals(ClassName.OBJECT))
                .toArray(TypeName[]::new);

            typeVariableName = TypeVariableName.get(element.getSimpleName().toString(), bounds)
                .annotated(typeVariable.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList());

            typeVariables.put(element, typeVariableName);
        }

        return typeVariableName;
    }

    private static WildcardTypeName getWildcardTypeName(WildcardType type, Map<TypeParameterElement, TypeVariableName> typeVariables) {
        TypeMirror extendsBound = type.getExtendsBound();
        if (extendsBound == null) {
            TypeMirror superBound = type.getSuperBound();
            if (superBound == null) {
                return WildcardTypeName.subtypeOf(Object.class);
            } else {
                return WildcardTypeName.supertypeOf(get(superBound, typeVariables))
                    .annotated(type.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList());
            }
        } else {
            return WildcardTypeName.subtypeOf(get(extendsBound, typeVariables))
                .annotated(type.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList());
        }
    }

    private AnnotatedTypeNameConverter() {}

}
