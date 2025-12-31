package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bean or injection point for lazy initialization.
 * The actual instance will be created only when first accessed.
 *
 * <p>On class - delays bean creation:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}Lazy
 * public class ExpensiveService {
 *     // Created only when first requested
 * }
 * </pre>
 *
 * <p>On field - injects a proxy:</p>
 * <pre>
 * {@literal @}Injectable
 * public class UserService {
 *     {@literal @}Inject
 *     {@literal @}Lazy
 *     private ExpensiveService expensiveService;
 *     // Actual instance created on first method call
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lazy {
}
