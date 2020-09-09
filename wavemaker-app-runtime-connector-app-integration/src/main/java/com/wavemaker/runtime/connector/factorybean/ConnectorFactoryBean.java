/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.connector.factorybean;


import java.lang.reflect.InvocationTargetException;
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
import com.wavemaker.runtime.connector.context.ConnectorContextProvider;
import com.wavemaker.runtime.connector.metadata.ConnectorMetadata;

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
    private ConnectorContextProvider contextProvider;

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
                        ConnectorContext connectorContext = contextProvider.get(connectorId, currentClassLoader);

                        //step2: set impl classloader to current thread
                        logger.info("Setting connector classloader {0} to current thread ", connectorContext.getClassLoader());
                        Thread.currentThread().setContextClassLoader(connectorContext.getClassLoader());

                        //step3: load connectormain which will build spring context and cache it for further request and get an instance of impl bean
                        implBeanObject = getImplBeanObject(method, connectorContext.getClassLoader(), connectorContext.getConnectorMetadata());

                        //step4: execute bean method
                        return method.invoke(implBeanObject, methodArgs);

                    } catch (InvocationTargetException e) {
                        logger.error("Exception during intercepting api invocation for connector {0} ", connectorId);
                        throw new RuntimeException("Exception during intercepting connector api invocation ", e.getTargetException());
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
            implBeanObject = (T) getBeanMethod.invoke(null, connectorId, configurationId, connectorMetadata.getSpringConfigurationClass(), connectorProperties, method.getDeclaringClass().getName());
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


