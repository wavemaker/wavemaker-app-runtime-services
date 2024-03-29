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
package com.wavemaker.runtime.connector.configuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 30/5/20
 */
public abstract class ConnectorConfigurationBase {

    public static final String CONNECTOR_LOCAL_PROPERTIES = "connector-local.properties";
    public static final String CONNECTOR_EXTERNALIZABLE_PROPERTIES = "connector-externalizable.properties";

    @Bean(name = "propertySourcesPlaceholderConfigurer")
    public PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer placeholderConfigurer
            = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = getClassPathResources();
        placeholderConfigurer.setLocations(resources);
        placeholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return placeholderConfigurer;
    }

    public abstract List<Resource> getClasspathPropertyResources();

    private Resource[] getClassPathResources() {
        List<Resource> resourceList = new ArrayList<>();
        if (resourceExist(CONNECTOR_LOCAL_PROPERTIES)) {
            resourceList.add(new ClassPathResource(CONNECTOR_LOCAL_PROPERTIES));
        }
        if (resourceExist(CONNECTOR_EXTERNALIZABLE_PROPERTIES)) {
            resourceList.add(new ClassPathResource(CONNECTOR_EXTERNALIZABLE_PROPERTIES));
        }
        resourceList.addAll(getClasspathPropertyResources());
        return toArray(resourceList);
    }

    private Resource[] toArray(List<Resource> resourceList) {
        Resource[] resources = new Resource[resourceList.size()];
        for (int i = 0; i < resourceList.size(); i++) {
            resources[i] = resourceList.get(i);
        }
        return resources;
    }

    private boolean resourceExist(String resourceName) {
        URL resource = this.getClass().getClassLoader().getResource(resourceName);
        if (resource == null) {
            return false;
        }
        return true;
    }
}
