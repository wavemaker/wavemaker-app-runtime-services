package com.wavemaker.runtime.rest;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.runtime.rest.service.RestRuntimeService;

public class RestFactoryBean<T> implements FactoryBean<T> {

    private Class<T> serviceKlass;

    private String serviceId;

    private ClassLoader classLoader;

    @Autowired
    private RestRuntimeService restRuntimeService;

    public RestFactoryBean(Class<T> serviceKlass, String serviceId, ClassLoader classLoader) {
        this.serviceKlass = serviceKlass;
        this.serviceId = serviceId;
        this.classLoader = classLoader;
    }

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
                classLoader,
                new Class[]{serviceKlass}, new RestInvocationHandler(serviceId, restRuntimeService));
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceKlass;
    }
}
