# AtBuilder

[![License](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg?style=for-the-badge&label=License)](https://github.com/Osmerion/AtBuilder/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.osmerion.atbuilder/atbuilder-runtime.svg?style=for-the-badge&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.osmerion.atbuilder/atbuilder-runtime)
[![JavaDoc](https://img.shields.io/maven-central/v/com.osmerion.atbuilder/atbuilder-runtime.svg?style=for-the-badge&label=JavaDoc&color=blue)](https://javadoc.io/doc/com.osmerion.atbuilder/atbuilder-runtime)
![Java](https://img.shields.io/badge/Java-17-green.svg?style=for-the-badge&color=b07219&logo=Java)

AtBuilder generates safe builders for your records with support for [nullable](https://jspecify.dev/)
and [omittable](https://github.com/Osmerion/Omittable) types.

```java
// com/example/Person.java
package com.example;

import com.osmerion.atbuilder.Builder;

@Builder
public record Person(
    String name,
    @Nullable String firstName,
    Omitable<String> nickname,
    Omittable<@Nullable String> country,
    String employmentStatus
) {
    
    public static PersonBuilder builder() {
        return new PersonBuilder()
            .employmentStatus("EMPLOYED");
    }
    
}

// Usage

Person person = Person.builder()
    .name("John Doe")
    .build(); // IllegalStateException: Missing required 'firstName' component 

Person person = Person.builder()
    .name("John Doe")
    .firstName(null)
    .build(); // Success!

Person person = Person.builder()
    .name("John Doe")
    .nickname(null) // NullPointerException: 'nickname' may not be null
    .build();

Person person = Person.builder()
    .name("John Doe")
    .country(null)
    .build(); // Success!
```


## Getting Started

AtBuilder ships in two artifacts:

- The `com.osmerion.atbuilder:atbuilder-runtime` artifact which contains the
  `@Builder` annotation and the runtime support library for the generated
  builders.
- The `com.osmerion.atbuilder:atbuilder-processor` artifact which contains the
  annotation processor that is responsible for generating the builders.

<details>
<summary>Setting up AtBuilder in Gradle</summary>

When using Gradle, the dependencies can simply be added to the respective configurations:

```kotlin
dependencies {
    implementation("com.osmerion.atbuilder:atbuilder-runtime:<version>")
    annotationProcessor("com.osmerion.atbuilder:atbuilder-processor:<version>")
}
```
</details>

<details>
<summary>Setting up AtBuilder in Maven</summary>

When using Maven, the `maven-compiler-plugin` needs to be configured to use the annotation processor:

```xml
<pluginManagement>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>${version}</version>
      <configuration>
        <annotationProcessorPaths>
          <annotationProcessorPath>
            <groupId>com.osmerion</groupId>
            <artifactId>atbuilder-processor</artifactId>
            <version>${version}</version>
          </annotationProcessorPath>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</pluginManagement>
<dependencies>
  <dependency>
    <groupId>com.osmerion.atbuilder</groupId>
    <artifactId>atbuilder-runtime</artifactId>
    <version>${version}</version>
  </dependency>
</dependencies>
```
</details>


## Usage

To use AtBuilder, simply annotate your record with the `@Builder` annotation.
The annotation processor will then generate a builder class for the record.

The builder is generated with a package-private constructor. It is recommended
to provide a static `builder` method in the annotated record that delegates to
the constructor. This method may be used to prepopulate the builder instance with
values that serve as default values.

```java
import com.osmerion.atbuilder.Builder;

@Builder
public record Person(
    String name,
    String country
) {
    
    public static PersonBuilder builder() {
        return new PersonBuilder()
            .country("Germany");
    }
    
}
```

### Omittable Support

An instance of the annotated record can be created by calling the `build` method
of a builder instance. This method checks if values for all required components
have been set and throws an `IllegalStateException` otherwise. `Omittable`
components are not required and initialized with `Omittable.absent()` when no
value is set.

```java
// com/example/Person.java
import com.osmerion.atbuilder.Builder;

@Builder
public record Person(
    String name,
    Omittable<Integer> age
) {
    
    public static PersonBuilder builder() {
        return new PersonBuilder()
            .name("Alice");
    }
    
}

// Usage

Person person = Person.builder()
    .build(); // IllegalStateException: Missing required 'name' component

Person person = Person.builder()
    .name("Alice")
    .build(); // Success! "age" is initialized with Omittable.absent()
```


### Annotations & JSpecify Support

Applicable annotations from record components are copied to the generated
builder. Notably, this preserves nullability information carried by annotations
(such as `Nullable`). Additionally, applicable `@NullMarked` and `@NullUnmarked`
annotations are copied to the generated builder class.


```java
// com/example/Person.java
package com.example;

import com.osmerion.atbuilder.Builder;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@Builder
@NullMarked
public record Person(String name, @Nullable String nickname) {}

// com/example/PersonBuilder.java (generated)
package com.example;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PersonBuilder {
    ...
    public PersonBuilder nickname(@Nullable String nickname) { ... }
}
```


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/current/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the project
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright 2025 Leon Linhart

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
