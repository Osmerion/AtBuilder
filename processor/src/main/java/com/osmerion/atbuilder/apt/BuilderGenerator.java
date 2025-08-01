package com.osmerion.atbuilder.apt;

import com.osmerion.omittable.Omittable;
import com.palantir.javapoet.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
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
    private static final ClassName NULLUNMARKED_CLASS_NAME = ClassName.get(NullUnmarked.class);

    private static final ClassName OMITTABLE_CLASS_NAME = ClassName.get(Omittable.class);

    private final Types types;

    BuilderGenerator(Types types) {
        this.types = types;
    }

    public JavaFile generateBuilder(Buildable buildable) {
        String packageName = buildable.className().packageName();
        ClassName builderClassName = ClassName.get(packageName, buildable.className().simpleName() + "Builder");

        TypeSpec.Builder bTypeSpec = TypeSpec.classBuilder(builderClassName)
            .addJavadoc(
                """
                A builder for {@link $T} instances.
                """,
                buildable.className()
            )
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addTypeVariables(
                buildable.typeParameters().stream()
                    .map(this::annotatedTypeVariableName)
                    .toList()
            )
            .addFields(buildable.components().stream().map(this::generateField).toList())
            .addMethod(MethodSpec.constructorBuilder().build())
            .addMethod(this.generateCopyConstructor(buildable))
            .addMethods(buildable.components().stream().map(component -> this.generateMethod(buildable, component, builderClassName)).toList())
            .addMethod(this.generateBuildMethod(buildable));

        switch (buildable.nullMarker()) {
            case MARKED -> bTypeSpec.addAnnotation(NULLMARKED_CLASS_NAME);
            case UNMARKED -> bTypeSpec.addAnnotation(NULLUNMARKED_CLASS_NAME);
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
        TypeName parametrizedBuildable = this.getParametrizedTypeName(buildable.className(), buildable);

        MethodSpec.Builder bMethodSpec = MethodSpec.methodBuilder("build")
            .addJavadoc(
                """
                Builds a new {@link $T} instance with the values set in this builder.
                
                @return the newly created instance
                
                @throws IllegalStateException   if any of the required components are not set
                """,
                buildable.className()
            )
            .addModifiers(Modifier.PUBLIC)
            .returns(parametrizedBuildable);

        StringJoiner joiner = new StringJoiner(",\n");
        for (Buildable.Component component : buildable.components()) {
            if (this.isOmittable(component)) {
                joiner.add("this." + component.name());
            } else {
                joiner.add("this." + component.name() + ".orElseThrow(() -> new IllegalStateException(\"Component '" + component.name() + "' must be set\"))");
            }
        }

        bMethodSpec.addCode("return new $T(\n" + joiner.toString().indent(4) + ");", parametrizedBuildable);
        return bMethodSpec.build();
    }

    private MethodSpec generateCopyConstructor(Buildable buildable) {
        MethodSpec.Builder bMethodSpec = MethodSpec.constructorBuilder()
            .addParameter(this.getParametrizedTypeName(buildable.className(), buildable), "other");

        for (Buildable.Component component : buildable.components()) {
            if (this.isOmittable(component)) {
                bMethodSpec.addStatement("this.$N = other.$N()", component.name(), component.name());
            } else {
                bMethodSpec.addStatement("this.$N = $T.of(other.$N())", component.name(), OMITTABLE_CLASS_NAME, component.name());
            }
        }

        return bMethodSpec.build();
    }

    private FieldSpec generateField(Buildable.Component component) {
        List<AnnotationSpec> typeAnnotationSpecs = component.annotationMirrors().stream()
            .filter(annotationMirror -> this.isAnnotationApplicableToAny(annotationMirror.getAnnotationType(), Set.of(ElementType.TYPE_USE)))
            .map(AnnotationSpec::get)
            .toList();

        TypeName fieldTypeName;
        if (this.isOmittable(component)) {
            fieldTypeName = TypeName.get(component.type()).annotated(typeAnnotationSpecs);
        } else {
            fieldTypeName = ParameterizedTypeName.get(ClassName.get(Omittable.class), TypeName.get(component.type()).box().annotated(typeAnnotationSpecs));
        }

        return FieldSpec.builder(fieldTypeName, component.name(), Modifier.PRIVATE)
            .initializer("$T.absent()", OMITTABLE_CLASS_NAME)
            .build();
    }

    private MethodSpec generateMethod(Buildable buildable, Buildable.Component component, ClassName builderClassName) {
        List<AnnotationSpec> paramAnnotationSpecs = component.annotationMirrors().stream()
            .filter(annotationMirror -> this.isAnnotationApplicableToAny(annotationMirror.getAnnotationType(), Set.of(ElementType.PARAMETER)))
            .map(AnnotationSpec::get)
            .toList();

        List<AnnotationSpec> typeAnnotationSpecs = component.type().getAnnotationMirrors().stream()
            .filter(annotationMirror -> this.isAnnotationApplicableToAny(annotationMirror.getAnnotationType(), Set.of(ElementType.TYPE_USE)))
            .map(AnnotationSpec::get)
            .toList();

        MethodSpec.Builder bMethodSpec = MethodSpec.methodBuilder(component.name())
            .addJavadoc(
                """
                Sets the value of the {@link $T#$N() $N} component.
                
                @param $N the value for the component
                
                @return  this builder instance
                """,
                buildable.className(),
                component.name(),
                component.name(),
                component.name()
            )
            .addModifiers(Modifier.PUBLIC)
            .returns(this.getParametrizedTypeName(builderClassName, buildable))
            .addParameter(
                ParameterSpec.builder(TypeName.get(component.type()).annotated(typeAnnotationSpecs), component.name())
                    .addAnnotations(paramAnnotationSpecs)
                    .build()
            );

        if (this.isNullable(component) || component.type().getKind().isPrimitive()) {
            bMethodSpec.addStatement("this.$N = $T.of($N)", component.name(), OMITTABLE_CLASS_NAME, component.name());
        } else {
            bMethodSpec.addStatement("this.$N = $T.of($T.requireNonNull($N, \"Component '$N' may not be null\"))", component.name(), OMITTABLE_CLASS_NAME, OBJECTS_CLASS_NAME, component.name(), component.name());
        }

        bMethodSpec.addStatement("return this");

        return bMethodSpec.build();
    }

    private TypeName getParametrizedTypeName(ClassName baseName, Buildable buildable) {
        if (buildable.typeParameters().isEmpty()) {
            return baseName;
        }

        return ParameterizedTypeName.get(
            baseName,
            buildable.typeParameters()
                .stream()
                .map(typeParameterElement -> TypeName.get(typeParameterElement.asType()))
                .toArray(TypeName[]::new)
        );
    }

    private boolean isAnnotationApplicableToAny(DeclaredType annotationType, Set<?> targets) {
        Target targetAnnotation = annotationType.asElement().getAnnotation(Target.class);

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
