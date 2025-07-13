package com.osmerion.atbuilder.apt;

import com.palantir.javapoet.ClassName;

import javax.lang.model.type.TypeMirror;
import java.util.List;

record Buildable(
    ClassName className,
    List<Component> components,
    boolean isNullMarked
) {

    record Component(
        String name,
        TypeMirror type
    ) {}

}
