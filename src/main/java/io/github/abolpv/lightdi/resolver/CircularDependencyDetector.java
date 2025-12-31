package io.github.abolpv.lightdi.resolver;

import io.github.abolpv.lightdi.exception.CircularDependencyException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects circular dependencies during bean resolution.
 * Uses a thread-local resolution stack to track the current dependency chain.
 *
 * <p>Circular dependency example:</p>
 * <pre>
 * A → B → C → A  (circular!)
 * </pre>
 *
 * <p>Usage:</p>
 * <pre>
 * detector.push(A.class);   // Start resolving A
 * detector.push(B.class);   // A needs B
 * detector.push(C.class);   // B needs C
 * detector.push(A.class);   // C needs A → throws CircularDependencyException!
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class CircularDependencyDetector {
    
    private final ThreadLocal<Set<Class<?>>> resolutionStack = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<List<Class<?>>> resolutionPath = ThreadLocal.withInitial(ArrayList::new);
    
    /**
     * Pushes a class onto the resolution stack.
     * Throws CircularDependencyException if the class is already being resolved.
     *
     * @param clazz the class being resolved
     * @throws CircularDependencyException if a cycle is detected
     */
    public void push(Class<?> clazz) {
        Set<Class<?>> stack = resolutionStack.get();
        List<Class<?>> path = resolutionPath.get();
        
        if (stack.contains(clazz)) {
            // Build the cycle path
            List<Class<?>> cycle = new ArrayList<>();
            boolean inCycle = false;
            for (Class<?> c : path) {
                if (c == clazz) {
                    inCycle = true;
                }
                if (inCycle) {
                    cycle.add(c);
                }
            }
            cycle.add(clazz); // Add the class that closes the cycle
            
            throw new CircularDependencyException(cycle);
        }
        
        stack.add(clazz);
        path.add(clazz);
    }
    
    /**
     * Pops a class from the resolution stack.
     * Call this after successfully resolving a bean.
     *
     * @param clazz the class that was resolved
     */
    public void pop(Class<?> clazz) {
        Set<Class<?>> stack = resolutionStack.get();
        List<Class<?>> path = resolutionPath.get();
        
        stack.remove(clazz);
        if (!path.isEmpty()) {
            path.remove(path.size() - 1);
        }
    }
    
    /**
     * Checks if a class is currently being resolved.
     *
     * @param clazz the class to check
     * @return true if the class is in the resolution stack
     */
    public boolean isResolving(Class<?> clazz) {
        return resolutionStack.get().contains(clazz);
    }
    
    /**
     * Clears the resolution stack.
     * Useful for testing or resetting state.
     */
    public void clear() {
        resolutionStack.get().clear();
        resolutionPath.get().clear();
    }
    
    /**
     * Gets the current resolution depth.
     *
     * @return the number of classes currently being resolved
     */
    public int getDepth() {
        return resolutionStack.get().size();
    }
    
    /**
     * Gets the current resolution path as a string.
     * Useful for debugging.
     *
     * @return string representation of the current resolution path
     */
    public String getCurrentPath() {
        List<Class<?>> path = resolutionPath.get();
        if (path.isEmpty()) {
            return "<empty>";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(path.get(i).getSimpleName());
        }
        return sb.toString();
    }
}
