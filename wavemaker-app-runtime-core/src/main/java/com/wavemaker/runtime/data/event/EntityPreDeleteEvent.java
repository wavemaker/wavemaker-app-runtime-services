package com.wavemaker.runtime.data.event;

/**
 * @author Uday Shankar
 */
public class EntityPreDeleteEvent<E> extends EntityCRUDEvent<E> {
    private Object entityId;

    public EntityPreDeleteEvent(String serviceId, Class<E> entityClass, Object entityId) {
        super(serviceId, entityClass);
        this.entityId = entityId;
    }

    public Object getEntity() {
        return entityId;
    }
}
