### 0.2.0

_Released 2025 Jul 28_

#### Improvements

- The annotation processor can now be used on the processor path (opposed to
  only the processor module path). [[GH-4](https://github.com/Osmerion/AtBuilder/issues/4)]
- The generated builder now provides a copy constructor that allows creating a
  builder from an existing buildable.

#### Fixes

- The annotation processor now supports primitive types. [[GH-3](https://github.com/Osmerion/AtBuilder/issues/3)]
- Type arguments are now properly carried over.

#### Breaking Changes

- Updated to [Omittable 0.2.0](https://github.com/Osmerion/Omittable/releases/tag/v0.2.0), which introduced breaking changes.


---

### 0.1.0

_Released 2025 Jul 22_

#### Overview

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
