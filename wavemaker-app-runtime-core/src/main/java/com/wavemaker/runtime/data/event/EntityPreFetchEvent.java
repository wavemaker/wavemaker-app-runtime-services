package com.wavemaker.runtime.data.event;

public class EntityPreFetchEvent<E> extends EntityCRUDEvent<E> {
    private String query;

    public EntityPreFetchEvent(String serviceId, Class<E> entityClass, String query) {
        super(serviceId, entityClass);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
