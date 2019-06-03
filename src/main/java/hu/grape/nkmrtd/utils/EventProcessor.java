package hu.grape.nkmrtd.utils;

import hu.grape.nkmrtd.dao.DecisionHistoryRepository;
import hu.grape.nkmrtd.dao.EventRepository;
import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.drools.RuleExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.streaming.api.java.JavaDStream;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class EventProcessor {

    public static void processEventStream(final boolean isHistoryEventProcess, final JavaDStream<Event> eventStream) {
        log.info("Processing event stream.");
        final JavaDStream<Event> executedEventStream = executeRulesForEvents(eventStream);
        saveEventsInDatastore(isHistoryEventProcess, executedEventStream);
    }

    private static JavaDStream<Event> executeRulesForEvents(final JavaDStream<Event> eventStream) {
        return eventStream.map(event -> {
            new RuleExecutor().evulate(event);
            return event;
        });
    }

    private static void saveEventsInDatastore(final boolean isHistoryEventProcess, final JavaDStream<Event> executedEventStream) {
        log.debug("Saving events in datastore.");
        executedEventStream.foreachRDD(rdd -> {
            rdd.foreachPartition(events -> {
                final List<Event> modifiedEvents = new LinkedList<>();
                events.forEachRemaining(event -> {
                    modifiedEvents.add(event);
                });

                if (isHistoryEventProcess) {
                    new DecisionHistoryRepository().saveAll(modifiedEvents);
                } else {
                    new EventRepository().saveAll(modifiedEvents);
                }
            });
        });
    }
}
