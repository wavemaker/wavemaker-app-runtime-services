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

package com.wavemaker.runtime.security.rememberme.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledBaseConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.rememberme.WMRememberMeAuthenticationFilter;

@Configuration
@Conditional({SecurityEnabledCondition.class, RememberMeConfigCondition.class})
public class RememberMeConfiguration implements WMSecurityConfiguration {

    @Autowired
    private SecurityEnabledBaseConfiguration securityEnabledBaseConfiguration;
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private Environment environment;

    @Bean(name = "rememberMeServices")
    public PersistentTokenBasedRememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices = new PersistentTokenBasedRememberMeServices("WM_APP_KEY", userDetailsService, rememberMeRepository());
        persistentTokenBasedRememberMeServices.setParameter("j_rememberme");
        persistentTokenBasedRememberMeServices.setTokenValiditySeconds(environment.getProperty("security.general.rememberMe.timeOut", Integer.class));
        return persistentTokenBasedRememberMeServices;
    }

    @Bean(name = "rememberMeAuthFilter")
    public Filter rememberMeAuthFilter() {
        WMRememberMeAuthenticationFilter wmRememberMeAuthenticationFilter = new WMRememberMeAuthenticationFilter(
            securityEnabledBaseConfiguration.authenticationManager(), rememberMeServices());
        wmRememberMeAuthenticationFilter.setAuthenticationSuccessHandler(rememberMeAuthenticationSuccessHandler());
        return wmRememberMeAuthenticationFilter;
    }

    @Bean(name = "rememberMeAuthenticationProvider")
    public AuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider("WM_APP_KEY");
    }

    @Bean(name = "rememberMeRepository")
    public PersistentTokenRepository rememberMeRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean(name = "rememberMeAuthenticationSuccessHandler")
    public AuthenticationSuccessHandler rememberMeAuthenticationSuccessHandler() {
        WMApplicationAuthenticationSuccessHandler wmApplicationAuthenticationSuccessHandler = new WMApplicationAuthenticationSuccessHandler();
        List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
        defaultSuccessHandlerList.add(securityEnabledBaseConfiguration.wmSecurityContextRepositorySuccessHandler());
        defaultSuccessHandlerList.add(securityEnabledBaseConfiguration.wmCsrfTokenRepositorySuccessHandler());
        wmApplicationAuthenticationSuccessHandler.setDefaultSuccessHandlerList(defaultSuccessHandlerList);
        return wmApplicationAuthenticationSuccessHandler;
    }

    @Override
    public void addInterceptUrls(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorizeRequestsCustomizer) {
        //No interceptUrls in rememberme configuration
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAt(rememberMeAuthFilter(), RememberMeAuthenticationFilter.class);
    }
}
