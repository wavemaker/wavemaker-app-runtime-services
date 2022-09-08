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

package com.wavemaker.runtime.rest;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class RestBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RestBeanFactoryPostProcessor.class);

    private List<Class> apiClasses;
    private String serviceId;

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setApiClasses(List<Class> apiClasses) {
        this.apiClasses = apiClasses;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        apiClasses.forEach(apiClass -> {
            AnnotatedGenericBeanDefinition bd = new AnnotatedGenericBeanDefinition(RestFactoryBean.class);
            ConstructorArgumentValues values = new ConstructorArgumentValues();
            values.addIndexedArgumentValue(0, apiClass);
            values.addIndexedArgumentValue(1, serviceId);
            values.addIndexedArgumentValue(2, configurableListableBeanFactory.getBeanClassLoader());
            bd.setPrimary(true);
            bd.setConstructorArgumentValues(values);
            ((DefaultListableBeanFactory) configurableListableBeanFactory).registerBeanDefinition(
                serviceId + StringUtils.capitalize(apiClass.getName()) +"VirtualControllerApi", bd);
            logger.info("Bean definition is loaded for interface {} ", apiClass);
        });
    }
}
