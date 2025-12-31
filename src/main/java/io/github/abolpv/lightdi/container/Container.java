package io.github.abolpv.lightdi.container;

import io.github.abolpv.lightdi.annotation.*;
import io.github.abolpv.lightdi.exception.AmbiguousBeanException;
import io.github.abolpv.lightdi.exception.BeanNotFoundException;
import io.github.abolpv.lightdi.exception.CircularDependencyException;
import io.github.abolpv.lightdi.exception.ContainerException;
import io.github.abolpv.lightdi.proxy.ProxyFactory;
import io.github.abolpv.lightdi.resolver.CircularDependencyDetector;
import io.github.abolpv.lightdi.scanner.ClassScanner;
import io.github.abolpv.lightdi.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lightweight dependency injection container.
 * Manages bean registration, lifecycle scopes, and dependency resolution.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Constructor injection with @Inject</li>
 *   <li>Field injection with @Inject</li>
 *   <li>Singleton and Prototype scopes</li>
 *   <li>Named qualifiers for multiple implementations</li>
 *   <li>Lazy initialization with @Lazy</li>
 *   <li>Circular dependency detection</li>
 *   <li>Package scanning for auto-discovery</li>
 *   <li>PostConstruct lifecycle callback</li>
 * </ul>
 *
 * <h2>Example usage:</h2>
 * <pre>
 * Container container = Container.builder()
 *     .scan("com.example.services")
 *     .register(DatabaseConfig.class)
 *     .build();
 *
 * UserService service = container.get(UserService.class);
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class Container {

    private final Map<Class<?>, BeanDefinition> registry = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> namedRegistry = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> singletonCache = new ConcurrentHashMap<>();
    private final Map<String, Object> namedSingletonCache = new ConcurrentHashMap<>();
    private final CircularDependencyDetector circularDetector = new CircularDependencyDetector();
    private final ClassScanner classScanner = new ClassScanner();

    /**
     * Creates a new empty container.
     */
    public Container() {
    }

    /**
     * Creates a new container builder for fluent configuration.
     *
     * @return a new builder instance
     */
    public static ContainerBuilder builder() {
        return new ContainerBuilder();
    }

    // ==================== Registration Methods ====================

    /**
     * Registers a class in the container.
     * The class must be annotated with @Injectable.
     *
     * @param clazz the class to register
     * @param <T> the type of the class
     * @return this container for method chaining
     * @throws ContainerException if the class is not annotated with @Injectable
     */
    public <T> Container register(Class<T> clazz) {
        validateInjectable(clazz);
        BeanDefinition definition = createBeanDefinition(clazz);
        
        registry.put(clazz, definition);
        
        // Also register by name if @Named is present
        if (definition.hasName()) {
            namedRegistry.put(buildNamedKey(clazz, definition.getName()), definition);
        }
        
        // Register for all implemented interfaces
        registerForInterfaces(clazz, definition);
        
        return this;
    }

    /**
     * Registers an implementation for an interface or base class.
     *
     * @param interfaceClass the interface or base class
     * @param implementationClass the implementation class
     * @param <T> the interface type
     * @return this container for method chaining
     */
    public <T> Container register(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        validateInjectable(implementationClass);
        BeanDefinition definition = createBeanDefinition(implementationClass);
        
        registry.put(interfaceClass, definition);
        registry.put(implementationClass, definition);
        
        if (definition.hasName()) {
            namedRegistry.put(buildNamedKey(interfaceClass, definition.getName()), definition);
        }
        
        return this;
    }

    /**
     * Registers an implementation with a specific name.
     *
     * @param interfaceClass the interface or base class
     * @param implementationClass the implementation class
     * @param name the qualifier name
     * @param <T> the interface type
     * @return this container for method chaining
     */
    public <T> Container register(Class<T> interfaceClass, Class<? extends T> implementationClass, String name) {
        validateInjectable(implementationClass);
        Scope scope = determineScope(implementationClass);
        boolean lazy = implementationClass.isAnnotationPresent(Lazy.class);
        BeanDefinition definition = new BeanDefinition(implementationClass, scope, name, lazy);
        
        namedRegistry.put(buildNamedKey(interfaceClass, name), definition);
        registry.put(implementationClass, definition);
        
        return this;
    }

    /**
     * Registers a pre-created instance as a singleton.
     *
     * @param clazz the class type
     * @param instance the instance to register
     * @param <T> the type
     * @return this container for method chaining
     */
    public <T> Container registerInstance(Class<T> clazz, T instance) {
        BeanDefinition definition = new BeanDefinition(clazz, Scope.SINGLETON);
        registry.put(clazz, definition);
        singletonCache.put(clazz, instance);
        return this;
    }

    /**
     * Scans a package and registers all @Injectable classes.
     *
     * @param packageName the package to scan
     * @return this container for method chaining
     */
    public Container scan(String packageName) {
        Set<Class<?>> classes = classScanner.scanPackage(packageName);
        for (Class<?> clazz : classes) {
            register(clazz);
        }
        return this;
    }

    /**
     * Scans multiple packages and registers all @Injectable classes.
     *
     * @param packageNames the packages to scan
     * @return this container for method chaining
     */
    public Container scan(String... packageNames) {
        for (String packageName : packageNames) {
            scan(packageName);
        }
        return this;
    }

    // ==================== Retrieval Methods ====================

    /**
     * Retrieves an instance of the requested type.
     * Dependencies are automatically injected.
     *
     * @param clazz the class to retrieve
     * @param <T> the type of the class
     * @return an instance of the requested type
     * @throws BeanNotFoundException if the type is not registered
     * @throws CircularDependencyException if a circular dependency is detected
     * @throws ContainerException if instantiation fails
     */
    public <T> T get(Class<T> clazz) {
        BeanDefinition definition = registry.get(clazz);
        
        if (definition == null) {
            throw new BeanNotFoundException(clazz);
        }

        return clazz.cast(getInstance(clazz, definition));
    }

    /**
     * Retrieves a named instance of the requested type.
     *
     * @param clazz the class to retrieve
     * @param name the qualifier name
     * @param <T> the type of the class
     * @return an instance of the requested type
     * @throws BeanNotFoundException if no bean with the given name is found
     */
    public <T> T get(Class<T> clazz, String name) {
        String key = buildNamedKey(clazz, name);
        BeanDefinition definition = namedRegistry.get(key);
        
        if (definition == null) {
            throw new BeanNotFoundException(clazz, name);
        }

        return clazz.cast(getNamedInstance(key, definition));
    }

    /**
     * Retrieves an optional instance of the requested type.
     *
     * @param clazz the class to retrieve
     * @param <T> the type of the class
     * @return optional containing the instance, or empty if not registered
     */
    public <T> Optional<T> getOptional(Class<T> clazz) {
        try {
            return Optional.of(get(clazz));
        } catch (BeanNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves all instances that implement the given interface.
     *
     * @param interfaceClass the interface class
     * @param <T> the interface type
     * @return list of all implementations
     */
    public <T> List<T> getAll(Class<T> interfaceClass) {
        List<T> instances = new ArrayList<>();
        
        for (Map.Entry<Class<?>, BeanDefinition> entry : registry.entrySet()) {
            Class<?> key = entry.getKey();
            if (interfaceClass.isAssignableFrom(entry.getValue().getImplementationClass()) 
                && key != interfaceClass) {
                try {
                    instances.add(interfaceClass.cast(getInstance(key, entry.getValue())));
                } catch (Exception e) {
                    // Skip beans that fail to instantiate
                }
            }
        }
        
        return instances;
    }

    // ==================== Query Methods ====================

    /**
     * Checks if a type is registered in the container.
     *
     * @param clazz the class to check
     * @return true if registered, false otherwise
     */
    public boolean contains(Class<?> clazz) {
        return registry.containsKey(clazz);
    }

    /**
     * Checks if a named bean is registered.
     *
     * @param clazz the class type
     * @param name the qualifier name
     * @return true if registered, false otherwise
     */
    public boolean contains(Class<?> clazz, String name) {
        return namedRegistry.containsKey(buildNamedKey(clazz, name));
    }

    /**
     * Returns the scope of a registered bean.
     *
     * @param clazz the class to check
     * @return the scope of the bean
     * @throws BeanNotFoundException if the type is not registered
     */
    public Scope getScope(Class<?> clazz) {
        BeanDefinition definition = registry.get(clazz);
        if (definition == null) {
            throw new BeanNotFoundException(clazz);
        }
        return definition.getScope();
    }

    /**
     * Returns the number of registered beans.
     *
     * @return the count of registered beans
     */
    public int size() {
        return registry.size();
    }

    /**
     * Returns all registered bean types.
     *
     * @return set of registered types
     */
    public Set<Class<?>> getRegisteredTypes() {
        return new HashSet<>(registry.keySet());
    }

    // ==================== Lifecycle Methods ====================

    /**
     * Clears all singleton instances from the cache.
     * Bean definitions remain registered.
     */
    public void clearSingletons() {
        singletonCache.clear();
        namedSingletonCache.clear();
    }

    /**
     * Clears all registrations and singleton caches.
     */
    public void clear() {
        registry.clear();
        namedRegistry.clear();
        singletonCache.clear();
        namedSingletonCache.clear();
    }

    // ==================== Private Methods ====================

    private void validateInjectable(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Injectable.class)) {
            throw new ContainerException(
                "Class " + clazz.getName() + " must be annotated with @Injectable"
            );
        }
    }

    private BeanDefinition createBeanDefinition(Class<?> clazz) {
        Scope scope = determineScope(clazz);
        String name = determineName(clazz);
        boolean lazy = clazz.isAnnotationPresent(Lazy.class);
        return new BeanDefinition(clazz, scope, name, lazy);
    }

    private Scope determineScope(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Singleton.class)) {
            return Scope.SINGLETON;
        }
        return Scope.PROTOTYPE;
    }

    private String determineName(Class<?> clazz) {
        Named named = clazz.getAnnotation(Named.class);
        return named != null ? named.value() : null;
    }

    private void registerForInterfaces(Class<?> clazz, BeanDefinition definition) {
        List<Class<?>> interfaces = ReflectionUtils.getAllInterfaces(clazz);
        for (Class<?> iface : interfaces) {
            // Don't override existing registrations
            if (!registry.containsKey(iface)) {
                registry.put(iface, definition);
            }
        }
    }

    private String buildNamedKey(Class<?> clazz, String name) {
        return clazz.getName() + ":" + name;
    }

    private Object getInstance(Class<?> key, BeanDefinition definition) {
        if (definition.isSingleton()) {
            return singletonCache.computeIfAbsent(key, k -> createInstance(definition));
        }
        
        if (definition.isLazy() && key.isInterface()) {
            return createLazyProxyForClass(key, definition);
        }
        
        return createInstance(definition);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createLazyProxyForClass(Class<T> type, BeanDefinition definition) {
        return ProxyFactory.createLazyProxy(type, () -> type.cast(createInstance(definition)));
    }

    private Object getNamedInstance(String key, BeanDefinition definition) {
        if (definition.isSingleton()) {
            return namedSingletonCache.computeIfAbsent(key, k -> createInstance(definition));
        }
        return createInstance(definition);
    }

    private Object createInstance(BeanDefinition definition) {
        Class<?> clazz = definition.getImplementationClass();
        
        // Check for circular dependency
        circularDetector.push(clazz);
        
        try {
            // Create instance via constructor
            Object instance = createViaConstructor(clazz);
            
            // Inject fields
            injectFields(instance, clazz);
            
            // Call @PostConstruct
            invokePostConstruct(instance, clazz);
            
            return instance;
        } finally {
            circularDetector.pop(clazz);
        }
    }

    private Object createViaConstructor(Class<?> clazz) {
        Constructor<?> constructor = ReflectionUtils.findInjectableConstructor(clazz);
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveParameter(parameters[i]);
        }

        return ReflectionUtils.createInstance(constructor, args);
    }

    private Object resolveParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        Optional<String> qualifier = ReflectionUtils.getParameterQualifier(parameter);
        
        if (qualifier.isPresent()) {
            return get(type, qualifier.get());
        }
        
        return get(type);
    }

    private void injectFields(Object instance, Class<?> clazz) {
        List<Field> fields = ReflectionUtils.findInjectableFields(clazz);
        
        for (Field field : fields) {
            Object dependency = resolveField(field);
            ReflectionUtils.setField(instance, field, dependency);
        }
    }

    private Object resolveField(Field field) {
        Class<?> type = field.getType();
        Optional<String> qualifier = ReflectionUtils.getFieldQualifier(field);
        
        // Handle @Lazy on field
        if (field.isAnnotationPresent(Lazy.class) && type.isInterface()) {
            if (qualifier.isPresent()) {
                String name = qualifier.get();
                return createLazyProxyForField(type, name);
            }
            return createLazyProxyForField(type);
        }
        
        if (qualifier.isPresent()) {
            return get(type, qualifier.get());
        }
        
        return get(type);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createLazyProxyForField(Class<T> type, String name) {
        return ProxyFactory.createLazyProxy(type, () -> get(type, name));
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createLazyProxyForField(Class<T> type) {
        return ProxyFactory.createLazyProxy(type, () -> get(type));
    }

    private void invokePostConstruct(Object instance, Class<?> clazz) {
        Optional<Method> postConstruct = ReflectionUtils.findPostConstructMethod(clazz);
        postConstruct.ifPresent(method -> ReflectionUtils.invokeMethod(instance, method));
    }
}
