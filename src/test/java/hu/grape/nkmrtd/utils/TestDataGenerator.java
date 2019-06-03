package hu.grape.nkmrtd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.domain.EventFactory;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.conf.ConstraintJittingThresholdOption;
import org.kie.internal.io.ResourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TestDataGenerator {

    private static final String TEST_EVENT_FILE = "/test_events.json";

    public static List<String> generateTestJsonOfEventsFromJson() throws IOException {
        final InputStream inputStream = TestDataGenerator.class.getResourceAsStream(TEST_EVENT_FILE);
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode testEventsTree = objectMapper.readTree(inputStream);

        final List<String> eventJsonList = new LinkedList<>();
        for (final JsonNode jsonNode : testEventsTree) {
            eventJsonList.add(objectMapper.writeValueAsString((ObjectNode) jsonNode));
        }
        return eventJsonList;
    }

    public static List<Event> generateTestEventsFromJson() throws IOException {
        final List<String> jsons = generateTestJsonOfEventsFromJson();
        final List<Event> events = new LinkedList<>();
        for (final String json : jsons) {
            events.add(EventFactory.generateFromJson(json));
        }

        return events;
    }

    public static Event generateStandardEventTestData() {
        final List<String> appliedActions = Arrays.asList("Test_Action1", "Test_Action2");
        final Map<String, String> properties = new HashMap<>();
        properties.put("Amount", "1000");
        properties.put("UserId", "1");

        return new Event("Purchase", appliedActions, properties);
    }

    public static StatelessKieSession generateKieContainer(final List<String> rules) {
        final KieServices kieServices = KieServices.Factory.get();
        final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        for (final String rule : rules) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(rule));
        }

        final KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kieBuilder.getResults().toString());
        }

        final KieContainer kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        final KieBaseConfiguration kieBaseConfiguration = KieServices.Factory.get().newKieBaseConfiguration();
        kieBaseConfiguration.setOption(ConstraintJittingThresholdOption.get(-1));

        return kieContainer.newStatelessKieSession();
    }
}
