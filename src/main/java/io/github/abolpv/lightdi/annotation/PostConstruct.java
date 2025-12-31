package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed after dependency injection is complete.
 * The annotated method must have no parameters and return void.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Injectable
 * public class CacheService {
 *     {@literal @}Inject
 *     private DatabaseService database;
 *
 *     {@literal @}PostConstruct
 *     public void initialize() {
 *         // Called after database is injected
 *         loadCacheFromDatabase();
 *     }
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
