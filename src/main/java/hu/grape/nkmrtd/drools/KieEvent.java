package hu.grape.nkmrtd.drools;

import java.math.BigDecimal;

public interface KieEvent {
    // number
    Integer getIntegerProperty(final String propertyKey);
    Double getDoubleProperty(final String propertyKey);
    BigDecimal getBigDecimalProperty(final String propertyKey);
    // varchar
    String getStringProperty(String propertyKey);
    // boolean
    Boolean getBooleanProperty(String propertyKey);
}
