package io.github.abolpv.lightdi.exception;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when a circular dependency is detected during bean resolution.
 * Circular dependencies occur when class A depends on B, and B depends on A
 * (directly or transitively).
 *
 * <p>Example of circular dependency:</p>
 * <pre>
 * class A {
 *     {@literal @}Inject A(B b) { }  // A needs B
 * }
 *
 * class B {
 *     {@literal @}Inject B(A a) { }  // B needs A → Circular!
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class CircularDependencyException extends ContainerException {

    private final List<Class<?>> dependencyChain;

    public CircularDependencyException(List<Class<?>> chain) {
        super(buildMessage(chain));
        this.dependencyChain = chain;
    }

    private static String buildMessage(List<Class<?>> chain) {
        String chainStr = chain.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.joining(" → "));
        return "Circular dependency detected: " + chainStr;
    }

    /**
     * Returns the chain of dependencies that form the cycle.
     *
     * @return list of classes in the dependency chain
     */
    public List<Class<?>> getDependencyChain() {
        return dependencyChain;
    }
}
