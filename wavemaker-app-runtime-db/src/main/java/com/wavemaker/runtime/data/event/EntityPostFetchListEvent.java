package com.wavemaker.runtime.data.event;

import org.springframework.data.domain.Page;

/**
 * @author Uday Shankar
 */
public class EntityPostFetchListEvent<E> extends EntityCRUDEvent<E> {
    private Page<E> results;

    public EntityPostFetchListEvent(String serviceId, Class<E> entityClass, Page<E> results) {
        super(serviceId, entityClass);
        this.results = results;
    }

    public Page<E> getResults() {
        return results;
    }
}
