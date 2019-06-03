package hu.grape.nkmrtd.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
public class EventFactory {
    public static Event generateFromEntity(final Entity entity) {
        return generateFromEntity(entity, true);
    }

    public static Event generateFromEntity(final Entity entity, final boolean isFilledAppliedActions) {
        final Event event = new Event();

        event.setId(entity.getKey().getId());
        event.setType(entity.getString("type"));
        final Date theCreateDate = entity.getTimestamp("created").toDate();
        event.setCreated(LocalDateTime.ofInstant(theCreateDate.toInstant(), ZoneId.systemDefault()));

        if (isFilledAppliedActions) {
            entity.getList("appliedActions").forEach(action -> {
                if (action != null && action.get() != null) {
                    event.getAppliedActions().add(action.get().toString());
                }
            });
        }

        entity.getList("properties").forEach(prop -> {
            if (prop != null && prop.get() != null) {
                FullEntity<IncompleteKey> fullEntity = (FullEntity<IncompleteKey>) prop.get();
                final String[] keys = fullEntity.getNames().toArray(new String[0]);

                event.getProperties().put(keys[0], fullEntity.getValue(keys[0]).get().toString());
            }
        });

        return event;
    }

    public static Event generateFromJson(final String json) {
        try {
            log.trace("Parsing event from json. > json: {}", json);
            final ObjectMapper objectMapper = new ObjectMapper();
            Event event = objectMapper.readValue(json, Event.class);
            log.debug("Event parsed from json successfully.");
            return event;
        } catch (Exception e) {
            log.warn("Failed to parse Event from json. Returning null. > json: {}", json, e);
            return null;
        }
    }
}
