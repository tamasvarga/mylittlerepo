package hu.grape.nkmrtd.drools;

import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.utils.TestDataGenerator;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testing for drools' execution methods.
 * @author jnagy
 */
public class ExecutionTest {
    private static final String EVENT_TYPE_PURCHASE = "Purchase";
    private static final String EVENT_TYPE_LOAN = "Loan";
    private static final String EVENT_TYPE_ORDER = "Order";

    private static final String RULE_FOR_PURCHASE = "drools/rulesForPurchase.drl";
    private static final String RULE_FOR_LOAN = "drools/rulesForLoan.drl";
    private static final String RULE_FOR_ORDER = "drools/rulesForOrder.drl";
    private static final String RULE_WITH_ORDER_BY_ASC = "drools/rulesForPurchaseWithOrderByAsc.drl";
    private static final String RULE_WITH_ORDER_BY_DESC = "drools/rulesForPurchaseWithOrderByDesc.drl";
    private static final String RULE_WITH_EXCEPTION = "drools/rulesForPurchaseWithException.drl";
    private static final String RULE_WITH_AGENDA_AND_PRIORITY = "drools/rulesWithSelectionAndPriority.drl";

    private List<Event> testEvents;

    @Before
    public void init() throws IOException {
        testEvents = TestDataGenerator.generateTestEventsFromJson();
    }

    @Test
    public void executeOneDroolTest() {
        final RuleExecutor ruleExecutor =
                new RuleExecutor(TestDataGenerator.generateKieContainer(Arrays.asList(RULE_FOR_PURCHASE)));
        for (final Event testEvent : testEvents) {
            ruleExecutor.evulate(testEvent);
            if ("Purchase".equalsIgnoreCase(testEvent.getType())) {
                assertTrue(!testEvent.getAppliedActions().isEmpty());
                assertTrue(StringUtils.containsIgnoreCase(testEvent.getAppliedActions().get(0), EVENT_TYPE_PURCHASE));
            }
        }
    }

    @Test
    public void executeTwoDroolsTest() {
        final RuleExecutor ruleExecutor =
                new RuleExecutor(TestDataGenerator.generateKieContainer(Arrays.asList(RULE_FOR_PURCHASE, RULE_FOR_LOAN)));
        for (final Event testEvent : testEvents) {
            ruleExecutor.evulate(testEvent);

            if (EVENT_TYPE_PURCHASE.equalsIgnoreCase(testEvent.getType())) {
                assertTrue(!testEvent.getAppliedActions().isEmpty());
                assertTrue(StringUtils.containsIgnoreCase(testEvent.getAppliedActions().get(0), EVENT_TYPE_PURCHASE));
            } else if (EVENT_TYPE_LOAN.equalsIgnoreCase(testEvent.getType())) {
                assertTrue(!testEvent.getAppliedActions().isEmpty());
                assertTrue(StringUtils.containsIgnoreCase(testEvent.getAppliedActions().get(0), EVENT_TYPE_LOAN));
            } else if (EVENT_TYPE_ORDER.equalsIgnoreCase(testEvent.getType())){
                assertTrue(testEvent.getAppliedActions().isEmpty());
            }
        }
    }

    @Test
    public void executeTwoDroolsWithOrderByAscTest() {
        final RuleExecutor ruleExecutor =
                new RuleExecutor(TestDataGenerator.generateKieContainer(Arrays.asList(RULE_WITH_ORDER_BY_ASC)));
        for (final Event testEvent : testEvents) {
            ruleExecutor.evulate(testEvent);

            if (EVENT_TYPE_PURCHASE.equalsIgnoreCase(testEvent.getType())) {
                assertTrue(!testEvent.getAppliedActions().isEmpty());
                for (int i = 0; i < testEvent.getAppliedActions().size(); i++) {
                    assertTrue(
                            StringUtils.containsIgnoreCase(
                                    testEvent.getAppliedActions().get(i),
                                    String.format("%s event %d",
                                            EVENT_TYPE_PURCHASE,
                                            (i + 1)
                                    )
                            )
                    );
                }
            }
        }
    }

    @Test
    public void executeTwoDroolsWithOrderByDescTest() {
        final RuleExecutor ruleExecutor =
                new RuleExecutor(TestDataGenerator.generateKieContainer(Arrays.asList(RULE_WITH_ORDER_BY_DESC)));
        for (final Event testEvent : testEvents) {
            ruleExecutor.evulate(testEvent);

            if (EVENT_TYPE_PURCHASE.equalsIgnoreCase(testEvent.getType())) {
                assertTrue(!testEvent.getAppliedActions().isEmpty());
                for (int i = 0; i < testEvent.getAppliedActions().size(); i++) {
                    assertTrue(
                            StringUtils.containsIgnoreCase(
                                    testEvent.getAppliedActions().get(i),
                                    String.format("%s event %d",
                                            EVENT_TYPE_PURCHASE,
                                            (testEvent.getAppliedActions().size() - i)
                                    )
                            )
                    );
                }
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void exectuteOneDroolsWithException() {
        final RuleExecutor ruleExecutor =
                new RuleExecutor(TestDataGenerator.generateKieContainer(Arrays.asList(RULE_WITH_EXCEPTION)));
        for (final Event testEvent : testEvents) {
            testEvent.getProperties().put("UserId", "asd");
            ruleExecutor.evulate(testEvent);
        }
    }

    @Test
    public void exectuteWithAgendaAndPriorityTest() {
        final RuleExecutor ruleExecutor =
                new RuleExecutor(TestDataGenerator.generateKieContainer(Arrays.asList(RULE_WITH_AGENDA_AND_PRIORITY)));

        for (final Event testEvent : testEvents) {
            testEvent.getProperties().put("PriceTag", "5000");
            testEvent.getProperties().put("Profit", "7500");

            ruleExecutor.evulate(testEvent);
            if (EVENT_TYPE_PURCHASE.equalsIgnoreCase(testEvent.getType())) {

            }
        }
    }
}
