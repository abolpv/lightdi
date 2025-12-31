package io.github.abolpv.lightdi.exception;

/**
 * Base exception for all container-related errors.
 * This is the parent class for all specific container exceptions.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class ContainerException extends RuntimeException {

    public ContainerException(String message) {
        super(message);
    }

    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
