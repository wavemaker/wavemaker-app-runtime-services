package com.wavemaker.runtime.connector.main;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.wavemaker.runtime.connector.cache.ConnectorCache;
import com.wavemaker.runtime.connector.cache.ConnectorConfiguration;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 29/4/20
 */
public class ConnectorMain {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorMain.class);

    public static Object getBean(String connectorId, String configurationId, String connectorSpringConfigurationClass, Properties properties, String beanClassName) {
        ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration(connectorId, configurationId);
        logger.info("Loading spring applicationContext from cache for connector {0} {1}", connectorId, configurationId);
        if (ConnectorCache.get(connectorConfiguration) == null) {
            ConnectorCache.put(connectorConfiguration, prepareSpringContext(properties, connectorSpringConfigurationClass));
        }
        try {
            return ConnectorCache.get(connectorConfiguration).getBean(Class.forName(beanClassName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load connector impl bean", e);
        }
    }


    private static ApplicationContext prepareSpringContext(Properties properties, String connectorSpringConfigurationClass) {
        logger.info("Loading Impl Spring context from connector spring metadata class {0}", connectorSpringConfigurationClass);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(getImplSpringConfigClazz(connectorSpringConfigurationClass));
        context.getEnvironment().getPropertySources().addLast(new PropertiesPropertySource("connector", properties));
        context.refresh();
        return context;
    }

    private static Class<?> getImplSpringConfigClazz(String connectorSpringConfigurationClass) {
        Class<?> implConfigClazz;
        try {
            implConfigClazz = Thread.currentThread().getContextClassLoader().loadClass(connectorSpringConfigurationClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load connector spring metadata class " + connectorSpringConfigurationClass, e);
        }
        return implConfigClazz;
    }

}
