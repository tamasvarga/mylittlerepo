package hu.grape.nkmrtd;

import hu.grape.nkmrtd.dao.DecisionHistoryRepository;
import hu.grape.nkmrtd.dao.EventRepository;
import hu.grape.nkmrtd.domain.Event;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import hu.grape.nkmrtd.utils.EventProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * History for event.
 * Process:
 *  - Get event(s) from Datastore
 *  - Process rule(s)
 */
public class EventHistoryStreaming {
    private static final EventRepository EVENT_REPOSITORY = new EventRepository();
    private static final DecisionHistoryRepository DECISION_HISTORY_REPOSITORY = new DecisionHistoryRepository();

    public static void main(String[] args){
        final JavaStreamingContext javaStreamingContext =
                new JavaStreamingContext(
                        new SparkConf()
                                .setAppName("Java history streaming application"),
                        Seconds.apply(1)
                );

        javaStreamingContext.sparkContext().setLogLevel("ERROR");

        DECISION_HISTORY_REPOSITORY.deleteAll();

        final List<Event> theEvents = EVENT_REPOSITORY.findAll(false, 500);
        final Queue<JavaRDD<Event>> theRddQueue = new LinkedList<>();
        theRddQueue.add(javaStreamingContext.sparkContext().parallelize(theEvents));

        final JavaDStream<Event> inputEventStream = javaStreamingContext.queueStream(theRddQueue);
        EventProcessor.processEventStream(true, inputEventStream);

        try {
            javaStreamingContext.start();
        } finally {
            javaStreamingContext.stop(true, true);
        }
    }
}
