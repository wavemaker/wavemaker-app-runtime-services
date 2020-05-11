package com.wavemaker.runtime.connector.factorybean;


import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import com.wavemaker.runtime.connector.classloader.WMConnectorClassLoaderProvider;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 20/2/20
 */

public class ConnectorFactoryBean<T> implements FactoryBean<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorFactoryBean.class);
    private Class<T> serviceKlass;
    private String connectorId;

    private ClassLoader cacheConnectorClassLoader;

    @Autowired
    private WMConnectorClassLoaderProvider wmConnectorClassLoaderProvider;

    public ConnectorFactoryBean(String connectorId, Class<T> serviceKlass) {
        this.connectorId = connectorId;
        this.serviceKlass = serviceKlass;
    }

    @Override
    public T getObject() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(getObjectType());

        enhancer.setCallbacks(new Callback[]{
                (MethodInterceptor) (o, method, methodArgs, methodProxy) -> {
                    logger.info("Intercepting connector api invocation");
                    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                    try {

                        //if (cacheConnectorClassLoader == null) {
                            logger.info("Building connector impl url class loader ");
                            cacheConnectorClassLoader = wmConnectorClassLoaderProvider.getClassLoader(connectorId, currentClassLoader);
                        //}

                        logger.info("Setting connector classloader {0} to current thread ", cacheConnectorClassLoader);
                        Thread.currentThread().setContextClassLoader(cacheConnectorClassLoader);


                        Class<?> aClass = cacheConnectorClassLoader.loadClass("com.wavemaker.runtime.connector.ConnectorAPIInvoker");
                        Object o1 = aClass.newInstance();
                        Method getInstanceMethod = aClass.getMethod("invokeMethod", Method.class, Object[].class);
                        return getInstanceMethod.invoke(o1, method, methodArgs);

                    } catch (Exception e) {
                        throw new RuntimeException("Exception during intercepting connector api invocation ", e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(currentClassLoader);
                        logger.info("Intercepting connector api invocation is completed");
                    }
                }
        });
        return (T) enhancer.create();
    }

    @Override
    public Class<?> getObjectType() {
        return serviceKlass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<T> getServiceKlass() {
        return serviceKlass;
    }

    public String getConnectorId() {
        return connectorId;
    }

}

