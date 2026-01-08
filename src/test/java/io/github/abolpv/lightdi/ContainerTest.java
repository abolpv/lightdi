package io.github.abolpv.lightdi;

import io.github.abolpv.lightdi.annotation.*;
import io.github.abolpv.lightdi.annotation.Named;
import io.github.abolpv.lightdi.container.Container;
import io.github.abolpv.lightdi.container.Scope;
import io.github.abolpv.lightdi.exception.BeanNotFoundException;
import io.github.abolpv.lightdi.exception.CircularDependencyException;
import io.github.abolpv.lightdi.exception.ContainerException;
import io.github.abolpv.lightdi.exception.AmbiguousBeanException;
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

    // ==================== Method Injection Test Classes ====================

    @Injectable
    static class ServiceWithMethodInjection {
        private SimpleService simpleService;
        private DatabaseRepository repository;

        @Inject
        public void setSimpleService(SimpleService simpleService) {
            this.simpleService = simpleService;
        }

        @Inject
        public void setRepository(DatabaseRepository repository) {
            this.repository = repository;
        }

        public SimpleService getSimpleService() {
            return simpleService;
        }

        public DatabaseRepository getRepository() {
            return repository;
        }
    }

    @Injectable
    static class ServiceWithNamedMethodInjection {
        private MessageSender emailSender;
        private MessageSender smsSender;

        @Inject
        public void setEmailSender(@Named("email") MessageSender emailSender) {
            this.emailSender = emailSender;
        }

        @Inject
        public void setSmsSender(@Named("sms") MessageSender smsSender) {
            this.smsSender = smsSender;
        }

        public MessageSender getEmailSender() {
            return emailSender;
        }

        public MessageSender getSmsSender() {
            return smsSender;
        }
    }

    @Injectable
    static class ServiceWithMultiParamMethodInjection {
        private SimpleService simpleService;
        private DatabaseRepository repository;

        @Inject
        public void setDependencies(SimpleService simpleService, DatabaseRepository repository) {
            this.simpleService = simpleService;
            this.repository = repository;
        }

        public SimpleService getSimpleService() {
            return simpleService;
        }

        public DatabaseRepository getRepository() {
            return repository;
        }
    }

    @Injectable
    static class ServiceWithMixedInjection {
        private final SimpleService constructorService;

        @Inject
        private DatabaseRepository fieldRepository;

        private UserService methodService;

        @Inject
        public ServiceWithMixedInjection(SimpleService simpleService) {
            this.constructorService = simpleService;
        }

        @Inject
        public void setUserService(UserService userService) {
            this.methodService = userService;
        }

        public SimpleService getConstructorService() {
            return constructorService;
        }

        public DatabaseRepository getFieldRepository() {
            return fieldRepository;
        }

        public UserService getMethodService() {
            return methodService;
        }
    }

    // ==================== Method Injection Tests ====================

    @Nested
    @DisplayName("Method Injection")
    class MethodInjectionTests {

        @Test
        @DisplayName("Should inject dependencies via setter methods")
        void shouldInjectViaSetterMethods() {
            container.register(SimpleService.class);
            container.register(DatabaseRepository.class);
            container.register(ServiceWithMethodInjection.class);

            ServiceWithMethodInjection service = container.get(ServiceWithMethodInjection.class);

            assertNotNull(service.getSimpleService());
            assertNotNull(service.getRepository());
            assertEquals("Hello from SimpleService", service.getSimpleService().getMessage());
            assertEquals("Data from database", service.getRepository().getData());
        }

        @Test
        @DisplayName("Should inject named dependencies via method parameters")
        void shouldInjectNamedDependenciesViaMethods() {
            container.register(MessageSender.class, EmailSender.class, "email");
            container.register(MessageSender.class, SmsSender.class, "sms");
            container.register(ServiceWithNamedMethodInjection.class);

            ServiceWithNamedMethodInjection service = container.get(ServiceWithNamedMethodInjection.class);

            assertNotNull(service.getEmailSender());
            assertNotNull(service.getSmsSender());
            assertEquals("email", service.getEmailSender().getType());
            assertEquals("sms", service.getSmsSender().getType());
        }

        @Test
        @DisplayName("Should inject multiple parameters in single method")
        void shouldInjectMultipleParametersInSingleMethod() {
            container.register(SimpleService.class);
            container.register(DatabaseRepository.class);
            container.register(ServiceWithMultiParamMethodInjection.class);

            ServiceWithMultiParamMethodInjection service = container.get(ServiceWithMultiParamMethodInjection.class);

            assertNotNull(service.getSimpleService());
            assertNotNull(service.getRepository());
        }

        @Test
        @DisplayName("Should support mixed injection (constructor + field + method)")
        void shouldSupportMixedInjection() {
            container.register(SimpleService.class);
            container.register(DatabaseRepository.class);
            container.register(UserService.class);
            container.register(ServiceWithMixedInjection.class);

            ServiceWithMixedInjection service = container.get(ServiceWithMixedInjection.class);

            assertNotNull(service.getConstructorService());
            assertNotNull(service.getFieldRepository());
            assertNotNull(service.getMethodService());
            assertEquals("Hello from SimpleService", service.getConstructorService().getMessage());
        }
    }

    // ==================== Primary Bean Test Classes ====================

    interface NotificationService {
        String getType();
    }

    @Injectable
    @Primary
    static class PrimaryEmailNotification implements NotificationService {
        @Override
        public String getType() {
            return "email";
        }
    }

    @Injectable
    static class SmsNotification implements NotificationService {
        @Override
        public String getType() {
            return "sms";
        }
    }

    @Injectable
    static class PushNotification implements NotificationService {
        @Override
        public String getType() {
            return "push";
        }
    }

    interface PaymentProcessor {
        String getProvider();
    }

    @Injectable
    @Primary
    static class StripeProcessor implements PaymentProcessor {
        @Override
        public String getProvider() {
            return "stripe";
        }
    }

    @Injectable
    @Primary
    static class PayPalProcessor implements PaymentProcessor {
        @Override
        public String getProvider() {
            return "paypal";
        }
    }

    @Injectable
    static class ServiceWithPrimaryDependency {
        private final NotificationService notificationService;

        @Inject
        public ServiceWithPrimaryDependency(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        public NotificationService getNotificationService() {
            return notificationService;
        }
    }

    // ==================== Primary Bean Tests ====================

    @Nested
    @DisplayName("Primary Bean Selection")
    class PrimaryBeanTests {

        @Test
        @DisplayName("Should select @Primary bean when multiple implementations exist")
        void shouldSelectPrimaryBean() {
            container.register(PrimaryEmailNotification.class);
            container.register(SmsNotification.class);
            container.register(PushNotification.class);

            NotificationService service = container.get(NotificationService.class);

            assertNotNull(service);
            assertTrue(service instanceof PrimaryEmailNotification);
            assertEquals("email", service.getType());
        }

        @Test
        @DisplayName("Should inject @Primary bean as dependency")
        void shouldInjectPrimaryBeanAsDependency() {
            container.register(PrimaryEmailNotification.class);
            container.register(SmsNotification.class);
            container.register(ServiceWithPrimaryDependency.class);

            ServiceWithPrimaryDependency service = container.get(ServiceWithPrimaryDependency.class);

            assertNotNull(service.getNotificationService());
            assertTrue(service.getNotificationService() instanceof PrimaryEmailNotification);
        }

        @Test
        @DisplayName("Should throw exception when multiple @Primary beans exist")
        void shouldThrowExceptionForMultiplePrimaryBeans() {
            container.register(StripeProcessor.class);

            assertThrows(AmbiguousBeanException.class, () -> {
                container.register(PayPalProcessor.class);
            });
        }

        @Test
        @DisplayName("@Primary bean should override non-primary registration")
        void primaryShouldOverrideNonPrimary() {
            // Register non-primary first
            container.register(SmsNotification.class);
            // Then register primary
            container.register(PrimaryEmailNotification.class);

            NotificationService service = container.get(NotificationService.class);

            assertTrue(service instanceof PrimaryEmailNotification);
        }

        @Test
        @DisplayName("Non-primary should not override @Primary registration")
        void nonPrimaryShouldNotOverridePrimary() {
            // Register primary first
            container.register(PrimaryEmailNotification.class);
            // Then register non-primary
            container.register(SmsNotification.class);
            container.register(PushNotification.class);

            NotificationService service = container.get(NotificationService.class);

            assertTrue(service instanceof PrimaryEmailNotification);
        }
    }
}
