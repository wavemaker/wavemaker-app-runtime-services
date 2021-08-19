package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.wavemaker.runtime.data.event.EntityPostFetchEvent;
import com.wavemaker.runtime.data.event.EntityPreFetchEvent;

class FindMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, java.lang.Object[] args) {
        Object entityId = args[0];
        applicationEventPublisher.publishEvent(new EntityPreFetchEvent<>(serviceId, entityClass, entityId));
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, java.lang.Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostFetchEvent<>(serviceId, entityClass, retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        String methodName = method.getName();
        return ("getById".equals(methodName) || "findById".equals(methodName)) && method.getParameterTypes().length == 1;
    }
}
