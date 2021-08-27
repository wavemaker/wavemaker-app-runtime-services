package com.wavemaker.runtime.security;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.util.PropertiesFileUtils;

//TODO this class right now handles securityService.properties. Ideally there should be common framework to handle bootstrap properties
public class DefaultBootStrapPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBootStrapPropertySourceInitializer.class);

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        applicationContext.getEnvironment().getPropertySources().addLast(new PropertySource<Object>("securityServicePropertySource") {
            @Override
            public Object getProperty(String name) {
                if (name != null && name.startsWith("security.")) {
                    return getSecurityServicesProperty(applicationContext.getClassLoader(), name);
                }
                return null;
            }
        });
    }

    private String getSecurityServicesProperty(ClassLoader classLoader, String name) {
        logger.info("Fetching security services property {}", name);
        ClassPathFile classPathFile = new ClassPathFile(classLoader, "conf/securityService.properties");
        if (classPathFile.exists()) {
            Properties properties = PropertiesFileUtils.loadProperties(classPathFile);
            return properties.getProperty(name);
        }
        return null;
    }
}
