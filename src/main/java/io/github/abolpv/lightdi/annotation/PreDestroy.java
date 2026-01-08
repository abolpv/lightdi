package io.github.abolpv.lightdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed when the container is shutting down.
 * The annotated method must have no parameters and return void.
 * This is typically used for cleanup operations like closing connections,
 * releasing resources, or flushing caches.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}Injectable
 * {@literal @}Singleton
 * public class DatabaseConnection {
 *     private Connection connection;
 *
 *     {@literal @}PostConstruct
 *     public void connect() {
 *         connection = DriverManager.getConnection(url);
 *     }
 *
 *     {@literal @}PreDestroy
 *     public void disconnect() {
 *         // Called when container.shutdown() is invoked
 *         if (connection != null) {
 *             connection.close();
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>Note: @PreDestroy methods are called in reverse order of bean creation,
 * ensuring that dependencies are still available during cleanup.</p>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 * @see PostConstruct
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}
