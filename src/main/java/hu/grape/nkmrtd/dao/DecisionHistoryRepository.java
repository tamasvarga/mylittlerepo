package hu.grape.nkmrtd.dao;

import com.google.cloud.datastore.*;
import com.google.common.collect.Lists;
import hu.grape.nkmrtd.domain.Event;
import hu.grape.nkmrtd.domain.EventFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static hu.grape.nkmrtd.utils.RtdConstants.DATASTORE_DECISION_HISTORY_KIND;

public class DecisionHistoryRepository implements Repository<Event> {
    private Datastore connector;
    private KeyFactory keyFactory;

    public DecisionHistoryRepository(){
        this.connector = DatastoreOptions.getDefaultInstance().getService();
        this.keyFactory = this.connector.newKeyFactory().setKind(DATASTORE_DECISION_HISTORY_KIND);
    }

    @Override
    public Event findById(final Long id) {
        Entity entity = connector.get(keyFactory.newKey(id));
        return EventFactory.generateFromEntity(entity);
    }

    @Override
    public List<Event> findAll() {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(DATASTORE_DECISION_HISTORY_KIND)
                .build();
        final QueryResults<Entity> tasks = connector.run(query);

        final List<Event> eventsFromDatastore = new LinkedList<>();
        while (tasks.hasNext()) {
            Entity entity = tasks.next();
            eventsFromDatastore.add(EventFactory.generateFromEntity(entity));
        }

        return eventsFromDatastore;
    }

    @Override
    public List<Event> findEntitiesWithLimitOrderByCreated(final int limit) {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(DATASTORE_DECISION_HISTORY_KIND)
                .setLimit(limit)
                .setOrderBy(StructuredQuery.OrderBy.asc("created"))
                .build();
        final QueryResults<Entity> tasks = connector.run(query);

        final List<Event> eventsFromDatastore = new LinkedList<>();
        while (tasks.hasNext()) {
            Entity entity = tasks.next();
            eventsFromDatastore.add(EventFactory.generateFromEntity(entity));
        }

        return eventsFromDatastore;
    }

    @Override
    public Long save(final Event entity) {
        final IncompleteKey key = keyFactory.newKey(entity.getId());
        final FullEntity.Builder<IncompleteKey> entityBuilder = Entity.newBuilder(key);

        EventRepositoryHelper.setEventFields(entity, entityBuilder);

        final Entity modifiedEntity = connector.add(entityBuilder.build());
        final Long id = modifiedEntity.getKey().getId();
        if (id == null) {
            throw new RuntimeException("Failed to save decision of event history into datastore.");
        }
        return id;
    }

    @Override
    public void saveAll(final List<Event> entities) {
        List<FullEntity<IncompleteKey>> fullEntities = new LinkedList<>();
        IncompleteKey key;
        FullEntity.Builder<IncompleteKey> entityBuilder;
        for (Event event : entities) {
            key = keyFactory.newKey(event.getId());
            entityBuilder = Entity.newBuilder(key);
            EventRepositoryHelper.setEventFields(event, entityBuilder);
            fullEntities.add(entityBuilder.build());
        }

        if (fullEntities.size() > 0) {
            if (fullEntities.size() <= 500) {
                connector.add(fullEntities.toArray(new FullEntity[fullEntities.size()]));
            } else {
                final List<List<FullEntity<IncompleteKey>>> largeList =
                        Lists.partition(fullEntities, 500);
                for (final List<FullEntity<IncompleteKey>> list : largeList) {
                    connector.add(list.toArray(new FullEntity[list.size()]));
                }
            }
        }
    }

    @Override
    public void update(final Event entity) {
        final IncompleteKey key = keyFactory.newKey(entity.getId());
        final FullEntity.Builder<IncompleteKey> entityBuilder = Entity.newBuilder(key);

        EventRepositoryHelper.setEventFields(entity, entityBuilder);

        entityBuilder.set("created", EventRepositoryHelper.convertLocalDateTimeToTimestamp(entity.getCreated()));

        connector.put(entityBuilder.build());
    }

    @Override
    public void delete(final Long id) {
        final Key key = keyFactory.newKey(id);
        connector.delete(key);
    }

    @Override
    public void deleteAll() {
        final Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(DATASTORE_DECISION_HISTORY_KIND)
                .build();

        final QueryResults<Entity> tasks = connector.run(query);

        final List<Key> entityIds = new LinkedList<>();
        tasks.forEachRemaining(entity -> entityIds.add(entity.getKey()));

        if(entityIds.size() > 0) {
            connector.delete(entityIds.toArray(new Key[entityIds.size()]));
        }
    }

    public List<Event> findByCreatedAfter(final LocalDateTime localDateTime, final boolean isFilledAppliedActions) {
        return EventRepositoryHelper.findByCreatedAfter(localDateTime, DATASTORE_DECISION_HISTORY_KIND, connector, isFilledAppliedActions);
    }
}
