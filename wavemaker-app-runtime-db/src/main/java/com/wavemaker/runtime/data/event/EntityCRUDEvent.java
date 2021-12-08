package com.wavemaker.runtime.data.event;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * @author Uday Shankar
 */
public class EntityCRUDEvent<E> implements ResolvableTypeProvider {

    private String serviceId;

    private Class<E> entityClass;

    public EntityCRUDEvent(String serviceId, Class<E> entityClass) {
        this.serviceId = serviceId;
        this.entityClass = entityClass;
    }

    public String getServiceId() {
        return serviceId;
    }

    public Object getEntityClass() {
        return entityClass;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forRawClass(entityClass));
    }
}
