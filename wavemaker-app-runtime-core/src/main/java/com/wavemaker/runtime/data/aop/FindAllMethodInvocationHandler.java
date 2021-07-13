package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;

import com.wavemaker.runtime.data.event.EntityPostFetchEvent;
import com.wavemaker.runtime.data.event.EntityPreFetchEvent;

class FindAllMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, Object[] args) {
        String query = (String) args[0];
        applicationEventPublisher.publishEvent(new EntityPreFetchEvent<>(serviceId, entityClass, query));
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostFetchEvent<>(serviceId, entityClass, (Page) retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        return "findAll".equals(method.getName()) && method.getParameterTypes().length >= 1 && method.getParameterTypes()[0] == String.class;
    }
}
