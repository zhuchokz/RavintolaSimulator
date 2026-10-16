

import org.example.simu.framework.Trace;
import org.example.eduni.distributions.Normal;
import org.example.simu.framework.Clock;
import org.example.simu.framework.EventList;
import org.example.simu.model.Customer;
import org.example.simu.model.CustomerType;
import org.example.simu.model.EventType;
import org.example.simu.model.ServicePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicePointTest {

    private ServicePoint servicePoint;
    private EventList eventList;


    @BeforeEach
    void setUp() {
        Clock.getInstance().setTime(0);

        // Initialize tracing before creating any Customer objects.
        Trace.setTraceLevel(Trace.Level.INFO);

        eventList = new EventList();

        servicePoint = new ServicePoint(
                "Test Station",
                new Normal(5, 0.1),
                eventList,
                EventType.DEP_ORDER
        );
    }


    @Test
    void newServicePointShouldHaveAnEmptyQueue() {
        assertEquals(0, servicePoint.getQueueSize());
        assertFalse(servicePoint.isOnQueue());
        assertFalse(servicePoint.isReserved());
    }

    @Test
    void queueShouldMaintainFirstInFirstOutOrder() {
        Customer customer1 = new Customer(CustomerType.TAKEAWAY);
        Customer customer2 = new Customer(CustomerType.DINEIN);

        servicePoint.addQueue(customer1);
        servicePoint.addQueue(customer2);

        assertEquals(2, servicePoint.getQueueSize());

        // Begin service for customer1
        servicePoint.beginService();
        Customer served1 = servicePoint.removeQueue();
        assertSame(customer1, served1);

        // Station is now free, begin service for customer2
        servicePoint.beginService();
        Customer served2 = servicePoint.removeQueue();
        assertSame(customer2, served2);
    }


  



    @Test
    void addingCustomerShouldIncreaseQueueSize() {
        Customer customer = new Customer(CustomerType.TAKEAWAY);

        servicePoint.addQueue(customer);

        assertEquals(1, servicePoint.getQueueSize());
        assertTrue(servicePoint.isOnQueue());
    }

    @Test
    void beginningServiceShouldReserveTheStation() {
        Customer customer = new Customer(CustomerType.TAKEAWAY);
        servicePoint.addQueue(customer);

        servicePoint.beginService();

        assertTrue(servicePoint.isReserved());
        assertFalse(servicePoint.isOnQueue());
        assertEquals(0, servicePoint.getQueueSize());

        // A service-completion event should have been scheduled.
        assertTrue(eventList.getNextTime() > Clock.getInstance().getTime());
    }

    @Test
    void removingCustomerAfterServiceShouldFreeTheStation() {
        Customer customer = new Customer(CustomerType.TAKEAWAY);
        servicePoint.addQueue(customer);

        servicePoint.beginService();

        Customer removedCustomer = servicePoint.removeQueue();

        assertSame(customer, removedCustomer);
        assertFalse(servicePoint.isReserved());
        assertFalse(servicePoint.isOnQueue());
        assertEquals(0, servicePoint.getQueueSize());
    }
}
