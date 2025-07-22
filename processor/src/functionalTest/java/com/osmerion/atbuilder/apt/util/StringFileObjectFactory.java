package com.osmerion.atbuilder.apt.util;

import com.tschuchort.compiletesting.SourceFile;
import org.intellij.lang.annotations.Language;

public final class StringFileObjectFactory {

    public static SourceFile createJavaFileObject(String fqName, @Language("java") String content) {
        return SourceFile.Companion.java(fqName, content, true);
    }

    public static SourceFile createKotlinFileObject(String fqName, @Language("kotlin") String content) {
        return SourceFile.Companion.kotlin(fqName, content, true);
    }

}
