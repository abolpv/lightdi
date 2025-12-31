package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifies a bean with a specific name for disambiguation.
 * Use when multiple implementations of an interface exist.
 *
 * <p>On implementation class:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}Named("email")
 * public class EmailSender implements MessageSender { }
 *
 * {@literal @}Injectable
 * {@literal @}Named("sms")
 * public class SmsSender implements MessageSender { }
 * </pre>
 *
 * <p>On injection point:</p>
 * <pre>
 * {@literal @}Injectable
 * public class NotificationService {
 *     {@literal @}Inject
 *     public NotificationService({@literal @}Named("email") MessageSender sender) {
 *         // Gets EmailSender
 *     }
 * }
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Named {
    
    /**
     * The qualifier name for the bean.
     *
     * @return the name identifier
     */
    String value();
}
