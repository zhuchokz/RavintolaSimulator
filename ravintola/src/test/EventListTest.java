import org.example.simu.framework.Clock;
import org.example.simu.framework.Event;
import org.example.simu.framework.EventList;
import org.example.simu.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventListTest {

    private EventList eventList;

    @BeforeEach
    void setUp() {
        Clock.getInstance().setTime(0.0);
        eventList = new EventList();
    }

    @Test
    void newEventListShouldBeEmpty() {
        assertNotNull(eventList);
    }

    @Test
    void eventsShouldBeOrderedChronologicallyByTime() {
        // Using EventType.DEP_ORDER for all events with different timestamps to test priority queue ordering
        Event event1 = new Event(EventType.DEP_ORDER, 20.0);
        Event event2 = new Event(EventType.DEP_ORDER, 5.0);
        Event event3 = new Event(EventType.DEP_ORDER, 12.0);

        eventList.add(event1);
        eventList.add(event2);
        eventList.add(event3);

        // The earliest event (t = 5.0) must be removed first
        Event nextEvent = eventList.remove();
        assertEquals(5.0, nextEvent.getTime(), 0.001);

        // Toinen earliest event (t = 12.0) should be next
        Event secondEvent = eventList.remove();
        assertEquals(12.0, secondEvent.getTime(), 0.001);
    }

    @Test
    void getNextTimeShouldReturnTimeOfEarliestEvent() {
        eventList.add(new Event(EventType.DEP_ORDER, 15.0));
        eventList.add(new Event(EventType.DEP_ORDER, 8.5));

        assertEquals(8.5, eventList.getNextTime(), 0.001);
    }
}