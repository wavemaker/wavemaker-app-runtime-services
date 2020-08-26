package com.wavemaker.runtime.web.listener;

import java.util.Map;
import java.util.zip.Deflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class CustomXmlLogConfiguration extends XmlConfiguration {

    private LoggerContext loggerContext;

    public CustomXmlLogConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
        super(loggerContext, configSource);
        this.loggerContext = loggerContext;
    }

    @Override
    protected void doConfigure() {
        super.doConfigure();
        Configuration configuration = loggerContext.getConfiguration();

        /*
         * * Adding newly created appender to loggerConfiguration
         * */
        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(configuration)
                .withPattern("%d{dd MMM yyyy HH:mm:ss,SSS} -%X{wm.app.name} -%X{X-WM-Request-Track-Id} %t %p [%c] - %m%n")
                .build();

        getAppenders().keySet().forEach(this::removeAppender);

        String fileName = System.getProperty("wm.apps.log", System.getProperty("java.io.tmpdir") + "/apps.log");
        String rollingAppenderName = "RollAppender";
        DefaultRolloverStrategy rolloverStrategy = DefaultRolloverStrategy.newBuilder()
                .withCompressionLevelStr(String.valueOf(Deflater.DEFAULT_COMPRESSION))
                .withConfig(configuration)
                .withMax(String.valueOf(10))
                .build();
        RollingFileAppender rollingFileAppender = RollingFileAppender.newBuilder()
                .setConfiguration(configuration)
                .setName(rollingAppenderName)
                .setLayout(layout)
                .withFileName(fileName)
                .withFilePattern(fileName.replace(".log", "%i.log"))
                .withPolicy(SizeBasedTriggeringPolicy.createPolicy("10MB"))
                .withStrategy(rolloverStrategy)
                .build();
        addAppender(rollingFileAppender);

        /*
         * Updating appenders of all the loggerConfigs configured in the log4j2 config file.
         * */
        Map<String, LoggerConfig> loggerMap = getLoggers();
        for (LoggerConfig loggerConfig : loggerMap.values()) {
            if (!LogManager.ROOT_LOGGER_NAME.equals(loggerConfig.getName())) {
                loggerConfig.setAdditive(false);
            }

            loggerConfig.getAppenders().keySet().forEach(this::removeAppender);
            loggerConfig.addAppender(rollingFileAppender, loggerConfig.getLevel(), loggerConfig.getFilter());
        }
    }
}
