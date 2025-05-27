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
import java.util.Objects;
import java.util.Properties;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.commons.properties.EnvironmentRegisteringPropertySourcesPlaceHolderConfigurer;
import com.wavemaker.commons.util.DefaultYamlProcessor;
import com.wavemaker.commons.util.StringUtils;
import com.wavemaker.commons.util.SystemUtils;
import com.wavemaker.runtime.RuntimeEnvironment;
import com.wavemaker.runtime.data.util.DataServiceConstants;
import com.wavemaker.runtime.data.util.DataServiceUtils;

public class AppPropertySourcesPlaceHolderConfigurer extends EnvironmentRegisteringPropertySourcesPlaceHolderConfigurer implements ServletContextAware {

    private static final String DEVELOPMENT = "development";
    private static final String APPLICATION_YAML = "application.yaml";
    private ServletContext servletContext;

    private Environment environment;

    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       final StringValueResolver valueResolver) {

        StringValueResolver updatedValueResolver = strVal -> convertPropertyValue(valueResolver.resolveStringValue(strVal));
        super.doProcessProperties(beanFactoryToProcess, updatedValueResolver);
    }

    /**
     * This method loads the properties from the yaml files and also call super to load the properties from a properties file
     * and adds them to the properties object.
     */

    @Override
    protected void loadProperties(Properties props) throws IOException {
        String activeProfile = environment.getProperty("spring.profiles.active");
        DefaultYamlProcessor defaultYamlProcessor = new DefaultYamlProcessor();
        String filePath;
        if (RuntimeEnvironment.isTestRunEnvironment() || org.apache.commons.lang3.StringUtils.isBlank(activeProfile)
            || Objects.equals(activeProfile, DEVELOPMENT)) {
            filePath = APPLICATION_YAML;
        } else {
            filePath = "application-" + activeProfile + ".yaml";
        }
        defaultYamlProcessor.setResources(new ClassPathResource(filePath));
        props.putAll(defaultYamlProcessor.getProperties());
        super.loadProperties(props);
    }

    @Override
    protected String convertPropertyValue(String value) {
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
            return value;
        }
        if (SystemUtils.isEncrypted(value)) {
            return SystemUtils.decrypt(value);
        }

        if (servletContext != null && value.contains(DataServiceConstants.WEB_ROOT_TOKEN)) {
            String path = servletContext.getRealPath("/");
            if (!org.apache.commons.lang3.StringUtils.isBlank(path)) {
                value = StringUtils.replacePlainStr(value, DataServiceConstants.WEB_ROOT_TOKEN, path);
            }
        }

        if (value.contains(DataServiceConstants.WM_MY_SQL_CLOUD_HOST_TOKEN)) {
            value = DataServiceUtils.replaceMySqlCloudToken(value, DataServiceConstants.WM_MY_SQL_CLOUD_HOST);
        }

        if (value.contains(DataServiceConstants.WM_MY_SQL_CLOUD_USER_NAME_TOKEN)) {
            value = StringUtils.replacePlainStr(value, DataServiceConstants.WM_MY_SQL_CLOUD_USER_NAME_TOKEN,
                DataServiceConstants.WM_MY_SQL_CLOUD_USER_NAME);
        }

        if (value.contains(DataServiceConstants.WM_MY_SQL_CLOUD_PASSWORD_TOKEN)) {
            value = StringUtils.replacePlainStr(value, DataServiceConstants.WM_MY_SQL_CLOUD_PASSWORD_TOKEN,
                DataServiceConstants.WM_MY_SQL_CLOUD_PASSWORD);
        }
        return value;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        super.setEnvironment(environment);
        this.environment = environment;
    }
}