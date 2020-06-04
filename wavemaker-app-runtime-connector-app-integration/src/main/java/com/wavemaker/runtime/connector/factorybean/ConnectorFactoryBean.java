package com.wavemaker.runtime.connector.factorybean;


import java.lang.reflect.Method;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import com.wavemaker.runtime.connector.context.ConnectorContext;
import com.wavemaker.runtime.connector.context.ConnectorContextBuilder;
import com.wavemaker.runtime.connector.configuration.ConnectorMetadata;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 20/2/20
 */

public class ConnectorFactoryBean<T> implements FactoryBean<T> {

    public static final String CONNECTOR_MAIN_CLASS = "com.wavemaker.runtime.connector.main.ConnectorMain";
    private static final Logger logger = LoggerFactory.getLogger(com.wavemaker.runtime.connector.factorybean.ConnectorFactoryBean.class);
    private T implBeanObject;
    private Class<T> serviceKlass;
    private String connectorId;
    private String configurationId;
    private Properties connectorProperties;
    @Autowired
    private ConnectorContextBuilder contextBuilder;

    public ConnectorFactoryBean(String connectorId, String configurationId, Class<T> serviceKlass, Properties properties) {
        this.connectorId = connectorId;
        this.configurationId = configurationId;
        this.serviceKlass = serviceKlass;
        this.connectorProperties = properties;
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

                        //step1 : get connector context which have connector impl classloader and metadata, and they are cached in builder
                        ConnectorContext connectorContext = contextBuilder.build(connectorId, currentClassLoader);

                        //step2: set impl classloader to current thread
                        logger.info("Setting connector classloader {0} to current thread ", connectorContext.getClassLoader());
                        Thread.currentThread().setContextClassLoader(connectorContext.getClassLoader());

                        //step3: load connectormain which will build spring context and cache it for further request and get an instance of impl bean
                        implBeanObject = getImplBeanObject(method, connectorContext.getClassLoader(), connectorContext.getConnectorMetadata());

                        //step4: execute bean method
                        return method.invoke(implBeanObject, methodArgs);

                    } catch (Exception e) {
                        logger.error("Exception during intercepting api invocation for connector {0} ", connectorId);
                        throw new RuntimeException("Exception during intercepting connector api invocation ", e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(currentClassLoader);
                        logger.info("Intercepting connector api invocation is completed");
                    }
                }
        });
        return (T) enhancer.create();
    }

    private T getImplBeanObject(Method method, ClassLoader connectorClassLoader, ConnectorMetadata connectorMetadata) throws ClassNotFoundException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        if (implBeanObject == null) {
            Class<?> aClass = connectorClassLoader.loadClass(CONNECTOR_MAIN_CLASS);
            Method getBeanMethod = aClass.getMethod("getBean", String.class, String.class, String.class, Properties.class, String.class);
            implBeanObject = (T) getBeanMethod.invoke(null, connectorId, configurationId, connectorMetadata.getConfigurationclass(), connectorProperties, method.getDeclaringClass().getName());
        }
        return implBeanObject;
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

}


