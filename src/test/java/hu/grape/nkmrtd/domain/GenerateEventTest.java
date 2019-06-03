package hu.grape.nkmrtd.domain;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class GenerateEventTest {

    private static final String TEST_FILE_NAME = "/test_event.json";

    @Test
    public void generateEventFromJsonTest() throws IOException {
        final InputStream inputStream = GenerateEventTest.class.getResourceAsStream(TEST_FILE_NAME);
        final StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "utf-8");
        final Event event = EventFactory.generateFromJson(writer.toString());

        Assert.assertTrue(event != null);
    }
}
