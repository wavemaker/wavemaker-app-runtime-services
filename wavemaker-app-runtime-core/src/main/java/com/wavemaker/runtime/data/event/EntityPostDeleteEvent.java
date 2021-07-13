package com.wavemaker.runtime.data.event;

public class EntityPostDeleteEvent<E> extends EntityCRUDEvent<E> {
    private E entity;

    public EntityPostDeleteEvent(String serviceId, Class<E> entityClass, E entity) {
        super(serviceId, entityClass);
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }
}
