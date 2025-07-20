package com.osmerion.atbuilder.apt;

import com.osmerion.omittable.Omittable;
import com.palantir.javapoet.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

final class BuilderGenerator {

    private static final ClassName OBJECTS_CLASS_NAME = ClassName.get(Objects.class);

    private static final ClassName NULLABLE_CLASS_NAME = ClassName.get(Nullable.class);
    private static final ClassName NULLMARKED_CLASS_NAME = ClassName.get(NullMarked.class);

    private static final ClassName OMITTABLE_CLASS_NAME = ClassName.get(Omittable.class);

    private final Elements elements;
    private final Types types;

    BuilderGenerator(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    public JavaFile generateBuilder(Buildable buildable) {
        String packageName = buildable.className().packageName();
        ClassName builderClassName = ClassName.get(packageName, buildable.className().simpleName() + "Builder");

        TypeSpec.Builder bTypeSpec = TypeSpec.classBuilder(builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addTypeVariables(
                buildable.typeParameters().stream()
                    .map(this::annotatedTypeVariableName)
                    .toList()
            )
            .addFields(buildable.components().stream().map(this::generateField).toList())
            .addMethod(MethodSpec.constructorBuilder().build())
            .addMethods(buildable.components().stream().map(component -> this.generateMethod(component, builderClassName)).toList())
            .addMethod(this.generateBuildMethod(buildable));

        if (buildable.isNullMarked()) {
            bTypeSpec.addAnnotation(NULLMARKED_CLASS_NAME);
        }

        return JavaFile.builder(packageName, bTypeSpec.build())
            .indent("    ")
            .skipJavaLangImports(true)
            .build();
    }

    private TypeVariableName annotatedTypeVariableName(TypeParameterElement element) {
        String name = element.getSimpleName().toString();
        List<TypeName> bounds = element.getBounds().stream()
            .map(AnnotatedTypeNameConverter::get)
            .toList();

        return TypeVariableName.get(name, bounds.toArray(new TypeName[0]))
            .annotated(element.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList());
    }

    private MethodSpec generateBuildMethod(Buildable buildable) {
        MethodSpec.Builder bMethodSpec = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(buildable.className());

        StringJoiner joiner = new StringJoiner(",\n");
        for (Buildable.Component component : buildable.components()) {
            if (this.isOmittable(component)) {
                joiner.add("this." + component.name());
            } else {
                // TODO Consider adding orElseThrow() method to Omittable
//                joiner.add("this." + component.name() + ".orElseThrow(() -> new IllegalStateException(\"Component '" + component.name() + "' is not set\"))");
                joiner.add("this." + component.name() + ".getOrThrow()");
            }
        }

        bMethodSpec.addCode("return new $T(\n" + joiner.toString().indent(4) + ");", buildable.className());
        return bMethodSpec.build();
    }

    private FieldSpec generateField(Buildable.Component component) {
        List<AnnotationSpec> annotationSpecs = component.type().getAnnotationMirrors().stream()
            .filter(annotationMirror -> this.isAnnotationApplicableToAny(annotationMirror.getAnnotationType(), Set.of(ElementType.FIELD, ElementType.TYPE_USE)))
            .map(AnnotationSpec::get)
            .toList();

        TypeName fieldTypeName;
        if (this.isOmittable(component)) {
            fieldTypeName = TypeName.get(component.type()).annotated(annotationSpecs);
        } else {
            fieldTypeName = ParameterizedTypeName.get(ClassName.get(Omittable.class), TypeName.get(component.type()).annotated(annotationSpecs));
        }

        return FieldSpec.builder(fieldTypeName, component.name(), Modifier.PRIVATE)
            .initializer("$T.absent()", OMITTABLE_CLASS_NAME)
            .build();
    }

    private MethodSpec generateMethod(Buildable.Component component, ClassName builderClassName) {
        List<AnnotationSpec> annotationSpecs = component.type().getAnnotationMirrors().stream()
            .filter(annotationMirror -> this.isAnnotationApplicableToAny(annotationMirror.getAnnotationType(), Set.of(ElementType.PARAMETER, ElementType.TYPE_USE)))
            .map(AnnotationSpec::get)
            .toList();

        MethodSpec.Builder bMethodSpec = MethodSpec.methodBuilder(component.name())
            .addJavadoc(
                """
                TODO doc
                
                @return  this builder instance
                """
            )
            .addModifiers(Modifier.PUBLIC)
            .returns(builderClassName)
            .addParameter(TypeName.get(component.type()).annotated(annotationSpecs), component.name());

        if (this.isNullable(component)) {
            bMethodSpec.addStatement("this.$N = $T.of($N)", component.name(), OMITTABLE_CLASS_NAME, component.name());
        } else {
            bMethodSpec.addStatement("this.$N = $T.of($T.requireNonNull($N, \"Component '$N' may not be null\"))", component.name(), OMITTABLE_CLASS_NAME, OBJECTS_CLASS_NAME, component.name(), component.name());
        }

        bMethodSpec.addStatement("return this");

        return bMethodSpec.build();
    }

    private boolean isAnnotationApplicableToAny(DeclaredType annotationType, Set<?> targets) {
        Target targetAnnotation = annotationType.getAnnotation(Target.class);

        if (targetAnnotation == null) {
            return targets.contains(ElementType.TYPE)
                || targets.contains(ElementType.FIELD)
                || targets.contains(ElementType.METHOD)
                || targets.contains(ElementType.PARAMETER)
                || targets.contains(ElementType.CONSTRUCTOR)
                || targets.contains(ElementType.LOCAL_VARIABLE)
                || targets.contains(ElementType.ANNOTATION_TYPE)
                || targets.contains(ElementType.PACKAGE);
        }

        return Set.of(targetAnnotation.value()).stream().anyMatch(targets::contains);
    }

    private boolean isOmittable(Buildable.Component component) {
        return Objects.equals(OMITTABLE_CLASS_NAME, TypeName.get(this.types.erasure(component.type())));
    }

    private boolean isNullable(Buildable.Component component) {
        return component.type().getAnnotationMirrors().stream()
            .anyMatch(annotationMirror -> Objects.equals(NULLABLE_CLASS_NAME, TypeName.get(annotationMirror.getAnnotationType())));
    }

}
