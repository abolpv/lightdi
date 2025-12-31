package io.github.abolpv.lightdi.resolver;

/**
 * Interface for dependency resolution strategies.
 * Implementations handle different types of injection (constructor, field, method).
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public interface DependencyResolver {
    
    /**
     * Resolves dependencies for the given target object.
     *
     * @param target the object to inject dependencies into
     * @param clazz the class of the target
     */
    void resolve(Object target, Class<?> clazz);
}
