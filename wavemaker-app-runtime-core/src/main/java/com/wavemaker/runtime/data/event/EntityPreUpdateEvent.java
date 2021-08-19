package com.wavemaker.runtime.data.event;

/**
 * @author Uday Shankar
 */
public class EntityPreUpdateEvent<E> extends EntityCRUDEvent<E> {
    private E entity;

    public EntityPreUpdateEvent(String serviceId, Class<E> entityClass, E entity) {
        super(serviceId, entityClass);
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }
}
