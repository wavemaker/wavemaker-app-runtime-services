package com.wavemaker.runtime.rest.feign;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.wavemaker.commons.util.StringUtils;

public class FeignBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FeignBeanFactoryPostProcessor.class);

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
            AnnotatedGenericBeanDefinition bd = new AnnotatedGenericBeanDefinition(FeignFactoryBean.class);
            ConstructorArgumentValues values = new ConstructorArgumentValues();
            values.addIndexedArgumentValue(0, apiClass);
            values.addIndexedArgumentValue(1, serviceId);
            values.addIndexedArgumentValue(2, configurableListableBeanFactory.getBeanClassLoader());
            bd.setPrimary(true);
            bd.setConstructorArgumentValues(values);
            ((DefaultListableBeanFactory) configurableListableBeanFactory).registerBeanDefinition(serviceId + StringUtils.upperCaseFirstLetter(apiClass.getName()) +
                            "VirtualControllerApi",
                    bd);
            logger.info("Bean definition is loaded for interface {} ", apiClass);
        });
    }
}
