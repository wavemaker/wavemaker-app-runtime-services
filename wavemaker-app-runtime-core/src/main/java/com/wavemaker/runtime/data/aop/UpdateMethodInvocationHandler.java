package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.wavemaker.runtime.data.event.EntityPostUpdateEvent;
import com.wavemaker.runtime.data.event.EntityPreUpdateEvent;

class UpdateMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, Object[] args) {
        Object entity = args[0];
        applicationEventPublisher.publishEvent(new EntityPreUpdateEvent<>(serviceId, entityClass, entity));
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostUpdateEvent<>(serviceId, entityClass, retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        return "update".equals(method.getName());
    }
}
