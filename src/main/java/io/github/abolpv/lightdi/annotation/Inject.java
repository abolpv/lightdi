package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor, field, or method for dependency injection.
 * The container will automatically provide the required dependencies.
 *
 * <p>Constructor injection example:</p>
 * <pre>
 * {@literal @}Injectable
 * public class UserService {
 *     private final UserRepository repository;
 *
 *     {@literal @}Inject
 *     public UserService(UserRepository repository) {
 *         this.repository = repository;
 *     }
 * }
 * </pre>
 *
 * <p>Field injection example:</p>
 * <pre>
 * {@literal @}Injectable
 * public class UserService {
 *     {@literal @}Inject
 *     private UserRepository repository;
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
