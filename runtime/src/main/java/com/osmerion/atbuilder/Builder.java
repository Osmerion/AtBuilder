package com.osmerion.atbuilder;

import com.osmerion.omittable.Omittable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be applied to a record to instruct the AtBuilder annotation processor to generate a builder for
 * the record.
 *
 * <p>The builder is generated with a package-private constructor. It is recommended to provide a static {@code builder}
 * method in the annotated record that delegates to the constructor. This method may be used to prepopulate the builder
 * instance with values that serve as default values.</p>
 *
 * <pre>{@code
 * @Builder
 * public record Person(String name, String country) {
 *
 *     public static PersonBuilder builder() {
 *       return new PersonBuilder()
 *          .country("Germany");
 *     }
 *
 * }
 * }</pre>
 *
 * <p>An instance of the annotated record can be created by calling the {@code build} method of a builder instance. This
 * method checks if values for all required components have been set and throws an {@link IllegalStateException}
 * otherwise. {@link Omittable} components are not required and initialized with {@link Omittable#absent()} when no value
 * is set.</p>
 *
 * <pre>{@code
 * @Builder
 * public record Person(String name, Omittable<Integer> age) { ... }
 *
 *
 * Person person = Person.builder()
 *     .build(); // throws IllegalStateException
 *
 * Person person = Person.builder()
 *     .name("Alice")
 *     .build(); // Successfully creates a Person instance with age set to Omittable.absent()
 * }</pre>
 *
 * <p>Applicable annotations from record components are copied to the generated builder. Notably, this preserves
 * nullability information carried by annotations (such as {@link Nullable}). Additionally, if the annotated record is
 * {@link NullMarked null-marked} or {@link NullUnmarked null-unmarked}, the generated builder will also be annotated
 * accordingly.</p>
 *
 * <pre>{@code
 * @Builder
 * @NullMarked
 * public record Person(String name, @Nullable String nickname) {}
 *
 * @NullMarked
 * public class PersonBuilder {
 *
 *     ...
 *
 *     public PersonBuilder nickname(@Nullable String nickname) { ... }
 *
 * }
 * }</pre>
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Builder {}
