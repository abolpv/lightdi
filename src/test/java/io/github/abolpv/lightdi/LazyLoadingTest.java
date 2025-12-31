package io.github.abolpv.lightdi;

import io.github.abolpv.lightdi.annotation.Inject;
import io.github.abolpv.lightdi.annotation.Injectable;
import io.github.abolpv.lightdi.annotation.Lazy;
import io.github.abolpv.lightdi.container.Container;
import io.github.abolpv.lightdi.proxy.ProxyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for lazy loading functionality.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
class LazyLoadingTest {

    private Container container;

    @BeforeEach
    void setUp() {
        container = new Container();
        ExpensiveService.resetCounter();
    }

    // Test classes
    
    interface ExpensiveResource {
        String process();
        int getInstanceId();
    }

    @Injectable
    @Lazy
    static class ExpensiveService implements ExpensiveResource {
        private static int instanceCount = 0;
        private final int instanceId;

        public ExpensiveService() {
            this.instanceId = ++instanceCount;
            // Simulate expensive initialization
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String process() {
            return "Processed by instance " + instanceId;
        }

        @Override
        public int getInstanceId() {
            return instanceId;
        }

        public static int getInstanceCount() {
            return instanceCount;
        }

        public static void resetCounter() {
            instanceCount = 0;
        }
    }

    @Injectable
    static class ServiceWithLazyField {
        @Inject
        @Lazy
        private ExpensiveResource expensiveResource;

        public ExpensiveResource getExpensiveResource() {
            return expensiveResource;
        }
    }

    @Test
    @DisplayName("Lazy proxy should not create instance until method call")
    void lazyProxyShouldNotCreateInstanceUntilMethodCall() {
        container.register(ExpensiveResource.class, ExpensiveService.class);

        // Getting the bean should return a proxy, not create the actual instance
        ExpensiveResource resource = container.get(ExpensiveResource.class);

        // The proxy should be created but not the actual instance
        assertTrue(ProxyFactory.isLazyProxy(resource));
        assertFalse(ProxyFactory.isInitialized(resource));
        assertEquals(0, ExpensiveService.getInstanceCount());

        // Calling a method should trigger initialization
        String result = resource.process();

        assertTrue(ProxyFactory.isInitialized(resource));
        assertEquals(1, ExpensiveService.getInstanceCount());
        assertEquals("Processed by instance 1", result);
    }

    @Test
    @DisplayName("Lazy field injection should use proxy")
    void lazyFieldInjectionShouldUseProxy() {
        container.register(ExpensiveResource.class, ExpensiveService.class);
        container.register(ServiceWithLazyField.class);

        ServiceWithLazyField service = container.get(ServiceWithLazyField.class);

        // Field should contain a proxy
        ExpensiveResource resource = service.getExpensiveResource();
        assertTrue(ProxyFactory.isLazyProxy(resource));
        assertEquals(0, ExpensiveService.getInstanceCount());

        // Using the resource should initialize it
        resource.process();
        assertEquals(1, ExpensiveService.getInstanceCount());
    }

    @Test
    @DisplayName("Multiple calls to lazy proxy should return same instance")
    void multipleCallsToLazyProxyShouldReturnSameInstance() {
        container.register(ExpensiveResource.class, ExpensiveService.class);

        ExpensiveResource resource = container.get(ExpensiveResource.class);

        // Multiple method calls should use the same underlying instance
        assertEquals(1, resource.getInstanceId());
        assertEquals(1, resource.getInstanceId());
        assertEquals(1, resource.getInstanceId());

        // Only one instance should have been created
        assertEquals(1, ExpensiveService.getInstanceCount());
    }

    @Test
    @DisplayName("Lazy proxy toString should indicate status")
    void lazyProxyToStringShouldIndicateStatus() {
        container.register(ExpensiveResource.class, ExpensiveService.class);

        ExpensiveResource resource = container.get(ExpensiveResource.class);

        String beforeInit = resource.toString();
        assertTrue(beforeInit.contains("not initialized"));

        resource.process(); // Initialize

        String afterInit = resource.toString();
        assertFalse(afterInit.contains("not initialized"));
    }
}
