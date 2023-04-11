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
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.commons.properties.EnvironmentRegisteringPropertySourcesPlaceHolderConfigurer;
import com.wavemaker.commons.util.StringUtils;
import com.wavemaker.commons.util.SystemUtils;
import com.wavemaker.runtime.data.util.DataServiceConstants;
import com.wavemaker.runtime.data.util.DataServiceUtils;

public class AppPropertySourcesPlaceHolderConfigurer extends EnvironmentRegisteringPropertySourcesPlaceHolderConfigurer implements ServletContextAware {

    private ServletContext servletContext;

    private Resource[] locations;

    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       final StringValueResolver valueResolver) {

        StringValueResolver updatedValueResolver = strVal -> convertPropertyValue(valueResolver.resolveStringValue(strVal));
        super.doProcessProperties(beanFactoryToProcess, updatedValueResolver);
    }

    @Override
    protected void loadProperties(Properties props) throws IOException {
        //Using YamlPropertiesFactoryBean instead of WMYamlPropertiesFactoryBean because @ConfigurationProperties is loading
        //custom objects if properties are in the form[0],[1] etc
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(getResourcesByFileExtension(locations, ".yml"));
        props.putAll(yamlPropertiesFactoryBean.getObject());
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
    public void setLocations(Resource... locations) {
        this.locations = locations;
        super.setLocations(getResourcesByFileExtension(locations, ".properties"));
    }

    private Resource[] getResourcesByFileExtension(Resource[] locations, String fileExtension) {
        return Arrays.stream(locations).filter(resource -> {
            return Objects.requireNonNull(resource.getFilename()).endsWith(fileExtension);
        }).toArray(Resource[]::new);
    }
}
