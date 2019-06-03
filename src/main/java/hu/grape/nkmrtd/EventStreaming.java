package hu.grape.nkmrtd;

import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.domain.EventFactory;
import hu.grape.nkmrtd.utils.RtdConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.pubsub.PubsubUtils;
import org.apache.spark.streaming.pubsub.SparkGCPCredentials;
import org.apache.spark.streaming.pubsub.SparkPubsubMessage;
import hu.grape.nkmrtd.utils.EventProcessor;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static hu.grape.nkmrtd.utils.RtdConstants.GCP_PROJECT_ID;
import static hu.grape.nkmrtd.utils.RtdConstants.PUBSUB_EVENT_SUBSCRIPTION;

@Slf4j
public class EventStreaming {

    public static void main(String[] args) throws InterruptedException {
        log.info("Streaming application started.");
        final JavaStreamingContext javaStreamingContext = createJavaStreamingContext();

        for (int i = 0; i < RtdConstants.EXECUTOR_NUMBER; i++) {
            final JavaReceiverInputDStream<SparkPubsubMessage> pubSubStream = createPubsubStream(javaStreamingContext);
            final JavaDStream<Event> messageStream =
                    pubSubStream
                            .map(msg -> EventFactory.generateFromJson(new String(msg.getData(), StandardCharsets.UTF_8)))
                            .filter(Objects::nonNull);
            EventProcessor.processEventStream(false, messageStream);
        }

        try {
            javaStreamingContext.start();
            javaStreamingContext.awaitTermination();
        } finally {
            javaStreamingContext.stop(true, true);
        }
    }

    private static JavaStreamingContext createJavaStreamingContext() {
        log.debug("Creating streaming context.");
        final JavaStreamingContext javaStreamingContext =
                new JavaStreamingContext(
                        new SparkConf()
                                .setAppName("Java streaming application"), Seconds.apply(1)
                );
        javaStreamingContext.sparkContext().setLogLevel("ERROR");
        log.trace("Streaming context created succesfully");
        return javaStreamingContext;
    }

    private static JavaReceiverInputDStream<SparkPubsubMessage> createPubsubStream(JavaStreamingContext javaStreamingContext) {
        log.debug("Creating pubsub stream. > GCP_PROJECT_ID: {}, PUBSUB_EVENT_SUBSCRIPTION: {}", GCP_PROJECT_ID, PUBSUB_EVENT_SUBSCRIPTION);
        return PubsubUtils.createStream(
                javaStreamingContext,
                GCP_PROJECT_ID,
                PUBSUB_EVENT_SUBSCRIPTION,
                new SparkGCPCredentials.Builder().build(),
                StorageLevel.MEMORY_AND_DISK_SER());
    }
}
