package com.wavemaker.runtime.rest.feign;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.runtime.rest.service.RestRuntimeService;

public class FeignFactoryBean<T> implements FactoryBean<T> {

    private Class<T> serviceKlass;

    private String serviceId;

    @Autowired
    private RestRuntimeService restRuntimeService;

    public FeignFactoryBean(Class<T> serviceKlass, String serviceId) {
        this.serviceKlass = serviceKlass;
        this.serviceId = serviceId;
    }

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
                FeignFactoryBean.class.getClassLoader(),
                new Class[]{serviceKlass}, new FeignInvocationHandler(serviceId, restRuntimeService));
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceKlass;
    }
}
