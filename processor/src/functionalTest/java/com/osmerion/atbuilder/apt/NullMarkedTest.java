package com.osmerion.atbuilder.apt;

import com.tschuchort.compiletesting.JvmCompilationResult;
import com.tschuchort.compiletesting.KotlinCompilation;
import com.tschuchort.compiletesting.SourceFile;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static com.osmerion.atbuilder.apt.util.StringFileObjectFactory.*;
import static org.assertj.core.api.Assertions.*;

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
                
                /**
                 * A builder for {@link Foo} instances.
                 */
                @NullMarked
                public final class FooBuilder {
                    private Omittable<String> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    FooBuilder(Foo other) {
                        this.value = Omittable.of(other.value());
                    }
                
                    /**
                     * Sets the value of the {@link Foo#value() value} component.
                     *
                     * @param value the value for the component
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(String value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    /**
                     * Builds a new {@link Foo} instance with the values set in this builder.
                     *
                     * @return the newly created instance
                     *
                     * @throws IllegalStateException   if any of the required components are not set
                     */
                    public Foo build() {
                        return new Foo(
                            this.value.orElseThrow(() -> new IllegalStateException("Component 'value' must be set"))
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
                
                /**
                 * A builder for {@link Foo.Bar} instances.
                 */
                @NullMarked
                public final class BarBuilder {
                    private Omittable<String> value = Omittable.absent();
                
                    BarBuilder() {
                    }
                
                    BarBuilder(Foo.Bar other) {
                        this.value = Omittable.of(other.value());
                    }
                
                    /**
                     * Sets the value of the {@link Foo.Bar#value() value} component.
                     *
                     * @param value the value for the component
                     *
                     * @return  this builder instance
                     */
                    public BarBuilder value(String value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    /**
                     * Builds a new {@link Foo.Bar} instance with the values set in this builder.
                     *
                     * @return the newly created instance
                     *
                     * @throws IllegalStateException   if any of the required components are not set
                     */
                    public Foo.Bar build() {
                        return new Foo.Bar(
                            this.value.orElseThrow(() -> new IllegalStateException("Component 'value' must be set"))
                        );
                    }
                }
                """
            );
    }


    @Test
    public void testNullUnmarked() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            @org.jspecify.annotations.NullUnmarked
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
                import org.jspecify.annotations.NullUnmarked;
                
                /**
                 * A builder for {@link Foo} instances.
                 */
                @NullUnmarked
                public final class FooBuilder {
                    private Omittable<String> value = Omittable.absent();
                
                    FooBuilder() {
                    }
                
                    FooBuilder(Foo other) {
                        this.value = Omittable.of(other.value());
                    }
                
                    /**
                     * Sets the value of the {@link Foo#value() value} component.
                     *
                     * @param value the value for the component
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(String value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    /**
                     * Builds a new {@link Foo} instance with the values set in this builder.
                     *
                     * @return the newly created instance
                     *
                     * @throws IllegalStateException   if any of the required components are not set
                     */
                    public Foo build() {
                        return new Foo(
                            this.value.orElseThrow(() -> new IllegalStateException("Component 'value' must be set"))
                        );
                    }
                }
                """
            );
    }

    @Test
    public void testNullUnmarkedOverride() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @org.jspecify.annotations.NullMarked
            public interface Foo {
                @com.osmerion.atbuilder.Builder
                @org.jspecify.annotations.NullUnmarked
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
                import org.jspecify.annotations.NullUnmarked;
                
                /**
                 * A builder for {@link Foo.Bar} instances.
                 */
                @NullUnmarked
                public final class BarBuilder {
                    private Omittable<String> value = Omittable.absent();
                
                    BarBuilder() {
                    }
                
                    BarBuilder(Foo.Bar other) {
                        this.value = Omittable.of(other.value());
                    }
                
                    /**
                     * Sets the value of the {@link Foo.Bar#value() value} component.
                     *
                     * @param value the value for the component
                     *
                     * @return  this builder instance
                     */
                    public BarBuilder value(String value) {
                        this.value = Omittable.of(Objects.requireNonNull(value, "Component 'value' may not be null"));
                        return this;
                    }
                
                    /**
                     * Builds a new {@link Foo.Bar} instance with the values set in this builder.
                     *
                     * @return the newly created instance
                     *
                     * @throws IllegalStateException   if any of the required components are not set
                     */
                    public Foo.Bar build() {
                        return new Foo.Bar(
                            this.value.orElseThrow(() -> new IllegalStateException("Component 'value' must be set"))
                        );
                    }
                }
                """
            );
    }

}
