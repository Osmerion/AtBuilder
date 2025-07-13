import org.jspecify.annotations.NullMarked;

/**
 * This module provides the runtime support for the builder type generation.
 *
 * @since   0.1.0
 */
@NullMarked
module com.osmerion.atbuilder {

    requires transitive com.osmerion.omittable;
    requires transitive org.jspecify;

    exports com.osmerion.atbuilder;

}
