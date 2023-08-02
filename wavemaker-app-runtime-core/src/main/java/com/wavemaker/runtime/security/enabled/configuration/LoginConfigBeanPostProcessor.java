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

package com.wavemaker.runtime.security.enabled.configuration;

import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.wavemaker.app.security.models.LoginConfig;
import com.wavemaker.app.security.models.LoginType;
import com.wavemaker.app.security.models.SessionTimeoutConfig;

public class LoginConfigBeanPostProcessor implements BeanPostProcessor {

    @Value("${security.providers.activeProviders}")
    private String activeProviders;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (Stream.of("CAS", "OPENID", "SAML").anyMatch(s -> Objects.requireNonNull(activeProviders).contains(s))) {
            if (bean instanceof LoginConfig) {
                LoginConfig loginConfig = (LoginConfig) bean;
                loginConfig.setType(LoginType.SSO);
            }
            if (bean instanceof SessionTimeoutConfig) {
                SessionTimeoutConfig sessionTimeoutConfig = (SessionTimeoutConfig) bean;
                sessionTimeoutConfig.setType(LoginType.SSO);
            }
        }
        return bean;
    }
}
