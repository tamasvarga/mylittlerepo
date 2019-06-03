package hu.grape.nkmrtd.drools;

import hu.grape.nkmrtd.domain.Event;
import org.drools.core.io.impl.ReaderResource;
import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.io.ResourceType;

import java.io.StringReader;

public class ValidationTest {

    private static final String TEST_DRL_FILE_NAME = "testRulesValidation.drl";

    @Test
    public void testDRLAsStringValidation() {
        String drl = "import " + Event.class.toString() + " \n";
        // drl += "rule 'Hello World' \n";
        drl += " when event:Event (type == 'PUBLIC') " +
                " then system.out.println('Hello') \n";
        drl += "end";

        VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
        Verifier verifier = vBuilder.newVerifier();
        verifier.addResourcesToVerify(new ReaderResource(new StringReader(drl)), ResourceType.DRL);

        verifier.dispose();

        Assert.assertTrue(verifier.getErrors().size() > 0);
    }

    /*
    @Test
    public void testDRLValidation() {
        RuleValidator ruleValidator = new RuleValidator();

        boolean isValid = ruleValidator.isRuleValid(KieCache.DRL_DIRECTORY_URL, TEST_DRL_FILE_NAME);
        Assert.assertTrue(isValid);
    }
    */
}
