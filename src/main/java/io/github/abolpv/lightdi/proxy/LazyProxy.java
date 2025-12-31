package io.github.abolpv.lightdi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Invocation handler for lazy-loaded beans.
 * Delays the actual bean creation until the first method is called.
 *
 * <p>This is used internally by the container when @Lazy annotation is present.
 * The actual bean instance is created on the first method invocation.</p>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class LazyProxy implements InvocationHandler {
    
    private final Supplier<Object> beanSupplier;
    private volatile Object instance;
    private final Object lock = new Object();
    
    /**
     * Creates a new lazy proxy.
     *
     * @param beanSupplier supplier that creates the actual bean instance
     */
    public LazyProxy(Supplier<Object> beanSupplier) {
        this.beanSupplier = beanSupplier;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods specially
        if (method.getName().equals("equals") && method.getParameterCount() == 1) {
            return proxy == args[0];
        }
        if (method.getName().equals("hashCode") && method.getParameterCount() == 0) {
            return System.identityHashCode(proxy);
        }
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            if (instance == null) {
                return "LazyProxy[not initialized]";
            }
            return "LazyProxy[" + instance.toString() + "]";
        }
        
        // Lazy initialization with double-checked locking
        Object target = instance;
        if (target == null) {
            synchronized (lock) {
                target = instance;
                if (target == null) {
                    target = beanSupplier.get();
                    instance = target;
                }
            }
        }

        method.setAccessible(true);
        return method.invoke(target, args);
    }
    
    /**
     * Checks if the actual instance has been created.
     *
     * @return true if the bean has been initialized
     */
    public boolean isInitialized() {
        return instance != null;
    }
    
    /**
     * Forces initialization of the lazy bean.
     *
     * @return the initialized instance
     */
    public Object getTarget() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = beanSupplier.get();
                }
            }
        }
        return instance;
    }
}
