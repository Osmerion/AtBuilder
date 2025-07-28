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
