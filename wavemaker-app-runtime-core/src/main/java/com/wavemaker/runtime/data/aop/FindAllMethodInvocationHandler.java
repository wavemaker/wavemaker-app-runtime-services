package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;

import com.wavemaker.runtime.data.event.EntityPostFetchListEvent;
import com.wavemaker.runtime.data.event.EntityPreFetchListEvent;
import com.wavemaker.runtime.data.model.FetchQuery;

class FindAllMethodInvocationHandler implements CRUDMethodInvocationHandler {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void preHandle(String serviceId, Class entityClass, Method method, Object[] args) {
        String query = (String) args[0];
        FetchQuery fetchQuery = new FetchQuery(query);
        applicationEventPublisher.publishEvent(new EntityPreFetchListEvent<>(serviceId, entityClass, fetchQuery));
        args[0] = fetchQuery.getQuery();
    }

    @Override
    public void postHandle(String serviceId, Class entityClass, Method method, Object retVal) {
        applicationEventPublisher.publishEvent(new EntityPostFetchListEvent<>(serviceId, entityClass, (Page) retVal));
    }

    @Override
    public boolean matches(Class entityClass, Method method) {
        return "findAll".equals(method.getName()) && method.getParameterTypes().length >= 1 && method.getParameterTypes()[0] == String.class;
    }
}
