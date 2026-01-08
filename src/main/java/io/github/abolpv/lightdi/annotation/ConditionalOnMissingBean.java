package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditionally registers a bean only if no other bean of the specified type exists.
 * This is useful for providing fallback/default implementations.
 *
 * <p>Usage example:</p>
 * <pre>
 * // Primary implementation (registered if cache.enabled=true)
 * {@literal @}Injectable
 * {@literal @}ConditionalOnProperty("cache.enabled")
 * public class RedisCacheService implements CacheService { }
 *
 * // Fallback implementation (registered only if no CacheService exists)
 * {@literal @}Injectable
 * {@literal @}ConditionalOnMissingBean(CacheService.class)
 * public class InMemoryCacheService implements CacheService { }
 * </pre>
 *
 * <p>The condition is evaluated at registration time. If the specified bean type
 * is already registered when this bean is being registered, this bean will be skipped.</p>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnMissingBean {

    /**
     * The bean type(s) that must be absent for this bean to be registered.
     * If multiple types are specified, ALL must be absent.
     *
     * @return the bean type(s) to check
     */
    Class<?>[] value();
}
