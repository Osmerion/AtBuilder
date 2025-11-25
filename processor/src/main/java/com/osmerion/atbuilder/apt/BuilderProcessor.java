package com.osmerion.atbuilder.apt;

import com.osmerion.atbuilder.Builder;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class BuilderProcessor extends AbstractProcessor {

    private static boolean isPrimaryCtor(Element element, List<? extends RecordComponentElement> componentElements) {
        if (element.getKind() != ElementKind.CONSTRUCTOR) return false;

        ExecutableElement executableElement = (ExecutableElement) element;
        if (executableElement.getParameters().size() != componentElements.size()) return false;

        for (int i = 0; i < executableElement.getParameters().size(); i++) {
            VariableElement parameterElement = executableElement.getParameters().get(i);
            RecordComponentElement componentElement = componentElements.get(i);

            if (!parameterElement.getSimpleName().contentEquals(componentElement.getSimpleName())) {
                return false;
            }
        }

        return true;
    }

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
                continue;
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

            Optional<? extends Element> optPrimaryCtor = typeElement.getEnclosedElements()
                .stream()
                .filter(it -> isPrimaryCtor(it, typeElement.getRecordComponents()))
                .findFirst();

            if (optPrimaryCtor.isEmpty()) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "No primary constructor found for record.", element);
                continue;
            }

            ExecutableElement primaryCtor = (ExecutableElement) optPrimaryCtor.get();

            Buildable buildable = new Buildable(
                ClassName.get(typeElement),
                typeElement.getTypeParameters(),
                typeElement.getRecordComponents().stream()
                    .map(component -> {
                        List<? extends AnnotationMirror> annotationMirrors = Stream.concat(
                            primaryCtor.getParameters().stream().filter(it -> it.getSimpleName().contentEquals(component.getSimpleName())).findFirst().orElseThrow().getAnnotationMirrors().stream(),
                            component.getAnnotationMirrors().stream()
                        ).toList();

                        return new Buildable.Component(
                            component.getSimpleName().toString(),
                            annotationMirrors,
                            component.asType()
                        );
                    })
                    .toList(),
                nullMarker
            );

            JavaFile builderFile = this.generator.generateBuilder(buildable);

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
