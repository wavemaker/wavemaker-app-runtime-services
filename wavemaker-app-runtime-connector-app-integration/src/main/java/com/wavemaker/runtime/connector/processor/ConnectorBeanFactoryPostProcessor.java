package com.wavemaker.runtime.connector.processor;

import com.wavemaker.runtime.connector.annotation.WMConnector;
import com.wavemaker.runtime.connector.factorybean.ConnectorFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import javax.servlet.ServletContext;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 9/3/20
 */

public class ConnectorBeanFactoryPostProcessor implements BeanFactoryPostProcessor {


    private static final Logger logger = LoggerFactory.getLogger(ConnectorBeanFactoryPostProcessor.class);

    private ClassLoader wmAppBaseClassLoader;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        logger.info("Loading connectors proxy bean definition in bean factory post processor");

        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader appClassLoader = getWMAppBaseClassLoader(beanFactory,currentClassLoader);
        try {
            Thread.currentThread().setContextClassLoader(appClassLoader);
            for (String beanName : beanDefinitionNames) {
                Class<?> aClass = null;
                String beanClassName = beanFactory.getBeanDefinition(beanName).getBeanClassName();
                try {
                    aClass = appClassLoader.loadClass(beanClassName);
                    //aClass = Class.forName(beanClassName);
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
                                WMConnector wmConnector = (WMConnector) annotation;
                                if (!doesBeanDefintionExist(beanFactory, wmConnector)) {
                                    GenericBeanDefinition bd = new GenericBeanDefinition();
                                    bd.setBeanClass(ConnectorFactoryBean.class);
                                    ConstructorArgumentValues values = new ConstructorArgumentValues();
                                    values.addIndexedArgumentValue(0, wmConnector.name());
                                    values.addIndexedArgumentValue(1, field.getType());
                                    bd.setConstructorArgumentValues(values);
                                    ((DefaultListableBeanFactory) beanFactory)
                                            .registerBeanDefinition(wmConnector.name(), bd);

                                    logger.info("Bean definition is loaded for connector {0} in bean class {1}", wmConnector.name(), beanClassName);
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

    private boolean doesBeanDefintionExist(ConfigurableListableBeanFactory beanFactory, WMConnector wmConnector) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(wmConnector.name());
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
        return true;
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
                    wmAppBaseClassLoader = new URLClassLoader(urls,currentClassLoader);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Failed to build application class loader ", e);
                }

            }
            return wmAppBaseClassLoader;
        }
    }
}