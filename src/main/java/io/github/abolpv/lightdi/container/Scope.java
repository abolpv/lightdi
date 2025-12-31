package io.github.abolpv.lightdi.container;

/**
 * Defines the lifecycle scope of a bean.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public enum Scope {
    
    /**
     * A new instance is created every time the bean is requested.
     * This is the default scope.
     */
    PROTOTYPE,
    
    /**
     * Only one instance is created and shared across all injection points.
     */
    SINGLETON
}
