package com.wavemaker.runtime.data.event;

import java.util.Optional;

/**
 * @author Uday Shankar
 */
public class EntityPostFetchEvent<E> extends EntityCRUDEvent<E> {
    private Optional<E> entity;

    public EntityPostFetchEvent(String serviceId, Class<E> entityClass, E entity) {
        super(serviceId, entityClass);
        this.entity = Optional.ofNullable(entity);
    }

    public Optional<E> getEntity() {
        return entity;
    }
}
