package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as injectable by the DI container.
 * Classes annotated with @Injectable can be automatically
 * instantiated and have their dependencies injected.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Injectable
 * public class UserService {
 *     // ...
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Injectable {
}
