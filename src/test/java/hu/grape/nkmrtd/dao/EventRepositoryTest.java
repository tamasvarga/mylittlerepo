package hu.grape.nkmrtd.dao;

import hu.grape.nkmrtd.utils.TestDataGenerator;
import hu.grape.nkmrtd.domain.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/***
 * Tested DATASTORE methods in Event table. For example: connection and read.
 */
public class EventRepositoryTest {
    private DecisionHistoryRepository connector;
    private EventRepository eventRepository;

    @Before
    public void init() {
        connector = new DecisionHistoryRepository();
        eventRepository = new EventRepository();
    }

    @Test
    public void getEventByIdTest() {
        // First save one event to datastore.
        final Event newEvent = new TestDataGenerator().generateStandardEventTestData();
        final Long id = eventRepository.save(newEvent);

        // Second get event which we saved it before.
        Event theEvent = eventRepository.findById(id);

        // Testing event data
        Assert.assertTrue(theEvent.getId().compareTo(id) == 0);
        Assert.assertTrue(theEvent.getAppliedActions().size() == newEvent.getAppliedActions().size());
        Assert.assertTrue(theEvent.getCreated() != null);
        Assert.assertTrue(theEvent.getType().equals(newEvent.getType()));

        final Map<String, String> theProperties = theEvent.getProperties();
        final Map<String, String> newProperties = newEvent.getProperties();
        Assert.assertTrue(theProperties.size() == newProperties.size());
        for (Map.Entry<String, String> theProps : theProperties.entrySet()) {
            Assert.assertTrue(theProps.getValue().equals(newProperties.get(theProps.getKey())));
        }
    }

    @Test
    public void testModifiedEventById() {
        // First save one event to datastore.
        final Event newEvent = new TestDataGenerator().generateStandardEventTestData();
        final Long id = eventRepository.save(newEvent);

        // Second get event which we saved it before.
        final Event theEvent = eventRepository.findById(id);

        // Third change type of event field
        theEvent.setType("Modified");
        eventRepository.update(theEvent);

        // Fourth get event which we modified it before.
        final Event modifiedEvent = eventRepository.findById(id);

        // Testing event data
        Assert.assertFalse(newEvent.getType().equalsIgnoreCase(modifiedEvent.getType()));
        Assert.assertTrue(theEvent.getCreated().compareTo(modifiedEvent.getCreated()) == 0);
    }
}
