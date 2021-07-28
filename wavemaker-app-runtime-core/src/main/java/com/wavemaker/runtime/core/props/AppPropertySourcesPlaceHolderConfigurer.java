package com.wavemaker.runtime.core.props;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;

import com.wavemaker.commons.properties.EnvironmentRegisteringPropertySourcesPlaceHolderConfigurer;
import com.wavemaker.commons.util.StringUtils;
import com.wavemaker.commons.util.SystemUtils;
import com.wavemaker.runtime.data.util.DataServiceConstants;
import com.wavemaker.runtime.data.util.DataServiceUtils;

public class AppPropertySourcesPlaceHolderConfigurer extends EnvironmentRegisteringPropertySourcesPlaceHolderConfigurer implements ServletContextAware {

    private ServletContext servletContext;

    @Override
    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       final StringValueResolver valueResolver) {

        StringValueResolver updatedValueResolver = strVal -> convertPropertyValue(valueResolver.resolveStringValue(strVal));
        super.doProcessProperties(beanFactoryToProcess, updatedValueResolver);
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
}
