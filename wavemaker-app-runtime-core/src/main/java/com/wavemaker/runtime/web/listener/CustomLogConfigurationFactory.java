package com.wavemaker.runtime.web.listener;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;

public class CustomLogConfigurationFactory extends ConfigurationFactory {
    @Override
    protected String[] getSupportedTypes() {
        return new String[] {".xml","*"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
        return new CustomXmlLogConfiguration(loggerContext, configurationSource);
    }
}
