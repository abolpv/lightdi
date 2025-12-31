package io.github.abolpv.lightdi.container;

/**
 * Holds metadata about a registered bean.
 * Stores the implementation class, scope, and optional qualifier name.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class BeanDefinition {
    
    private final Class<?> implementationClass;
    private final Scope scope;
    private final String name;
    private final boolean lazy;
    
    public BeanDefinition(Class<?> implementationClass, Scope scope) {
        this(implementationClass, scope, null, false);
    }
    
    public BeanDefinition(Class<?> implementationClass, Scope scope, String name) {
        this(implementationClass, scope, name, false);
    }
    
    public BeanDefinition(Class<?> implementationClass, Scope scope, String name, boolean lazy) {
        this.implementationClass = implementationClass;
        this.scope = scope;
        this.name = name;
        this.lazy = lazy;
    }
    
    public Class<?> getImplementationClass() {
        return implementationClass;
    }
    
    public Scope getScope() {
        return scope;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isLazy() {
        return lazy;
    }
    
    public boolean isSingleton() {
        return scope == Scope.SINGLETON;
    }
    
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
    
    @Override
    public String toString() {
        return "BeanDefinition{" +
               "class=" + implementationClass.getSimpleName() +
               ", scope=" + scope +
               (hasName() ? ", name='" + name + "'" : "") +
               (lazy ? ", lazy=true" : "") +
               '}';
    }
}
