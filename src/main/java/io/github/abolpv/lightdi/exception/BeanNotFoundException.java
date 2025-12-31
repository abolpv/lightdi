package io.github.abolpv.lightdi.exception;

/**
 * Thrown when a requested bean is not found in the container.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class BeanNotFoundException extends ContainerException {

    private final Class<?> requestedType;
    private final String qualifierName;

    public BeanNotFoundException(Class<?> type) {
        super("No bean found for type: " + type.getName());
        this.requestedType = type;
        this.qualifierName = null;
    }

    public BeanNotFoundException(Class<?> type, String name) {
        super("No bean found for type: " + type.getName() + " with qualifier: " + name);
        this.requestedType = type;
        this.qualifierName = name;
    }

    public BeanNotFoundException(String message) {
        super(message);
        this.requestedType = null;
        this.qualifierName = null;
    }

    public Class<?> getRequestedType() {
        return requestedType;
    }

    public String getQualifierName() {
        return qualifierName;
    }
}
