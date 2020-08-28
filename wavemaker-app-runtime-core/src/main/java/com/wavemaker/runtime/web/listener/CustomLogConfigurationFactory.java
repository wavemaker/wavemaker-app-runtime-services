package com.wavemaker.runtime.web.listener;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.status.StatusLogger;

public class CustomLogConfigurationFactory extends ConfigurationFactory {

    private static final Logger logger = StatusLogger.getLogger();
    @Override
    protected String[] getSupportedTypes() {
        return new String[] {".xml","*"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
        logger.info("Initializing the WaveMaker specific customXML configuration class");
        return new CustomXmlLogConfiguration(loggerContext, configurationSource);
    }
}
