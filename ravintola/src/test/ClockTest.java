import org.example.simu.framework.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClockTest {

    @BeforeEach
    void setUp() {
        Clock.getInstance().setTime(0.0);
    }

    @Test
    void getInstanceShouldAlwaysReturnSameSingletonInstance() {
        Clock clock1 = Clock.getInstance();
        Clock clock2 = Clock.getInstance();

        assertSame(clock1, clock2, "Clock must follow the Singleton pattern");
    }

    @Test
    void setTimeShouldUpdateSimulationTime() {
        Clock.getInstance().setTime(42.5);

        assertEquals(42.5, Clock.getInstance().getTime(), 0.001);
    }
}