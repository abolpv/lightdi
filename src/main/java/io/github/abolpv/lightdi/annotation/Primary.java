package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bean as the primary candidate when multiple implementations exist.
 * When resolving dependencies without a specific @Named qualifier,
 * the @Primary bean will be selected by default.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}Primary
 * public class EmailSender implements MessageSender { }
 *
 * {@literal @}Injectable
 * public class SmsSender implements MessageSender { }
 *
 * // Gets EmailSender (the primary)
 * MessageSender sender = container.get(MessageSender.class);
 * </pre>
 *
 * <p>Note: Only one implementation per interface can be marked as @Primary.
 * Having multiple @Primary beans for the same type will result in an
 * AmbiguousBeanException.</p>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
}
