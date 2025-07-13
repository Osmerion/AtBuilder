package com.osmerion.atbuilder.apt.util;

import com.tschuchort.compiletesting.SourceFile;
import org.intellij.lang.annotations.Language;

public final class StringJavaFileObjectFactory {

    public static SourceFile createJavaFileObject(String fqName, @Language("java") String content) {
        return SourceFile.Companion.java(fqName, content, true);
    }

}
