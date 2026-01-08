package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditionally registers a bean only if another bean of the specified type exists.
 * This is useful for optional features that depend on other beans being present.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}ConditionalOnBean(DataSource.class)
 * public class JdbcUserRepository implements UserRepository { }
 * </pre>
 *
 * <p>The condition is evaluated at registration time. If the specified bean type
 * is not registered when this bean is being registered, this bean will be skipped.</p>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnBean {

    /**
     * The bean type(s) that must be present for this bean to be registered.
     * If multiple types are specified, ALL must be present.
     *
     * @return the bean type(s) to check
     */
    Class<?>[] value();
}
