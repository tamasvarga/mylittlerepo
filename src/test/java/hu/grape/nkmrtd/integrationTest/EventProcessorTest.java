package hu.grape.nkmrtd.integrationTest;

import hu.grape.nkmrtd.dao.EventRepository;
import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.domain.EventFactory;
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
import java.util.Objects;
import java.util.Queue;

public class EventProcessorTest {
    private static final String EVENT_TYPE_PURCHASE = "Purchase";
    private static final EventRepository EVENT_REPOSITORY = new EventRepository();

    private JavaStreamingContext javaStreamingContext;

    @Before
    public void init() {
        javaStreamingContext = new JavaStreamingContext(
                new SparkConf()
                        .setAppName("Test java streaming application")
                        .setMaster("local[4]"),
                Seconds.apply(1));
    }

    @Test
    public void testProcessMessageStream() throws IOException {
        final List<String> testEventJsons = TestDataGenerator.generateTestJsonOfEventsFromJson();
        final JavaDStream<String> javaDStream = createEventJsonStream(javaStreamingContext, testEventJsons);
        final JavaDStream<Event> inputEventStream = parseEventsFromJson(javaDStream);

        EventProcessor.processEventStream(false, inputEventStream);

        final LocalDateTime testStartTime = LocalDateTime.now();
        try {
            javaStreamingContext.start();
        } finally {
            javaStreamingContext.stop(true, true);

            // Handle test result
            final List<Event> eventsFromDatastore = EVENT_REPOSITORY.findByCreatedAfter(testStartTime, true);
            for (final Event event : eventsFromDatastore) {
                if (EVENT_TYPE_PURCHASE.equals(event.getType())) {
                    Assert.assertTrue(event.getAppliedActions().size() > 0);
                }
            }
        }
    }

    private JavaDStream<Event> parseEventsFromJson(final JavaDStream<String> eventJsonStream) {
        return eventJsonStream
                .map(EventFactory::generateFromJson)
                .filter(Objects::nonNull);
    }

    private JavaDStream<String> createEventJsonStream(final JavaStreamingContext jsc, final List<String> list) {
        final Queue<JavaRDD<String>> rddQueue =
                new LinkedList<>();
        rddQueue.add(jsc.sparkContext().parallelize(list));

        return jsc.queueStream(rddQueue);
    }
}

