package com.wavemaker.runtime.core.props;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public abstract class AbstractDynamicPropertySource extends PropertySource implements EnvironmentAware {

    protected AbstractDynamicPropertySource() {
        super("applicationDynamicProperties");
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
            addPropertySource(configurableEnvironment, this);
        }
    }

    /**
     * Default implementation adds the dynamic property source as the first element in property sources list.
     * subclasses can override this method for more fine-grained control
     */
    protected void addPropertySource(ConfigurableEnvironment configurableEnvironment, PropertySource propertySource) {
        configurableEnvironment.getPropertySources().addFirst(propertySource);
    }
}
