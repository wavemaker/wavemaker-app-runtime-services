package com.wavemaker.runtime.core.props;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.wavemaker.commons.WMRuntimeException;

public class WMXmlWebApplicationContext extends XmlWebApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(WMXmlWebApplicationContext.class);

    @Override
    protected void initPropertySources() {
        super.initPropertySources();

        String bootstrapPropertySourceParam = getServletContext().getInitParameter("bootstrapPropertySource");
        if (bootstrapPropertySourceParam != null) {
            logger.info("Found bootstrapPropertySourceParam param {}", bootstrapPropertySourceParam);
            try {
                Class<?> bootStrapPropertySourceClass = Class.forName(bootstrapPropertySourceParam, true, getClassLoader());
                Object o = BeanUtils.instantiateClass(bootStrapPropertySourceClass);
                if (o instanceof AbstractBootstrapPropertySource) {
                    AbstractBootstrapPropertySource bootstrapPropertySource = (AbstractBootstrapPropertySource) o;
                    ConfigurableEnvironment environment = getEnvironment();
                    bootstrapPropertySource.setEnvironment(environment);
                    environment.getPropertySources().addFirst(bootstrapPropertySource);
                    logger.info("Added new bootstrap property source {} as the first property source", bootStrapPropertySourceClass.getName());
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
