/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.rest.handler;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.wavemaker.runtime.rest.controller.RestRuntimeController;
import com.wavemaker.runtime.rest.model.RestServiceInfoBean;
import com.wavemaker.studio.common.WMRuntimeException;

/**
 * Created by ArjunSahasranam on 6/10/15.
 */

/**
 * Registers REST API urls to SimpleUrlHandlerMapping.
 */
public class RestServiceBeanPostProcessor implements BeanPostProcessor {


    @Autowired
    private RestRuntimeController restRuntimeController;

    @Autowired
    private ApplicationContext applicationContext;

    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @PostConstruct
    private void init() {
        Map<String, RequestMappingHandlerMapping> beans = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
        if (beans == null || beans.size() == 0) {
            throw new WMRuntimeException("No beans of type RequestMappingHandlerMapping found in " + applicationContext);
        } else {
            for (RequestMappingHandlerMapping bean : beans.values() ) {
                if (bean.getApplicationContext() == applicationContext) {
                    requestMappingHandlerMapping = bean;
                    break;
                }
            }
            if (requestMappingHandlerMapping == null) {
                throw new WMRuntimeException("No beans of type RequestMappingHandlerMapping found in " + applicationContext);
            }
        }
    }


    private static final Method HANDLE_REQUEST_METHOD;
    static {
        try {
            HANDLE_REQUEST_METHOD = RestRuntimeController.class.getDeclaredMethod("handleRequest", HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException e) {
            throw new WMRuntimeException("Couldn't find handleRequestInternal method in RestRuntimeController", e);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof RestServiceInfoBean) {
            RestServiceInfoBean restServiceInfoBean = (RestServiceInfoBean) bean;
            RequestMappingInfo requestMappingInfo = getRequestMappingInfo(restServiceInfoBean);
            requestMappingHandlerMapping.registerMapping(requestMappingInfo, restRuntimeController, HANDLE_REQUEST_METHOD);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    private RequestMappingInfo getRequestMappingInfo(RestServiceInfoBean restServiceInfoBean) {
        RequestMethodsRequestCondition methods = new RequestMethodsRequestCondition(RequestMethod.valueOf(restServiceInfoBean.getHttpMethod()));
        PatternsRequestCondition patterns = new PatternsRequestCondition(restServiceInfoBean.getUrl());
        ParamsRequestCondition params = new ParamsRequestCondition();
        HeadersRequestCondition headers = new HeadersRequestCondition();
        ConsumesRequestCondition consumes = new ConsumesRequestCondition();
        ProducesRequestCondition produces = new ProducesRequestCondition();
        return new RequestMappingInfo(patterns, methods, params, headers, consumes, produces, null);
    }
}
