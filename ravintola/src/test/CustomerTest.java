import org.example.simu.framework.Clock;
import org.example.simu.framework.Trace;
import org.example.simu.model.Customer;
import org.example.simu.model.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @BeforeEach
    void setUp() {
        // Initialize trace level so Trace.out() in Customer constructor doesn't throw NPE
        Trace.setTraceLevel(Trace.Level.INFO);
        Clock.getInstance().setTime(10.0);
    }

    @Test
    void customerShouldRecordArrivalTimeFromClock() {
        Customer customer = new Customer(CustomerType.DINEIN);

        assertEquals(10.0, customer.getArrivalTime(), 0.001);
        assertEquals(CustomerType.DINEIN, customer.getType());
    }

    @Test
    void customerShouldCalculateTotalStayTimeCorrectly() {
        Customer customer = new Customer(CustomerType.TAKEAWAY);
        customer.setArrivalTime(10.0);
        customer.setRemovalTime(25.5);

        assertEquals(15.5, customer.getRemovalTime() - customer.getArrivalTime(), 0.001);
    }

    @Test
    void customerIdsShouldIncrementSequentially() {
        Customer c1 = new Customer(CustomerType.ONLINE);
        Customer c2 = new Customer(CustomerType.ONLINE);

        assertTrue(c2.getId() > c1.getId(), "Each new customer should receive an incremented ID");
    }
}