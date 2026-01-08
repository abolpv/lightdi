<p align="center">
  <img src="assets/logo.svg" alt="LightDI Logo" width="120" height="120">
</p>

<h1 align="center">LightDI</h1>

<p align="center">
  <strong>A lightweight, annotation-based dependency injection framework for Java</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#contributing">Contributing</a>
</p>

<p align="center">
  <a href="https://github.com/abolpv/lightdi/releases/tag/v1.1.0"><img src="https://img.shields.io/badge/Version-1.1.0-green?style=for-the-badge" alt="Version 1.1.0"></a>
  <a href="https://www.java.com"><img src="https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17+"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge" alt="License"></a>
</p>

<p align="center">
  <a href="https://github.com/abolpv/lightdi/actions/workflows/maven.yml"><img src="https://github.com/abolpv/lightdi/actions/workflows/maven.yml/badge.svg" alt="Build Status"></a>
  <a href="https://jitpack.io/#abolpv/lightdi"><img src="https://jitpack.io/v/abolpv/lightdi.svg" alt="JitPack"></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Zero-Dependencies-success?style=flat-square" alt="Zero Dependencies">
  <img src="https://img.shields.io/badge/100%25-Pure%20Java-orange?style=flat-square" alt="Pure Java">
  <img src="https://img.shields.io/badge/Lightweight-%3C50KB-blueviolet?style=flat-square" alt="Lightweight">
</p>

---

## 🎉 What's New in v1.1.0

> Released January 8, 2026

- **Method Injection** - Inject dependencies via setter methods with `@Inject`
- **@Primary Beans** - Mark default implementation when multiple candidates exist
- **@PreDestroy Lifecycle** - Cleanup callbacks on container shutdown
- **Conditional Registration** - `@ConditionalOnProperty`, `@ConditionalOnBean`, `@ConditionalOnMissingBean`

See [CHANGELOG.md](CHANGELOG.md) for full details.

---

## Why LightDI?

LightDI was created to provide a **simple**, **lightweight**, and **educational** dependency injection solution for Java applications. Unlike heavyweight frameworks like Spring or Guice, LightDI focuses on:

- 🪶 **Minimal footprint** - No external dependencies, pure Java implementation
- 📚 **Educational value** - Clean, readable source code perfect for learning DI internals
- ⚡ **Quick setup** - Get started in minutes, not hours
- 🎯 **Focused functionality** - Does one thing well: dependency injection

---

## Features

| Feature | Description |
|---------|-------------|
| 🔧 **Constructor Injection** | Automatic dependency resolution via constructors |
| 📍 **Field Injection** | Inject dependencies directly into fields with `@Inject` |
| 💉 **Method Injection** | Inject dependencies via setter methods with `@Inject` |
| 🔄 **Singleton Scope** | Share single instance across all injection points |
| 🆕 **Prototype Scope** | Create new instance for each injection |
| 🏷️ **Named Qualifiers** | Support multiple implementations of same interface |
| ⭐ **Primary Beans** | Mark default bean with `@Primary` for ambiguous types |
| 😴 **Lazy Loading** | Delay bean creation until first use |
| 🔍 **Circular Detection** | Fail-fast with clear error messages |
| 📦 **Package Scanning** | Auto-discover injectable classes |
| 🚀 **PostConstruct** | Lifecycle callbacks after injection |
| 🛑 **PreDestroy** | Cleanup callbacks on container shutdown |
| ⚡ **Conditional Registration** | Register beans based on properties or other beans |
| 🛠️ **Fluent Builder** | Clean, readable container configuration |

---

## Installation

### Maven

Add JitPack repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.abolpv</groupId>
        <artifactId>lightdi</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

### Gradle

Add to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.abolpv:lightdi:1.1.0'
}
```

### Gradle Kotlin DSL

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.abolpv:lightdi:1.1.0")
}
```

### Manual Installation

Clone and build from source:

```bash
git clone https://github.com/abolpv/lightdi.git
cd lightdi
mvn clean install
```

---

## Quick Start

### 1. Define Your Classes

```java
import io.github.abolpv.lightdi.annotation.*;

@Injectable
public class UserRepository {
    public User findById(Long id) {
        // Database logic here
        return new User(id, "John Doe");
    }
}

@Injectable
public class UserService {
    private final UserRepository repository;
    
    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    public User getUser(Long id) {
        return repository.findById(id);
    }
}
```

### 2. Create Container & Get Beans

```java
import io.github.abolpv.lightdi.container.Container;

public class Application {
    public static void main(String[] args) {
        // Create container and register classes
        Container container = new Container();
        container.register(UserRepository.class);
        container.register(UserService.class);
        
        // Get instance with dependencies injected
        UserService service = container.get(UserService.class);
        User user = service.getUser(1L);
        
        System.out.println("Hello, " + user.getName());
    }
}
```

### 3. Or Use Fluent Builder

```java
Container container = Container.builder()
    .scan("com.example.services")
    .scan("com.example.repositories")
    .register(AppConfig.class)
    .build();

UserService service = container.get(UserService.class);
```

---

## Documentation

### Annotations Reference

| Annotation | Target | Description |
|------------|--------|-------------|
| `@Injectable` | Class | Marks class as managed by the container |
| `@Inject` | Constructor, Field, Method | Marks injection point for dependencies |
| `@Singleton` | Class | Creates single shared instance |
| `@Primary` | Class | Marks bean as preferred when multiple candidates exist |
| `@Lazy` | Class, Field | Delays instantiation until first use |
| `@Named` | Class, Field, Parameter | Qualifies beans for disambiguation |
| `@PostConstruct` | Method | Invoked after all dependencies injected |
| `@PreDestroy` | Method | Invoked when container shuts down |
| `@ConditionalOnProperty` | Class | Register only when property matches |
| `@ConditionalOnBean` | Class | Register only when specified beans exist |
| `@ConditionalOnMissingBean` | Class | Register only when specified beans are absent |
| `@ComponentScan` | Class | Specifies packages to scan |

---

### Scopes

#### Prototype (Default)

New instance created for each request:

```java
@Injectable
public class RequestHandler {
    // New instance every time container.get() is called
}
```

#### Singleton

Single instance shared across all injection points:

```java
@Injectable
@Singleton
public class DatabaseConnection {
    // Same instance always returned
    private final Connection connection;
    
    public DatabaseConnection() {
        this.connection = createConnection();
    }
}
```

---

### Named Bindings

When you have multiple implementations of an interface:

```java
// Define interface
public interface MessageSender {
    void send(String message);
}

// Implementation 1
@Injectable
@Named("email")
public class EmailSender implements MessageSender {
    @Override
    public void send(String message) {
        // Send via email
    }
}

// Implementation 2
@Injectable
@Named("sms")
public class SmsSender implements MessageSender {
    @Override
    public void send(String message) {
        // Send via SMS
    }
}
```

**Inject by qualifier:**

```java
@Injectable
public class NotificationService {
    @Inject
    @Named("email")
    private MessageSender emailSender;
    
    @Inject
    @Named("sms")
    private MessageSender smsSender;
    
    public void notifyAll(String message) {
        emailSender.send(message);
        smsSender.send(message);
    }
}
```

**Or via constructor:**

```java
@Injectable
public class AlertService {
    private final MessageSender sender;
    
    @Inject
    public AlertService(@Named("email") MessageSender sender) {
        this.sender = sender;
    }
}
```

**Or programmatically:**

```java
Container container = Container.builder()
    .bind(MessageSender.class, EmailSender.class).named("email")
    .bind(MessageSender.class, SmsSender.class).named("sms")
    .build();

MessageSender email = container.get(MessageSender.class, "email");
MessageSender sms = container.get(MessageSender.class, "sms");
```

---

### Lazy Loading

Delay expensive initialization until first use:

```java
@Injectable
@Lazy
public class ExpensiveService implements ServiceInterface {
    public ExpensiveService() {
        // Heavy initialization - only runs when first method called
        loadLargeDataset();
    }
}
```

**On specific injection points:**

```java
@Injectable
public class MyService {
    @Inject
    @Lazy
    private ExpensiveService expensive;
    
    public void doWork() {
        // ExpensiveService created here on first access
        expensive.process();
    }
}
```

> ⚠️ **Note:** Lazy loading requires an interface type for proxy creation.

---

### PostConstruct Lifecycle

Execute initialization logic after all dependencies are injected:

```java
@Injectable
@Singleton
public class CacheService {
    @Inject
    private DatabaseService database;
    
    private Map<String, Object> cache;
    
    @PostConstruct
    public void initialize() {
        // Called after database is injected
        cache = new HashMap<>();
        loadInitialData();
    }
    
    private void loadInitialData() {
        // Load from database into cache
    }
}
```

---

### Field Injection

Alternative to constructor injection:

```java
@Injectable
public class OrderService {
    @Inject
    private UserService userService;
    
    @Inject
    private PaymentService paymentService;
    
    @Inject
    private InventoryService inventoryService;
    
    public Order createOrder(Long userId, List<Item> items) {
        User user = userService.getUser(userId);
        // ... order logic
    }
}
```

> 💡 **Best Practice:** Prefer constructor injection for required dependencies. Use field injection for optional dependencies or to avoid constructor bloat.

---

### Method Injection

Inject dependencies via setter methods:

```java
@Injectable
public class NotificationService {
    private EmailSender emailSender;
    private SmsSender smsSender;

    @Inject
    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Inject
    public void setSmsSender(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    // Or inject multiple dependencies in one method
    @Inject
    public void setDependencies(LogService log, MetricsService metrics) {
        this.log = log;
        this.metrics = metrics;
    }
}
```

Method injection works with `@Named` qualifiers on parameters:

```java
@Injectable
public class AlertService {
    private MessageSender sender;

    @Inject
    public void setSender(@Named("email") MessageSender sender) {
        this.sender = sender;
    }
}
```

---

### Primary Beans

When multiple implementations of an interface exist, use `@Primary` to designate the default:

```java
interface CacheService {
    void put(String key, Object value);
}

@Injectable
@Primary  // This will be injected by default
public class RedisCacheService implements CacheService {
    @Override
    public void put(String key, Object value) {
        // Redis implementation
    }
}

@Injectable
public class InMemoryCacheService implements CacheService {
    @Override
    public void put(String key, Object value) {
        // In-memory implementation
    }
}

@Injectable
public class DataService {
    @Inject
    private CacheService cache;  // RedisCacheService will be injected
}
```

> ⚠️ **Note:** If multiple `@Primary` beans exist for the same type, an `AmbiguousBeanException` is thrown.

---

### PreDestroy Lifecycle

Execute cleanup logic when the container shuts down:

```java
@Injectable
@Singleton
public class DatabaseConnection {
    private Connection connection;

    @PostConstruct
    public void connect() {
        connection = DriverManager.getConnection(url);
    }

    @PreDestroy
    public void disconnect() {
        // Called when container.shutdown() is invoked
        if (connection != null) {
            connection.close();
        }
    }
}

// Application code
Container container = Container.builder()
    .register(DatabaseConnection.class)
    .build();

// ... use container ...

// On shutdown - invokes @PreDestroy in reverse creation order
container.shutdown();
```

> 💡 **Note:** `@PreDestroy` methods are called in **reverse order** of bean creation (LIFO), ensuring dependencies are cleaned up properly.

---

### Conditional Registration

Register beans conditionally based on properties or other beans:

#### @ConditionalOnProperty

```java
// Register only when cache.enabled=true
@Injectable
@ConditionalOnProperty("cache.enabled")
public class RedisCacheService implements CacheService { }

// Match specific value
@Injectable
@ConditionalOnProperty(value = "cache.type", havingValue = "memcached")
public class MemcachedCacheService implements CacheService { }

// Register when property is missing (fallback)
@Injectable
@ConditionalOnProperty(value = "feature.new", matchIfMissing = true)
public class DefaultFeatureService implements FeatureService { }
```

#### @ConditionalOnBean

```java
// Register only when UserRepository exists
@Injectable
@ConditionalOnBean(UserRepository.class)
public class UserService {
    @Inject
    private UserRepository repository;
}
```

#### @ConditionalOnMissingBean

```java
// Register only when no CacheService exists (fallback pattern)
@Injectable
@ConditionalOnMissingBean(CacheService.class)
public class InMemoryCacheService implements CacheService { }
```

#### Using with ContainerBuilder

```java
Container container = Container.builder()
    .property("cache.enabled", "true")
    .property("db.type", "postgresql")
    .register(RedisCacheService.class)      // Registered (cache.enabled=true)
    .register(InMemoryCacheService.class)   // Skipped (CacheService exists)
    .build();
```

---

### Container API Reference

```java
// =============== Registration ===============

// Register a single class
container.register(MyService.class);

// Register interface with implementation
container.register(UserRepository.class, JpaUserRepository.class);

// Register with qualifier name
container.register(MessageSender.class, EmailSender.class, "email");

// Register pre-created instance
container.registerInstance(Config.class, loadedConfig);

// Scan package for @Injectable classes
container.scan("com.example.services");


// =============== Retrieval ===============

// Get instance (dependencies auto-injected)
MyService service = container.get(MyService.class);

// Get named instance
MessageSender sender = container.get(MessageSender.class, "email");

// Get optional (no exception if not found)
Optional<MyService> optional = container.getOptional(MyService.class);

// Get all implementations of interface
List<MessageSender> allSenders = container.getAll(MessageSender.class);


// =============== Query ===============

// Check if registered
boolean exists = container.contains(MyService.class);
boolean namedExists = container.contains(MessageSender.class, "email");

// Get scope
Scope scope = container.getScope(MyService.class);

// Count registered beans
int count = container.size();

// Get all registered types
Set<Class<?>> types = container.getRegisteredTypes();


// =============== Properties ===============

// Set a property (for conditional registration)
container.setProperty("cache.enabled", "true");

// Get a property
String value = container.getProperty("cache.enabled");
String withDefault = container.getProperty("cache.type", "inmemory");

// Check if property exists
boolean hasProperty = container.hasProperty("cache.enabled");


// =============== Lifecycle ===============

// Shutdown container (invokes @PreDestroy)
container.shutdown();

// Check if shutdown was called
boolean isShutdown = container.isShutdown();

// Clear singleton cache (definitions remain)
container.clearSingletons();

// Clear everything
container.clear();
```

---

### Builder API

```java
Container container = Container.builder()
    // Scan packages
    .scan("com.example.services")
    .scan("com.example.repositories", "com.example.controllers")

    // Set properties (for conditional registration)
    .property("cache.enabled", "true")
    .property("db.type", "postgresql")

    // Register individual classes
    .register(AppConfig.class)
    .register(SecurityService.class)

    // Bind interfaces to implementations
    .bind(UserRepository.class, JpaUserRepository.class)
    .bind(CacheService.class, RedisCacheService.class)

    // Named bindings
    .bind(MessageSender.class, EmailSender.class).named("email")
    .bind(MessageSender.class, SmsSender.class).named("sms")

    // Pre-created instances
    .instance(Configuration.class, loadConfig())

    // Build the container
    .build();
```

---

### Exception Handling

LightDI provides clear, descriptive exceptions:

| Exception | Cause |
|-----------|-------|
| `BeanNotFoundException` | Requested bean not registered in container |
| `CircularDependencyException` | Circular dependency detected (A → B → A) |
| `AmbiguousBeanException` | Multiple candidates found without qualifier |
| `ContainerException` | General container errors (instantiation, etc.) |

**Example handling:**

```java
try {
    UserService service = container.get(UserService.class);
} catch (BeanNotFoundException e) {
    System.err.println("Bean not found: " + e.getRequestedType());
} catch (CircularDependencyException e) {
    System.err.println("Circular dependency: " + e.getDependencyChain());
} catch (ContainerException e) {
    System.err.println("Container error: " + e.getMessage());
}
```

---

## Project Structure

```
lightdi/
├── src/
│   ├── main/java/io/github/abolpv/lightdi/
│   │   ├── annotation/          # @Injectable, @Inject, @Singleton, etc.
│   │   ├── container/           # Container, ContainerBuilder, BeanDefinition
│   │   ├── resolver/            # Dependency resolution, circular detection
│   │   ├── proxy/               # Lazy loading proxy implementation
│   │   ├── scanner/             # Classpath scanning
│   │   ├── exception/           # Custom exceptions
│   │   └── util/                # Reflection utilities
│   └── test/java/               # Unit tests
├── assets/                      # Logo and images
├── pom.xml                      # Maven configuration
├── LICENSE                      # Apache 2.0 License
└── README.md                    # This file
```

---

## Best Practices

1. **Prefer Constructor Injection**
   - Makes dependencies explicit and immutable
   - Easier to test with mock objects
   - Fails fast if dependencies missing

2. **Use Interfaces for Flexibility**
   - Enables lazy loading (proxy-based)
   - Easier to swap implementations
   - Better for testing

3. **Keep Singletons Stateless**
   - Or ensure proper synchronization
   - Avoid mutable shared state

4. **Avoid Circular Dependencies**
   - Redesign if detected
   - Consider using `@Lazy` as temporary fix

5. **Use @Named for Multiple Implementations**
   - Clear disambiguation
   - Self-documenting code

---

## Requirements

- **Java 17** or higher
- No additional dependencies

---

## Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/abolpv/lightdi.git
cd lightdi

# Build the project
mvn clean compile

# Run tests
mvn test

# Install to local repository
mvn install
```

---

## License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

```
Copyright 2025-2026 Abolfazl Azizi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

## Author

<p align="center">
  <strong>Abolfazl Azizi</strong>
  <br>
  <a href="https://github.com/abolpv">GitHub</a>
</p>

---

<p align="center">
  Made with ❤️ for the Java community
  <br><br>
  ⭐ Star this repo if you find it useful!
</p>
