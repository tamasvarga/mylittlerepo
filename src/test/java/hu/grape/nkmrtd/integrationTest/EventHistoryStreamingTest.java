package hu.grape.nkmrtd.integrationTest;

import hu.grape.nkmrtd.dao.DecisionHistoryRepository;
import hu.grape.nkmrtd.dao.EventRepository;
import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.utils.TestDataGenerator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import hu.grape.nkmrtd.utils.EventProcessor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Test Event history process.
 */
public class EventHistoryStreamingTest {
    private static final EventRepository EVENT_REPOSITORY = new EventRepository();
    private static final DecisionHistoryRepository DECISION_HISTORY_REPOSITORY = new DecisionHistoryRepository();

    private JavaStreamingContext javaStreamingContext;

    @Before
    public void init() {
        DECISION_HISTORY_REPOSITORY.deleteAll();

        javaStreamingContext = new JavaStreamingContext(
                new SparkConf()
                        .setAppName("Test java history streaming application")
                        .setMaster("local[4]"),
                Seconds.apply(1));
    }

    @Test
    public void mainProcessTest() throws IOException, InterruptedException {
        final List<Event> events = TestDataGenerator.generateTestEventsFromJson();
        EVENT_REPOSITORY.saveAll(events);

        final List<Event> theEvents = EVENT_REPOSITORY.findEntitiesWithLimitOrderByCreated(events.size());
        final Queue<JavaRDD<Event>> theRddQueue = new LinkedList<>();
        theRddQueue.add(javaStreamingContext.sparkContext().parallelize(theEvents));

        final JavaDStream<Event> inputEventStream = javaStreamingContext.queueStream(theRddQueue);
        EventProcessor.processEventStream(true, inputEventStream);

        try {
            javaStreamingContext.start();
        } finally {
            javaStreamingContext.stop(true, true);

            // Handle test result
            Thread.sleep(1000);
            final List<Event> decisionHistoriesFromDb = DECISION_HISTORY_REPOSITORY.findEntitiesWithLimitOrderByCreated(events.size());
            Assert.assertTrue(decisionHistoriesFromDb.size() == events.size());

        }
    }

    @Test
    public void mainProcessWithZeroDataTest() {
        final List<Event> theEvents = new LinkedList<>();
        final Queue<JavaRDD<Event>> theRddQueue = new LinkedList<>();
        theRddQueue.add(javaStreamingContext.sparkContext().parallelize(theEvents));

        final LocalDateTime timeBeforeTest = LocalDateTime.now().minusMinutes(1);
        final JavaDStream<Event> inputEventStream = javaStreamingContext.queueStream(theRddQueue);
        EventProcessor.processEventStream(true, inputEventStream);

        try {
            javaStreamingContext.start();
        } finally {
            javaStreamingContext.stop(true, true);

            // Handle test result
            final List<Event> decisionHistoriesFromDb = DECISION_HISTORY_REPOSITORY.findByCreatedAfter(timeBeforeTest, true);
            Assert.assertTrue(decisionHistoriesFromDb.size() == 0);
        }
    }
}
