package io.github.abolpv.lightdi.util;

import io.github.abolpv.lightdi.annotation.Inject;
import io.github.abolpv.lightdi.annotation.Named;
import io.github.abolpv.lightdi.annotation.PostConstruct;
import io.github.abolpv.lightdi.annotation.PreDestroy;
import io.github.abolpv.lightdi.exception.ContainerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for reflection operations.
 * Provides helper methods for working with annotations, constructors, fields, and methods.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public final class ReflectionUtils {
    
    private ReflectionUtils() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Finds the constructor to use for injection.
     * Priority: @Inject annotated constructor > single constructor > default constructor
     *
     * @param clazz the class to inspect
     * @return the constructor to use
     * @throws ContainerException if no suitable constructor is found
     */
    public static Constructor<?> findInjectableConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        
        // Look for @Inject annotated constructor
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        
        // If only one constructor exists, use it
        if (constructors.length == 1) {
            constructors[0].setAccessible(true);
            return constructors[0];
        }
        
        // Fall back to default constructor
        try {
            Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor;
        } catch (NoSuchMethodException e) {
            throw new ContainerException(
                "No suitable constructor found for " + clazz.getName() +
                ". Add @Inject to a constructor or provide a default constructor."
            );
        }
    }
    
    /**
     * Finds all fields annotated with @Inject.
     *
     * @param clazz the class to inspect
     * @return list of injectable fields
     */
    public static List<Field> findInjectableFields(Class<?> clazz) {
        List<Field> injectableFields = new ArrayList<>();
        
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    injectableFields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        
        return injectableFields;
    }
    
    /**
     * Finds all methods annotated with @Inject.
     *
     * @param clazz the class to inspect
     * @return list of injectable methods
     */
    public static List<Method> findInjectableMethods(Class<?> clazz) {
        List<Method> injectableMethods = new ArrayList<>();

        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Inject.class)
                    && !method.isAnnotationPresent(PostConstruct.class)) {
                    validateInjectMethod(method);
                    method.setAccessible(true);
                    injectableMethods.add(method);
                }
            }
            current = current.getSuperclass();
        }

        return injectableMethods;
    }

    private static void validateInjectMethod(Method method) {
        if (method.getParameterCount() == 0) {
            throw new ContainerException(
                "@Inject method must have at least one parameter: " + method
            );
        }
    }

    /**
     * Finds the method annotated with @PostConstruct.
     *
     * @param clazz the class to inspect
     * @return optional containing the post-construct method if found
     */
    public static Optional<Method> findPostConstructMethod(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    validatePostConstructMethod(method);
                    method.setAccessible(true);
                    return Optional.of(method);
                }
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }
    
    private static void validatePostConstructMethod(Method method) {
        if (method.getParameterCount() != 0) {
            throw new ContainerException(
                "@PostConstruct method must have no parameters: " + method
            );
        }
        if (method.getReturnType() != void.class) {
            throw new ContainerException(
                "@PostConstruct method must return void: " + method
            );
        }
    }

    /**
     * Finds the method annotated with @PreDestroy.
     *
     * @param clazz the class to inspect
     * @return optional containing the pre-destroy method if found
     */
    public static Optional<Method> findPreDestroyMethod(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    validatePreDestroyMethod(method);
                    method.setAccessible(true);
                    return Optional.of(method);
                }
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    private static void validatePreDestroyMethod(Method method) {
        if (method.getParameterCount() != 0) {
            throw new ContainerException(
                "@PreDestroy method must have no parameters: " + method
            );
        }
        if (method.getReturnType() != void.class) {
            throw new ContainerException(
                "@PreDestroy method must return void: " + method
            );
        }
    }

    /**
     * Gets the @Named qualifier value from an annotation array.
     *
     * @param annotations the annotations to search
     * @return optional containing the qualifier name if found
     */
    public static Optional<String> getQualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Named) {
                return Optional.of(((Named) annotation).value());
            }
        }
        return Optional.empty();
    }
    
    /**
     * Gets the @Named qualifier from a parameter.
     *
     * @param parameter the parameter to inspect
     * @return optional containing the qualifier name if found
     */
    public static Optional<String> getParameterQualifier(Parameter parameter) {
        Named named = parameter.getAnnotation(Named.class);
        return named != null ? Optional.of(named.value()) : Optional.empty();
    }
    
    /**
     * Gets the @Named qualifier from a field.
     *
     * @param field the field to inspect
     * @return optional containing the qualifier name if found
     */
    public static Optional<String> getFieldQualifier(Field field) {
        Named named = field.getAnnotation(Named.class);
        return named != null ? Optional.of(named.value()) : Optional.empty();
    }
    
    /**
     * Checks if a class has a specific annotation.
     *
     * @param clazz the class to check
     * @param annotationClass the annotation to look for
     * @return true if the annotation is present
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return clazz.isAnnotationPresent(annotationClass);
    }
    
    /**
     * Gets all interfaces implemented by a class (including inherited).
     *
     * @param clazz the class to inspect
     * @return list of all interfaces
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new ArrayList<>();
        
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        
        return interfaces;
    }
    
    /**
     * Creates an instance using the given constructor and arguments.
     *
     * @param constructor the constructor to invoke
     * @param args the arguments to pass
     * @return the created instance
     * @throws ContainerException if instantiation fails
     */
    public static Object createInstance(Constructor<?> constructor, Object[] args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new ContainerException(
                "Failed to create instance using constructor: " + constructor, e
            );
        }
    }
    
    /**
     * Sets a field value on an object.
     *
     * @param target the object to modify
     * @param field the field to set
     * @param value the value to set
     * @throws ContainerException if setting the field fails
     */
    public static void setField(Object target, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new ContainerException(
                "Failed to set field " + field.getName() + " on " + target.getClass().getName(), e
            );
        }
    }
    
    /**
     * Invokes a method on an object.
     *
     * @param target the object to invoke the method on
     * @param method the method to invoke
     * @throws ContainerException if invocation fails
     */
    public static void invokeMethod(Object target, Method method) {
        try {
            method.setAccessible(true);
            method.invoke(target);
        } catch (Exception e) {
            throw new ContainerException(
                "Failed to invoke method " + method.getName() + " on " + target.getClass().getName(), e
            );
        }
    }

    /**
     * Invokes a method on an object with the given arguments.
     *
     * @param target the object to invoke the method on
     * @param method the method to invoke
     * @param args the arguments to pass to the method
     * @throws ContainerException if invocation fails
     */
    public static void invokeMethodWithArgs(Object target, Method method, Object[] args) {
        try {
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (Exception e) {
            throw new ContainerException(
                "Failed to invoke method " + method.getName() + " on " + target.getClass().getName(), e
            );
        }
    }
}
