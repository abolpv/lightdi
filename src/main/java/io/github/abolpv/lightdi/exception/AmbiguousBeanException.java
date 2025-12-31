package io.github.abolpv.lightdi.exception;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Thrown when multiple beans match a requested type without proper qualification.
 * This typically happens when an interface has multiple implementations
 * and no {@literal @}Named qualifier is specified.
 *
 * <p>Example:</p>
 * <pre>
 * interface MessageSender { }
 *
 * {@literal @}Injectable
 * class EmailSender implements MessageSender { }
 *
 * {@literal @}Injectable
 * class SmsSender implements MessageSender { }
 *
 * // This will throw AmbiguousBeanException:
 * container.get(MessageSender.class);
 *
 * // Solution: Use @Named qualifier
 * </pre>
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class AmbiguousBeanException extends ContainerException {

    private final Class<?> requestedType;
    private final Set<Class<?>> candidates;

    public AmbiguousBeanException(Class<?> type, Set<Class<?>> candidates) {
        super(buildMessage(type, candidates));
        this.requestedType = type;
        this.candidates = candidates;
    }

    private static String buildMessage(Class<?> type, Set<Class<?>> candidates) {
        String candidateNames = candidates.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "));
        return "Multiple beans found for type " + type.getSimpleName() + 
               ": [" + candidateNames + "]. Use @Named qualifier to disambiguate.";
    }

    public Class<?> getRequestedType() {
        return requestedType;
    }

    public Set<Class<?>> getCandidates() {
        return candidates;
    }
}
