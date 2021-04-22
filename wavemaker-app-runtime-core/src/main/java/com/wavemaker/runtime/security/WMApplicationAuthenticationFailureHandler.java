/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.wavemaker.runtime.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.wavemaker.runtime.security.handler.WMAuthenticationFailureHandler;

/**
 * @author Uday Shankar
 */
public class WMApplicationAuthenticationFailureHandler implements AuthenticationFailureHandler, BeanPostProcessor {


    private static final Logger logger = LoggerFactory.getLogger(WMApplicationAuthenticationFailureHandler.class);
    private List<AuthenticationFailureHandler> defaultFailureHandlerList = new ArrayList<>();
    private List<WMAuthenticationFailureHandler> customFailureHandlerList = new ArrayList<>();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        invokeCustomWMAuthenticationFailureHandler(request, response, exception);
        invokeDefaultWMAuthenticationFailureHandler(request, response, exception);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WMAuthenticationFailureHandler) {
            customFailureHandlerList.add((WMAuthenticationFailureHandler) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void invokeCustomWMAuthenticationFailureHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (CollectionUtils.isNotEmpty(customFailureHandlerList)) {
            logger.info("Invoking CustomAuthenticationFailureHandlers");
            for (WMAuthenticationFailureHandler authenticationFailureHandler : customFailureHandlerList) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
            }
        }
    }

    private void invokeDefaultWMAuthenticationFailureHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (CollectionUtils.isNotEmpty(defaultFailureHandlerList)) {
            logger.info("Invoking DefaultAuthenticationFailureHandlers");
            for (AuthenticationFailureHandler authenticationFailureHandler : defaultFailureHandlerList) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
            }
        }
    }

    public void setDefaultFailureHandlerList(List<AuthenticationFailureHandler> defaultFailureHandlerList) {
        this.defaultFailureHandlerList = defaultFailureHandlerList;
    }
}

