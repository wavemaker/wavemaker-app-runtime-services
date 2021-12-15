package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;

interface CRUDMethodInvocationHandler {
    void preHandle(String serviceId, Class entityClass, Method method, Object[] args);

    void postHandle(String serviceId, Class entityClass, Method method, Object retVal);

    boolean matches(Class entityClass, Method method);
}