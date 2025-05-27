/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wavemaker.runtime.core.props;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.DefaultYamlProcessor;
import com.wavemaker.commons.util.PropertiesFileUtils;
import com.wavemaker.runtime.RuntimeEnvironment;

public class WMApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(WMApplicationContextInitializer.class);

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        registerBootstrapPropertySource(applicationContext);
        registerApplicationPropertySource(applicationContext);
        registerRuntimeFrameworkPropertySource(applicationContext);
    }

    private static void registerBootstrapPropertySource(ConfigurableWebApplicationContext applicationContext) {
        String bootstrapPropertySourceParam = applicationContext.getServletContext().getInitParameter("bootstrapPropertySource");
        if (bootstrapPropertySourceParam != null) {
            logger.info("Found bootstrapPropertySourceParam param {}", bootstrapPropertySourceParam);
            try {
                Class<?> bootStrapPropertySourceClass = Class.forName(bootstrapPropertySourceParam, true, applicationContext.getClassLoader());
                Object o = BeanUtils.instantiateClass(bootStrapPropertySourceClass);
                if (o instanceof AbstractBootstrapPropertySource bootstrapPropertySource) {
                    bootstrapPropertySource.init(applicationContext);
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

    private void registerApplicationPropertySource(ConfigurableWebApplicationContext applicationContext) {
        String applicationYamlFile = getPropertiesFile(applicationContext);
        Resource applicationPropertiesResource = applicationContext.getResource("classpath:" + applicationYamlFile);
        if (!applicationPropertiesResource.exists()) {
            throw new WMRuntimeException(
                "Failed to register application property source. The properties file " + applicationYamlFile + " is not found on the classpath.");
        }
        DefaultYamlProcessor defaultYamlProcessor = new DefaultYamlProcessor();
        defaultYamlProcessor.setResources(applicationPropertiesResource);
        Properties properties = defaultYamlProcessor.getProperties();
        applicationContext.getEnvironment().getPropertySources()
            .addLast(new MapPropertySource("applicationPropertySource", buildPropertyMap(properties)));
    }

    private static String getPropertiesFile(ConfigurableWebApplicationContext applicationContext) {
        String activeProfile = applicationContext.getEnvironment().getProperty("spring.profiles.active");
        String applicationPropertiesYamlFile;

        if (RuntimeEnvironment.isTestRunEnvironment() || StringUtils.isBlank(activeProfile)
            || Objects.equals(activeProfile, "development")) {
            applicationPropertiesYamlFile = "application.yaml";
        } else {
            applicationPropertiesYamlFile = "application-" + activeProfile + ".yaml";
        }
        return applicationPropertiesYamlFile;
    }

    private void registerRuntimeFrameworkPropertySource(ConfigurableWebApplicationContext applicationContext) {

        try {
            Properties properties = new Properties();
            Resource[] contextResources = applicationContext.getResources("classpath*:default-runtime-overrides/*.properties");
            if (contextResources.length == 0) {
                throw new WMRuntimeException("Resource [classpath*:default-runtime-overrides/*.properties] not found.");
            }
            for (Resource res : contextResources) {
                try (InputStream in = res.getInputStream()) {
                    properties.putAll(PropertiesFileUtils.loadProperties(in));
                } catch (IOException e) {
                    throw new WMRuntimeException("Failed to load properties from " + res.getFilename(), e);
                }
            }
            applicationContext.getEnvironment().getPropertySources()
                .addLast(new MapPropertySource("runtimeFrameworkPropertySource", buildPropertyMap(properties)));
        } catch (IOException e) {
            throw new WMRuntimeException("Failed to register runtimeFrameworkPropertySource.", e);
        }
    }

    private static Map<String, Object> buildPropertyMap(Properties prefabYamlProperties) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (String name : prefabYamlProperties.stringPropertyNames()) {
            propertyMap.put(name, prefabYamlProperties.getProperty(name));
        }
        return propertyMap;
    }
}