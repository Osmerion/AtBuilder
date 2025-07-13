import com.osmerion.atbuilder.apt.BuilderProcessor;
import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.Processor;

/**
 * This module provides the annotation processor for the builder type generation.
 *
 * @since   0.1.0
 */
@NullMarked
module com.osmerion.atbuilder.apt {

    requires java.compiler;

    requires com.osmerion.atbuilder;
    requires com.palantir.javapoet;
    requires org.jspecify;

    provides Processor with BuilderProcessor;

}
