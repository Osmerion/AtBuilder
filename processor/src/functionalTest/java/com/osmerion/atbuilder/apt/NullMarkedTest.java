package com.osmerion.atbuilder.apt;

import com.tschuchort.compiletesting.JvmCompilationResult;
import com.tschuchort.compiletesting.KotlinCompilation;
import com.tschuchort.compiletesting.SourceFile;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static com.osmerion.atbuilder.apt.util.StringJavaFileObjectFactory.createJavaFileObject;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests that validate that the {@code @NullMarked} annotation is correctly applied to the generated
 * builder classes when the record or an enclosing type is annotated accordingly.
 *
 * @author  Leon Linhart
 */
public final class NullMarkedTest extends AbstractFunctionalTest {

    @Test
    public void testNullMarked() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            @org.jspecify.annotations.NullMarked
            public record Foo(String value) {}
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
                import org.jspecify.annotations.NullMarked;
                
                @NullMarked
                public final class FooBuilder {
                    private Omittable<String> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    /**
                     * TODO doc
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(String value) {
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
    public void testNullMarkedOnEnclosingType() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @org.jspecify.annotations.NullMarked
            public interface Foo {
                @com.osmerion.atbuilder.Builder
                record Bar(String value) {}
            }
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
                import org.jspecify.annotations.NullMarked;
                
                @NullMarked
                public final class BarBuilder {
                    private Omittable<String> value = Omittable.absent();
                
                    BarBuilder() {
                    }
                
                    /**
                     * TODO doc
                     *
                     * @return  this builder instance
                     */
                    public BarBuilder value(String value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    Foo.Bar build() {
                        return new Foo.Bar(
                            this.value.getOrThrow()
                
                        );
                    }
                }
                """
            );
    }

}
