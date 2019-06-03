package hu.grape.nkmrtd.dao;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.domain.EventFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EventRepositoryHelper {
    public static void setEventFields(Event event, FullEntity.Builder<IncompleteKey> entityBuilder) {
        entityBuilder.set("type", event.getType());
        EventRepositoryHelper.setProperties(event, entityBuilder);
        EventRepositoryHelper.setAppliedActions(event, entityBuilder);
        entityBuilder.set("created", TimestampValue.of(Timestamp.now()));
    }

    public static void setProperties(Event event, FullEntity.Builder<IncompleteKey> entityBuilder) {
        log.trace("Setting properties.");
        if (CollectionUtils.isNotEmpty(event.getProperties().entrySet())) {
            List<FullEntity<IncompleteKey>> properties = createProperties(event);
            switch (properties.size()) {
                case 1:
                    entityBuilder.set("properties", properties.get(0));
                    break;
                case 2:
                    entityBuilder.set("properties", properties.get(0), properties.get(1));
                    break;
                default:
                    entityBuilder.set("properties", properties.get(0), properties.get(1), properties.subList(2, properties.size()).toArray(new FullEntity[properties.size()-2]));
            }
        }
    }

    public static void setAppliedActions(Event event, FullEntity.Builder<IncompleteKey> entityBuilder) {
        log.trace("Setting applied actions.");
        if (CollectionUtils.isNotEmpty(event.getAppliedActions())) {
            List<Value<String>> values = event.getAppliedActions()
                    .stream()
                    .map(v -> new StringValue(v))
                    .collect(Collectors.toList());
            switch (values.size()) {
                case 1:
                    entityBuilder.set("appliedActions", Arrays.asList(values.get(0)));
                    break;
                case 2:
                    entityBuilder.set("appliedActions", values.get(0), values.get(1));
                    break;
                default:
                    entityBuilder.set("appliedActions", values.get(0), values.get(1), values.subList(2, values.size()).toArray(new Value[values.size() - 2]));
            }
        } else {
            entityBuilder.set("appliedActions", new ArrayList<>());
        }
    }

    public static List<FullEntity<IncompleteKey>> createProperties(Event event) {
        List<FullEntity<IncompleteKey>> result = new ArrayList<>();
        for (Map.Entry<String, String> propertyEntry : event.getProperties().entrySet()) {
            FullEntity.Builder<IncompleteKey> entityBuilder = Entity
                    .newBuilder()
                    .set(propertyEntry.getKey(), propertyEntry.getValue());
            result.add(entityBuilder.build());
        }
        return result;
    }

    public static List<Event> findByCreatedAfter(final LocalDateTime localDateTime, final String kind,
                                                 final Datastore connector, final boolean isFilledAppliedActions) {
        List<Event> events = new ArrayList<>();
        TimestampValue timestampValue = TimestampValue.of(Timestamp.of(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(StructuredQuery.PropertyFilter.gt("created", timestampValue))
                .build();

        QueryResults<Entity> queryResults = connector.run(query);
        queryResults.forEachRemaining(e -> events.add(EventFactory.generateFromEntity(e, isFilledAppliedActions)));
        return events;
    }

    public static Timestamp convertLocalDateTimeToTimestamp(final LocalDateTime localDateTime) {
        final Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        final Date dateFromOld = Date.from(instant);
        return Timestamp.of(dateFromOld);
    }
}
