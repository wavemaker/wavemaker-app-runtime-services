package com.wavemaker.runtime.core.props;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public abstract class AbstractBootstrapPropertySource extends PropertySource implements EnvironmentAware {

    private Environment environment;

    public AbstractBootstrapPropertySource() {
        super("bootstrapPropertySource");
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected Environment getEnvironment() {
        return environment;
    }
}
