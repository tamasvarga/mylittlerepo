package hu.grape.nkmrtd.domain;

import hu.grape.nkmrtd.drools.KieEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
public class Event implements Serializable, KieEvent {
    private Long id;
    private String type;
    private LocalDateTime created;
    private List<String> appliedActions = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();

    public Event(String type, List<String> appliedActions, Map<String, String> properties) {
        this.type = type;
        this.appliedActions = appliedActions;        
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id) &&
                Objects.equals(type, event.type) &&
                Objects.equals(created, event.created) &&
                Objects.equals(appliedActions, event.appliedActions) &&
                Objects.equals(properties, event.properties);
    }

    private boolean actionsEquals(Event event) {
        final List<String> eventAppliedActions = event.getAppliedActions();
        return eventAppliedActions.size() == getAppliedActions().size()
                && eventAppliedActions.containsAll(getAppliedActions())
                && getAppliedActions().containsAll(eventAppliedActions);
    }

    private boolean propertiesEquals(Event event) {
        Set<Map.Entry<String, String>> eventPropertiesEntrySet = event.getProperties().entrySet();
        Set<Map.Entry<String, String>> propertiesEntrySet = getProperties().entrySet();
        return eventPropertiesEntrySet.size() == propertiesEntrySet.size()
                && eventPropertiesEntrySet.containsAll(propertiesEntrySet)
                && propertiesEntrySet.containsAll(eventPropertiesEntrySet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, appliedActions, properties);
    }

    @Override
    public Integer getIntegerProperty(String propertyKey) {
        return Integer.valueOf(this.getProperties().get(propertyKey));
    }

    @Override
    public Double getDoubleProperty(String propertyKey) {
        return Double.valueOf(this.getProperties().get(propertyKey));
    }

    @Override
    public BigDecimal getBigDecimalProperty(String propertyKey) {
        return new BigDecimal(this.getProperties().get(propertyKey));
    }

    @Override
    public String getStringProperty(String propertyKey) {
        return this.getProperties().get(propertyKey);
    }

    @Override
    public Boolean getBooleanProperty(String propertyKey) {
        return Boolean.valueOf(this.getProperties().get(propertyKey));
    }
}
