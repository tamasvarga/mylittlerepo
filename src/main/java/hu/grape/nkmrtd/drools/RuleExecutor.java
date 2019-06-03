package hu.grape.nkmrtd.drools;

import hu.grape.nkmrtd.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

@Slf4j
public class RuleExecutor {

    private StatelessKieSession statelessKieSession;

    public RuleExecutor() {
        KieContainer kieContainer = KieCache.getKieContainer();
        this.statelessKieSession = kieContainer.newStatelessKieSession();
    }

    public RuleExecutor(final StatelessKieSession statelessKieSession) {
        this.statelessKieSession = statelessKieSession;
    }

    public void evulate(final Event event) {
        log.trace("evaluate > event: {}", event);
        this.statelessKieSession.setGlobal("activator", new ActionActivator());
        this.statelessKieSession.execute(event);
    }
}