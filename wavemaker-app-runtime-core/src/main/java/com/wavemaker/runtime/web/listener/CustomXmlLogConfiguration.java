/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.web.listener;

import java.util.Map;
import java.util.zip.Deflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;

public class CustomXmlLogConfiguration extends XmlConfiguration {
    private static final Logger logger = StatusLogger.getLogger();

    private LoggerContext loggerContext;

    public CustomXmlLogConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
        super(loggerContext, configSource);
        this.loggerContext = loggerContext;
    }

    @Override
    protected void doConfigure() {
        super.doConfigure();
        logger.info("Configuring custom WaveMaker appenders and removing existing appenders");
        Configuration configuration = loggerContext.getConfiguration();

        /*
         * * Adding newly created appender to loggerConfiguration
         * */
        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(configuration)
                .withPattern("%d{dd MMM yyyy HH:mm:ss,SSS} -%X{wm.app.name} -%X{X-WM-Request-Track-Id} %t %p [%c] - %encode{%m}{CRLF}%n")
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
