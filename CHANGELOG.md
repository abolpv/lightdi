# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2026-01-08

### Added

- **Method Injection Support** ([#1](https://github.com/abolpv/lightdi/issues/1))
  - Inject dependencies via setter methods annotated with `@Inject`
  - Support for `@Named` qualifiers on method parameters
  - Multiple parameters per method supported

- **@Primary Annotation** ([#2](https://github.com/abolpv/lightdi/issues/2))
  - Mark a bean as the preferred candidate when multiple implementations exist
  - Automatically selected when injecting interface without qualifier
  - Throws `AmbiguousBeanException` if multiple `@Primary` beans exist for same type

- **@PreDestroy Lifecycle Hook** ([#3](https://github.com/abolpv/lightdi/issues/3))
  - Execute cleanup logic when container shuts down
  - `container.shutdown()` invokes `@PreDestroy` methods in reverse creation order (LIFO)
  - `container.isShutdown()` to check shutdown state
  - Idempotent shutdown - safe to call multiple times

- **Conditional Bean Registration** ([#4](https://github.com/abolpv/lightdi/issues/4))
  - `@ConditionalOnProperty` - register bean only when property matches
    - `value`/`name` - property key to check
    - `havingValue` - exact value to match
    - `matchIfMissing` - register when property is absent
  - `@ConditionalOnBean` - register only when specified beans exist
  - `@ConditionalOnMissingBean` - register only when specified beans are absent
  - Property management API: `setProperty()`, `getProperty()`, `hasProperty()`
  - `ContainerBuilder.property()` for fluent configuration

### Changed

- Updated `@Inject` annotation to support methods (in addition to constructors and fields)
- `BeanDefinition` now tracks `primary` status
- `Container` now maintains singleton creation order for proper `@PreDestroy` invocation

## [1.0.0] - 2025-01-01

### Added

- Initial release of LightDI framework
- Constructor injection with `@Inject`
- Field injection with `@Inject`
- `@Injectable` annotation for marking managed classes
- `@Singleton` and prototype (default) scopes
- `@Named` qualifier for multiple implementations
- `@Lazy` loading with proxy-based deferred initialization
- `@PostConstruct` lifecycle callback
- `@ComponentScan` for package scanning
- Circular dependency detection with clear error messages
- Fluent `ContainerBuilder` API
- Interface-to-implementation binding
- Pre-created instance registration
- Optional bean retrieval with `getOptional()`
- Get all implementations with `getAll()`
- Zero external dependencies
- Java 17+ support

[Unreleased]: https://github.com/abolpv/lightdi/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/abolpv/lightdi/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/abolpv/lightdi/releases/tag/v1.0.0
