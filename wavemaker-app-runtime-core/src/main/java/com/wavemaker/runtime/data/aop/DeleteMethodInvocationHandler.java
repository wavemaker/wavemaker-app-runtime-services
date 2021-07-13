package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.wavemaker.runtime.data.event.EntityPostDeleteEvent;
import com.wavemaker.runtime.data.event.EntityPreDeleteEvent;

class DeleteMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, Object[] args) {
        Object entityId = args[0];
        applicationEventPublisher.publishEvent(new EntityPreDeleteEvent(serviceId, entityClass, entityId));
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostDeleteEvent(serviceId, entityClass, retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        return "delete".equals(method.getName()) && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] != entityClass;
    }
}
