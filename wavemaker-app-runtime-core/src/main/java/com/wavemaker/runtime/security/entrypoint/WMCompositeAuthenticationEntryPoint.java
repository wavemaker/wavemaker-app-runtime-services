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
package com.wavemaker.runtime.security.entrypoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * Created by srujant on 2/8/18.
 */
public class WMCompositeAuthenticationEntryPoint implements AuthenticationEntryPoint, BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WMCompositeAuthenticationEntryPoint.class);
    private List<AuthenticationEntryPoint> authenticationEntryPointList = new ArrayList<>();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authenticationEntryPointList.size() == 1) {
            authenticationEntryPointList.get(0).commence(request, response, authException);
        } else if (authenticationEntryPointList.size() > 0) {
            logger.info("As no other AuthenticationEntryPoint is configured, commencing the request to index.html");
            LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint = new LoginUrlAuthenticationEntryPoint("/index.html");
            loginUrlAuthenticationEntryPoint.commence(request, response, authException);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FormLoginEntryPoint || bean instanceof SSOEntryPoint) {
            authenticationEntryPointList.add((AuthenticationEntryPoint) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
