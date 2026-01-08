package io.github.abolpv.lightdi.container;

/**
 * Holds metadata about a registered bean.
 * Stores the implementation class, scope, optional qualifier name, and primary status.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class BeanDefinition {

    private final Class<?> implementationClass;
    private final Scope scope;
    private final String name;
    private final boolean lazy;
    private final boolean primary;

    public BeanDefinition(Class<?> implementationClass, Scope scope) {
        this(implementationClass, scope, null, false, false);
    }

    public BeanDefinition(Class<?> implementationClass, Scope scope, String name) {
        this(implementationClass, scope, name, false, false);
    }

    public BeanDefinition(Class<?> implementationClass, Scope scope, String name, boolean lazy) {
        this(implementationClass, scope, name, lazy, false);
    }

    public BeanDefinition(Class<?> implementationClass, Scope scope, String name, boolean lazy, boolean primary) {
        this.implementationClass = implementationClass;
        this.scope = scope;
        this.name = name;
        this.lazy = lazy;
        this.primary = primary;
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

    public boolean isPrimary() {
        return primary;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
               "class=" + implementationClass.getSimpleName() +
               ", scope=" + scope +
               (hasName() ? ", name='" + name + "'" : "") +
               (lazy ? ", lazy=true" : "") +
               (primary ? ", primary=true" : "") +
               '}';
    }
}
