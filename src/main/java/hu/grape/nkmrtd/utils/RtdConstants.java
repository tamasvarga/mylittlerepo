package hu.grape.nkmrtd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import hu.grape.nkmrtd.exception.RtdInternalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

@Slf4j
public class RtdConstants {
    public static final String GCP_PROJECT_ID = "nkm-rtd";
    public static String PUBSUB_EVENT_SUBSCRIPTION;
    public static String DATASTORE_EVENT_KIND;
    public static String DATASTORE_DECISION_HISTORY_KIND;
    public static String BUCKET_NAME_IN_STORAGE_FOR_RULES;
    public static String FOLDER_NAME_IN_STORAGE_FOR_RULES;
    public static int EXECUTOR_NUMBER;

    static {
        log.info("Initializing projetct constants.");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            String profile = StringUtils.defaultIfEmpty(System.getProperty("profile"), "test");

            InputStream inputStream = RtdConstants.class.getResourceAsStream("/config/" + profile + ".yaml");
            JsonNode jsonNode = mapper.readTree(inputStream);
            PUBSUB_EVENT_SUBSCRIPTION = jsonNode.get("pubsub").get("subscription").get("event").asText();

            JsonNode datastoreKinds = jsonNode.get("datastore").get("kind");
            DATASTORE_EVENT_KIND = datastoreKinds.get("event").asText();
            DATASTORE_DECISION_HISTORY_KIND = datastoreKinds.get("decision-history").asText();

            final JsonNode storageSettings = jsonNode.get("storage");
            BUCKET_NAME_IN_STORAGE_FOR_RULES = storageSettings.get("bucket_name").asText();
            FOLDER_NAME_IN_STORAGE_FOR_RULES = storageSettings.get("folder_namne").asText();

            EXECUTOR_NUMBER = Integer.valueOf(System.getProperty("executorNumber", "2"));
            log.info("Project constants initialized successfully.");
            log.info("GCP_PROJECT_ID: {}, PUBSUB_EVENT_SUBSCRIPTION: {}, DATASTORE_EVENT_KIND: {}, DATASTORE_DECISION_HISTORY_KIND: {}, " +
                    "BUCKET_NAME_IN_STORAGE_FOR_RULES: {}, FOLDER_NAME_IN_STORAGE_FOR_RULES, EXECUTOR_NUMBER: {}",
                    GCP_PROJECT_ID, PUBSUB_EVENT_SUBSCRIPTION, DATASTORE_EVENT_KIND, DATASTORE_DECISION_HISTORY_KIND,
                    BUCKET_NAME_IN_STORAGE_FOR_RULES, FOLDER_NAME_IN_STORAGE_FOR_RULES, EXECUTOR_NUMBER);
        } catch (Exception e) {
            log.error("Failed to initialize project constants.", e);
            throw new RtdInternalException("Failed to initialize project constants.");
        }
    }
}
