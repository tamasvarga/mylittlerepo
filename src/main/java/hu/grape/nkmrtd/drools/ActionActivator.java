package hu.grape.nkmrtd.drools;

import hu.grape.nkmrtd.domain.Event;

public class ActionActivator {
    public void sendEmail(String templateKey, Event e){
        e.getAppliedActions().add("Email sent to user. Template name: " + templateKey);
    }

    public void sendSms(String templateKey, Event e){
        e.getAppliedActions().add("Sms sent to user. Template name: " + templateKey);
    }

    public void sendEnvelope(String templateKey, Event e){
        e.getAppliedActions().add("Envelope sent to user. Template name: " + templateKey);
    }
}
