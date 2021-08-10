package com.wavemaker.runtime.data.event;

import com.wavemaker.runtime.data.model.FetchQuery;

public class EntityPreFetchEvent<E> extends EntityCRUDEvent<E> {
    private FetchQuery fetchQuery;

    public EntityPreFetchEvent(String serviceId, Class<E> entityClass, FetchQuery fetchQuery) {
        super(serviceId, entityClass);
        this.fetchQuery = fetchQuery;
    }

    public FetchQuery getFetchQuery() {
        return fetchQuery;
    }
}
