package com.wavemaker.runtime.data.event;

import org.springframework.data.domain.Page;

public class EntityPostFetchEvent<E> extends EntityCRUDEvent<E> {
    private Page<E> results;

    public EntityPostFetchEvent(String serviceId, Class<E> entityClass, Page<E> results) {
        super(serviceId, entityClass);
        this.results = results;
    }

    public Page<E> getResults() {
        return results;
    }
}
