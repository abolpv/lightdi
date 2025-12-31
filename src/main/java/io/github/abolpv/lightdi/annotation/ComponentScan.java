package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies packages to scan for injectable components.
 * Can be used on configuration classes or the main application class.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}ComponentScan("com.example.services")
 * public class AppConfig { }
 *
 * {@literal @}ComponentScan({"com.example.services", "com.example.repositories"})
 * public class AppConfig { }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    
    /**
     * Base packages to scan for components.
     *
     * @return array of package names
     */
    String[] value() default {};
}
