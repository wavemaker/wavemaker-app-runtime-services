package com.wavemaker.runtime.data.event;

/**
 * @author Uday Shankar
 */
public class EntityPreFetchEvent<E> extends EntityCRUDEvent<E> {
    private Object entityId;

    public EntityPreFetchEvent(String serviceId, Class<E> entityClass, Object entityId) {
        super(serviceId, entityClass);
        this.entityId = entityId;
    }

    public Object getEntityId() {
        return entityId;
    }
}
