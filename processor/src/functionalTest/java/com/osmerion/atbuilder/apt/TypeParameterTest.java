package com.osmerion.atbuilder.apt;

import com.tschuchort.compiletesting.JvmCompilationResult;
import com.tschuchort.compiletesting.KotlinCompilation;
import com.tschuchort.compiletesting.SourceFile;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static com.osmerion.atbuilder.apt.util.StringJavaFileObjectFactory.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TODO doc
 *
 * @author  Leon Linhart
 */
public final class TypeParameterTest extends AbstractFunctionalTest {

    @Test
    public void testTypeParameter() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            public record Foo<T>(T value) {}
            """
        );

        JvmCompilationResult result = this.compile(cls);

        assertThat(result.getExitCode()).isEqualTo(KotlinCompilation.ExitCode.OK);
        assertThat(result.getSourcesGeneratedByAnnotationProcessor())
            .hasSize(1)
            .first(InstanceOfAssertFactories.FILE)
            .content()
            .isEqualTo(
                """
                package com.example;
                
                import com.osmerion.omittable.Omittable;
                import java.util.Objects;
                
                public final class FooBuilder<T> {
                    private Omittable<T> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    /**
                     * TODO doc
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(T value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    Foo build() {
                        return new Foo(
                            this.value.getOrThrow()
                
                        );
                    }
                }
                """
            );
    }

    @Test
    public void testTypeParameterWithBound() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            public record Foo<T extends CharSequence>(T value) {}
            """
        );

        JvmCompilationResult result = this.compile(cls);

        assertThat(result.getExitCode()).isEqualTo(KotlinCompilation.ExitCode.OK);
        assertThat(result.getSourcesGeneratedByAnnotationProcessor())
            .hasSize(1)
            .first(InstanceOfAssertFactories.FILE)
            .content()
            .isEqualTo(
                """
                package com.example;
                
                import com.osmerion.omittable.Omittable;
                import java.util.Objects;
                
                public final class FooBuilder<T extends CharSequence> {
                    private Omittable<T> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    /**
                     * TODO doc
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(T value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    Foo build() {
                        return new Foo(
                            this.value.getOrThrow()
                
                        );
                    }
                }
                """
            );
    }

    @Test
    public void testTypeParameterWithAnnotatedBound() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            public record Foo<T extends @org.jspecify.annotations.Nullable CharSequence>(T value) {}
            """
        );

        JvmCompilationResult result = this.compile(cls);

        assertThat(result.getExitCode()).isEqualTo(KotlinCompilation.ExitCode.OK);
        assertThat(result.getSourcesGeneratedByAnnotationProcessor())
            .hasSize(1)
            .first(InstanceOfAssertFactories.FILE)
            .content()
            .isEqualTo(
                """
                package com.example;
                
                import com.osmerion.omittable.Omittable;
                import java.util.Objects;
                import org.jspecify.annotations.Nullable;
                
                public final class FooBuilder<T extends @Nullable CharSequence> {
                    private Omittable<T> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    /**
                     * TODO doc
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(T value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    Foo build() {
                        return new Foo(
                            this.value.getOrThrow()
                
                        );
                    }
                }
                """
            );
    }

    @Test
    public void testAnnotatedTypeParameter() {
        SourceFile marker = createJavaFileObject(
            "com/example/Marker.java",
            """
            package com.example;
            
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Target;
            
            @Target(ElementType.TYPE_PARAMETER)
            public @interface Marker {}
            """
        );

        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            public record Foo<@Marker T extends CharSequence>(T value) {}
            """
        );

        JvmCompilationResult result = this.compile(marker, cls);

        assertThat(result.getExitCode()).isEqualTo(KotlinCompilation.ExitCode.OK);
        assertThat(result.getSourcesGeneratedByAnnotationProcessor())
            .hasSize(1)
            .first(InstanceOfAssertFactories.FILE)
            .content()
            .isEqualTo(
                """
                package com.example;
                
                import com.osmerion.omittable.Omittable;
                import java.util.Objects;
                
                public final class FooBuilder<@Marker T extends CharSequence> {
                    private Omittable<T> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    /**
                     * TODO doc
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(T value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    Foo build() {
                        return new Foo(
                            this.value.getOrThrow()
                
                        );
                    }
                }
                """
            );
    }

}
