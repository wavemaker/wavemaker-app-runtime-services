package com.wavemaker.runtime.connector.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.wavemaker.runtime.connector.annotation.WMConnector;
import com.wavemaker.runtime.connector.factorybean.ConnectorFactoryBean;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 9/3/20
 */

public class ConnectorBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    public static final String APP_PROPERTIES = "/app.properties";
    public static final String CONNECTOR_PROPERTY_PREFIX = "connector.";
    public static final String CONNECTOR_PROPERTY_SEPERATOR = ".";
    private static final Logger logger = LoggerFactory.getLogger(ConnectorBeanFactoryPostProcessor.class);
    private ClassLoader wmAppBaseClassLoader;

    private Environment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        logger.info("Loading connectors proxy bean definition in bean factory post processor");

        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader appClassLoader = getWMAppBaseClassLoader(beanFactory, currentClassLoader);
        try {
            Thread.currentThread().setContextClassLoader(appClassLoader);
            for (String beanName : beanDefinitionNames) {
                Class<?> aClass = null;
                String beanClassName = beanFactory.getBeanDefinition(beanName).getBeanClassName();
                try {
                    aClass = appClassLoader.loadClass(beanClassName);
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to load bean class {0} ", beanClassName);
                    throw new RuntimeException("Failed to load bean class " + beanClassName, e);
                }
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (!field.getType().isPrimitive()) {
                        Annotation[] annotations = field.getType().getAnnotations();
                        for (Annotation annotation : annotations) {
                            if (annotation.annotationType().equals(WMConnector.class)) {
                                logger.info("Identified a connector declaration {0} in class {1}", field.getType().getName(), beanClassName);
                                WMConnector wmConnector = (WMConnector) annotation;
                                String qualifierName = findQualifierName(field.getAnnotations(), wmConnector.name());
                                if (!doesBeanDefinitionExist(beanFactory, field.getType(), qualifierName)) {
                                    AnnotatedGenericBeanDefinition bd = new AnnotatedGenericBeanDefinition(ConnectorFactoryBean.class);
                                    ConstructorArgumentValues values = new ConstructorArgumentValues();
                                    values.addIndexedArgumentValue(0, wmConnector.name());
                                    values.addIndexedArgumentValue(1, qualifierName);
                                    values.addIndexedArgumentValue(2, field.getType());
                                    Properties properties = loadProperties(qualifierName, wmConnector.name());
                                    values.addIndexedArgumentValue(3, properties);
                                    boolean primary = qualifierName.equals(wmConnector.name()) ? true : false;
                                    // when there are two beans of same connector with one has qualifier and one doesn't
                                    bd.setPrimary(primary);
                                    bd.setConstructorArgumentValues(values);
                                    // if bean have qualifier then bean name is qualifiername, if it doesn't have qualifier then assigning bean class name as bean name.
                                    String connectorBeanName = primary ? field.getType().getSimpleName() : qualifierName;
                                    ((DefaultListableBeanFactory) beanFactory)
                                            .registerBeanDefinition(connectorBeanName, bd);
                                    logger.info("Bean definition is loaded for connector {0} in bean class {1}", field.getType().getName(), beanClassName);
                                }
                            }
                        }

                    }

                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

        logger.info("Loaded connectors proxy bean definitions");
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

    private String findQualifierName(Annotation[] annotations, String defaultName) {
        String value = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Qualifier.class)) {
                Qualifier qualifier = (Qualifier) annotation;
                value = qualifier.value();
            }
        }
        return value == null ? defaultName : value;
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
        String prefix = CONNECTOR_PROPERTY_PREFIX + connectorId + CONNECTOR_PROPERTY_SEPERATOR + connectorName + CONNECTOR_PROPERTY_SEPERATOR;
        for (String key : prop.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                filteredProperties.setProperty(key.substring(prefix.length()), environment.getProperty(key));
            }
        }
        return filteredProperties;
    }

    private ClassLoader getWMAppBaseClassLoader(ConfigurableListableBeanFactory beanFactory, ClassLoader currentClassLoader) {
        synchronized (this) {
            if (wmAppBaseClassLoader == null) {
                ServletContext servletContext = beanFactory.getBean(ServletContext.class);
                try {
                    URL url = servletContext.getResource("/WEB-INF/lib/");
                    String path = url.getPath();
                    File[] fList = new File(path).listFiles();
                    URL[] urls = new URL[fList.length];
                    int i = 0;
                    for (File file : fList) {
                        urls[i] = file.toURI().toURL();
                        i++;
                    }
                    wmAppBaseClassLoader = new URLClassLoader(urls, currentClassLoader);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Failed to build application class loader ", e);
                }

            }
            return wmAppBaseClassLoader;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

