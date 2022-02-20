package com.wavemaker.runtime.core.props;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public abstract class AbstractBootstrapPropertySource extends PropertySource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBootstrapPropertySource.class);

    private ConfigurableEnvironment environment;

    public AbstractBootstrapPropertySource() {
        super("applicationBootstrapProperties");
    }

    /**
     * Default implementation adds the dynamic property source as the first element in property sources list.
     * subclasses can override this method for more fine-grained control
     */
    protected void init(ConfigurableEnvironment environment) {
        this.environment = environment;
        environment.getPropertySources().addFirst(this);
        logger.info("Added new bootstrap property source {} as the first property source", getName());
    }

    protected Environment getEnvironment() {
        return environment;
    }
}
