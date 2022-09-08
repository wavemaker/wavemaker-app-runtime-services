/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wavemaker.runtime.connector.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.wavemaker.runtime.connector.annotation.WMConnector;
import com.wavemaker.runtime.connector.factorybean.ConnectorFactoryBean;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 9/3/20
 */

public class ConnectorBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final String APP_PROPERTIES = "/app.properties";
    private static final String CONNECTOR_PROPERTY_PREFIX = "connector.";
    private static final String CONNECTOR_PROPERTY_SEPARATOR = ".";
    private static final String DEFAULT_CONNECTOR_CONFIGURATION_ID = "default";
    private static final List<String> WHITELISTED_PACKAGE_PREFIX = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(ConnectorBeanFactoryPostProcessor.class);

    static {
        WHITELISTED_PACKAGE_PREFIX.add("org.springframework.");
    }

    private Environment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        logger.info("Loading connectors proxy bean definition in bean factory post processor");

        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        ServletContext servletContext = beanFactory.getBean(ServletContext.class);
        ClassLoader appClassLoader = servletContext.getClassLoader();
        if(appClassLoader == Thread.currentThread().getContextClassLoader())
        {
            for (String beanName : beanDefinitionNames) {
                Class<?> aClass = null;
                String beanClassName = getBeanClassName(beanName, beanFactory);
                if (beanClassName == null && execludeWhiteListBean(beanName)) {
                    continue;
                } else if (beanClassName == null) {
                    throw new RuntimeException("Unable to derive bean class name from bean definition for bean name" + beanName);
                }
                try {
                    aClass = appClassLoader.loadClass(beanClassName);
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to load bean class {} ", beanClassName);
                    throw new RuntimeException("Failed to load bean class " + beanClassName, e);
                }
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (!field.getType().isPrimitive()) {
                        Annotation[] annotations = field.getType().getAnnotations();
                        for (Annotation annotation : annotations) {
                            if (annotation.annotationType().equals(WMConnector.class)) {
                                logger.info("Identified a connector declaration {} in class {}", field.getType().getName(), beanClassName);
                                WMConnector wmConnector = (WMConnector) annotation;
                                String qualifierName = findQualifierName(field.getAnnotations());
                                String connectorBeanName = qualifierName == null ? field.getType().getSimpleName() : qualifierName;
                                String configurationId = qualifierName == null ? DEFAULT_CONNECTOR_CONFIGURATION_ID : qualifierName;
                                if (!doesBeanDefinitionExist(beanFactory, field.getType(), connectorBeanName)) {
                                    AnnotatedGenericBeanDefinition bd = new AnnotatedGenericBeanDefinition(ConnectorFactoryBean.class);
                                    ConstructorArgumentValues values = new ConstructorArgumentValues();
                                    values.addIndexedArgumentValue(0, wmConnector.name());
                                    values.addIndexedArgumentValue(1, configurationId);
                                    values.addIndexedArgumentValue(2, field.getType());
                                    Properties properties = loadProperties(wmConnector.name(), configurationId);
                                    values.addIndexedArgumentValue(3, properties);
                                    boolean primary = configurationId.equals(DEFAULT_CONNECTOR_CONFIGURATION_ID) ? true : false;
                                    // when there are two beans of same connector with one has qualifier and one doesn't
                                    bd.setPrimary(primary);
                                    bd.setConstructorArgumentValues(values);
                                    // if bean have qualifier then bean name is qualifier name, if it doesn't have qualifier then assigning bean class name as bean name.
                                    ((DefaultListableBeanFactory) beanFactory)
                                            .registerBeanDefinition(connectorBeanName, bd);
                                    logger.info("Bean definition is loaded for connector {} in bean class {}", field.getType().getName(), beanClassName);
                                }
                            }
                        }

                    } }

            }
        }

        logger.info("Loaded connectors proxy bean definitions");
    }

    private boolean execludeWhiteListBean(String beanName) {
        for (String packagePrefix : WHITELISTED_PACKAGE_PREFIX) {
            if (beanName.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesBeanDefinitionExist(ConfigurableListableBeanFactory beanFactory, Class<?> type, String beanName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            // if a connector having two connector interfaces which has WMConnector annotation with the same name
            if (beanDefinition.getBeanClassName().equals(ConnectorFactoryBean.class.getName())) {
                ConstructorArgumentValues.ValueHolder argumentValue = beanDefinition.getConstructorArgumentValues().getArgumentValue(2, type);
                if (type != argumentValue.getValue()) {
                    return false;
                }
            }
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
        return true;
    }

    private String findQualifierName(Annotation[] annotations) {
        String value = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Qualifier.class)) {
                Qualifier qualifier = (Qualifier) annotation;
                value = qualifier.value();
                break;
            }
        }
        return value;
    }

    private Properties loadProperties(String connectorName, String connectorId) {
        InputStream is = null;

        Properties prop;
        try {
            prop = new Properties();
            is = this.getClass().getResourceAsStream(APP_PROPERTIES);
            prop.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read app.properties file from classpath", e);
        }
        Properties filteredProperties = new Properties();
        String prefix = CONNECTOR_PROPERTY_PREFIX + connectorName + CONNECTOR_PROPERTY_SEPARATOR + connectorId + CONNECTOR_PROPERTY_SEPARATOR;
        for (String key : prop.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                filteredProperties.setProperty(key.substring(prefix.length()), environment.getProperty(key));
            }
        }
        return filteredProperties;
    }

    private String getBeanClassName(String beanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName != null) {
            return beanClassName;
        } else if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
            beanClassName = metadata.getClassName();
        } else {
            beanClassName = beanDefinition.getBeanClassName();
            while (beanClassName == null && beanDefinition.getParentName() != null) {
                BeanDefinition parentBeanDefinition = beanFactory.getBeanDefinition(beanDefinition.getParentName());
                beanClassName = parentBeanDefinition.getBeanClassName();
                beanDefinition = parentBeanDefinition;
            }
        }
        return beanClassName;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

