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
import java.util.Collections;
import java.util.List;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;

import com.wavemaker.app.security.models.RememberMeConfig;
import com.wavemaker.app.security.models.SecurityInterceptUrlEntry;
import com.wavemaker.runtime.security.authenticationprovider.WMDelegatingAuthenticationProvider;
import com.wavemaker.runtime.security.config.WMSecurityConfiguration;
import com.wavemaker.runtime.security.enabled.configuration.SecurityEnabledCondition;
import com.wavemaker.runtime.security.handler.WMApplicationAuthenticationSuccessHandler;
import com.wavemaker.runtime.security.rememberme.WMRememberMeAuthenticationFilter;

@Configuration
@Conditional({SecurityEnabledCondition.class, RememberMeConfigCondition.class})
public class RememberMeConfiguration implements WMSecurityConfiguration {
    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("wmCsrfTokenRepositorySuccessHandler")
    @Lazy
    private AuthenticationSuccessHandler wmCsrfTokenRepositorySuccessHandler;

    @Autowired
    @Lazy
    private SecurityContextRepository securityContextRepository;

    @Bean(name = "rememberMeServices")
    public RememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices =
            new PersistentTokenBasedRememberMeServices("WM_APP_KEY", userDetailsService, rememberMeRepository());
        persistentTokenBasedRememberMeServices.setParameter("j_rememberme");
        persistentTokenBasedRememberMeServices.setTokenValiditySeconds((int) rememberMeConfig().getTokenValiditySeconds());
        return persistentTokenBasedRememberMeServices;
    }

    @Bean(name = "rememberMeAuthFilter")
    public Filter rememberMeAuthFilter() {
        WMRememberMeAuthenticationFilter wmRememberMeAuthenticationFilter = new WMRememberMeAuthenticationFilter(
            authenticationManager, rememberMeServices());
        wmRememberMeAuthenticationFilter.setAuthenticationSuccessHandler(rememberMeAuthenticationSuccessHandler());
        wmRememberMeAuthenticationFilter.setSecurityContextRepository(securityContextRepository);
        return wmRememberMeAuthenticationFilter;
    }

    @Bean(name = "rememberMeAuthenticationProvider")
    public AuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider("WM_APP_KEY");
    }

    @Bean(name = "rememberMeDelegatingAuthenticationProvider")
    public WMDelegatingAuthenticationProvider rememberMeDelegatingAuthenticationProvider(AuthenticationProvider rememberMeAuthenticationProvider) {
        return new WMDelegatingAuthenticationProvider(rememberMeAuthenticationProvider, null);
    }

    @Bean(name = "rememberMeRepository")
    public PersistentTokenRepository rememberMeRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean(name = "rememberMeAuthenticationSuccessHandler")
    public AuthenticationSuccessHandler rememberMeAuthenticationSuccessHandler() {
        WMApplicationAuthenticationSuccessHandler wmApplicationAuthenticationSuccessHandler = new WMApplicationAuthenticationSuccessHandler();
        List<AuthenticationSuccessHandler> defaultSuccessHandlerList = new ArrayList<>();
        defaultSuccessHandlerList.add(wmCsrfTokenRepositorySuccessHandler);
        wmApplicationAuthenticationSuccessHandler.setDefaultSuccessHandlerList(defaultSuccessHandlerList);
        return wmApplicationAuthenticationSuccessHandler;
    }

    @Override
    public List<SecurityInterceptUrlEntry> getSecurityInterceptUrls() {
        return Collections.emptyList();
    }

    @Bean(name = "rememberMeConfig")
    public RememberMeConfig rememberMeConfig() {
        return new RememberMeConfig();
    }

    @Override
    public void addFilters(HttpSecurity http) {
        http.addFilterAt(rememberMeAuthFilter(), RememberMeAuthenticationFilter.class);
    }
}
