package com.osmerion.atbuilder.apt;

import com.palantir.javapoet.ClassName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

record Buildable(
    ClassName className,
    List<? extends TypeParameterElement> typeParameters,
    List<Component> components,
    NullMarker nullMarker
) {

    record Component(
        String name,
        List<? extends AnnotationMirror> annotationMirrors,
        TypeMirror type
    ) {}

}
