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
package com.wavemaker.runtime.prefab.context;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import jakarta.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.DefaultYamlProcessor;
import com.wavemaker.commons.util.PropertiesFileUtils;
import com.wavemaker.runtime.prefab.core.Prefab;

/**
 * @author Dilip Kumar
 */
public class PrefabWebApplicationContext extends XmlWebApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(PrefabWebApplicationContext.class);

    public PrefabWebApplicationContext(final Prefab prefab, final ApplicationContext parent, final ServletContext servletContext) {
        setId(prefab.getName());
        setParent(parent);
        ClassLoader prefabClassLoader = prefab.getClassLoader();

        // This removes the parent properties added and creates new property sources,
        // as we want this context to have its own property sources and not get the parent properties
        setEnvironment(loadEnvironment(parent, prefabClassLoader));

        setClassLoader(prefabClassLoader);
        setServletContext(servletContext);
        setDisplayName("Prefab Context [" + prefab.getName() + "]");
        setConfigLocations("classpath:prefab-springapp.xml");
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(prefabClassLoader);
            refresh();
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
        }
    }

    private static ConfigurableEnvironment loadEnvironment(ApplicationContext applicationContext, ClassLoader prefabClassLoader) {
        log.info("Preparing environment for prefab context");
        ConfigurableEnvironment configurableEnvironment = new StandardServletEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        loadPrefabOverriddenProperties(prefabClassLoader, getPrefabOverriddenPropertiesFile(applicationContext), propertySources);
        loadPrefabBundleProperties(prefabClassLoader, propertySources);
        return configurableEnvironment;
    }

    private static String getPrefabOverriddenPropertiesFile(ApplicationContext applicationContext) {
        String activeProfile = applicationContext.getEnvironment().getProperty("spring.profiles.active");
        String prefabOverriddenPropertiesFile;
        if (StringUtils.isBlank(activeProfile) || Objects.equals(activeProfile, "development") || Objects.equals(activeProfile, "wm_preview")) {
            prefabOverriddenPropertiesFile = "prefab-overridden.properties";
        } else {
            prefabOverriddenPropertiesFile = "prefab-overridden-" + activeProfile + ".properties";
        }
        return prefabOverriddenPropertiesFile;
    }

    private static void loadPrefabOverriddenProperties(ClassLoader prefabClassLoader, String prefabOverriddenPropertiesFile, MutablePropertySources propertySources) {
        Properties properties;
        try (InputStream inputStream = prefabClassLoader.getResourceAsStream(prefabOverriddenPropertiesFile)) {
            properties = PropertiesFileUtils.loadProperties(inputStream);
        } catch (Exception e) {
            throw new WMRuntimeException(e);
        }
        Map<String, Object> propertyMap = buildPropertyMap(properties);
        propertySources.addLast(new MapPropertySource("appPrefabOverriddenPropertySource", propertyMap));
    }

    private static void loadPrefabBundleProperties(ClassLoader prefabClassLoader, MutablePropertySources propertySources) {
        InputStream resourceAsStream = prefabClassLoader.getResourceAsStream("prefab-properties.yaml");
        if (resourceAsStream == null) {
            throw new WMRuntimeException("Failed to load prefabBundlePropertySource as prefab-properties.yaml is not found");
        }
        Resource prefabPropetiesYamlResource = new InputStreamResource(resourceAsStream);
        log.info("initialising prefab property source");
        DefaultYamlProcessor defaultYamlProcessor = new DefaultYamlProcessor();
        defaultYamlProcessor.setResources(prefabPropetiesYamlResource);
        Properties prefabYamlProperties = defaultYamlProcessor.getProperties();
        Map<String, Object> propertyMap = buildPropertyMap(prefabYamlProperties);
        propertySources.addLast(new MapPropertySource("prefabBundlePropertySource", propertyMap));
    }

    private static Map<String, Object> buildPropertyMap(Properties prefabYamlProperties) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (String name : prefabYamlProperties.stringPropertyNames()) {
            propertyMap.put(name, prefabYamlProperties.getProperty(name));
        }
        return propertyMap;
    }
}
