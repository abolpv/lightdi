package io.github.abolpv.lightdi;

import io.github.abolpv.lightdi.annotation.*;
import io.github.abolpv.lightdi.annotation.Named;
import io.github.abolpv.lightdi.container.Container;
import io.github.abolpv.lightdi.container.Scope;
import io.github.abolpv.lightdi.exception.BeanNotFoundException;
import io.github.abolpv.lightdi.exception.CircularDependencyException;
import io.github.abolpv.lightdi.exception.ContainerException;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Container class.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
class ContainerTest {

    private Container container;

    @BeforeEach
    void setUp() {
        container = new Container();
        SingletonService.resetCounter();
        PrototypeService.resetCounter();
        PostConstructService.resetCounter();
    }

    // ==================== Test Classes ====================

    @Injectable
    static class SimpleService {
        public String getMessage() {
            return "Hello from SimpleService";
        }
    }

    @Injectable
    static class DatabaseRepository {
        public String getData() {
            return "Data from database";
        }
    }

    @Injectable
    static class UserService {
        private final DatabaseRepository repository;

        @Inject
        public UserService(DatabaseRepository repository) {
            this.repository = repository;
        }

        public DatabaseRepository getRepository() {
            return repository;
        }
    }

    @Injectable
    static class ComplexService {
        private final SimpleService simpleService;
        private final UserService userService;

        @Inject
        public ComplexService(SimpleService simpleService, UserService userService) {
            this.simpleService = simpleService;
            this.userService = userService;
        }

        public SimpleService getSimpleService() {
            return simpleService;
        }

        public UserService getUserService() {
            return userService;
        }
    }

    static class NotInjectableService {
    }

    interface MessageSender {
        void send(String message);
        String getType();
    }

    @Injectable
    @Named("email")
    static class EmailSender implements MessageSender {
        @Override
        public void send(String message) { }
        
        @Override
        public String getType() {
            return "email";
        }
    }

    @Injectable
    @Named("sms")
    static class SmsSender implements MessageSender {
        @Override
        public void send(String message) { }
        
        @Override
        public String getType() {
            return "sms";
        }
    }

    @Injectable
    @Singleton
    static class SingletonService {
        private static int instanceCount = 0;
        private final int instanceId;

        public SingletonService() {
            this.instanceId = ++instanceCount;
        }

        public int getInstanceId() {
            return instanceId;
        }

        public static void resetCounter() {
            instanceCount = 0;
        }
    }

    @Injectable
    static class PrototypeService {
        private static int instanceCount = 0;
        private final int instanceId;

        public PrototypeService() {
            this.instanceId = ++instanceCount;
        }

        public int getInstanceId() {
            return instanceId;
        }

        public static void resetCounter() {
            instanceCount = 0;
        }
    }

    @Injectable
    @Singleton
    static class SingletonWithDependency {
        private final PrototypeService prototypeService;

        @Inject
        public SingletonWithDependency(PrototypeService prototypeService) {
            this.prototypeService = prototypeService;
        }

        public PrototypeService getPrototypeService() {
            return prototypeService;
        }
    }

    @Injectable
    static class ServiceWithFieldInjection {
        @Inject
        private SimpleService simpleService;

        @Inject
        private DatabaseRepository repository;

        public SimpleService getSimpleService() {
            return simpleService;
        }

        public DatabaseRepository getRepository() {
            return repository;
        }
    }

    @Injectable
    static class ServiceWithNamedFieldInjection {
        @Inject
        @Named("email")
        private MessageSender emailSender;

        @Inject
        @Named("sms")
        private MessageSender smsSender;

        public MessageSender getEmailSender() {
            return emailSender;
        }

        public MessageSender getSmsSender() {
            return smsSender;
        }
    }

    @Injectable
    static class ServiceWithNamedConstructorInjection {
        private final MessageSender sender;

        @Inject
        public ServiceWithNamedConstructorInjection(@Named("email") MessageSender sender) {
            this.sender = sender;
        }

        public MessageSender getSender() {
            return sender;
        }
    }

    @Injectable
    static class PostConstructService {
        private static int initCount = 0;
        private boolean initialized = false;

        @PostConstruct
        public void init() {
            initialized = true;
            initCount++;
        }

        public boolean isInitialized() {
            return initialized;
        }

        public static int getInitCount() {
            return initCount;
        }

        public static void resetCounter() {
            initCount = 0;
        }
    }

    @Injectable
    static class PostConstructWithDependency {
        @Inject
        private SimpleService simpleService;

        private String message;

        @PostConstruct
        public void init() {
            message = simpleService.getMessage();
        }

        public String getMessage() {
            return message;
        }
    }

    // Circular dependency test classes
    @Injectable
    static class CircularA {
        @Inject
        public CircularA(CircularB b) { }
    }

    @Injectable
    static class CircularB {
        @Inject
        public CircularB(CircularA a) { }
    }

    @Injectable
    static class CircularX {
        @Inject
        public CircularX(CircularY y) { }
    }

    @Injectable
    static class CircularY {
        @Inject
        public CircularY(CircularZ z) { }
    }

    @Injectable
    static class CircularZ {
        @Inject
        public CircularZ(CircularX x) { }
    }

    // ==================== Basic Tests ====================

    @Nested
    @DisplayName("Basic Registration and Retrieval")
    class BasicTests {

        @Test
        @DisplayName("Should register and retrieve simple bean")
        void shouldRegisterAndRetrieveSimpleBean() {
            container.register(SimpleService.class);

            SimpleService service = container.get(SimpleService.class);

            assertNotNull(service);
            assertEquals("Hello from SimpleService", service.getMessage());
        }

        @Test
        @DisplayName("Should inject constructor dependencies")
        void shouldInjectConstructorDependencies() {
            container.register(DatabaseRepository.class);
            container.register(UserService.class);

            UserService service = container.get(UserService.class);

            assertNotNull(service);
            assertNotNull(service.getRepository());
            assertEquals("Data from database", service.getRepository().getData());
        }

        @Test
        @DisplayName("Should resolve transitive dependencies")
        void shouldResolveTransitiveDependencies() {
            container.register(SimpleService.class);
            container.register(DatabaseRepository.class);
            container.register(UserService.class);
            container.register(ComplexService.class);

            ComplexService service = container.get(ComplexService.class);

            assertNotNull(service);
            assertNotNull(service.getSimpleService());
            assertNotNull(service.getUserService());
            assertNotNull(service.getUserService().getRepository());
        }

        @Test
        @DisplayName("Should throw exception for unregistered bean")
        void shouldThrowExceptionForUnregisteredBean() {
            assertThrows(BeanNotFoundException.class, () -> {
                container.get(SimpleService.class);
            });
        }

        @Test
        @DisplayName("Should throw exception for non-injectable class")
        void shouldThrowExceptionForNonInjectableClass() {
            assertThrows(ContainerException.class, () -> {
                container.register(NotInjectableService.class);
            });
        }

        @Test
        @DisplayName("Should register interface with implementation")
        void shouldRegisterInterfaceWithImplementation() {
            container.register(MessageSender.class, EmailSender.class);

            MessageSender sender = container.get(MessageSender.class);

            assertNotNull(sender);
            assertTrue(sender instanceof EmailSender);
        }

        @Test
        @DisplayName("Should check if bean is registered")
        void shouldCheckIfBeanIsRegistered() {
            assertFalse(container.contains(SimpleService.class));

            container.register(SimpleService.class);

            assertTrue(container.contains(SimpleService.class));
        }

        @Test
        @DisplayName("Should return correct size")
        void shouldReturnCorrectSize() {
            assertEquals(0, container.size());

            container.register(SimpleService.class);
            assertEquals(1, container.size());

            container.register(DatabaseRepository.class);
            assertEquals(2, container.size());
        }

        @Test
        @DisplayName("Should register pre-created instance")
        void shouldRegisterPreCreatedInstance() {
            SimpleService instance = new SimpleService();
            container.registerInstance(SimpleService.class, instance);

            SimpleService retrieved = container.get(SimpleService.class);

            assertSame(instance, retrieved);
        }

        @Test
        @DisplayName("Should return optional for registered bean")
        void shouldReturnOptionalForRegisteredBean() {
            container.register(SimpleService.class);

            Optional<SimpleService> optional = container.getOptional(SimpleService.class);

            assertTrue(optional.isPresent());
        }

        @Test
        @DisplayName("Should return empty optional for unregistered bean")
        void shouldReturnEmptyOptionalForUnregisteredBean() {
            Optional<SimpleService> optional = container.getOptional(SimpleService.class);

            assertTrue(optional.isEmpty());
        }
    }

    // ==================== Scope Tests ====================

    @Nested
    @DisplayName("Scope Management")
    class ScopeTests {

        @Test
        @DisplayName("Prototype scope should create new instance each time")
        void prototypeScopeShouldCreateNewInstanceEachTime() {
            container.register(PrototypeService.class);

            PrototypeService first = container.get(PrototypeService.class);
            PrototypeService second = container.get(PrototypeService.class);
            PrototypeService third = container.get(PrototypeService.class);

            assertNotSame(first, second);
            assertNotSame(second, third);
            assertEquals(1, first.getInstanceId());
            assertEquals(2, second.getInstanceId());
            assertEquals(3, third.getInstanceId());
        }

        @Test
        @DisplayName("Singleton scope should return same instance")
        void singletonScopeShouldReturnSameInstance() {
            container.register(SingletonService.class);

            SingletonService first = container.get(SingletonService.class);
            SingletonService second = container.get(SingletonService.class);
            SingletonService third = container.get(SingletonService.class);

            assertSame(first, second);
            assertSame(second, third);
            assertEquals(1, first.getInstanceId());
        }

        @Test
        @DisplayName("Should correctly identify singleton scope")
        void shouldCorrectlyIdentifySingletonScope() {
            container.register(SingletonService.class);
            assertEquals(Scope.SINGLETON, container.getScope(SingletonService.class));
        }

        @Test
        @DisplayName("Should correctly identify prototype scope")
        void shouldCorrectlyIdentifyPrototypeScope() {
            container.register(PrototypeService.class);
            assertEquals(Scope.PROTOTYPE, container.getScope(PrototypeService.class));
        }

        @Test
        @DisplayName("Should clear singleton cache")
        void shouldClearSingletonCache() {
            container.register(SingletonService.class);

            SingletonService first = container.get(SingletonService.class);
            container.clearSingletons();
            SingletonService second = container.get(SingletonService.class);

            assertNotSame(first, second);
        }
    }

    // ==================== Field Injection Tests ====================

    @Nested
    @DisplayName("Field Injection")
    class FieldInjectionTests {

        @Test
        @DisplayName("Should inject fields annotated with @Inject")
        void shouldInjectFields() {
            container.register(SimpleService.class);
            container.register(DatabaseRepository.class);
            container.register(ServiceWithFieldInjection.class);

            ServiceWithFieldInjection service = container.get(ServiceWithFieldInjection.class);

            assertNotNull(service.getSimpleService());
            assertNotNull(service.getRepository());
            assertEquals("Hello from SimpleService", service.getSimpleService().getMessage());
        }

        @Test
        @DisplayName("Should inject named fields")
        void shouldInjectNamedFields() {
            container.register(MessageSender.class, EmailSender.class, "email");
            container.register(MessageSender.class, SmsSender.class, "sms");
            container.register(ServiceWithNamedFieldInjection.class);

            ServiceWithNamedFieldInjection service = container.get(ServiceWithNamedFieldInjection.class);

            assertNotNull(service.getEmailSender());
            assertNotNull(service.getSmsSender());
            assertEquals("email", service.getEmailSender().getType());
            assertEquals("sms", service.getSmsSender().getType());
        }
    }

    // ==================== Named Binding Tests ====================

    @Nested
    @DisplayName("Named Bindings")
    class NamedBindingTests {

        @Test
        @DisplayName("Should retrieve bean by name")
        void shouldRetrieveBeanByName() {
            container.register(MessageSender.class, EmailSender.class, "email");
            container.register(MessageSender.class, SmsSender.class, "sms");

            MessageSender email = container.get(MessageSender.class, "email");
            MessageSender sms = container.get(MessageSender.class, "sms");

            assertEquals("email", email.getType());
            assertEquals("sms", sms.getType());
        }

        @Test
        @DisplayName("Should inject named constructor parameter")
        void shouldInjectNamedConstructorParameter() {
            container.register(MessageSender.class, EmailSender.class, "email");
            container.register(ServiceWithNamedConstructorInjection.class);

            ServiceWithNamedConstructorInjection service = 
                container.get(ServiceWithNamedConstructorInjection.class);

            assertNotNull(service.getSender());
            assertEquals("email", service.getSender().getType());
        }

        @Test
        @DisplayName("Should throw exception for unknown name")
        void shouldThrowExceptionForUnknownName() {
            container.register(MessageSender.class, EmailSender.class, "email");

            assertThrows(BeanNotFoundException.class, () -> {
                container.get(MessageSender.class, "unknown");
            });
        }

        @Test
        @DisplayName("Should check if named bean exists")
        void shouldCheckIfNamedBeanExists() {
            container.register(MessageSender.class, EmailSender.class, "email");

            assertTrue(container.contains(MessageSender.class, "email"));
            assertFalse(container.contains(MessageSender.class, "sms"));
        }
    }

    // ==================== PostConstruct Tests ====================

    @Nested
    @DisplayName("PostConstruct Lifecycle")
    class PostConstructTests {

        @Test
        @DisplayName("Should call @PostConstruct method")
        void shouldCallPostConstruct() {
            container.register(PostConstructService.class);

            PostConstructService service = container.get(PostConstructService.class);

            assertTrue(service.isInitialized());
            assertEquals(1, PostConstructService.getInitCount());
        }

        @Test
        @DisplayName("Should call @PostConstruct after field injection")
        void shouldCallPostConstructAfterFieldInjection() {
            container.register(SimpleService.class);
            container.register(PostConstructWithDependency.class);

            PostConstructWithDependency service = container.get(PostConstructWithDependency.class);

            assertEquals("Hello from SimpleService", service.getMessage());
        }
    }

    // ==================== Circular Dependency Tests ====================

    @Nested
    @DisplayName("Circular Dependency Detection")
    class CircularDependencyTests {

        @Test
        @DisplayName("Should detect direct circular dependency")
        void shouldDetectDirectCircularDependency() {
            container.register(CircularA.class);
            container.register(CircularB.class);

            assertThrows(CircularDependencyException.class, () -> {
                container.get(CircularA.class);
            });
        }

        @Test
        @DisplayName("Should detect transitive circular dependency")
        void shouldDetectTransitiveCircularDependency() {
            container.register(CircularX.class);
            container.register(CircularY.class);
            container.register(CircularZ.class);

            assertThrows(CircularDependencyException.class, () -> {
                container.get(CircularX.class);
            });
        }
    }

    // ==================== Builder Tests ====================

    @Nested
    @DisplayName("Container Builder")
    class BuilderTests {

        @Test
        @DisplayName("Should build container with registered classes")
        void shouldBuildContainerWithRegisteredClasses() {
            Container container = Container.builder()
                .register(SimpleService.class)
                .register(DatabaseRepository.class)
                .build();

            assertTrue(container.contains(SimpleService.class));
            assertTrue(container.contains(DatabaseRepository.class));
        }

        @Test
        @DisplayName("Should build container with bindings")
        void shouldBuildContainerWithBindings() {
            Container container = Container.builder()
                .bind(MessageSender.class, EmailSender.class).named("email")
                .bind(MessageSender.class, SmsSender.class).named("sms")
                .build();

            assertEquals("email", container.get(MessageSender.class, "email").getType());
            assertEquals("sms", container.get(MessageSender.class, "sms").getType());
        }

        @Test
        @DisplayName("Should build container with instance")
        void shouldBuildContainerWithInstance() {
            SimpleService instance = new SimpleService();
            
            Container container = Container.builder()
                .instance(SimpleService.class, instance)
                .build();

            assertSame(instance, container.get(SimpleService.class));
        }
    }

    // ==================== GetAll Tests ====================

    @Nested
    @DisplayName("GetAll Implementations")
    class GetAllTests {

        @Test
        @DisplayName("Should get all implementations of interface")
        void shouldGetAllImplementations() {
            container.register(EmailSender.class);
            container.register(SmsSender.class);

            List<MessageSender> senders = container.getAll(MessageSender.class);

            assertEquals(2, senders.size());
        }
    }
}
