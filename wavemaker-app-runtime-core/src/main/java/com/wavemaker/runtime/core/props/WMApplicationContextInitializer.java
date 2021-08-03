package com.wavemaker.runtime.core.props;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.wavemaker.commons.WMRuntimeException;

public class WMApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(WMApplicationContextInitializer.class);

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        String bootstrapPropertySourceParam = applicationContext.getServletContext().getInitParameter("bootstrapPropertySource");
        if (bootstrapPropertySourceParam != null) {
            logger.info("Found bootstrapPropertySourceParam param {}", bootstrapPropertySourceParam);
            try {
                Class<?> bootStrapPropertySourceClass = Class.forName(bootstrapPropertySourceParam, true, applicationContext.getClassLoader());
                Object o = BeanUtils.instantiateClass(bootStrapPropertySourceClass);
                if (o instanceof AbstractBootstrapPropertySource) {
                    AbstractBootstrapPropertySource bootstrapPropertySource = (AbstractBootstrapPropertySource) o;
                    ConfigurableEnvironment environment = applicationContext.getEnvironment();
                    bootstrapPropertySource.init(environment);
                } else {
                    throw new WMRuntimeException("Parameter 'bootstrapPropertySource' " + bootstrapPropertySourceParam +
                            " should extend " + AbstractBootstrapPropertySource.class.getName());
                }
            } catch (Exception e) {
                throw new WMRuntimeException("Failed to instantiate bootstrap property source class " + bootstrapPropertySourceParam, e);
            }
        } else {
            logger.debug("bootstrapPropertySourceParam not found");
        }
    }
}
