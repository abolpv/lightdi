package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditionally registers a bean based on the presence or value of a configuration property.
 * The bean will only be registered if the specified condition is met.
 *
 * <p>Usage examples:</p>
 *
 * <p>Register only if property exists:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}ConditionalOnProperty("cache.enabled")
 * public class RedisCacheService implements CacheService { }
 * </pre>
 *
 * <p>Register only if property has specific value:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}ConditionalOnProperty(value = "cache.type", havingValue = "redis")
 * public class RedisCacheService implements CacheService { }
 * </pre>
 *
 * <p>Register if property is missing (with matchIfMissing):</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}ConditionalOnProperty(value = "cache.enabled", matchIfMissing = true)
 * public class DefaultCacheService implements CacheService { }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnProperty {

    /**
     * The name of the property to check.
     * Alias for {@link #name()}.
     *
     * @return the property name
     */
    String value() default "";

    /**
     * The name of the property to check.
     *
     * @return the property name
     */
    String name() default "";

    /**
     * The expected value of the property.
     * If not specified, the condition matches if the property exists
     * and is not equal to "false".
     *
     * @return the expected value
     */
    String havingValue() default "";

    /**
     * Whether to match if the property is missing.
     * If true, the condition matches when the property is not set.
     * Default is false.
     *
     * @return true to match when property is missing
     */
    boolean matchIfMissing() default false;
}
