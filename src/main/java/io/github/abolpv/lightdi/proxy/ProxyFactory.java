package io.github.abolpv.lightdi.proxy;

import io.github.abolpv.lightdi.exception.ContainerException;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * Factory for creating proxy instances.
 * Supports creating lazy proxies for interface-based beans.
 *
 * <p>Note: Lazy proxies can only be created for interface types.
 * Classes without interfaces cannot be lazily proxied using JDK proxies.</p>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public final class ProxyFactory {
    
    private ProxyFactory() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Creates a lazy proxy for the given interface.
     *
     * @param interfaceType the interface to proxy
     * @param beanSupplier supplier that creates the actual bean
     * @param <T> the interface type
     * @return a proxy instance
     * @throws ContainerException if the type is not an interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T createLazyProxy(Class<T> interfaceType, Supplier<T> beanSupplier) {
        if (!interfaceType.isInterface()) {
            throw new ContainerException(
                "Lazy proxy can only be created for interfaces. " +
                interfaceType.getName() + " is not an interface."
            );
        }
        
        LazyProxy handler = new LazyProxy(() -> beanSupplier.get());
        
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[] { interfaceType },
            handler
        );
    }
    
    /**
     * Creates a lazy proxy for multiple interfaces.
     *
     * @param interfaces the interfaces to proxy
     * @param beanSupplier supplier that creates the actual bean
     * @return a proxy instance
     */
    public static Object createLazyProxy(Class<?>[] interfaces, Supplier<Object> beanSupplier) {
        for (Class<?> iface : interfaces) {
            if (!iface.isInterface()) {
                throw new ContainerException(
                    "All types must be interfaces for lazy proxy. " +
                    iface.getName() + " is not an interface."
                );
            }
        }
        
        if (interfaces.length == 0) {
            throw new ContainerException("At least one interface is required for lazy proxy.");
        }
        
        LazyProxy handler = new LazyProxy(beanSupplier);
        
        return Proxy.newProxyInstance(
            interfaces[0].getClassLoader(),
            interfaces,
            handler
        );
    }
    
    /**
     * Checks if the given object is a lazy proxy.
     *
     * @param object the object to check
     * @return true if the object is a lazy proxy
     */
    public static boolean isLazyProxy(Object object) {
        if (object == null) {
            return false;
        }
        if (!Proxy.isProxyClass(object.getClass())) {
            return false;
        }
        return Proxy.getInvocationHandler(object) instanceof LazyProxy;
    }
    
    /**
     * Gets the underlying LazyProxy handler if the object is a lazy proxy.
     *
     * @param object the proxy object
     * @return the LazyProxy handler, or null if not a lazy proxy
     */
    public static LazyProxy getLazyProxyHandler(Object object) {
        if (!isLazyProxy(object)) {
            return null;
        }
        return (LazyProxy) Proxy.getInvocationHandler(object);
    }
    
    /**
     * Checks if a lazy proxy has been initialized.
     *
     * @param object the proxy object
     * @return true if initialized, false if not a proxy or not initialized
     */
    public static boolean isInitialized(Object object) {
        LazyProxy handler = getLazyProxyHandler(object);
        return handler != null && handler.isInitialized();
    }
}
