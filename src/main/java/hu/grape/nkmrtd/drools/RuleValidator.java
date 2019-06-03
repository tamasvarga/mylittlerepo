package hu.grape.nkmrtd.drools;

import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;

public class RuleValidator {

    private final KieServices kieServices;
    private final KieResources kieResources;

    public RuleValidator() {
        kieServices = KieServices.Factory.get();
        kieResources = kieServices.getResources();
    }

    public boolean isRuleValid(final String drlDirectoryUrl, final String drlFileName) {
        final Resource resource = kieResources.newUrlResource(drlDirectoryUrl + drlFileName);
        System.out.println("Checking drl file validation. DRL path: " + resource.getSourcePath());

        if (isStructureValid(resource) && isBuildValid(resource)) {
            System.out.println(String.format("Rule is right. Rule path: ", resource.getSourcePath()));
            return true;
        }

        return false;
    }

    private static boolean isStructureValid(final Resource resource) {
        final VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
        final Verifier verifier = vBuilder.newVerifier();

        verifier.addResourcesToVerify(resource, ResourceType.DRL);
        if (verifier.getErrors().size() > 0) {
            System.out.println(String.format("Rule has: %d, Rule Path: ", verifier.getErrors().size(), resource.getSourcePath()));
            for (int i = 0; i < verifier.getErrors().size(); i++) {
                System.out.println("Error: " + verifier.getErrors().get(i).getMessage());
            }
            return false;
        }

        verifier.dispose();

        return true;
    }

    private boolean isBuildValid(final Resource resource) {
        final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(resource);
        final KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            System.out.println("Build Errors: " + kieBuilder.getResults().toString());
            return false;
        }

        return true;
    }
}
