package com.wavemaker.runtime.rest.feign;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.runtime.rest.service.RestRuntimeService;

public class FeignFactoryBean<T> implements FactoryBean<T> {

    private Class<T> serviceKlass;

    private String serviceId;

    private ClassLoader classLoader;

    @Autowired
    private RestRuntimeService restRuntimeService;

    public FeignFactoryBean(Class<T> serviceKlass, String serviceId, ClassLoader classLoader) {
        this.serviceKlass = serviceKlass;
        this.serviceId = serviceId;
        this.classLoader = classLoader;
    }

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
                classLoader,
                new Class[]{serviceKlass}, new FeignInvocationHandler(serviceId, restRuntimeService));
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceKlass;
    }
}
