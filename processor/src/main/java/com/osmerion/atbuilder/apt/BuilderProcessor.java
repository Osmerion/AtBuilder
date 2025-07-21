package com.osmerion.atbuilder.apt;

import com.osmerion.atbuilder.Builder;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

public final class BuilderProcessor extends AbstractProcessor {

    private @Nullable Elements elements;
    private @Nullable Filer filer;
    private @Nullable BuilderGenerator generator;
    private @Nullable Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.elements = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.generator = new BuilderGenerator(processingEnv.getTypeUtils());
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        assert this.elements != null && this.filer != null && this.generator != null && this.messager != null;
        for (Element element : roundEnv.getElementsAnnotatedWith(Builder.class)) {
            if (element.getKind() != ElementKind.RECORD) {
                AnnotationMirror annotationMirror = element.getAnnotationMirrors().stream()
                        .filter(am -> am.getAnnotationType().toString().equals(Builder.class.getCanonicalName()))
                        .findFirst()
                        .orElseThrow();

                this.messager.printMessage(Diagnostic.Kind.ERROR, "@Builder may only be applied to records.", element, annotationMirror);
            }

            TypeElement typeElement = (TypeElement) element;
            NullMarker nullMarker = NullMarker.NONE;

            {
                Element currentElement = typeElement;
                while (currentElement.getKind() != ElementKind.PACKAGE) {
                    if (currentElement.getAnnotation(NullMarked.class) != null) {
                        nullMarker = NullMarker.MARKED;
                        break;
                    } else if (currentElement.getAnnotation(NullUnmarked.class) != null) {
                        nullMarker = NullMarker.UNMARKED;
                        break;
                    }

                    currentElement = currentElement.getEnclosingElement();
                }
            }

            Buildable buildable = new Buildable(
                ClassName.get(typeElement),
                typeElement.getTypeParameters(),
                typeElement.getRecordComponents().stream()
                    .map(component -> new Buildable.Component(
                        component.getSimpleName().toString(),
                        component.asType()
                    ))
                    .toList(),
                nullMarker
            );

            JavaFile builderFile = this.generator.generateBuilder(buildable);
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Generated builder file: " + builderFile);

            try {
                builderFile.writeTo(this.filer);
            } catch (IOException e) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write builder file: " + e.getMessage(), element);
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Builder.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
