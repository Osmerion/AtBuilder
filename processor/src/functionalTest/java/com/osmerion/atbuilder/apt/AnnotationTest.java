package com.osmerion.atbuilder.apt;

import com.tschuchort.compiletesting.JvmCompilationResult;
import com.tschuchort.compiletesting.KotlinCompilation;
import com.tschuchort.compiletesting.SourceFile;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static com.osmerion.atbuilder.apt.util.StringFileObjectFactory.createJavaFileObject;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests that validate that annotations on record components are correctly copied to the generated builder.
 *
 * @author  Leon Linhart
 */
public final class AnnotationTest extends AbstractFunctionalTest {

    @Test
    public void testParameterAnnotation() {
        SourceFile cls = createJavaFileObject(
            "com/example/Foo.java",
            """
            package com.example;
            
            @com.osmerion.atbuilder.Builder
            @org.jspecify.annotations.NullMarked
            public record Foo(@ParamAnno String value) {}
            """
        );

        SourceFile paramAnno = createJavaFileObject(
            "com/example/ParamAnno.java",
            """
            package com.example;
            
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Target;
            
            @Target(ElementType.PARAMETER)
            public @interface ParamAnno {}
            """
        );

        JvmCompilationResult result = this.compile(cls, paramAnno);

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
                
                    /**
                     * Sets the value of the {@link Foo#value() value} component.
                     *
                     * @param value the value for the component
                     *
                     * @return  this builder instance
                     */
                    public FooBuilder value(@ParamAnno String value) {
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
                            this.value.getOrThrow()
                        );
                    }
                }
                """
            );
    }

}
