/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.data.cloudfoundry;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.relational.RdbmsServiceCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.util.CollectionUtils;

import com.wavemaker.runtime.data.spring.ConfigurationAndSessionFactoryBean;
import com.wavemaker.runtime.data.util.DataServiceConstants;

/**
 * {@link BeanFactoryPostProcessor} implementation that replaces all local MySQL datasources with services provided by
 * CloudFoundry.
 * 
 * @author Jeremy Grelle
 */
public class CloudFoundryDataServiceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Log log = LogFactory.getLog(CloudFoundryDataServiceBeanFactoryPostProcessor.class);

    private static final String DS_BEAN_SUFFIX = "DataSource";

    private final CloudEnvironment cloudEnvironment = new CloudEnvironment();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;

        if (CollectionUtils.isEmpty(this.cloudEnvironment.getServices())) {
            return;
        }
        processDataSources(defaultListableBeanFactory);
        processHibernateProperties(defaultListableBeanFactory);
    }

    /**
     * @param beanFactory
     */
    private void processHibernateProperties(DefaultListableBeanFactory beanFactory) {
        String[] sessionFactoryBeanNames = beanFactory.getBeanNamesForType(ConfigurationAndSessionFactoryBean.class);

        for (String sfBean : sessionFactoryBeanNames) {
            BeanDefinition beanDefinition = getBeanDefinition(beanFactory, sfBean);
            if (sfBean.contains(DataServiceConstants.AUX_BEAN_SUFFIX)) {
                beanDefinition.setLazyInit(false);
            } else {
                beanDefinition.setLazyInit(true);    
            }
            MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
            PropertyValue hibernateProperties = propertyValues.getPropertyValue("hibernateProperties");

            ManagedProperties hibernatePropsPropertyValue = null;
            if (hibernateProperties != null) {
                Object value = hibernateProperties.getValue();
                if (value instanceof ManagedProperties) {
                    hibernatePropsPropertyValue = (ManagedProperties) hibernateProperties.getValue();
                    TypedStringValue dialect = (TypedStringValue) hibernatePropsPropertyValue.get(new TypedStringValue(DataServiceConstants.HIBERNATE_DIALECT_PROPERTY));
                    if (dialect != null && dialect.equals(new TypedStringValue("com.wavemaker.runtime.data.dialect.MySQLDialect"))) {
                        hibernatePropsPropertyValue.put(new TypedStringValue(DataServiceConstants.HIBERNATE_DIALECT_PROPERTY), new TypedStringValue(
                            "org.hibernate.dialect.MySQLDialect"));
                    }
                }
            } else {
                hibernatePropsPropertyValue = new ManagedProperties();
            }
        }
    }

    private BeanDefinition getBeanDefinition(DefaultListableBeanFactory beanFactory, String beanName) {
        if (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
        }
        return beanFactory.getBeanDefinition(beanName);
    }

    /**
     * @param defaultListableBeanFactory
     */
    private void processDataSources(DefaultListableBeanFactory defaultListableBeanFactory) {
        String[] dataSourceBeanNames = defaultListableBeanFactory.getBeanNamesForType(DataSource.class);

        if (dataSourceBeanNames.length <= 1) {
            // When there is only 1 DataSource, the provided auto-staging should be sufficient
            return;
        }

        for (String dsBean : dataSourceBeanNames) {
            if (!dsBean.endsWith(DS_BEAN_SUFFIX)) {
                continue;
            }

            String serviceName = dsBean.substring(0, dsBean.indexOf(DS_BEAN_SUFFIX));

            if (!serviceExists(serviceName)) {
                boolean foundAlias = false;
                for (String alias : defaultListableBeanFactory.getAliases(dsBean)) {
                    if (serviceExists(alias)) {
                        serviceName = alias;
                        foundAlias = true;
                        break;
                    }
                }
                if (!foundAlias) {
                    log.warn("Expected to find a service with the name '" + serviceName + "' but none was found.");
                    continue;
                }
            }

            RdbmsServiceInfo service = this.cloudEnvironment.getServiceInfo(serviceName, RdbmsServiceInfo.class);
            if (service != null) {
                defaultListableBeanFactory.removeBeanDefinition(dsBean);
                DataSource cfDataSource = new RdbmsServiceCreator().createService(service);
                defaultListableBeanFactory.registerSingleton(dsBean, cfDataSource);
            } else {
                log.warn("Service " + serviceName + " exists, but is not an RDBMS service as expected.");
            }
        }
    }

    /**
     * @param serviceName
     * @return
     */
    private boolean serviceExists(String serviceName) {
        List<Map<String, Object>> services = this.cloudEnvironment.getServices();
        for (Map<String, Object> serviceProps : services) {
            if (serviceName.equals(serviceProps.get("name"))) {
                return true;
            }
        }
        return false;
    }

}
