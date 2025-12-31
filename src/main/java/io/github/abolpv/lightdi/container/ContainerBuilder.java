package io.github.abolpv.lightdi.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating and configuring Container instances.
 * Provides a fluent API for container setup.
 *
 * <h2>Example usage:</h2>
 * <pre>
 * Container container = Container.builder()
 *     .scan("com.example.services")
 *     .scan("com.example.repositories")
 *     .register(AppConfig.class)
 *     .bind(MessageSender.class, EmailSender.class).named("email")
 *     .bind(MessageSender.class, SmsSender.class).named("sms")
 *     .build();
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class ContainerBuilder {
    
    private final List<String> packagesToScan = new ArrayList<>();
    private final List<Class<?>> classesToRegister = new ArrayList<>();
    private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
    private final List<NamedBinding> namedBindings = new ArrayList<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    
    private BindingBuilder<?> pendingBinding;
    
    /**
     * Adds a package to be scanned for @Injectable classes.
     *
     * @param packageName the package to scan
     * @return this builder
     */
    public ContainerBuilder scan(String packageName) {
        completePendingBinding();
        packagesToScan.add(packageName);
        return this;
    }
    
    /**
     * Adds multiple packages to be scanned.
     *
     * @param packageNames the packages to scan
     * @return this builder
     */
    public ContainerBuilder scan(String... packageNames) {
        completePendingBinding();
        for (String pkg : packageNames) {
            packagesToScan.add(pkg);
        }
        return this;
    }
    
    /**
     * Registers a class with the container.
     *
     * @param clazz the class to register
     * @return this builder
     */
    public ContainerBuilder register(Class<?> clazz) {
        completePendingBinding();
        classesToRegister.add(clazz);
        return this;
    }
    
    /**
     * Starts a binding definition for an interface to implementation.
     *
     * @param interfaceClass the interface to bind
     * @param <T> the interface type
     * @return a binding builder for further configuration
     */
    public <T> BindingBuilder<T> bind(Class<T> interfaceClass) {
        completePendingBinding();
        BindingBuilder<T> builder = new BindingBuilder<>(this, interfaceClass);
        pendingBinding = builder;
        return builder;
    }
    
    /**
     * Binds an interface directly to an implementation.
     *
     * @param interfaceClass the interface
     * @param implementationClass the implementation
     * @param <T> the interface type
     * @return a binding builder for optional naming
     */
    public <T> BindingBuilder<T> bind(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        completePendingBinding();
        BindingBuilder<T> builder = new BindingBuilder<>(this, interfaceClass);
        builder.to(implementationClass);
        pendingBinding = builder;
        return builder;
    }
    
    /**
     * Registers a pre-created instance.
     *
     * @param clazz the class type
     * @param instance the instance
     * @param <T> the type
     * @return this builder
     */
    public <T> ContainerBuilder instance(Class<T> clazz, T instance) {
        completePendingBinding();
        instances.put(clazz, instance);
        return this;
    }
    
    /**
     * Builds and returns the configured container.
     *
     * @return the configured Container
     */
    @SuppressWarnings("unchecked")
    public Container build() {
        completePendingBinding();
        
        Container container = new Container();
        
        // Scan packages first
        for (String pkg : packagesToScan) {
            container.scan(pkg);
        }
        
        // Register individual classes
        for (Class<?> clazz : classesToRegister) {
            container.register(clazz);
        }
        
        // Register bindings
        for (Map.Entry<Class<?>, Class<?>> entry : bindings.entrySet()) {
            registerBinding(container, entry.getKey(), entry.getValue());
        }
        
        // Register named bindings
        for (NamedBinding binding : namedBindings) {
            registerNamedBinding(container, binding.interfaceClass, binding.implementationClass, binding.name);
        }
        
        // Register instances
        for (Map.Entry<Class<?>, Object> entry : instances.entrySet()) {
            registerInstance(container, entry.getKey(), entry.getValue());
        }
        
        return container;
    }
    
    @SuppressWarnings("unchecked")
    private <T> void registerBinding(Container container, Class<T> interfaceClass, Class<?> implClass) {
        container.register(interfaceClass, (Class<? extends T>) implClass);
    }
    
    @SuppressWarnings("unchecked")
    private <T> void registerNamedBinding(Container container, Class<T> interfaceClass, Class<?> implClass, String name) {
        container.register(interfaceClass, (Class<? extends T>) implClass, name);
    }
    
    @SuppressWarnings("unchecked")
    private <T> void registerInstance(Container container, Class<T> clazz, Object instance) {
        container.registerInstance(clazz, (T) instance);
    }
    
    void completePendingBinding() {
        if (pendingBinding != null) {
            pendingBinding.complete();
            pendingBinding = null;
        }
    }
    
    void addBinding(Class<?> interfaceClass, Class<?> implementationClass) {
        bindings.put(interfaceClass, implementationClass);
    }
    
    void addNamedBinding(Class<?> interfaceClass, Class<?> implementationClass, String name) {
        namedBindings.add(new NamedBinding(interfaceClass, implementationClass, name));
    }
    
    /**
     * Builder for configuring a single binding.
     *
     * @param <T> the interface type
     */
    public static class BindingBuilder<T> {
        private final ContainerBuilder parent;
        private final Class<T> interfaceClass;
        private Class<? extends T> implementationClass;
        private String name;
        private boolean completed = false;
        
        BindingBuilder(ContainerBuilder parent, Class<T> interfaceClass) {
            this.parent = parent;
            this.interfaceClass = interfaceClass;
        }
        
        /**
         * Specifies the implementation class.
         *
         * @param implementationClass the implementation
         * @return this binding builder
         */
        public BindingBuilder<T> to(Class<? extends T> implementationClass) {
            this.implementationClass = implementationClass;
            return this;
        }
        
        /**
         * Specifies a name qualifier for this binding.
         *
         * @param name the qualifier name
         * @return the parent builder
         */
        public ContainerBuilder named(String name) {
            this.name = name;
            complete();
            return parent;
        }
        
        /**
         * Continues with another binding.
         *
         * @param interfaceClass the next interface to bind
         * @param <U> the interface type
         * @return a new binding builder
         */
        public <U> BindingBuilder<U> bind(Class<U> interfaceClass) {
            complete();
            return parent.bind(interfaceClass);
        }
        
        /**
         * Scans a package.
         *
         * @param packageName the package to scan
         * @return the parent builder
         */
        public ContainerBuilder scan(String packageName) {
            complete();
            return parent.scan(packageName);
        }
        
        /**
         * Builds the container.
         *
         * @return the configured container
         */
        public Container build() {
            complete();
            return parent.build();
        }
        
        void complete() {
            if (!completed && implementationClass != null) {
                if (name != null) {
                    parent.addNamedBinding(interfaceClass, implementationClass, name);
                } else {
                    parent.addBinding(interfaceClass, implementationClass);
                }
                completed = true;
            }
        }
    }
    
    private static class NamedBinding {
        final Class<?> interfaceClass;
        final Class<?> implementationClass;
        final String name;
        
        NamedBinding(Class<?> interfaceClass, Class<?> implementationClass, String name) {
            this.interfaceClass = interfaceClass;
            this.implementationClass = implementationClass;
            this.name = name;
        }
    }
}
