package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bean as singleton scope.
 * Only one instance will be created and shared across all injection points.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}Singleton
 * public class DatabaseConnection {
 *     // Only one instance will exist
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {
}
