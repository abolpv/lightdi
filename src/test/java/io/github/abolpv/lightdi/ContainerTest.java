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

import java.util.ArrayList;
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
            container.register(SmsNotification.class);
            container.register(PrimaryEmailNotification.class);

            NotificationService service = container.get(NotificationService.class);

            assertTrue(service instanceof PrimaryEmailNotification);
        }

        @Test
        @DisplayName("Non-primary should not override @Primary registration")
        void nonPrimaryShouldNotOverridePrimary() {
            container.register(PrimaryEmailNotification.class);
            container.register(SmsNotification.class);
            container.register(PushNotification.class);

            NotificationService service = container.get(NotificationService.class);

            assertTrue(service instanceof PrimaryEmailNotification);
        }
    }

    // ==================== PreDestroy Test Classes ====================

    @Injectable
    @Singleton
    static class ResourceService {
        private static List<String> events = new ArrayList<>();
        private boolean connected = false;

        @PostConstruct
        public void connect() {
            connected = true;
            events.add("ResourceService:connect");
        }

        @PreDestroy
        public void disconnect() {
            connected = false;
            events.add("ResourceService:disconnect");
        }

        public boolean isConnected() {
            return connected;
        }

        public static List<String> getEvents() {
            return events;
        }

        public static void resetEvents() {
            events = new ArrayList<>();
        }
    }

    @Injectable
    @Singleton
    static class LifecycleCacheService {
        private static List<String> events = new ArrayList<>();

        @PostConstruct
        public void initialize() {
            events.add("CacheService:initialize");
        }

        @PreDestroy
        public void flush() {
            events.add("CacheService:flush");
        }

        public static List<String> getEvents() {
            return events;
        }

        public static void resetEvents() {
            events = new ArrayList<>();
        }
    }

    @Injectable
    @Singleton
    static class DependentService {
        private static List<String> events = new ArrayList<>();
        private final ResourceService resourceService;

        @Inject
        public DependentService(ResourceService resourceService) {
            this.resourceService = resourceService;
            events.add("DependentService:construct");
        }

        @PreDestroy
        public void cleanup() {
            events.add("DependentService:cleanup");
        }

        public ResourceService getResourceService() {
            return resourceService;
        }

        public static List<String> getEvents() {
            return events;
        }

        public static void resetEvents() {
            events = new ArrayList<>();
        }
    }

    @Injectable
    @Singleton
    static class ServiceWithoutPreDestroy {
        private static int instanceCount = 0;

        public ServiceWithoutPreDestroy() {
            instanceCount++;
        }

        public static int getInstanceCount() {
            return instanceCount;
        }

        public static void resetCounter() {
            instanceCount = 0;
        }
    }

    // ==================== PreDestroy Tests ====================

    @Nested
    @DisplayName("PreDestroy Lifecycle")
    class PreDestroyTests {

        @BeforeEach
        void resetTestClasses() {
            ResourceService.resetEvents();
            LifecycleCacheService.resetEvents();
            DependentService.resetEvents();
            ServiceWithoutPreDestroy.resetCounter();
        }

        @Test
        @DisplayName("Should call @PreDestroy on shutdown")
        void shouldCallPreDestroyOnShutdown() {
            container.register(ResourceService.class);
            ResourceService service = container.get(ResourceService.class);

            assertTrue(service.isConnected());
            assertTrue(ResourceService.getEvents().contains("ResourceService:connect"));

            container.shutdown();

            assertFalse(service.isConnected());
            assertTrue(ResourceService.getEvents().contains("ResourceService:disconnect"));
        }

        @Test
        @DisplayName("Should call @PreDestroy in reverse creation order")
        void shouldCallPreDestroyInReverseOrder() {
            container.register(ResourceService.class);
            container.register(DependentService.class);

            DependentService dependent = container.get(DependentService.class);

            assertNotNull(dependent.getResourceService());

            container.shutdown();

            List<String> events = DependentService.getEvents();
            List<String> resourceEvents = ResourceService.getEvents();

            assertTrue(events.contains("DependentService:cleanup"));
            assertTrue(resourceEvents.contains("ResourceService:disconnect"));
        }

        @Test
        @DisplayName("Should handle beans without @PreDestroy")
        void shouldHandleBeansWithoutPreDestroy() {
            container.register(ServiceWithoutPreDestroy.class);
            container.get(ServiceWithoutPreDestroy.class);

            assertDoesNotThrow(() -> container.shutdown());
        }

        @Test
        @DisplayName("Should mark container as shutdown")
        void shouldMarkContainerAsShutdown() {
            container.register(SimpleService.class);
            container.get(SimpleService.class);

            assertFalse(container.isShutdown());

            container.shutdown();

            assertTrue(container.isShutdown());
        }

        @Test
        @DisplayName("Should prevent new instances after shutdown")
        void shouldPreventNewInstancesAfterShutdown() {
            container.register(SimpleService.class);
            container.register(DatabaseRepository.class);

            container.get(SimpleService.class);
            container.shutdown();

            assertThrows(ContainerException.class, () -> {
                container.get(DatabaseRepository.class);
            });
        }

        @Test
        @DisplayName("Should be idempotent - multiple shutdown calls safe")
        void shutdownShouldBeIdempotent() {
            container.register(ResourceService.class);
            container.get(ResourceService.class);

            container.shutdown();
            int eventCountAfterFirstShutdown = ResourceService.getEvents().size();

            container.shutdown();
            int eventCountAfterSecondShutdown = ResourceService.getEvents().size();

            assertEquals(eventCountAfterFirstShutdown, eventCountAfterSecondShutdown);
        }

        @Test
        @DisplayName("Should call @PreDestroy for multiple singletons")
        void shouldCallPreDestroyForMultipleSingletons() {
            container.register(ResourceService.class);
            container.register(LifecycleCacheService.class);

            container.get(ResourceService.class);
            container.get(LifecycleCacheService.class);

            container.shutdown();

            assertTrue(ResourceService.getEvents().contains("ResourceService:disconnect"));
            assertTrue(LifecycleCacheService.getEvents().contains("CacheService:flush"));
        }
    }

    // ==================== Conditional Registration Test Classes ====================

    interface CacheServiceInterface {
        String getType();
    }

    @Injectable
    @ConditionalOnProperty("cache.enabled")
    static class RedisCacheService implements CacheServiceInterface {
        @Override
        public String getType() {
            return "redis";
        }
    }

    @Injectable
    @ConditionalOnMissingBean(CacheServiceInterface.class)
    static class InMemoryCacheService implements CacheServiceInterface {
        @Override
        public String getType() {
            return "inmemory";
        }
    }

    @Injectable
    @ConditionalOnProperty(value = "cache.type", havingValue = "memcached")
    static class MemcachedCacheService implements CacheServiceInterface {
        @Override
        public String getType() {
            return "memcached";
        }
    }

    @Injectable
    @ConditionalOnProperty(value = "feature.optional", matchIfMissing = true)
    static class DefaultFeatureService {
        public String getName() {
            return "default";
        }
    }

    @Injectable
    @ConditionalOnBean(SimpleService.class)
    static class ServiceDependentOnSimple {
        public String getName() {
            return "dependent";
        }
    }

    @Injectable
    @ConditionalOnProperty(value = "db.enabled", havingValue = "true")
    @Singleton
    static class ConditionalDatabaseService {
        public String getConnection() {
            return "connected";
        }
    }

    // ==================== Conditional Registration Tests ====================

    @Nested
    @DisplayName("Conditional Bean Registration")
    class ConditionalRegistrationTests {

        @Test
        @DisplayName("@ConditionalOnProperty - should register when property is set")
        void shouldRegisterWhenPropertyIsSet() {
            container.setProperty("cache.enabled", "true");
            container.register(RedisCacheService.class);

            assertTrue(container.contains(RedisCacheService.class));
            assertTrue(container.contains(CacheServiceInterface.class));
            assertEquals("redis", container.get(CacheServiceInterface.class).getType());
        }

        @Test
        @DisplayName("@ConditionalOnProperty - should not register when property is missing")
        void shouldNotRegisterWhenPropertyIsMissing() {
            container.register(RedisCacheService.class);

            assertFalse(container.contains(RedisCacheService.class));
            assertFalse(container.contains(CacheServiceInterface.class));
        }

        @Test
        @DisplayName("@ConditionalOnProperty - should not register when property is 'false'")
        void shouldNotRegisterWhenPropertyIsFalse() {
            container.setProperty("cache.enabled", "false");
            container.register(RedisCacheService.class);

            assertFalse(container.contains(RedisCacheService.class));
        }

        @Test
        @DisplayName("@ConditionalOnProperty with havingValue - should match exact value")
        void shouldMatchExactPropertyValue() {
            container.setProperty("cache.type", "memcached");
            container.register(MemcachedCacheService.class);

            assertTrue(container.contains(MemcachedCacheService.class));
            assertEquals("memcached", container.get(CacheServiceInterface.class).getType());
        }

        @Test
        @DisplayName("@ConditionalOnProperty with havingValue - should not match different value")
        void shouldNotMatchDifferentPropertyValue() {
            container.setProperty("cache.type", "redis");
            container.register(MemcachedCacheService.class);

            assertFalse(container.contains(MemcachedCacheService.class));
        }

        @Test
        @DisplayName("@ConditionalOnProperty with matchIfMissing - should register when property missing")
        void shouldRegisterWithMatchIfMissingWhenPropertyMissing() {
            container.register(DefaultFeatureService.class);

            assertTrue(container.contains(DefaultFeatureService.class));
        }

        @Test
        @DisplayName("@ConditionalOnProperty with matchIfMissing - should not register when property is 'false'")
        void shouldNotRegisterWithMatchIfMissingWhenPropertyFalse() {
            container.setProperty("feature.optional", "false");
            container.register(DefaultFeatureService.class);

            assertFalse(container.contains(DefaultFeatureService.class));
        }

        @Test
        @DisplayName("@ConditionalOnMissingBean - should register when no bean exists")
        void shouldRegisterWhenBeanMissing() {
            container.register(InMemoryCacheService.class);

            assertTrue(container.contains(InMemoryCacheService.class));
            assertEquals("inmemory", container.get(CacheServiceInterface.class).getType());
        }

        @Test
        @DisplayName("@ConditionalOnMissingBean - should not register when bean exists")
        void shouldNotRegisterWhenBeanExists() {
            container.setProperty("cache.enabled", "true");
            container.register(RedisCacheService.class);
            container.register(InMemoryCacheService.class);

            assertTrue(container.contains(RedisCacheService.class));
            assertFalse(container.contains(InMemoryCacheService.class));
            assertEquals("redis", container.get(CacheServiceInterface.class).getType());
        }

        @Test
        @DisplayName("@ConditionalOnBean - should register when required bean exists")
        void shouldRegisterWhenRequiredBeanExists() {
            container.register(SimpleService.class);
            container.register(ServiceDependentOnSimple.class);

            assertTrue(container.contains(ServiceDependentOnSimple.class));
        }

        @Test
        @DisplayName("@ConditionalOnBean - should not register when required bean missing")
        void shouldNotRegisterWhenRequiredBeanMissing() {
            container.register(ServiceDependentOnSimple.class);

            assertFalse(container.contains(ServiceDependentOnSimple.class));
        }

        @Test
        @DisplayName("ContainerBuilder with properties - should support conditional registration")
        void shouldSupportConditionalRegistrationViaBuilder() {
            Container c = Container.builder()
                .property("db.enabled", "true")
                .register(ConditionalDatabaseService.class)
                .build();

            assertTrue(c.contains(ConditionalDatabaseService.class));
            assertEquals("connected", c.get(ConditionalDatabaseService.class).getConnection());
        }

        @Test
        @DisplayName("ContainerBuilder without property - should skip conditional bean")
        void shouldSkipConditionalBeanWithoutProperty() {
            Container c = Container.builder()
                .register(ConditionalDatabaseService.class)
                .build();

            assertFalse(c.contains(ConditionalDatabaseService.class));
        }

        @Test
        @DisplayName("Fallback pattern - primary with fallback via @ConditionalOnMissingBean")
        void shouldSupportFallbackPattern() {
            Container c1 = Container.builder()
                .register(RedisCacheService.class)
                .register(InMemoryCacheService.class)
                .build();

            assertTrue(c1.contains(InMemoryCacheService.class));
            assertEquals("inmemory", c1.get(CacheServiceInterface.class).getType());

            Container c2 = Container.builder()
                .property("cache.enabled", "true")
                .register(RedisCacheService.class)
                .register(InMemoryCacheService.class)
                .build();

            assertTrue(c2.contains(RedisCacheService.class));
            assertFalse(c2.contains(InMemoryCacheService.class));
            assertEquals("redis", c2.get(CacheServiceInterface.class).getType());
        }
    }
}
