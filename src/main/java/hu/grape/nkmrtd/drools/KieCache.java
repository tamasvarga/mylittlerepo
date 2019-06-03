package hu.grape.nkmrtd.drools;

import hu.grape.nkmrtd.utils.GCPStorageUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class KieCache {
    private static final KieContainerHolder kieContainerHolder = new KieContainerHolder();

    public static synchronized KieContainer getKieContainer() {
        return createKieContainer();
    }

    private static KieContainer createKieContainer() {
        log.debug("Create new kieContainer");
        final KieServices kieServices = KieServices.Factory.get();
        final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        uploadDroolFiles(kieFileSystem);

        final KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kieBuilder.getResults().toString());
        }
        final KieContainer kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());

        log.info("KieContainer created successfully.");
        return kieContainer;
    }

    private static void uploadDroolFiles(final KieFileSystem kieFileSystem) {
        final List<String> ruleNames = GCPStorageUtils.readRulesFromGCPStorage();
        for (final String ruleName : ruleNames) {
            kieFileSystem.write(ResourceFactory.newUrlResource(GCPStorageUtils.DRL_DIRECTORY_URL + ruleName));
        }
    }

    @Data
    private static class KieContainerHolder {
        private KieContainer kieContainer;
        private LocalDateTime updated;
    }
}
