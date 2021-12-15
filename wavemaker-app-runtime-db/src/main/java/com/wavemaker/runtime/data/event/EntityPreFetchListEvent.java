package com.wavemaker.runtime.data.event;

import com.wavemaker.runtime.data.model.FetchQuery;

/**
 * @author Uday Shankar
 */
public class EntityPreFetchListEvent<E> extends EntityCRUDEvent<E> {
    private FetchQuery fetchQuery;

    public EntityPreFetchListEvent(String serviceId, Class<E> entityClass, FetchQuery fetchQuery) {
        super(serviceId, entityClass);
        this.fetchQuery = fetchQuery;
    }

    public FetchQuery getFetchQuery() {
        return fetchQuery;
    }
}
