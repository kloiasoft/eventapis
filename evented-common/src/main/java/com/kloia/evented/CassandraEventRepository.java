package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
public class CassandraEventRepository<T extends Entity> implements IEventRepository<T> {

    private String tableName;
    private CassandraTemplate cassandraOperations;
    private Map<String, AggregateFunction<T>> functionMap = new HashMap<>();


    public CassandraEventRepository(String tableName, CassandraTemplate cassandraOperations) {
        this.tableName = tableName;
        this.cassandraOperations = cassandraOperations;
    }



    @Override
    public T queryEntity(long entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityId));
        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);

        T result = null;
        for (EntityEvent entityEvent : entityEvents) {
            result = functionMap.get(entityEvent.getAggregateName()).apply(result, entityEvent);
            result.setVersion(entityEvent.getEventKey().getVersion());
        }
        return result;
    }

    @Override
    public void addAggregateSpecs(CommandSpec commandSpec) {
        functionMap.put(commandSpec.getCommandName(), commandSpec.getApply());

    }

    @Override
    public void recordAggregateEvent(EntityEvent entityEvent) throws EventStoreException {
        Insert insertQuery = cassandraOperations.createInsertQuery(tableName, entityEvent, null, cassandraOperations.getConverter());
        insertQuery.ifNotExists();
        cassandraOperations.execute(insertQuery);
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityEvent.getEventKey().getEntityId()));
        select.where(QueryBuilder.eq("version", entityEvent.getEventKey().getVersion()) );
        EntityEvent appliedEntityEvent = cassandraOperations.selectOne(select, EntityEvent.class);
        if(! appliedEntityEvent.getOpId().equals(entityEvent.getOpId()))
            throw new EventStoreException("Concurrent Event from Op:"+ appliedEntityEvent.getOpId());
    }

    @Override
    public void markFail(UUID key) {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("opId", key));
        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);

        entityEvents.forEach(entityEvent -> {
            entityEvent.setStatus("FAILED");
        });
        cassandraOperations.update(entityEvents);
    }
}
