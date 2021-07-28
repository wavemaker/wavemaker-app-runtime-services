package com.wavemaker.runtime.core.props;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public abstract class AbstractAppCustomPropertySource extends PropertySource implements EnvironmentAware {

    protected AbstractAppCustomPropertySource() {
        super("appCustomPropertySource");
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
            configurableEnvironment.getPropertySources().addFirst(this);
        }
    }
}
