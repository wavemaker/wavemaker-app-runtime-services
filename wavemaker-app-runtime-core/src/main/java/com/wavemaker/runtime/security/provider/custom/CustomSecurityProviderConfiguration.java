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

package com.wavemaker.runtime.security.provider.custom;

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.wavemaker.runtime.security.WMCustomAuthenticationManager;
import com.wavemaker.runtime.security.WMCustomAuthenticationProvider;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;

@Configuration
@Conditional({SecurityEnabledCondition.class, CustomSecurityProviderCondition.class})
public class CustomSecurityProviderConfiguration {

    @Value("${security.providers.custom.class}")
    private String customAuthenticationManagerClass;

    @Bean(name = "wmCustomAuthenticationProvider")
    public AuthenticationProvider wmCustomAuthenticationProvider()
        throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        WMCustomAuthenticationProvider wmCustomAuthenticationProvider = new WMCustomAuthenticationProvider();
        wmCustomAuthenticationProvider.setWmCustomAuthenticationManager(wmCustomAuthenticationManager());
        return wmCustomAuthenticationProvider;
    }

    @Bean(name = "wmCustomAuthenticationManager")
    public WMCustomAuthenticationManager wmCustomAuthenticationManager()
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object o = Class.forName(customAuthenticationManagerClass).getDeclaredConstructor().newInstance();
        return (WMCustomAuthenticationManager) o;
    }

    @Bean(name = "logoutFilter")
    public LogoutFilter logoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler securityContextLogoutHandler,
                                     LogoutHandler wmCsrfLogoutHandler) {
        LogoutFilter logoutFilter = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, wmCsrfLogoutHandler);
        logoutFilter.setFilterProcessesUrl("/j_spring_security_logout");
        return logoutFilter;
    }
}
