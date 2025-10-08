/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.prefab.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wavemaker.commons.WMRuntimeException;

public class AppRuntimePropertiesUtils {
    private static final Logger logger = LoggerFactory.getLogger(AppRuntimePropertiesUtils.class);
    private static final String APPLICATION_YAML_PATH = "application.yaml";
    private static final String PREFAB_PROPERTIES_PATH = "overridden/prefab.properties";
    private static final String APPLICATION_YAML_PATH_PREFIX = "application-";
    private static final String PREFAB_PROPERTIES_PATH_PREFIX = "prefab-";
    private static final String YAML_EXT = ".yaml";
    private static final String PROPERTIES_EXT = ".properties";

    private AppRuntimePropertiesUtils() {
    }

    public static String resolveApplicationYamlPath(String activeProfile, ClassLoader classLoader) {
        String propertyFilePath = resolvePropertyFilePath(
            activeProfile, classLoader,
            APPLICATION_YAML_PATH,
            "", APPLICATION_YAML_PATH_PREFIX, YAML_EXT);
        logger.info("Loading application properties from '{}' for active profile '{}'", propertyFilePath, activeProfile);
        return propertyFilePath;
    }

    public static String resolvePrefabPropertiesPath(String activeProfile, ClassLoader prefabClassLoader) {
        return resolvePropertyFilePath(
            activeProfile, prefabClassLoader,
            PREFAB_PROPERTIES_PATH,
            "overridden/", PREFAB_PROPERTIES_PATH_PREFIX, PROPERTIES_EXT);
    }

    private static String resolvePropertyFilePath(
        String activeProfile, ClassLoader classLoader,
        String defaultPropertyFilePath,
        String directory, String pathPrefix, String pathSuffix) {

        boolean defaultPropertyFileExists = resourceExists(classLoader, defaultPropertyFilePath);
        if (StringUtils.isBlank(activeProfile)) {
            logger.debug("Spring active profile not set");
            if (defaultPropertyFileExists) {
                logger.debug("Using default property file: {}", defaultPropertyFilePath);
                return defaultPropertyFilePath;
            }
        } else {
            logger.debug("Spring active profile set to '{}'", activeProfile);
            String profileSpecificFile = directory + pathPrefix + activeProfile + pathSuffix;

            if (resourceExists(classLoader, profileSpecificFile)) {
                logger.debug("Found profile-specific property file: {}", profileSpecificFile);
                return profileSpecificFile;
            } else if (defaultPropertyFileExists) {
                logger.warn("Profile-specific property file not found. Falling back to default file: {}", defaultPropertyFilePath);
                return defaultPropertyFilePath;
            }
        }

        List<String> availableProfiles = getAvailableProfiles(classLoader, directory, pathPrefix, pathSuffix);
        if (availableProfiles.isEmpty()) {
            throw new WMRuntimeException("Missing " + defaultPropertyFilePath + " file.");
        }

        throw new WMRuntimeException("No property file found for active profile '" + activeProfile + "'. " +
            "Expected spring.profiles.active property to be set with one of the values [" + String.join(", ", availableProfiles) + "]");
    }

    private static List<String> getAvailableProfiles(ClassLoader classLoader, String directory, String prefix, String suffix) {
        try {
            Enumeration<URL> resources = classLoader.getResources(directory);
            List<String> availableProfiles = new ArrayList<>();

            while (resources.hasMoreElements()) {
                URL dirUrl = resources.nextElement();
                File dir = new File(dirUrl.getPath());
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((d, name) ->
                        name.startsWith(prefix) && name.endsWith(suffix));
                    if (files != null) {
                        for (File file : files) {
                            availableProfiles.add(file.getName()
                                .replace(prefix, "")
                                .replace(suffix, ""));
                        }
                    }
                }
            }
            return availableProfiles;
        } catch (IOException e) {
            throw new WMRuntimeException("Failed to read property files from classpath", e);
        }
    }

    private static boolean resourceExists(ClassLoader classLoader, String resourcePath) {
        return classLoader.getResource(resourcePath) != null;
    }
}
