package com.wavemaker.runtime.data.event;

/**
 * @author Uday Shankar
 */
public class EntityPreCreateEvent<E> extends EntityCRUDEvent<E> {
    private E entity;

    public EntityPreCreateEvent(String serviceId, Class<E> entityClass, E entity) {
        super(serviceId, entityClass);
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }
}
